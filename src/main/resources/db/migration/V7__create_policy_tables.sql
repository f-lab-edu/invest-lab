create table policies (
    id bigint not null auto_increment primary key,
    member_id bigint,
    name varchar(50) not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint fk_policies_member
        foreign key (member_id) references members (id) on delete cascade
);

create table policy_versions (
    id bigint not null auto_increment primary key,
    policy_id bigint not null,
    version_no int not null,
    review_interval_months int not null,
    threshold_bps int not null,
    allow_sell boolean not null,
    cash_target_bps int not null,
    created_at datetime(6) not null,
    constraint uk_policy_versions_policy_version_no unique (policy_id, version_no),
    constraint fk_policy_versions_policy
        foreign key (policy_id) references policies (id) on delete cascade,
    constraint ck_policy_versions_version_no check (version_no >= 1),
    constraint ck_policy_versions_review_interval_months
        check (review_interval_months between 1 and 12),
    constraint ck_policy_versions_threshold_bps check (threshold_bps between 1 and 10000),
    constraint ck_policy_versions_cash_target_bps
        check (cash_target_bps between 0 and 10000)
);

create table policy_allocations (
    id bigint not null auto_increment primary key,
    policy_version_id bigint not null,
    asset_class varchar(30) not null,
    instrument_id bigint not null,
    target_bps int not null,
    constraint uk_policy_allocations_version_asset_class unique (policy_version_id, asset_class),
    constraint uk_policy_allocations_version_instrument unique (policy_version_id, instrument_id),
    constraint fk_policy_allocations_version
        foreign key (policy_version_id) references policy_versions (id) on delete cascade,
    constraint fk_policy_allocations_instrument
        foreign key (instrument_id) references instruments (id),
    constraint ck_policy_allocations_target_bps check (target_bps between 0 and 10000)
);
