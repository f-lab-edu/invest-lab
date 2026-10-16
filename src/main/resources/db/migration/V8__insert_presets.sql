insert into policies (member_id, name, created_at, updated_at)
values (null, '주식 60 · 채권 40', utc_timestamp(6), utc_timestamp(6));
set @policy_id = last_insert_id();

insert into policy_versions (policy_id, version_no, review_interval_months, threshold_bps,
                             allow_sell, cash_target_bps, created_at)
values (@policy_id, 1, 1, 500, true, 0, utc_timestamp(6));
set @version_id = last_insert_id();

insert into policy_allocations (policy_version_id, asset_class, instrument_id, target_bps)
values (@version_id, '미국주식', (select id from instruments where market = 'KRX' and symbol = '360750'), 4000),
       (@version_id, '국내주식', (select id from instruments where market = 'KRX' and symbol = '069500'), 2000),
       (@version_id, '국내채권', (select id from instruments where market = 'KRX' and symbol = '114260'), 4000);

insert into policies (member_id, name, created_at, updated_at)
values (null, '올웨더형', utc_timestamp(6), utc_timestamp(6));
set @policy_id = last_insert_id();

insert into policy_versions (policy_id, version_no, review_interval_months, threshold_bps,
                             allow_sell, cash_target_bps, created_at)
values (@policy_id, 1, 3, 500, true, 0, utc_timestamp(6));
set @version_id = last_insert_id();

insert into policy_allocations (policy_version_id, asset_class, instrument_id, target_bps)
values (@version_id, '미국주식', (select id from instruments where market = 'KRX' and symbol = '360750'), 3000),
       (@version_id, '미국채권', (select id from instruments where market = 'KRX' and symbol = '305080'), 4000),
       (@version_id, '국내채권', (select id from instruments where market = 'KRX' and symbol = '114260'), 1500),
       (@version_id, '금', (select id from instruments where market = 'KRX' and symbol = '132030'), 1500);
