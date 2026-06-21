# ShellAtlas — Runbook (Desktop / Web / Android / iOS)

Guía paso a paso, sin asumir nada. Cubre: arranque por plataforma, variables de
entorno correctas, y el bug de Confluence/Asistente que nunca conecta.

---

## 0. Piezas del sistema (léelo antes de tocar nada)

Hay **3 procesos independientes** que deben correr para tener todo funcionando:

1. **Supabase local** (Postgres + Auth + REST) — puerto `54321` (API) / `54322` (DB).
2. **Backend Node** (`backend/`, Hono) — puerto `8787`. Es el único que habla con
   **Confluence**. La app (Desktop/Web/Android/iOS) NUNCA llama a Confluence
   directamente, siempre pasa por este backend.
3. **Ollama** (LLM local) — puerto `11434`. Opcional, solo si `useOllama=true`.

Si el backend Node no está corriendo, o corre sin las variables de Confluence,
el asistente jamás traerá datos de Confluence — sin importar qué tan bien
configurada esté la app. Esta es la causa del bug que reportas (ver sección 4).

---

## 1. Variables de entorno — cuál archivo usa cada cosa

Hay 4 archivos de entorno en la raíz, y se confunden fácil:

| Archivo                              | Quién lo lee                                                                 | Cuándo                                             |
|--------------------------------------|------------------------------------------------------------------------------|----------------------------------------------------|
| `.env.demo`                          | **Nadie automáticamente** (ver nota abajo)                                   | —                                                  |
| `.env`                               | Desktop/iOS/Web cuando flavor != `demo`, y backend con `npm run dev`/`start` | flavor=dev o prod                                  |
| `.env.example` / `.env.prod.example` | Solo plantillas, nunca se leen                                               | —                                                  |
| `backend/.env`                       | Backend con `npm run dev` / `npm start`                                      | sin Confluence configurado                         |
| `backend/.env.local`                 | Backend con `npm run dev:local` / `start:local`                              | **el que tiene credenciales reales de Confluence** |

**Nota importante sobre `.env.demo`:** `DesktopAppConfig.kt`, `WebAppConfig.kt` e
`IosAppConfig.kt` revisan el flavor; si el flavor es literalmente `"demo"` (el
default), **retornan modo demo en memoria y NUNCA leen ningún archivo `.env`**,
ni siquiera `.env.demo`. O sea: hoy `.env.demo` es contenido muerto a menos que
cambies el flavor a algo distinto de `demo` (típicamente `dev`), en cuyo caso
el loader busca `.env.dev` (no existe) y cae a `.env` (el de la raíz, sin sufijo).

➡️ **Para desarrollo real (Supabase local + Ollama + backend + Confluence) usa
siempre el archivo `.env`** (raíz), con flavor `dev`. Las claves correctas
tienen el prefijo `SHELLDOC_DEV_`:

```env
SHELLDOC_APP_ENVIRONMENT=DEV

SHELLDOC_DEV_SUPABASE_URL=http://127.0.0.1:54321
SHELLDOC_DEV_SUPABASE_ANON_KEY=<ANON_KEY de "supabase status">
SHELLDOC_DEV_API_BASE_URL=http://127.0.0.1:8787
SHELLDOC_DEV_API_BEARER_TOKEN=

SHELLDOC_DEV_USE_OLLAMA=true
SHELLDOC_DEV_OLLAMA_BASE_URL=http://127.0.0.1:11434
SHELLDOC_DEV_OLLAMA_MODEL=llama3.2

SHELLDOC_DEV_CONFLUENCE_BASE_URL=https://sancheznormandev.atlassian.net/wiki
SHELLDOC_DEV_CONFLUENCE_API_TOKEN=<tu API token>
SHELLDOC_DEV_CONFLUENCE_USER_EMAIL=<tu email Atlassian>
```

