drop index idx_refresh_sessions_previous_token_hash
    on refresh_sessions;

alter table refresh_sessions
    drop column previous_token_hash;
