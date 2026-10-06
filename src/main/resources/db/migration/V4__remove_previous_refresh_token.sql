drop index if exists idx_refresh_sessions_previous_token_hash;

alter table refresh_sessions
    drop column if exists previous_token_hash;
