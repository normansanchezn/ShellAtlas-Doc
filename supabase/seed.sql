SET
session_replication_role = replica;

--
-- PostgreSQL database dump
--

-- \restrict cSoPehyTLVqF6G7pNRVgbdFdUnXNTp7nFEi7NqwWkImpCIlo9xJH1nhXUMJvIb3

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

SET
statement_timeout = 0;
SET
lock_timeout = 0;
SET
idle_in_transaction_session_timeout = 0;
SET
transaction_timeout = 0;
SET
client_encoding = 'UTF8';
SET
standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET
check_function_bodies = false;
SET
xmloption = content;
SET
client_min_messages = warning;
SET
row_security = off;

--
-- Data for Name: audit_log_entries; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."audit_log_entries" ("instance_id", "id", "payload", "created_at", "ip_address")
VALUES ('00000000-0000-0000-0000-000000000000', 'd656dd07-89cd-43b1-81b5-01bdd92ef458',
        '{"action":"user_signedup","actor_id":"00000000-0000-0000-0000-000000000000","actor_username":"service_role","actor_via_sso":false,"log_type":"team","traits":{"provider":"email","user_email":"norman.sanchez@demo.com","user_id":"5d25d418-2a38-440c-90d6-9cfeb74fc565","user_phone":""}}',
        '2026-06-16 20:37:31.660455+00', ''),
       ('00000000-0000-0000-0000-000000000000', '5b20d105-3070-4325-8372-a347bea57dd8',
        '{"action":"user_signedup","actor_id":"00000000-0000-0000-0000-000000000000","actor_username":"service_role","actor_via_sso":false,"log_type":"team","traits":{"provider":"email","user_email":"demo@demo.com","user_id":"f8c1ea2d-4041-4457-a013-5da99aeca565","user_phone":""}}',
        '2026-06-16 20:38:14.170286+00', ''),
       ('00000000-0000-0000-0000-000000000000', '1684ef88-4129-4cae-b80a-b173d7e9b722',
        '{"action":"login","actor_id":"f8c1ea2d-4041-4457-a013-5da99aeca565","actor_username":"demo@demo.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}',
        '2026-06-16 20:38:28.043447+00', ''),
       ('00000000-0000-0000-0000-000000000000', '882a9a1b-3b3d-465e-a60a-f8adcd459d05',
        '{"action":"login","actor_id":"f8c1ea2d-4041-4457-a013-5da99aeca565","actor_username":"demo@demo.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}',
        '2026-06-16 20:38:49.821249+00', ''),
       ('00000000-0000-0000-0000-000000000000', '60c1fd87-d798-4f5c-be63-671e4812386f',
        '{"action":"login","actor_id":"f8c1ea2d-4041-4457-a013-5da99aeca565","actor_username":"demo@demo.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}',
        '2026-06-16 21:37:17.745455+00', ''),
       ('00000000-0000-0000-0000-000000000000', '19688e25-72e8-42c7-899c-4a3c50037f7c',
        '{"action":"login","actor_id":"f8c1ea2d-4041-4457-a013-5da99aeca565","actor_username":"demo@demo.com","actor_via_sso":false,"log_type":"account","traits":{"provider":"email"}}',
        '2026-06-16 22:06:39.062572+00', '');


--
-- Data for Name: custom_oauth_providers; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--


--
-- Data for Name: flow_state; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--


--
-- Data for Name: users; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."users" ("instance_id", "id", "aud", "role", "email", "encrypted_password", "email_confirmed_at",
                            "invited_at", "confirmation_token", "confirmation_sent_at", "recovery_token",
                            "recovery_sent_at", "email_change_token_new", "email_change", "email_change_sent_at",
                            "last_sign_in_at", "raw_app_meta_data", "raw_user_meta_data", "is_super_admin",
                            "created_at", "updated_at", "phone", "phone_confirmed_at", "phone_change",
                            "phone_change_token", "phone_change_sent_at", "email_change_token_current",
                            "email_change_confirm_status", "banned_until", "reauthentication_token",
                            "reauthentication_sent_at", "is_sso_user", "deleted_at", "is_anonymous")