⚠️ Importante: estas tres últimas (`CONFLUENCE_*`) viven en `.env` solo como
referencia — **la app Kotlin (Desktop/Web/iOS/Android) no las lee nunca**
(`DesktopAppConfig.kt` etc. no tienen ningún `resolveProfileSetting(..., "CONFLUENCE_...")`).
Las que realmente importan para Confluence están en `backend/.env.local`
(sección 4).

Saca el `ANON_KEY` real con:

```bash
supabase status
```

---

## 2. Arrancar todo, paso a paso, plataforma por plataforma

### 2.0 Prerrequisitos comunes (hazlo una sola vez, antes de cualquier plataforma)

```bash
# 1) Levanta Supabase local
supabase start
# (si ya estaba iniciado, usa: supabase status)

# 2) Levanta Ollama (si vas a usar el asistente con LLM real)
ollama serve &
ollama pull llama3.2   # solo la primera vez

# 3) Levanta el backend (Confluence + API de documentos)
cd backend
npm install            # solo la primera vez
npm run dev:local       # usa backend/.env.local (Confluence real + Supabase local)
```

Déjalo corriendo en su propia terminal. Verifica que responde (no hay `/health`,
usa un endpoint real):

```bash
curl http://127.0.0.1:8787/v1/documents
```

---

### 2.1 Desktop

```bash
# Desde la raíz del repo
./gradlew :composeApp:run -PshellFlavor=dev
```

- `-PshellFlavor=dev` es obligatorio. Sin él, default es `demo` → app en
  memoria, sin Supabase/Ollama/Confluence reales, aunque tengas `.env` perfecto.
- Esto pasa `-Dshelldocs.flavor=dev` a la JVM, que hace que `DesktopAppConfig.kt`
  busque `.env.dev` (no existe) y caiga a `.env` (raíz).
- Verifica en consola los logs `ShellDocs/Startup`, `ShellDocs/DB`,
  `ShellDocs/Ollama` que aparecen al boot (gracias al logging nuevo) — te dicen
  exactamente qué conectó y qué no.

Para volver a modo demo (sin nada externo): corre sin el flag, o
`./gradlew :composeApp:run` a secas.

---

### 2.2 Web (wasmJs)

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun -PshellFlavor=dev
```

- Abre el navegador automáticamente (usualmente `http://localhost:8080`).
- El flavor para Web se "hornea" en tiempo de compilación
  (`generateWebBuildFlavor` escribe `BUILD_FLAVOR` en un archivo Kotlin), así
  que necesitas pasar `-PshellFlavor=dev` al **comando de build**, no puedes
  cambiarlo después solo con variables de entorno del sistema operativo.
- Truco extra: puedes sobreescribir el flavor sin recompilar, agregando
  `?SHELLDOC_FLAVOR=dev` a la URL — pero las demás variables (`SUPABASE_URL`,
  etc.) deben venir igual como **query params** en la URL porque el navegador
  no puede leer archivos `.env` del filesystem:
  ```
  http://localhost:8080/?SHELLDOC_FLAVOR=dev&SHELLDOC_DEV_SUPABASE_URL=http://127.0.0.1:54321&SHELLDOC_DEV_SUPABASE_ANON_KEY=...&SHELLDOC_DEV_API_BASE_URL=http://127.0.0.1:8787&SHELLDOC_DEV_USE_OLLAMA=true&SHELLDOC_DEV_OLLAMA_BASE_URL=http://127.0.0.1:11434
  ```
- CORS: el backend Node debe permitir el origen del navegador (revisa
  `cors()` en `backend/src/index.ts` si ves errores de CORS en consola).

---

### 2.3 Android

⚠️ Diferencia clave con las otras plataformas: en Android, **el build type
`debug` está hard-codeado a modo demo** (`build.gradle.kts` líneas ~165-172:
todos los `buildConfigField` de `debug` son vacíos/false). No hay manera de
apuntar `debug` a Supabase/Ollama/Confluence reales solo con variables de
entorno — tendrías que editar esos `buildConfigField` directamente, o usar
`release`.

