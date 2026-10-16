alter table policy_versions
    add column parent_version_id bigint,
    add constraint fk_policy_versions_parent
        foreign key (parent_version_id) references policy_versions (id) on delete set null;
