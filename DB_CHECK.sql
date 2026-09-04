-- À exécuter dans enquete_db pour vérifier les données créées par la plateforme.
SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;
SELECT id, identifiant, email FROM users ORDER BY id;
SELECT id, owner_id, public_key, title, status, updated_at FROM surveys ORDER BY updated_at DESC;
SELECT id, survey_id, event_type, channel, occurred_at FROM survey_delivery_events ORDER BY occurred_at DESC;
SELECT id, survey_id, anonymous, completed_at FROM survey_responses ORDER BY completed_at DESC;