El build type `release` sí lee variables, pero con prefijo **`SHELLDOC_PROD_`**
(no `DEV`), porque Android solo tiene dos sabores (debug=demo, release=config
real). Para probar contra tu entorno local desde un emulador:

```bash
# En tu shell (o agrégalo a tu .env de raíz, que el build también lee como fallback):
export SHELLDOC_PROD_SUPABASE_URL=http://127.0.0.1:54321
export SHELLDOC_PROD_SUPABASE_ANON_KEY=<ANON_KEY de "supabase status">
export SHELLDOC_PROD_API_BASE_URL=http://127.0.0.1:8787
export SHELLDOC_PROD_USE_OLLAMA=true
export SHELLDOC_PROD_OLLAMA_BASE_URL=http://127.0.0.1:11434
export SHELLDOC_PROD_OLLAMA_MODEL=llama3.2

./gradlew :composeApp:installRelease
```

- No necesitas cambiar `127.0.0.1` a `10.0.2.2` manualmente: `AndroidAppConfig.kt`
  (`normalizeAndroidLocalhost`) lo hace automático para que el emulador
  encuentre tu máquina anfitriona.
- Si usas un dispositivo físico (no emulador), `10.0.2.2` no sirve — usa la IP
  de tu Mac en la red local (ej. `192.168.x.x`) para Supabase/backend/Ollama,
  y asegúrate que el firewall no bloquee esos puertos.
- Para instalar y abrir directo: `adb shell am start -n com.shelldocs.app/.MainActivity`
  (o ábrela a mano desde el launcher).

---

### 2.4 iOS

```bash
open iosApp/iosApp.xcodeproj
```

1. En Xcode, selecciona un simulador (ej. iPhone 16).
2. Edita el **Scheme** (`Product > Scheme > Edit Scheme... > Run > Arguments
   > Environment Variables`) y agrega:
    - `SHELLDOC_FLAVOR` = `dev`
    - `SHELLDOC_APP_ENVIRONMENT` = `DEV`
    - `SHELLDOC_DEV_SUPABASE_URL` = `http://127.0.0.1:54321`
    - `SHELLDOC_DEV_SUPABASE_ANON_KEY` = `<ANON_KEY>`
    - `SHELLDOC_DEV_API_BASE_URL` = `http://127.0.0.1:8787`
    - `SHELLDOC_DEV_USE_OLLAMA` = `true`
    - `SHELLDOC_DEV_OLLAMA_BASE_URL` = `http://127.0.0.1:11434`
    - `SHELLDOC_DEV_OLLAMA_MODEL` = `llama3.2`

   (`IosAppConfig.kt` lee `NSProcessInfo.processInfo.environment`, es decir,
   variables de entorno del proceso — no hay archivo `.env` en iOS, todo se
   inyecta vía el Scheme de Xcode.)
3. Run (▶) con el simulador seleccionado. El simulador comparte red con tu
   Mac, así que `127.0.0.1` apunta correctamente a Supabase/backend/Ollama
   corriendo en tu máquina — no necesitas alias como en Android.
4. Si pruebas en dispositivo físico, igual que Android: cambia `127.0.0.1`
   por la IP LAN de tu Mac.

---

## 3. El bug de `supabase db pull` / `db push` — ya corregido

**Causa raíz:** la migración `supabase/migrations/20260616202947_remote_schema.sql`
hacía `drop function "public"."handle_new_user"()` ANTES de
`drop trigger "on_auth_user_created" on "auth"."users"` — pero el trigger
depende de esa función. Postgres rechaza el drop con:

```
ERROR: cannot drop function handle_new_user() because other objects depend on it (SQLSTATE 2BP01)
```

Esto rompía la "shadow database" que tanto `db pull` como `db push` construyen
internamente para diffear esquemas, así que ambos comandos fallaban en cadena
(no era un problema de credenciales ni de red).

**Fix aplicado:** reordené esa migración para hacer `drop trigger` antes del
`drop function` (el orden lógico correcto). Ya verifiqué:

