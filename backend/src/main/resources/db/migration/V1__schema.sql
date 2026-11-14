create table application (
    id         bigserial primary key,
    name       varchar(255) not null unique,
    owner_team varchar(255) not null
);

create table server (
    id          bigserial primary key,
    hostname    varchar(255) not null unique,
    environment varchar(20)  not null check (environment in ('PROD', 'STAGING'))
);

create table incident (
    id             bigserial primary key,
    title          varchar(500) not null,
    severity       varchar(20)  not null check (severity in ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status         varchar(20)  not null check (status in ('OPEN', 'INVESTIGATING', 'RESOLVED')),
    application_id bigint       not null references application (id),
    server_id      bigint       references server (id),
    started_at     timestamptz  not null default now(),
    resolved_at    timestamptz,
    root_cause     text
);

create table incident_note (
    id          bigserial primary key,
    incident_id bigint      not null references incident (id),
    body        text        not null,
    created_at  timestamptz not null default now()
);

create index on incident (status);
create index on incident (application_id);
create index on incident_note (incident_id);
