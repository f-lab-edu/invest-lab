alter table policies add column latest_version_no int;

update policies p
set latest_version_no = (select max(v.version_no) from policy_versions v where v.policy_id = p.id);

alter table policies modify latest_version_no int not null;
