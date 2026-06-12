SET session_replication_role = replica;

--
-- PostgreSQL database dump
--

-- \restrict 7DfUv9wrWKGsO3yZebbOO9B1htlK82cGZDSiXf0FRcblBMfWPK8tOtfkZjJ2pZo

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: audit_log_entries; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."audit_log_entries" ("instance_id", "id", "payload", "created_at", "ip_address") VALUES
	('00000000-0000-0000-0000-000000000000', 'c2119bb8-7f77-450e-8a59-e8109ff9214a', '{"action":"user_signedup","actor_id":"00000000-0000-0000-0000-000000000000","actor_username":"service_role","actor_via_sso":false,"log_type":"team","traits":{"provider":"email","user_email":"norman@shelldoc.local","user_id":"c9b023a5-a0bf-4026-a888-3331a91724f9","user_phone":""}}', '2026-06-11 09:57:02.081563+00', ''),
	('00000000-0000-0000-0000-000000000000', 'ec68f404-8169-4f86-91a5-b6a480061ce3', '{"action":"login","actor_id":"c9b023a5-a0bf-4026-a888-3331a91724f9","actor_username":"norman@shelldoc.local","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 09:58:31.876699+00', ''),
	('00000000-0000-0000-0000-000000000000', '5bbf5f08-99ed-4335-9b31-6408ed3dc503', '{"action":"login","actor_id":"c9b023a5-a0bf-4026-a888-3331a91724f9","actor_username":"norman@shelldoc.local","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 09:59:03.067041+00', ''),
	('00000000-0000-0000-0000-00000' ||
	 '0000000', '665f2430-43c6-4a2b-8798-860767bedcfc', '{"action":"login","actor_id":"c9b023a5-a0bf-4026-a888-3331a91724f9","actor_username":"norman@shelldoc.local","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:03:24.983008+00', ''),
	('00000000-0000-0000-0000-000000000000', '0ea4b124-128b-454f-8759-99c12cd9e4c0', '{"action":"login","actor_id":"c9b023a5-a0bf-4026-a888-3331a91724f9","actor_username":"norman@shelldoc.local","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:06:33.34231+00', ''),
	('00000000-0000-0000-0000-000000000000', 'ef92fdf4-0cbb-4388-95d5-4a6f713b5e40', '{"action":"login","actor_id":"c9b023a5-a0bf-4026-a888-3331a91724f9","actor_username":"norman@shelldoc.local","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:06:53.330783+00', ''),
	('00000000-0000-0000-0000-000000000000', '190976f7-2ff4-4d39-85a0-a918d616f3b4', '{"action":"user_signedup","actor_id":"00000000-0000-0000-0000-000000000000","actor_username":"service_role","actor_via_sso":false,"log_type":"team","traits":{"provider":"email","user_email":"norman@test.com","user_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","user_phone":""}}', '2026-06-11 10:13:40.911445+00', ''),
	('00000000-0000-0000-0000-000000000000', '9f2a98c2-14ed-474d-bee9-3adff65232e2', '{"action":"login","actor_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","actor_username":"norman@test.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:14:04.694852+00', ''),
	('00000000-0000-0000-0000-000000000000', '98b4c4ed-2d5f-4214-9d3f-c87e2e618538', '{"action":"login","actor_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","actor_username":"norman@test.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:15:21.683901+00', ''),
	('00000000-0000-0000-0000-000000000000', 'f6106053-949b-43b0-a6b3-2c3c70cf05a3', '{"action":"login","actor_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","actor_username":"norman@test.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:16:28.171263+00', ''),
	('00000000-0000-0000-0000-000000000000', '4c381f92-0f57-4b68-a550-5c00c19b4752', '{"action":"login","actor_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","actor_username":"norman@test.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:17:42.076882+00', ''),
	('00000000-0000-0000-0000-000000000000', 'eb420544-a056-479a-904c-a4e90efae9d6', '{"action":"login","actor_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","actor_username":"norman@test.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:18:13.959339+00', ''),
	('00000000-0000-0000-0000-000000000000', '1ce8b3b9-0afb-465d-81a5-24c708693ed1', '{"action":"login","actor_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","actor_username":"norman@test.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:19:13.220166+00', ''),
	('00000000-0000-0000-0000-000000000000', 'ec9425af-b703-4eaa-abf9-c11150acd097', '{"action":"login","actor_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","actor_username":"norman@test.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:19:57.97635+00', ''),
	('00000000-0000-0000-0000-000000000000', '785995f6-50ec-4e2f-a682-424f2cffb423', '{"action":"login","actor_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","actor_username":"norman@test.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:21:53.546222+00', ''),
	('00000000-0000-0000-0000-000000000000', 'aac68fb8-8d93-4239-9dc8-8256f1ec6d34', '{"action":"login","actor_id":"0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e","actor_username":"norman@test.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}', '2026-06-11 10:27:58.538323+00', '');