```bash
supabase db pull         # ✅ corre limpio, generó supabase/migrations/20260621085818_remote_schema.sql
supabase db push --dry-run   # ✅ "Remote database is up to date."
```

La migración nueva (`20260621085818_remote_schema.sql`) solo trae `GRANT`s
explícitos en tablas públicas (relacionado con el cambio de default
`auto_expose_new_tables` que Supabase está deprecando) — no es destructiva.

**Pendiente de tu parte:** revisa `git diff` de esa migración y, si todo se
ve bien, commitea los dos archivos (`20260616202947_remote_schema.sql`
corregido + `20260621085818_remote_schema.sql` nuevo) y haz push al repo para
que el resto del equipo no pise el mismo error.

---

## 4. Por qué el asistente nunca trae datos de Confluence

Encontré 3 causas combinadas — necesitas las 3 resueltas para que funcione:

### Causa 1 — La app corre en modo demo (no llama a nada externo)

Si no pasas `-PshellFlavor=dev` (Desktop/Web) o no configuras el Scheme de
Xcode (iOS) o usas el build `debug` (Android), la app vive en memoria pura.
El asistente entonces usa el motor "grounded" (offline, sin LLM, sin fuentes
externas) — nunca intenta hablar con Confluence ni con el backend. Ver
sección 2 para arrancar en modo `dev` real.

### Causa 2 — El backend, si corre con `npm run dev` (no `dev:local`), no tiene Confluence configurado

`backend/src/confluence-sync.ts`:

```ts
export function confluenceConfigFromEnv(): ConfluenceConfig | null {
    const baseUrl = process.env.CONFLUENCE_BASE_URL;
    const apiToken = process.env.CONFLUENCE_API_TOKEN;
    const userEmail = process.env.CONFLUENCE_USER_EMAIL;
    if (!baseUrl || !apiToken || !userEmail) return null;
...
}
```

y en `index.ts`, si es `null`, el endpoint responde `400` con
`"Confluence is not configured (missing CONFLUENCE_BASE_URL, ...)"`.

- `backend/.env` (el que usa `npm run dev` / `npm start`) **NO tiene**
  `CONFLUENCE_BASE_URL` / `CONFLUENCE_API_TOKEN` / `CONFLUENCE_USER_EMAIL`.
- `backend/.env.local` (el que usa `npm run dev:local` / `npm run start:local`)
  **sí los tiene**, con credenciales reales.

➡️ **Siempre arranca el backend con `npm run dev:local`**, no `npm run dev`,
mientras trabajes en local. Si en algún momento corriste `npm run dev` a secas,
ese es exactamente el motivo del error "no puedo obtener datos de Confluence".

### Causa 3 — La app Kotlin nunca toca Confluence directamente

Aunque pongas `SHELLDOC_DEV_CONFLUENCE_*` en el `.env` de la raíz, ningún
`*AppConfig.kt` (Desktop/Web/iOS/Android) las lee — no hay ningún
`resolveProfileSetting(..., "CONFLUENCE_BASE_URL")` en ese código. Esas
variables del `.env` raíz son solo documentación/plantilla; las reales que
importan están en `backend/.env.local`. La app solo necesita saber la URL del
backend (`SHELLDOC_DEV_API_BASE_URL=http://127.0.0.1:8787`); el backend hace
de puente hacia Confluence.

### Checklist para confirmar que ya funciona

```bash
# 1. Backend con Confluence real:
cd backend && npm run dev:local

# 2. Prueba directo el backend (sin pasar por la app):
curl -X POST http://127.0.0.1:8787/v1/sources/confluence/sync
curl http://127.0.0.1:8787/v1/sources/confluence/tree

# 3. Si el backend responde bien, levanta la app en modo dev (sección 2)
#    y mira los logs "ShellDocs/Integration" / "ShellDocs/Startup" al boot.
```

Si el `curl` directo al backend ya falla, el problema es 100% Confluence/red/
credenciales (revisa que el API token en `backend/.env.local` no haya
expirado) — no es un bug de la app KMM.
