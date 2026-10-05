alter table refresh_sessions
    add column previous_token_hash varchar(64);

create index idx_refresh_sessions_previous_token_hash
    on refresh_sessions (previous_token_hash);
