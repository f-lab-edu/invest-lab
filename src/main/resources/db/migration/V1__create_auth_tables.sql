create table members (
    id bigint not null auto_increment primary key,
    email varchar(255) not null,
    password_hash varchar(255) not null,
    nickname varchar(20) not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint uk_members_email unique (email),
    constraint uk_members_nickname unique (nickname)
);

create table refresh_sessions (
    id bigint not null auto_increment primary key,
    member_id bigint not null,
    token_hash varchar(64) not null,
    expires_at datetime(6) not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint uk_refresh_sessions_member unique (member_id),
    constraint fk_refresh_sessions_member
        foreign key (member_id) references members (id) on delete cascade
);