VALUES ('00000000-0000-0000-0000-000000000000', '5d25d418-2a38-440c-90d6-9cfeb74fc565', 'authenticated',
        'authenticated', 'norman.sanchez@demo.com', '$2a$10$sJrKoIz7UGsY.JCCK77JNuf0zBkBlnOqCdl/Z8YGP3svb08K.LMC2',
        '2026-06-16 20:37:31.661033+00', NULL, '', NULL, '', NULL, '', '', NULL, NULL,
        '{"provider": "email", "providers": ["email"]}', '{"email_verified": true}', NULL,
        '2026-06-16 20:37:31.6569+00', '2026-06-16 20:37:31.661422+00', NULL, NULL, '', '', NULL, '', 0, NULL, '', NULL,
        false, NULL, false),
       ('00000000-0000-0000-0000-000000000000', 'f8c1ea2d-4041-4457-a013-5da99aeca565', 'authenticated',
        'authenticated', 'demo@demo.com', '$2a$10$Wkq9zsUCety6v3ftgYXWeugW5YQ5CueDBha.QRNjDXAOF5SQikC8q',
        '2026-06-16 20:38:14.170889+00', NULL, '', NULL, '', NULL, '', '', NULL, '2026-06-16 22:06:39.06328+00',
        '{"provider": "email", "providers": ["email"]}', '{"email_verified": true}', NULL,
        '2026-06-16 20:38:14.168257+00', '2026-06-16 22:06:39.064919+00', NULL, NULL, '', '', NULL, '', 0, NULL, '',
        NULL, false, NULL, false);


--
-- Data for Name: identities; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."identities" ("provider_id", "user_id", "identity_data", "provider", "last_sign_in_at", "created_at",
                                 "updated_at", "id")
VALUES ('5d25d418-2a38-440c-90d6-9cfeb74fc565', '5d25d418-2a38-440c-90d6-9cfeb74fc565',
        '{"sub": "5d25d418-2a38-440c-90d6-9cfeb74fc565", "email": "norman.sanchez@demo.com", "email_verified": false, "phone_verified": false}',
        'email', '2026-06-16 20:37:31.65984+00', '2026-06-16 20:37:31.659877+00', '2026-06-16 20:37:31.659877+00',
        '6b6ff843-4f46-4f3d-b038-a830a98dda0f'),
       ('f8c1ea2d-4041-4457-a013-5da99aeca565', 'f8c1ea2d-4041-4457-a013-5da99aeca565',
        '{"sub": "f8c1ea2d-4041-4457-a013-5da99aeca565", "email": "demo@demo.com", "email_verified": false, "phone_verified": false}',
        'email', '2026-06-16 20:38:14.169898+00', '2026-06-16 20:38:14.169916+00', '2026-06-16 20:38:14.169916+00',
        '5fc84bbe-eab9-4d31-8274-7316ae7b8890');


--
-- Data for Name: instances; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--


--
-- Data for Name: oauth_clients; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--


--
-- Data for Name: sessions; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."sessions" ("id", "user_id", "created_at", "updated_at", "factor_id", "aal", "not_after",
                               "refreshed_at", "user_agent", "ip", "tag", "oauth_client_id", "refresh_token_hmac_key",
                               "refresh_token_counter", "scopes")
VALUES ('b7371ef2-de49-4ee4-8da5-a6efc7ca8dc5', 'f8c1ea2d-4041-4457-a013-5da99aeca565', '2026-06-16 20:38:28.044039+00',
        '2026-06-16 20:38:28.044039+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '192.168.65.1', NULL, NULL, NULL,
        NULL, NULL),
       ('6a7d42b3-91b9-47e6-9e2a-d93ee199baad', 'f8c1ea2d-4041-4457-a013-5da99aeca565', '2026-06-16 20:38:49.822801+00',
        '2026-06-16 20:38:49.822801+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '192.168.65.1', NULL, NULL, NULL,
        NULL, NULL),
       ('e6bc64aa-a788-4780-9a56-95ab3890c2fc', 'f8c1ea2d-4041-4457-a013-5da99aeca565', '2026-06-16 21:37:17.746244+00',
        '2026-06-16 21:37:17.746244+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '192.168.65.1', NULL, NULL, NULL,
        NULL, NULL),
       ('01023873-21ef-41a2-8c9c-dd14a9db0aee', 'f8c1ea2d-4041-4457-a013-5da99aeca565', '2026-06-16 22:06:39.063342+00',
        '2026-06-16 22:06:39.063342+00', NULL, 'aal1', NULL, NULL, 'ktor-client', '192.168.65.1', NULL, NULL, NULL,
        NULL, NULL);