--
-- Data for Name: custom_oauth_providers; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: flow_state; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: users; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."users" ("instance_id", "id", "aud", "role", "email", "encrypted_password", "email_confirmed_at", "invited_at", "confirmation_token", "confirmation_sent_at", "recovery_token", "recovery_sent_at", "email_change_token_new", "email_change", "email_change_sent_at", "last_sign_in_at", "raw_app_meta_data", "raw_user_meta_data", "is_super_admin", "created_at", "updated_at", "phone", "phone_confirmed_at", "phone_change", "phone_change_token", "phone_change_sent_at", "email_change_token_current", "email_change_confirm_status", "banned_until", "reauthentication_token", "reauthentication_sent_at", "is_sso_user", "deleted_at", "is_anonymous") VALUES
	('00000000-0000-0000-0000-000000000000', 'c9b023a5-a0bf-4026-a888-3331a91724f9', 'authenticated', 'authenticated', 'norman@shelldoc.local', '$2a$10$v5KwVgv0eaoZiR3hJzhen.8bBYe3P63GhNU1Zag7GOddyx0I69eYq', '2026-06-11 09:57:02.082933+00', NULL, '', NULL, '', NULL, '', '', NULL, '2026-06-11 10:06:53.331519+00', '{"provider": "email", "providers": ["email"]}', '{"email_verified": true}', NULL, '2026-06-11 09:57:02.079101+00', '2026-06-11 10:06:53.334445+00', NULL, NULL, '', '', NULL, '', 0, NULL, '', NULL, false, NULL, false),
	('00000000-0000-0000-0000-000000000000', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', 'authenticated', 'authenticated', 'norman@test.com', '$2a$10$NHTrOI6i2r8Zkr8aeI2HOOTl62y0oPkBphhyPPlRlpNq76ObEJNrS', '2026-06-11 10:13:40.912543+00', NULL, '', NULL, '', NULL, '', '', NULL, '2026-06-11 10:27:58.539077+00', '{"provider": "email", "providers": ["email"]}', '{"email_verified": true}', NULL, '2026-06-11 10:13:40.909628+00', '2026-06-11 10:27:58.540791+00', NULL, NULL, '', '', NULL, '', 0, NULL, '', NULL, false, NULL, false);


--
-- Data for Name: identities; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."identities" ("provider_id", "user_id", "identity_data", "provider", "last_sign_in_at", "created_at", "updated_at", "id") VALUES
	('c9b023a5-a0bf-4026-a888-3331a91724f9', 'c9b023a5-a0bf-4026-a888-3331a91724f9', '{"sub": "c9b023a5-a0bf-4026-a888-3331a91724f9", "email": "norman@shelldoc.local", "email_verified": false, "phone_verified": false}', 'email', '2026-06-11 09:57:02.080917+00', '2026-06-11 09:57:02.08094+00', '2026-06-11 09:57:02.08094+00', '25e292d7-8138-4c16-b974-1e03ce0bec1b'),
	('0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '{"sub": "0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e", "email": "norman@test.com", "email_verified": false, "phone_verified": false}', 'email', '2026-06-11 10:13:40.910678+00', '2026-06-11 10:13:40.910694+00', '2026-06-11 10:13:40.910694+00', '8d3a55fa-3293-4d9e-a44e-58d50da05753');


--
-- Data for Name: instances; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: oauth_clients; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: sessions; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."sessions" ("id", "user_id", "created_at", "updated_at", "factor_id", "aal", "not_after", "refreshed_at", "user_agent", "ip", "tag", "oauth_client_id", "refresh_token_hmac_key", "refresh_token_counter", "scopes") VALUES
	('5ab40dc0-40a3-42a3-8b16-9ea46181f3d0', 'c9b023a5-a0bf-4026-a888-3331a91724f9', '2026-06-11 09:58:31.877492+00', '2026-06-11 09:58:31.877492+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('26424ae3-4f10-44ea-958a-ff1ec984aeba', 'c9b023a5-a0bf-4026-a888-3331a91724f9', '2026-06-11 09:59:03.067959+00', '2026-06-11 09:59:03.067959+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('ecdda77e-73c1-4915-b8e8-5cc0badb48b5', 'c9b023a5-a0bf-4026-a888-3331a91724f9', '2026-06-11 10:03:24.98407+00', '2026-06-11 10:03:24.98407+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('96819939-9dca-4c36-9ce9-551b51967cd9', 'c9b023a5-a0bf-4026-a888-3331a91724f9', '2026-06-11 10:06:33.343042+00', '2026-06-11 10:06:33.343042+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('6c032e77-a4e6-488a-972d-015137e81bed', 'c9b023a5-a0bf-4026-a888-3331a91724f9', '2026-06-11 10:06:53.331561+00', '2026-06-11 10:06:53.331561+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('35bf91bc-ac25-4629-b6ff-645cc3d455ce', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '2026-06-11 10:14:04.695667+00', '2026-06-11 10:14:04.695667+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('7afef06d-0ed7-4049-a68e-cab0c0e2bbc2', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '2026-06-11 10:15:21.684781+00', '2026-06-11 10:15:21.684781+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('a97682b4-1c98-4174-a13f-9b164827e018', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '2026-06-11 10:16:28.172008+00', '2026-06-11 10:16:28.172008+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('f9fa176b-3c62-4df2-9954-8efab8438e62', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '2026-06-11 10:17:42.078527+00', '2026-06-11 10:17:42.078527+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('29f069b5-2700-4a2a-beeb-96542494c012', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '2026-06-11 10:18:13.960038+00', '2026-06-11 10:18:13.960038+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('092d2415-7f01-49b0-97c8-ea63c19a277a', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '2026-06-11 10:19:13.221189+00', '2026-06-11 10:19:13.221189+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('73eb1719-cfaa-48b0-a8d8-81c964b68d74', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '2026-06-11 10:19:57.977562+00', '2026-06-11 10:19:57.977562+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('553fde0e-a52f-40ba-aa8d-ca71cf351d84', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '2026-06-11 10:21:53.548583+00', '2026-06-11 10:21:53.548583+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL),
	('a5dc4c0b-9559-45ed-881b-cd2c1a428652', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', '2026-06-11 10:27:58.539135+00', '2026-06-11 10:27:58.539135+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '172.22.0.1', NULL, NULL, NULL, NULL, NULL);


--
-- Data for Name: mfa_amr_claims; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."mfa_amr_claims" ("session_id", "created_at", "updated_at", "authentication_method", "id") VALUES
	('5ab40dc0-40a3-42a3-8b16-9ea46181f3d0', '2026-06-11 09:58:31.881613+00', '2026-06-11 09:58:31.881613+00', 'password', '76b09e0c-1c1d-4427-9287-7c684f4e7965'),
	('26424ae3-4f10-44ea-958a-ff1ec984aeba', '2026-06-11 09:59:03.069684+00', '2026-06-11 09:59:03.069684+00', 'password', '0979122d-7c06-4bbc-8911-547fd0bbf468'),
	('ecdda77e-73c1-4915-b8e8-5cc0badb48b5', '2026-06-11 10:03:24.986305+00', '2026-06-11 10:03:24.986305+00', 'password', '46349a3c-1a8e-42a7-9181-2622e7e873ba'),
	('96819939-9dca-4c36-9ce9-551b51967cd9', '2026-06-11 10:06:33.345025+00', '2026-06-11 10:06:33.345025+00', 'password', 'dd255f46-dc9e-44e2-bb20-e7fce6b51954'),
	('6c032e77-a4e6-488a-972d-015137e81bed', '2026-06-11 10:06:53.335468+00', '2026-06-11 10:06:53.335468+00', 'password', 'f9159609-d7ea-4d3b-b6e2-4704bf9c7e91'),
	('35bf91bc-ac25-4629-b6ff-645cc3d455ce', '2026-06-11 10:14:04.697827+00', '2026-06-11 10:14:04.697827+00', 'password', 'ab23f341-efe6-4804-81b9-24170f0edf1e'),
	('7afef06d-0ed7-4049-a68e-cab0c0e2bbc2', '2026-06-11 10:15:21.687056+00', '2026-06-11 10:15:21.687056+00', 'password', 'dc3dcb9a-e734-4694-833a-2a80929bf57e'),
	('a97682b4-1c98-4174-a13f-9b164827e018', '2026-06-11 10:16:28.174299+00', '2026-06-11 10:16:28.174299+00', 'password', 'e5ac071b-95c5-4184-96e1-c6e29739a84d'),
	('f9fa176b-3c62-4df2-9954-8efab8438e62', '2026-06-11 10:17:42.081927+00', '2026-06-11 10:17:42.081927+00', 'password', '3318425b-cde4-4b5b-938f-440cc10ce140'),
	('29f069b5-2700-4a2a-beeb-96542494c012', '2026-06-11 10:18:13.962072+00', '2026-06-11 10:18:13.962072+00', 'password', '875f4ca6-c510-4c25-9191-c051ede0e9c4'),
	('092d2415-7f01-49b0-97c8-ea63c19a277a', '2026-06-11 10:19:13.223577+00', '2026-06-11 10:19:13.223577+00', 'password', '9835dd2c-7a6f-46d7-af9a-19f340e8549c'),
	('73eb1719-cfaa-48b0-a8d8-81c964b68d74', '2026-06-11 10:19:57.979905+00', '2026-06-11 10:19:57.979905+00', 'password', 'd9057598-b809-42f4-aaef-940b804f7cb8'),
	('553fde0e-a52f-40ba-aa8d-ca71cf351d84', '2026-06-11 10:21:53.554797+00', '2026-06-11 10:21:53.554797+00', 'password', '3378c7ee-bc7f-4ae8-ac55-377d168a9ea5'),
	('a5dc4c0b-9559-45ed-881b-cd2c1a428652', '2026-06-11 10:27:58.541057+00', '2026-06-11 10:27:58.541057+00', 'password', '28d66e71-12b8-4b27-bffa-be6b8482fec0');


--
-- Data for Name: mfa_factors; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: mfa_challenges; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: oauth_authorizations; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: oauth_client_states; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: oauth_consents; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: one_time_tokens; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."refresh_tokens" ("instance_id", "id", "token", "user_id", "revoked", "created_at", "updated_at", "parent", "session_id") VALUES
	('00000000-0000-0000-0000-000000000000', 1, 'gs6h3gciff4r', 'c9b023a5-a0bf-4026-a888-3331a91724f9', false, '2026-06-11 09:58:31.879083+00', '2026-06-11 09:58:31.879083+00', NULL, '5ab40dc0-40a3-42a3-8b16-9ea46181f3d0'),
	('00000000-0000-0000-0000-000000000000', 2, 'rjykcdppm7gh', 'c9b023a5-a0bf-4026-a888-3331a91724f9', false, '2026-06-11 09:59:03.068973+00', '2026-06-11 09:59:03.068973+00', NULL, '26424ae3-4f10-44ea-958a-ff1ec984aeba'),
	('00000000-0000-0000-0000-000000000000', 3, 'x4wqgbfnqi5p', 'c9b023a5-a0bf-4026-a888-3331a91724f9', false, '2026-06-11 10:03:24.985214+00', '2026-06-11 10:03:24.985214+00', NULL, 'ecdda77e-73c1-4915-b8e8-5cc0badb48b5'),
	('00000000-0000-0000-0000-000000000000', 4, 'yqdemiyc3mqb', 'c9b023a5-a0bf-4026-a888-3331a91724f9', false, '2026-06-11 10:06:33.344057+00', '2026-06-11 10:06:33.344057+00', NULL, '96819939-9dca-4c36-9ce9-551b51967cd9'),
	('00000000-0000-0000-0000-000000000000', 5, '7qm5spqkl47q', 'c9b023a5-a0bf-4026-a888-3331a91724f9', false, '2026-06-11 10:06:53.332867+00', '2026-06-11 10:06:53.332867+00', NULL, '6c032e77-a4e6-488a-972d-015137e81bed'),
	('00000000-0000-0000-0000-000000000000', 6, '24g5v4kjaxfv', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', false, '2026-06-11 10:14:04.696819+00', '2026-06-11 10:14:04.696819+00', NULL, '35bf91bc-ac25-4629-b6ff-645cc3d455ce'),
	('00000000-0000-0000-0000-000000000000', 7, 'zz57jdwo5lys', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', false, '2026-06-11 10:15:21.686092+00', '2026-06-11 10:15:21.686092+00', NULL, '7afef06d-0ed7-4049-a68e-cab0c0e2bbc2'),
	('00000000-0000-0000-0000-000000000000', 8, '27cwtrsuc56u', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', false, '2026-06-11 10:16:28.173343+00', '2026-06-11 10:16:28.173343+00', NULL, 'a97682b4-1c98-4174-a13f-9b164827e018'),
	('00000000-0000-0000-0000-000000000000', 9, 'rvbto7zx6hvt', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', false, '2026-06-11 10:17:42.080779+00', '2026-06-11 10:17:42.080779+00', NULL, 'f9fa176b-3c62-4df2-9954-8efab8438e62'),
	('00000000-0000-0000-0000-000000000000', 10, '5bxebk3eoe7s', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', false, '2026-06-11 10:18:13.961176+00', '2026-06-11 10:18:13.961176+00', NULL, '29f069b5-2700-4a2a-beeb-96542494c012'),
	('00000000-0000-0000-0000-000000000000', 11, '5hwgztvnlmvj', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', false, '2026-06-11 10:19:13.222552+00', '2026-06-11 10:19:13.222552+00', NULL, '092d2415-7f01-49b0-97c8-ea63c19a277a'),
	('00000000-0000-0000-0000-000000000000', 12, 'abpbybdnkbzj', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', false, '2026-06-11 10:19:57.978879+00', '2026-06-11 10:19:57.978879+00', NULL, '73eb1719-cfaa-48b0-a8d8-81c964b68d74'),
	('00000000-0000-0000-0000-000000000000', 13, 'vzfgrtqc4xei', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', false, '2026-06-11 10:21:53.551962+00', '2026-06-11 10:21:53.551962+00', NULL, '553fde0e-a52f-40ba-aa8d-ca71cf351d84'),
	('00000000-0000-0000-0000-000000000000', 14, '5ndad3ihqvgv', '0b364f86-3b0e-4827-be4f-e1d3dd9dfe7e', false, '2026-06-11 10:27:58.540205+00', '2026-06-11 10:27:58.540205+00', NULL, 'a5dc4c0b-9559-45ed-881b-cd2c1a428652');


--
-- Data for Name: sso_providers; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: saml_providers; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: saml_relay_states; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: sso_domains; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: webauthn_challenges; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: webauthn_credentials; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: assistant_intelligence; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."assistant_intelligence" ("id", "question_hash", "keywords", "cached_answer", "diagram_type", "source_doc_ids", "source_titles", "hit_count", "confidence", "created_at", "updated_at") VALUES
	('58fcdd37-b4e3-4659-8dae-902acdd1b9c8', 'abc123', '{eosb,build,release}', 'EoSB1 is the End of Sprint Build 1 process.', 'flowchart TD', '{doc-1}', '{"EoSB1 Process"}', 2, 'high', '2026-06-06 22:47:42.903699+00', '2026-06-06 22:47:47.548+00'),
	('a39e5c01-1ba6-4327-ab2b-bc277bc1e758', 'bc5caeacac655de4', '{eosb}', '## Meaning
El EoSB (End of Sprint Build) es un término utilizado en el contexto de sprints, que se refiere al primer build generado al finalizar un sprint. Este proceso es crucial para la validación y la preparación del software antes de pasar a los siguientes etapas.

## Context
El acrónimo EoSB aparece en el contexto de los sprints de desarrollo, específicamente mencionado en los documentos sobre procesos de construcción (EoSB1) y pruebas de calidad (QA).

## Example
Un ejemplo concreto podría ser un sprint finalizado que requiere la generación del EoSB1 para validar las funcionalidades implementadas durante el sprint.

## Confidence
High.', NULL, '{internal-acronyms-glossary,59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5}', '{"Internal Acronyms Glossary","EoSB #1 - Shell"}', 1, 'medium', '2026-06-06 23:00:03.121839+00', '2026-06-06 23:00:03.115+00'),
	('fc2b004a-6e38-4f8f-b712-7321bda5e983', '4266f56497d777b6', '{cuales,pasos,eosb}', '## Summary
El proceso de End of Sprint Build (EoSB) incluye varios pasos clave para completar la construcción final de una iteración en un sprint.

## Diagram
```mermaid
flowchart TD
    A[Pause Merges] --> B[Update Lokalise Strings]
    B --> C[Run Quality Assurance Tests]
    C --> D[Prepare for Deployment]
    D --> E[Build and Push to Repositories]
```

## Step-by-step flow

1. **Pause Merges**:
   - Notificar a los miembros del equipo Android que el proceso de EoSB está comenzando.
   - Pausar todos los merges hasta que el proceso esté completo.
   - Asegurarse de que no se incluyan nuevos trabajos de sprint en la construcción actual.

2. **Update Lokalise Strings**:
   - Solicitar al BA el bundle de Lokalise para Android, que contiene archivos `strings.xml` localizados en los directorios correspondientes.

3. **Run Quality Assurance Tests**:
   - Ejecutar pruebas de garantía de calidad (QA) como pruebas de integración, pruebas de sistema y pruebas de regresión.

4. **Prepare for Deployment**:
   - Preparar el código para la implementación asegurándome de que todos los cambios estén listos y probados.

5. **Build and Push to Repositories**:
   - Generar la construcción final del sprint.
   - Empujar la construcción a los repositorios correspondientes para su implementación.

## Notes
El proceso documentado se refiere específicamente al EoSB1, que es el primer build generado en una iteración. El EoSB2 sigue un proceso similar pero con cambios adicionales según la necesidad del proyecto.

## Missing or unclear details
No hay detalles adicionales sobre los pasos específicos de las pruebas de garantía de calidad o cómo preparar el código para la implementación.', 'flowchart TD', '{internal-acronyms-glossary,59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5}', '{"Internal Acronyms Glossary","EoSB #1 - Shell"}', 1, 'medium', '2026-06-06 23:00:52.102425+00', '2026-06-06 23:00:52.099+00'),
	('fb20fa2e-8a98-44b7-8d9e-c5b1bc6e1e12', '29c7243667066b00', '{soy,android,dev,puedo,hacer,eosb}', '## Summary
Como desarrollador Android, puedes seguir el proceso descrito para realizar un End of Sprint Build (EoSB) 1.

## Diagram
```mermaid
flowchart TD
    A[Pause Merges] --> B[Update Lokalise Strings]
    B --> C[Prepare Branches]
    C --> D[Test Features]
    D --> E[Sign Off]
```

## Step-by-step flow

1. **Pause Merges**: Notifica a los miembros del equipo Android que el proceso de EoSB 1 ha comenzado.
   - Todos los mergers deben ser pausados hasta completar el proceso.
   - No debe incluirse nueva work en el build que se está generando.
   - Actualiza la rama `develop` y asegúrate de que esté completamente actualizada.

2. **Update Lokalise Strings**: Solicita el archivo ZIP del bundle de Lokalise al BA.
   - El BA proporcionará un ZIP para Android que contiene los siguientes archivos:
     ```
     values
     values-en-rCA
     values-en-rUS
     values-fr-rCA
     ```
   - Dentro de cada carpeta, habrá un archivo `strings.xml`.
   - Copia estos archivos en la siguiente ruta:
     ```
     presentation/src/main/res/
     ```
   - Reemplaza los correspondientes archivos existentes.

3. **Prepare Branches**: Prepara las ramas necesarias para el build.
   - Comprueba que los branches correctos estén listos y preparados para el EoSB 1.

4. **Test Features**: Prueba todas las características de la rama actual en el Pilot Branch.
   - Asegúrate de que todas las funciones se comporten como esperado.

5. **Sign Off**: Realiza una revisión final para asegurarte de que todo esté funcionando correctamente y procede a la sign-off.

## Notes
- El proceso implica pausar los mergers durante el día, así que asegúrate de tener suficiente tiempo.
- Asegúrate de que todas las funcionalidades principales estén pruebas en el Pilot Branch antes del EoSB 1.

## Missing or unclear details
- El contexto menciona "Pilot Branch", pero no se proporciona detalles específicos sobre cómo manejar o configurar esta rama.', 'flowchart TD', '{internal-acronyms-glossary,59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5}', '{"Internal Acronyms Glossary","EoSB #1 - Shell"}', 1, 'medium', '2026-06-06 23:01:58.873987+00', '2026-06-06 23:01:58.859+00');


--
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."documents" ("id", "source_type", "source_external_id", "source_space_key", "title", "slug", "status", "classification", "current_version_id", "created_by", "updated_by", "created_at", "updated_at", "deleted_at") VALUES
	('59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'manual', NULL, NULL, 'EoSB #1 - Shell', 'eosb-1-shell-1780784441775', 'published', 'internal', '70db46d9-b8b3-4954-a572-21740803209c', NULL, NULL, '2026-06-06 22:20:41.790492+00', '2026-06-06 22:20:41.807613+00', NULL);


--
-- Data for Name: document_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."document_attributes" ("id", "document_id", "key", "value", "source", "created_at", "updated_at") VALUES
	('64857bd3-419c-4960-b09e-29c8fa4b5757', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'team', '"Snake"', 'api', '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00'),
	('cb48e4c5-3e1a-4e19-9c3c-5b57352efa8e', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'status', '"published"', 'api', '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00'),
	('6ce05a26-fedf-4236-b93a-b2054fb932c4', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'tags', '["Android", "Snake", "Team", "Shell", "Documentation", "High"]', 'api', '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00'),
	('58a821ce-0a17-4488-972a-3e47b2e9b189', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'module', '"Android"', 'api', '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00'),
	('384e7204-ec99-4073-834b-6fcc3801c142', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'owner', '"Norman Sanchez"', 'api', '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00'),
	('f98ea0f7-33c5-4625-aa67-d8e73cc76a8d', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'summary', '"Process to complete an End of Sprint Build 1."', 'api', '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00');


--
-- Data for Name: document_versions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."document_versions" ("id", "document_id", "version_number", "title", "raw_markdown", "content_json", "content_plaintext", "content_hash", "change_summary", "source_version", "created_by", "created_at") VALUES
	('70db46d9-b8b3-4954-a572-21740803209c', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 1, 'EoSB #1 - Shell', '# EoSB1 Process for America''s App - Android (2025)

## Concepts

### EoSB1
**End of Sprint Build 1** is the first build generated in an iteration and is usually done one day after the first sprint has finished.

### EoSB2
**End of Sprint Build 2** is the second build generated and is usually done one day after the second sprint has finished.

### Acronyms

| Acronym | Meaning            |
| ------- | ------------------ |
| BA      | Business Analyst   |
| SA      | Solution Architect |
| SM      | Scrum Master       |

---

## Important Notes

For **EoSB1** and **EoSB2** we work with **2 builds**:

1. **Develop Branch**
2. **Extra/Pilot Branch**

The **Pilot Branch** is a branch where the client and US teams can test the application with all feature flags enabled.

---

# EoSB1 Process

## 1. Pause Merges

Notify Android team members that the EoSB1 process is starting.

- All merges should be paused until the process is complete.
- New sprint work should not be included in the build being generated.
- Update the `develop` branch and ensure it is fully up to date.

Example:

```kotlin
buildscript {
    ext {
        versionCode = 810001
        versionCodeUat = 810002
        versionCodeStage = 810003
        versionCodeProd = 810004

        minSdkVersion = 24
        compileSdkVersion = 35
        targetSdkVersion = 35

        versionName = "8.10.0"
    }
}
```

Verify that the correct `versionCodes` exist in `build.gradle`.

---

## 2. Update Lokalise Strings

Request the **Lokalise bundle** from the BA.

The BA will provide a ZIP file for Android containing:

```text
values
values-en-rCA
values-en-rUS
values-fr-rCA
```

Inside each folder there should be a:

```text
strings.xml
```

Copy the files into:

```text
presentation/src/main/res/
```

Replace the corresponding localization files.

---

## 3. Run Validation Tests

Execute:

```bash
gradlew --build-cache --parallel \
compileProdDebugUnitTestSources \
compileProdDebugAndroidTestSources \
jacocoTestReport
```

Purpose:

- Detect missing strings.
- Detect duplicated strings.
- Validate build integrity.

If any string issue is found:

- Notify the SA.
- Request a new Lokalise bundle.
- Repeat the process.

---

## 4. Execute Configuration Generator

Run:

```bash
python3 ./updateconfig.py
```

This updates generated configuration files based on Coco settings.

---

## 5. Validate Generated Files

Review all files modified by `updateconfig.py`.

### Common Error A: FLAG UNSUPPORTED

Example:

```kotlin
GeneralFeatures.NEW_HOME_SCREEN,
GeneralFeatures.ZENDESK,
UNSUPPORTED
```

#### Cause

A new feature flag was added in Coco but has not yet been implemented in the Android codebase.

#### Solution

Remove the conflicting line:

```text
UNSUPPORTED
```

---

### Common Error B: Variable Type Mismatch

Example:

```kotlin
logicTriggerIntervalCW = "720"
```

But the model expects:

```kotlin
val logicTriggerIntervalCW: Int?
```

#### Cause

The value is received as a String from Coco while the generated model expects an Integer.

#### Solution

Update `updateconfig.py` so that the value is parsed using the correct type.

---

## 6. Validate New Secrets

Check whether new secrets were introduced during the sprint.

If yes:

- Verify they exist in Azure.
- Validate all environments:

```text
QA
UAT
STAGE
PROD
```

If they do not exist:

- Request values from the SA.

Reference:

```text
Secrets Management Documentation
```

---

## 7. Compile and Run the Application

Build and execute the application.

Validate:

- No crashes.
- No missing imports.
- No compilation warnings.
- Configuration generated correctly.

---

## 8. Create EoSB1 PR (Develop)

Create a new branch:

```text
feature/eosb1-8100
```

Where:

```text
8100 = Version 8.10.0
```

Commit message:

```text
Generate Build Version for EoSB1 - 8.10.0
```

Target branch:

```text
develop
```

---

## 9. Create EoSB1 PR (Pilot)

After the develop PR is merged:

Move to:

```bash
git checkout extra/pilot-8.99.0
git pull
git merge develop
```

Expected conflicts:

```text
build.gradle.kts
updateconfig.py
```

Keep:

```text
Local changes from extra/pilot
```

Then:

1. Update versionCodes.
2. Run `updateconfig.py`.
3. Compile and execute the app.

Create branch:

```text
extra/eosb1-8100
```

Commit message:

```text
Generate Build Version for EoSB1 - pilot-8.99.0
```

Target branch:

```text
extra/pilot-8.99.0
```

---

## 10. Wait for GitHub Actions

After both PRs are approved and merged:

Go to GitHub Actions and wait for the workflows to finish.

Identify workflows by commit message.

---

## 11. Share Build Information with QA

Send the following information through Microsoft Teams.

Example:

```text
Android build for EoSB1 - 8.10.0

8.10.0 -> #219948
UAT  = 810002
PROD = 810004

8.99.0 -> #219953
UAT  = 899088
PROD = 899090
```

Share:

- Workflow ID
- UAT versionCode
- PROD versionCode

---

## 12. If QA Finds a Critical Bug

Create a fix as a normal:

- User Story
- Defect

Generate a new build.

Important:

- The original EoSB1 build will no longer be considered the official build.
- The fixed build becomes the candidate build.

Also:

- Push the fix to `extra/pilot`.
- Generate a new pilot build.
- Share the new build with QA.

---

## 13. If QA Completes Smoke Test Successfully

Update:

```bash
git checkout madf/pilot
git pull
```

Merge:

```bash
git merge extra/pilot-8.99.0
```

> This step is not required for the develop branch.

---

## 14. Confirm Delivery

Verify that the code has already been pushed into:

```text
madf/pilot
```

The Scrum Master can now notify the client that:

```text
EoSB1 has been completed successfully.
```

---

# Quick Checklist

- [ ] Pause merges
- [ ] Update develop
- [ ] Verify versionCodes
- [ ] Import Lokalise bundle
- [ ] Run Gradle validation commands
- [ ] Execute updateconfig.py
- [ ] Fix generated configuration issues
- [ ] Validate Azure secrets
- [ ] Compile and run app
- [ ] Create Develop PR
- [ ] Merge into Pilot branch
- [ ] Create Pilot PR
- [ ] Wait for GitHub Actions
- [ ] Share build info with QA
- [ ] Resolve smoke test issues (if any)
- [ ] Merge into madf/pilot
- [ ] Notify SM / Client', '{"blocks": [{"text": "EoSB1 Process for America''s App - Android (2025)", "type": "heading", "level": 1}, {"text": "Concepts", "type": "heading", "level": 2}, {"text": "EoSB1", "type": "heading", "level": 3}, {"text": "**End of Sprint Build 1** is the first build generated in an iteration and is usually done one day after the first sprint has finished.", "type": "paragraph"}, {"text": "EoSB2", "type": "heading", "level": 3}, {"text": "**End of Sprint Build 2** is the second build generated and is usually done one day after the second sprint has finished.", "type": "paragraph"}, {"text": "Acronyms", "type": "heading", "level": 3}, {"text": "| Acronym | Meaning            | | ------- | ------------------ | | BA      | Business Analyst   | | SA      | Solution Architect | | SM      | Scrum Master       |", "type": "paragraph"}, {"text": "---", "type": "paragraph"}, {"text": "Important Notes", "type": "heading", "level": 2}, {"text": "For **EoSB1** and **EoSB2** we work with **2 builds**:", "type": "paragraph"}, {"type": "list", "items": ["**Develop Branch**", "**Extra/Pilot Branch**"], "style": "ordered"}, {"text": "The **Pilot Branch** is a branch where the client and US teams can test the application with all feature flags enabled.", "type": "paragraph"}, {"text": "---", "type": "paragraph"}, {"text": "EoSB1 Process", "type": "heading", "level": 1}, {"text": "1. Pause Merges", "type": "heading", "level": 2}, {"text": "Notify Android team members that the EoSB1 process is starting.", "type": "paragraph"}, {"type": "list", "items": ["All merges should be paused until the process is complete.", "New sprint work should not be included in the build being generated.", "Update the `develop` branch and ensure it is fully up to date."], "style": "unordered"}, {"text": "Example:", "type": "paragraph"}, {"code": "buildscript {\n    ext {\n        versionCode = 810001\n        versionCodeUat = 810002\n        versionCodeStage = 810003\n        versionCodeProd = 810004\n\n        minSdkVersion = 24\n        compileSdkVersion = 35\n        targetSdkVersion = 35\n\n        versionName = \"8.10.0\"\n    }\n}", "type": "code", "language": "kotlin"}, {"text": "Verify that the correct `versionCodes` exist in `build.gradle`.", "type": "paragraph"}, {"text": "---", "type": "paragraph"}, {"text": "2. Update Lokalise Strings", "type": "heading", "level": 2}, {"text": "Request the **Lokalise bundle** from the BA.", "type": "paragraph"}, {"text": "The BA will provide a ZIP file for Android containing:", "type": "paragraph"}, {"code": "values\nvalues-en-rCA\nvalues-en-rUS\nvalues-fr-rCA", "type": "code", "language": "text"}, {"text": "Inside each folder there should be a:", "type": "paragraph"}, {"code": "strings.xml", "type": "code", "language": "text"}, {"text": "Copy the files into:", "type": "paragraph"}, {"code": "presentation/src/main/res/", "type": "code", "language": "text"}, {"text": "Replace the corresponding localization files.", "type": "paragraph"}, {"text": "---", "type": "paragraph"}, {"text": "3. Run Validation Tests", "type": "heading", "level": 2}, {"text": "Execute:", "type": "paragraph"}, {"code": "gradlew --build-cache --parallel \\\ncompileProdDebugUnitTestSources \\\ncompileProdDebugAndroidTestSources \\\njacocoTestReport", "type": "code", "language": "bash"}, {"text": "Purpose:", "type": "paragraph"}, {"type": "list", "items": ["Detect missing strings.", "Detect duplicated strings.", "Validate build integrity."], "style": "unordered"}, {"text": "If any string issue is found:", "type": "paragraph"}, {"type": "list", "items": ["Notify the SA.", "Request a new Lokalise bundle.", "Repeat the process."], "style": "unordered"}, {"text": "---", "type": "paragraph"}, {"text": "4. Execute Configuration Generator", "type": "heading", "level": 2}, {"text": "Run:", "type": "paragraph"}, {"code": "python3 ./updateconfig.py", "type": "code", "language": "bash"}, {"text": "This updates generated configuration files based on Coco settings.", "type": "paragraph"}, {"text": "---", "type": "paragraph"}, {"text": "5. Validate Generated Files", "type": "heading", "level": 2}, {"text": "Review all files modified by `updateconfig.py`.", "type": "paragraph"}, {"text": "Common Error A: FLAG UNSUPPORTED", "type": "heading", "level": 3}, {"text": "Example:", "type": "paragraph"}, {"code": "GeneralFeatures.NEW_HOME_SCREEN,\nGeneralFeatures.ZENDESK,\nUNSUPPORTED", "type": "code", "language": "kotlin"}, {"text": "Cause", "type": "heading", "level": 4}, {"text": "A new feature flag was added in Coco but has not yet been implemented in the Android codebase.", "type": "paragraph"}, {"text": "Solution", "type": "heading", "level": 4}, {"text": "Remove the conflicting line:", "type": "paragraph"}, {"code": "UNSUPPORTED", "type": "code", "language": "text"}, {"text": "---", "type": "paragraph"}, {"text": "Common Error B: Variable Type Mismatch", "type": "heading", "level": 3}, {"text": "Example:", "type": "paragraph"}, {"code": "logicTriggerIntervalCW = \"720\"", "type": "code", "language": "kotlin"}, {"text": "But the model expects:", "type": "paragraph"}, {"code": "val logicTriggerIntervalCW: Int?", "type": "code", "language": "kotlin"}, {"text": "Cause", "type": "heading", "level": 4}, {"text": "The value is received as a String from Coco while the generated model expects an Integer.", "type": "paragraph"}, {"text": "Solution", "type": "heading", "level": 4}, {"text": "Update `updateconfig.py` so that the value is parsed using the correct type.", "type": "paragraph"}, {"text": "---", "type": "paragraph"}, {"text": "6. Validate New Secrets", "type": "heading", "level": 2}, {"text": "Check whether new secrets were introduced during the sprint.", "type": "paragraph"}, {"text": "If yes:", "type": "paragraph"}, {"type": "list", "items": ["Verify they exist in Azure.", "Validate all environments:"], "style": "unordered"}, {"code": "QA\nUAT\nSTAGE\nPROD", "type": "code", "language": "text"}, {"text": "If they do not exist:", "type": "paragraph"}, {"type": "list", "items": ["Request values from the SA."], "style": "unordered"}, {"text": "Reference:", "type": "paragraph"}, {"code": "Secrets Management Documentation", "type": "code", "language": "text"}, {"text": "---", "type": "paragraph"}, {"text": "7. Compile and Run the Application", "type": "heading", "level": 2}, {"text": "Build and execute the application.", "type": "paragraph"}, {"text": "Validate:", "type": "paragraph"}, {"type": "list", "items": ["No crashes.", "No missing imports.", "No compilation warnings.", "Configuration generated correctly."], "style": "unordered"}, {"text": "---", "type": "paragraph"}, {"text": "8. Create EoSB1 PR (Develop)", "type": "heading", "level": 2}, {"text": "Create a new branch:", "type": "paragraph"}, {"code": "feature/eosb1-8100", "type": "code", "language": "text"}, {"text": "Where:", "type": "paragraph"}, {"code": "8100 = Version 8.10.0", "type": "code", "language": "text"}, {"text": "Commit message:", "type": "paragraph"}, {"code": "Generate Build Version for EoSB1 - 8.10.0", "type": "code", "language": "text"}, {"text": "Target branch:", "type": "paragraph"}, {"code": "develop", "type": "code", "language": "text"}, {"text": "---", "type": "paragraph"}, {"text": "9. Create EoSB1 PR (Pilot)", "type": "heading", "level": 2}, {"text": "After the develop PR is merged:", "type": "paragraph"}, {"text": "Move to:", "type": "paragraph"}, {"code": "git checkout extra/pilot-8.99.0\ngit pull\ngit merge develop", "type": "code", "language": "bash"}, {"text": "Expected conflicts:", "type": "paragraph"}, {"code": "build.gradle.kts\nupdateconfig.py", "type": "code", "language": "text"}, {"text": "Keep:", "type": "paragraph"}, {"code": "Local changes from extra/pilot", "type": "code", "language": "text"}, {"text": "Then:", "type": "paragraph"}, {"type": "list", "items": ["Update versionCodes.", "Run `updateconfig.py`.", "Compile and execute the app."], "style": "ordered"}, {"text": "Create branch:", "type": "paragraph"}, {"code": "extra/eosb1-8100", "type": "code", "language": "text"}, {"text": "Commit message:", "type": "paragraph"}, {"code": "Generate Build Version for EoSB1 - pilot-8.99.0", "type": "code", "language": "text"}, {"text": "Target branch:", "type": "paragraph"}, {"code": "extra/pilot-8.99.0", "type": "code", "language": "text"}, {"text": "---", "type": "paragraph"}, {"text": "10. Wait for GitHub Actions", "type": "heading", "level": 2}, {"text": "After both PRs are approved and merged:", "type": "paragraph"}, {"text": "Go to GitHub Actions and wait for the workflows to finish.", "type": "paragraph"}, {"text": "Identify workflows by commit message.", "type": "paragraph"}, {"text": "---", "type": "paragraph"}, {"text": "11. Share Build Information with QA", "type": "heading", "level": 2}, {"text": "Send the following information through Microsoft Teams.", "type": "paragraph"}, {"text": "Example:", "type": "paragraph"}, {"code": "Android build for EoSB1 - 8.10.0\n\n8.10.0 -> #219948\nUAT  = 810002\nPROD = 810004\n\n8.99.0 -> #219953\nUAT  = 899088\nPROD = 899090", "type": "code", "language": "text"}, {"text": "Share:", "type": "paragraph"}, {"type": "list", "items": ["Workflow ID", "UAT versionCode", "PROD versionCode"], "style": "unordered"}, {"text": "---", "type": "paragraph"}, {"text": "12. If QA Finds a Critical Bug", "type": "heading", "level": 2}, {"text": "Create a fix as a normal:", "type": "paragraph"}, {"type": "list", "items": ["User Story", "Defect"], "style": "unordered"}, {"text": "Generate a new build.", "type": "paragraph"}, {"text": "Important:", "type": "paragraph"}, {"type": "list", "items": ["The original EoSB1 build will no longer be considered the official build.", "The fixed build becomes the candidate build."], "style": "unordered"}, {"text": "Also:", "type": "paragraph"}, {"type": "list", "items": ["Push the fix to `extra/pilot`.", "Generate a new pilot build.", "Share the new build with QA."], "style": "unordered"}, {"text": "---", "type": "paragraph"}, {"text": "13. If QA Completes Smoke Test Successfully", "type": "heading", "level": 2}, {"text": "Update:", "type": "paragraph"}, {"code": "git checkout madf/pilot\ngit pull", "type": "code", "language": "bash"}, {"text": "Merge:", "type": "paragraph"}, {"code": "git merge extra/pilot-8.99.0", "type": "code", "language": "bash"}, {"text": "This step is not required for the develop branch.", "type": "blockquote"}, {"text": "---", "type": "paragraph"}, {"text": "14. Confirm Delivery", "type": "heading", "level": 2}, {"text": "Verify that the code has already been pushed into:", "type": "paragraph"}, {"code": "madf/pilot", "type": "code", "language": "text"}, {"text": "The Scrum Master can now notify the client that:", "type": "paragraph"}, {"code": "EoSB1 has been completed successfully.", "type": "code", "language": "text"}, {"text": "---", "type": "paragraph"}, {"text": "Quick Checklist", "type": "heading", "level": 1}, {"type": "list", "items": ["[ ] Pause merges", "[ ] Update develop", "[ ] Verify versionCodes", "[ ] Import Lokalise bundle", "[ ] Run Gradle validation commands", "[ ] Execute updateconfig.py", "[ ] Fix generated configuration issues", "[ ] Validate Azure secrets", "[ ] Compile and run app", "[ ] Create Develop PR", "[ ] Merge into Pilot branch", "[ ] Create Pilot PR", "[ ] Wait for GitHub Actions", "[ ] Share build info with QA", "[ ] Resolve smoke test issues (if any)", "[ ] Merge into madf/pilot", "[ ] Notify SM / Client"], "style": "unordered"}], "schema_version": 1}', 'EoSB1 Process for America''s App - Android (2025)
Concepts
EoSB1
**End of Sprint Build 1** is the first build generated in an iteration and is usually done one day after the first sprint has finished.
EoSB2
**End of Sprint Build 2** is the second build generated and is usually done one day after the second sprint has finished.
Acronyms
| Acronym | Meaning            | | ------- | ------------------ | | BA      | Business Analyst   | | SA      | Solution Architect | | SM      | Scrum Master       |
---
Important Notes
For **EoSB1** and **EoSB2** we work with **2 builds**:
**Develop Branch**
**Extra/Pilot Branch**
The **Pilot Branch** is a branch where the client and US teams can test the application with all feature flags enabled.
---
EoSB1 Process
1. Pause Merges
Notify Android team members that the EoSB1 process is starting.
All merges should be paused until the process is complete.
New sprint work should not be included in the build being generated.
Update the `develop` branch and ensure it is fully up to date.
Example:
buildscript {
    ext {
        versionCode = 810001
        versionCodeUat = 810002
        versionCodeStage = 810003
        versionCodeProd = 810004

        minSdkVersion = 24
        compileSdkVersion = 35
        targetSdkVersion = 35

        versionName = "8.10.0"
    }
}
Verify that the correct `versionCodes` exist in `build.gradle`.
---
2. Update Lokalise Strings
Request the **Lokalise bundle** from the BA.
The BA will provide a ZIP file for Android containing:
values
values-en-rCA
values-en-rUS
values-fr-rCA
Inside each folder there should be a:
strings.xml
Copy the files into:
presentation/src/main/res/
Replace the corresponding localization files.
---
3. Run Validation Tests
Execute:
gradlew --build-cache --parallel \
compileProdDebugUnitTestSources \
compileProdDebugAndroidTestSources \
jacocoTestReport
Purpose:
Detect missing strings.
Detect duplicated strings.
Validate build integrity.
If any string issue is found:
Notify the SA.
Request a new Lokalise bundle.
Repeat the process.
---
4. Execute Configuration Generator
Run:
python3 ./updateconfig.py
This updates generated configuration files based on Coco settings.
---
5. Validate Generated Files
Review all files modified by `updateconfig.py`.
Common Error A: FLAG UNSUPPORTED
Example:
GeneralFeatures.NEW_HOME_SCREEN,
GeneralFeatures.ZENDESK,
UNSUPPORTED
Cause
A new feature flag was added in Coco but has not yet been implemented in the Android codebase.
Solution
Remove the conflicting line:
UNSUPPORTED
---
Common Error B: Variable Type Mismatch
Example:
logicTriggerIntervalCW = "720"
But the model expects:
val logicTriggerIntervalCW: Int?
Cause
The value is received as a String from Coco while the generated model expects an Integer.
Solution
Update `updateconfig.py` so that the value is parsed using the correct type.
---
6. Validate New Secrets
Check whether new secrets were introduced during the sprint.
If yes:
Verify they exist in Azure.
Validate all environments:
QA
UAT
STAGE
PROD
If they do not exist:
Request values from the SA.
Reference:
Secrets Management Documentation
---
7. Compile and Run the Application
Build and execute the application.
Validate:
No crashes.
No missing imports.
No compilation warnings.
Configuration generated correctly.
---
8. Create EoSB1 PR (Develop)
Create a new branch:
feature/eosb1-8100
Where:
8100 = Version 8.10.0
Commit message:
Generate Build Version for EoSB1 - 8.10.0
Target branch:
develop
---
9. Create EoSB1 PR (Pilot)
After the develop PR is merged:
Move to:
git checkout extra/pilot-8.99.0
git pull
git merge develop
Expected conflicts:
build.gradle.kts
updateconfig.py
Keep:
Local changes from extra/pilot
Then:
Update versionCodes.
Run `updateconfig.py`.
Compile and execute the app.
Create branch:
extra/eosb1-8100
Commit message:
Generate Build Version for EoSB1 - pilot-8.99.0
Target branch:
extra/pilot-8.99.0
---
10. Wait for GitHub Actions
After both PRs are approved and merged:
Go to GitHub Actions and wait for the workflows to finish.
Identify workflows by commit message.
---
11. Share Build Information with QA
Send the following information through Microsoft Teams.
Example:
Android build for EoSB1 - 8.10.0

8.10.0 -> #219948
UAT  = 810002
PROD = 810004

8.99.0 -> #219953
UAT  = 899088
PROD = 899090
Share:
Workflow ID
UAT versionCode
PROD versionCode
---
12. If QA Finds a Critical Bug
Create a fix as a normal:
User Story
Defect
Generate a new build.
Important:
The original EoSB1 build will no longer be considered the official build.
The fixed build becomes the candidate build.
Also:
Push the fix to `extra/pilot`.
Generate a new pilot build.
Share the new build with QA.
---
13. If QA Completes Smoke Test Successfully
Update:
git checkout madf/pilot
git pull
Merge:
git merge extra/pilot-8.99.0
This step is not required for the develop branch.
---
14. Confirm Delivery
Verify that the code has already been pushed into:
madf/pilot
The Scrum Master can now notify the client that:
EoSB1 has been completed successfully.
---
Quick Checklist
[ ] Pause merges
[ ] Update develop
[ ] Verify versionCodes
[ ] Import Lokalise bundle
[ ] Run Gradle validation commands
[ ] Execute updateconfig.py
[ ] Fix generated configuration issues
[ ] Validate Azure secrets
[ ] Compile and run app
[ ] Create Develop PR
[ ] Merge into Pilot branch
[ ] Create Pilot PR
[ ] Wait for GitHub Actions
[ ] Share build info with QA
[ ] Resolve smoke test issues (if any)
[ ] Merge into madf/pilot
[ ] Notify SM / Client', 'sha256:e7ca45c98bf76eb553e8d517dd32a43a6d87557ddbd19610778c974c309ad198', 'Created from ShellDoc', NULL, NULL, '2026-06-06 22:20:41.800264+00');


--
-- Data for Name: document_drafts; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: document_links; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: sync_runs; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: buckets; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: buckets_analytics; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: buckets_vectors; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: iceberg_namespaces; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: iceberg_tables; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: objects; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: s3_multipart_uploads; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: s3_multipart_uploads_parts; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: vector_indexes; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: hooks; Type: TABLE DATA; Schema: supabase_functions; Owner: supabase_functions_admin
--



--
-- Name: refresh_tokens_id_seq; Type: SEQUENCE SET; Schema: auth; Owner: supabase_auth_admin
--

SELECT pg_catalog.setval('"auth"."refresh_tokens_id_seq"', 14, true);


--
-- Name: hooks_id_seq; Type: SEQUENCE SET; Schema: supabase_functions; Owner: supabase_functions_admin
--

SELECT pg_catalog.setval('"supabase_functions"."hooks_id_seq"', 1, false);


--
-- PostgreSQL database dump complete
--

-- \unrestrict 7DfUv9wrWKGsO3yZebbOO9B1htlK82cGZDSiXf0FRcblBMfWPK8tOtfkZjJ2pZo

RESET ALL;
