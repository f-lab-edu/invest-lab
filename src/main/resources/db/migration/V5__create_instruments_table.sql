create table instruments (
    id bigint not null auto_increment primary key,
    market varchar(20) not null,
    symbol varchar(20) not null,
    name varchar(100) not null,
    created_at datetime(6) not null,
    constraint uk_instruments_market_symbol unique (market, symbol)
);