--
-- Data for Name: mfa_amr_claims; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."mfa_amr_claims" ("session_id", "created_at", "updated_at", "authentication_method", "id")
VALUES ('b7371ef2-de49-4ee4-8da5-a6efc7ca8dc5', '2026-06-16 20:38:28.045932+00', '2026-06-16 20:38:28.045932+00',
        'password', 'fc388984-d075-4175-9e5d-92c8b9d86f8a'),
       ('6a7d42b3-91b9-47e6-9e2a-d93ee199baad', '2026-06-16 20:38:49.825332+00', '2026-06-16 20:38:49.825332+00',
        'password', 'f49b8a1f-6a54-4172-8e6f-c7935b5476c6'),
       ('e6bc64aa-a788-4780-9a56-95ab3890c2fc', '2026-06-16 21:37:17.749578+00', '2026-06-16 21:37:17.749578+00',
        'password', '25c5f42e-4f37-4a5a-98ec-0293515e637b'),
       ('01023873-21ef-41a2-8c9c-dd14a9db0aee', '2026-06-16 22:06:39.065242+00', '2026-06-16 22:06:39.065242+00',
        'password', 'f5b122b0-f21b-4d29-a9ab-8030a5227a53');


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

INSERT INTO "auth"."refresh_tokens" ("instance_id", "id", "token", "user_id", "revoked", "created_at", "updated_at",
                                     "parent", "session_id")
VALUES ('00000000-0000-0000-0000-000000000000', 1, 'hzkd4e7nb7p6', 'f8c1ea2d-4041-4457-a013-5da99aeca565', false,
        '2026-06-16 20:38:28.045056+00', '2026-06-16 20:38:28.045056+00', NULL, 'b7371ef2-de49-4ee4-8da5-a6efc7ca8dc5'),
       ('00000000-0000-0000-0000-000000000000', 2, 'dwlfntwlt7nv', 'f8c1ea2d-4041-4457-a013-5da99aeca565', false,
        '2026-06-16 20:38:49.824186+00', '2026-06-16 20:38:49.824186+00', NULL, '6a7d42b3-91b9-47e6-9e2a-d93ee199baad'),
       ('00000000-0000-0000-0000-000000000000', 3, 'fmgrk32fcaqq', 'f8c1ea2d-4041-4457-a013-5da99aeca565', false,
        '2026-06-16 21:37:17.748374+00', '2026-06-16 21:37:17.748374+00', NULL, 'e6bc64aa-a788-4780-9a56-95ab3890c2fc'),
       ('00000000-0000-0000-0000-000000000000', 4, 'jrxeapz3uwsu', 'f8c1ea2d-4041-4457-a013-5da99aeca565', false,
        '2026-06-16 22:06:39.064289+00', '2026-06-16 22:06:39.064289+00', NULL, '01023873-21ef-41a2-8c9c-dd14a9db0aee');


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

INSERT INTO "public"."assistant_intelligence" ("id", "question_hash", "keywords", "cached_answer", "diagram_type",
                                               "source_doc_ids", "source_titles", "intent", "confidence", "hit_count",
                                               "created_at", "updated_at")
VALUES ('58fcdd37-b4e3-4659-8dae-902acdd1b9c8', 'abc123', '{eosb,build,release}',
        'EoSB1 is the End of Sprint Build 1 process.', 'flowchart TD', '{59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5}',
        '{"EoSB #1 - Shell"}', 'question', 'high', 2, '2026-06-06 22:47:42.903699+00', '2026-06-06 22:47:47.548+00');


--
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."documents" ("id", "title", "slug", "status", "classification", "source_type",
                                  "source_external_id", "source_space_key", "current_version_id", "created_by",
                                  "updated_by", "created_at", "updated_at", "deleted_at")
VALUES ('59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'EoSB #1 - Shell', 'eosb-1-shell-1780784441775', 'published',
        'internal', 'manual', NULL, NULL, '70db46d9-b8b3-4954-a572-21740803209c', NULL, NULL,
        '2026-06-06 22:20:41.790492+00', '2026-06-16 20:29:21.853459+00', NULL);


--
-- Data for Name: document_attributes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."document_attributes" ("id", "document_id", "key", "value", "source", "created_at", "updated_at")
VALUES ('64857bd3-419c-4960-b09e-29c8fa4b5757', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'team', '"Snake"', 'api',
        '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00'),
       ('cb48e4c5-3e1a-4e19-9c3c-5b57352efa8e', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'status', '"published"', 'api',
        '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00'),
       ('6ce05a26-fedf-4236-b93a-b2054fb932c4', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'tags',
        '["Android", "Snake", "Team", "Shell", "Documentation", "High"]', 'api', '2026-06-06 22:20:41.811642+00',
        '2026-06-06 22:20:41.811642+00'),
       ('58a821ce-0a17-4488-972a-3e47b2e9b189', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'module', '"Android"', 'api',
        '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00'),
       ('384e7204-ec99-4073-834b-6fcc3801c142', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'owner', '"Norman Sanchez"',
        'api', '2026-06-06 22:20:41.811642+00', '2026-06-06 22:20:41.811642+00'),
       ('f98ea0f7-33c5-4625-aa67-d8e73cc76a8d', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 'summary',
        '"Process to complete an End of Sprint Build 1."', 'api', '2026-06-06 22:20:41.811642+00',
        '2026-06-06 22:20:41.811642+00');


