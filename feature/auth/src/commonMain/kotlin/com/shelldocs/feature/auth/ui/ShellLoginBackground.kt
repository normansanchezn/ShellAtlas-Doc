package com.shelldocs.feature.auth.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.onPointerEvent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val TWO_PI = (PI * 2).toFloat()

private data class Particle(
    val x: Float,
    val y: Float,
    val speedX: Float,
    val speedY: Float,
    val phaseX: Float,
    val phaseY: Float,
    val radius: Float,
    val baseAlpha: Float,
    val pulseSpeed: Float,
    val pulsePha: Float,
    val isAccent: Boolean,
)

private fun buildParticles(count: Int): List<Particle> {
    val rng = Random(seed = 0x5E11)
    return List(count) { i ->
        Particle(
            x = rng.nextFloat(),
            y = rng.nextFloat(),
            speedX = rng.nextFloat() * 0.25f + 0.08f,
            speedY = rng.nextFloat() * 0.18f + 0.05f,
            phaseX = rng.nextFloat() * TWO_PI,
            phaseY = rng.nextFloat() * TWO_PI,
            radius = rng.nextFloat() * 2.4f + 0.6f,
            baseAlpha = rng.nextFloat() * 0.35f + 0.1f,
            pulseSpeed = rng.nextFloat() * 0.4f + 0.15f,
            pulsePha = rng.nextFloat() * TWO_PI,
            isAccent = i % 11 == 0,
        )
    }
}

/**
 * Full-bleed animated background for the sign-in screen.
 *
 * 68 particles orbit their seed positions with independent sine/cosine cycles.
 * Nearby pairs are joined by faint lines (network-graph aesthetic).  Every 11th
 * node is tinted Shell yellow.  Full cycle = 22 s.
 *
 * On desktop and web, moving the mouse cursor near particles repels them.
 * The repulsion is filtered to [PointerType.Mouse] so touch devices never see
 * the effect.  When the cursor leaves, an exponential spring (critically
 * damped, low stiffness) eases particles back to their orbit — giving a
 * springy "memory of position" feel without individual particle physics.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShellLoginBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "login-bg")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "time",
    )

    val particles = remember { buildParticles(count = 68) }

    // Mouse-repulsion state — only ever non-null when a real mouse pointer moves.
    var mouseActive by remember { mutableStateOf(false) }
    var lastPointerPos by remember { mutableStateOf<Offset?>(null) }

    // Critically-damped spring: instant lock-in when mouse enters, smooth
    // exponential decay back to 0 when mouse exits (~0.8 s to settle).
    val influence by animateFloatAsState(
        targetValue = if (mouseActive) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "mouse-influence",
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onPointerEvent(PointerEventType.Move) { event ->
                val change = event.changes.firstOrNull()
                if (change?.type == PointerType.Mouse) {
                    lastPointerPos = change.position
                    mouseActive = true
                }
            }
            .onPointerEvent(PointerEventType.Exit) {
                mouseActive = false
                // Keep lastPointerPos so the spring-back targets the last known
                // cursor location and particles visibly drift back from there.
            },
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0B0D10), Color(0xFF131720)),
            ),
        )

        val w = size.width
        val h = size.height
        val connectionThreshold = w * 0.14f
        val repelRadius = w * 0.13f
        val repelStrength = w * 0.05f
        val accentColor = Color(0xFFFFD100)
        val nodeColor = Color.White

        // Orbit positions driven purely by time.
        val orbits = particles.map { p ->
            Offset(
                x = (p.x + sin(time * p.speedX + p.phaseX) * 0.06f) * w,
                y = (p.y + cos(time * p.speedY + p.phaseY) * 0.05f) * h,
            )
        }

        // Apply mouse repulsion with the animated influence factor.
        val ptr = lastPointerPos
        val positions = if (ptr != null && influence > 0.001f) {
            orbits.map { orbit ->
                val dx = orbit.x - ptr.x
                val dy = orbit.y - ptr.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < repelRadius && dist > 0.5f) {
                    val strength = influence * repelStrength * (1f - dist / repelRadius)
                    Offset(orbit.x + dx / dist * strength, orbit.y + dy / dist * strength)
                } else {
                    orbit
                }
            }
        } else {
            orbits
        }

        // Connection lines.
        for (i in particles.indices) {
            for (j in i + 1 until particles.size) {
                val dx = positions[i].x - positions[j].x
                val dy = positions[i].y - positions[j].y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < connectionThreshold) {
                    val alpha = (1f - dist / connectionThreshold) * 0.12f
                    drawLine(
                        color = nodeColor.copy(alpha = alpha),
                        start = positions[i],
                        end = positions[j],
                        strokeWidth = 0.6f,
                    )
                }
            }
        }

        // Particle nodes.
        particles.forEachIndexed { i, p ->
            val pulse = sin(time * p.pulseSpeed + p.pulsePha) * 0.12f
            val alpha = (p.baseAlpha + pulse).coerceIn(0.05f, 0.80f)
            drawCircle(
                color = if (p.isAccent) accentColor else nodeColor,
                radius = p.radius * density,
                center = positions[i],
                alpha = alpha,
            )
        }
    }
}