--
-- Data for Name: document_versions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."document_versions" ("id", "document_id", "version_number", "title", "raw_markdown",
                                          "content_json", "content_plaintext", "content_hash", "change_summary",
                                          "source_version", "created_by", "created_at")
VALUES ('70db46d9-b8b3-4954-a572-21740803209c', '59b2c7ed-cb35-47eb-a0b3-0c68e9e3dcb5', 1, 'EoSB #1 - Shell', '# EoSB1 Process for America''s App - Android (2025)

## Concepts

### EoSB1
**End of Sprint Build 1** is the first build generated in an iteration and is usually done one day after the first sprint has finished.

## Quick Checklist

- Pause merges
- Update develop
- Verify versionCodes
- Run updateconfig.py
- Validate Azure secrets',
        '{"blocks": [{"text": "EoSB1 Process for America''s App - Android (2025)", "type": "heading", "level": 1}, {"text": "Concepts", "type": "heading", "level": 2}, {"text": "EoSB1", "type": "heading", "level": 3}, {"text": "**End of Sprint Build 1** is the first build generated in an iteration and is usually done one day after the first sprint has finished.", "type": "paragraph"}, {"text": "Quick Checklist", "type": "heading", "level": 2}, {"type": "list", "items": ["Pause merges", "Update develop", "Verify versionCodes", "Run updateconfig.py", "Validate Azure secrets"], "style": "unordered"}], "schema_version": 1}',
        'EoSB1 Process for America''s App - Android (2025) Concepts EoSB1 End of Sprint Build 1 is the first build generated in an iteration. Quick Checklist Pause merges Update develop Verify versionCodes Run updateconfig.py Validate Azure secrets',
        '8fdd8d907165f7d7a0f9aa8f7b9a100ea0ee4fdaf2ed5fb426d67d95c9df27cd', 'Initial imported seed', 'seed-v1', NULL,
        '2026-06-06 22:20:41.807613+00');


--
-- Data for Name: document_drafts; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: document_links; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: profiles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."profiles" ("id", "full_name", "team", "email", "created_at", "updated_at")
VALUES ('5d25d418-2a38-440c-90d6-9cfeb74fc565', '', '', 'norman.sanchez@demo.com', '2026-06-16 20:37:31.656725+00',
        '2026-06-16 20:37:31.656725+00'),
       ('f8c1ea2d-4041-4457-a013-5da99aeca565', '', '', 'demo@demo.com', '2026-06-16 20:38:14.168118+00',
        '2026-06-16 20:38:14.168118+00');


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."roles" ("key", "display_name", "description")
VALUES ('owner', 'Owner', 'Full control: content, members, integrations and analytics'),
       ('business', 'Business', 'Reads knowledge, asks the assistant and views analytics'),
       ('develop', 'Develop', 'Creates, edits and publishes technical documentation'),
       ('viewer', 'Viewer', 'Read-only fallback for unassigned users');


--
-- Data for Name: sync_runs; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."user_roles" ("user_id", "role_key", "assigned_by", "created_at", "updated_at")
VALUES ('5d25d418-2a38-440c-90d6-9cfeb74fc565', 'viewer', NULL, '2026-06-16 20:37:31.656725+00',
        '2026-06-16 20:37:31.656725+00'),
       ('f8c1ea2d-4041-4457-a013-5da99aeca565', 'viewer', NULL, '2026-06-16 20:38:14.168118+00',
        '2026-06-16 20:38:14.168118+00');


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

SELECT pg_catalog.setval('"auth"."refresh_tokens_id_seq"', 4, true);


--
-- Name: hooks_id_seq; Type: SEQUENCE SET; Schema: supabase_functions; Owner: supabase_functions_admin
--

SELECT pg_catalog.setval('"supabase_functions"."hooks_id_seq"', 1, false);


--
-- PostgreSQL database dump complete
--

-- \unrestrict cSoPehyTLVqF6G7pNRVgbdFdUnXNTp7nFEi7NqwWkImpCIlo9xJH1nhXUMJvIb3

RESET
ALL;
