create table etl_file_tracker
(
    id              serial
        primary key,
    file_name       text                                             not null,
    sharepoint_path text                                             not null
        unique,
    sharepoint_id   text,
    etag            text,
    last_modified   timestamp with time zone,
    last_processed  timestamp with time zone,
    status          text                     default 'pending'::text not null
        constraint etl_file_tracker_status_check
            check (status = ANY (ARRAY ['pending'::text, 'processed'::text, 'failed'::text])),
    created_at      timestamp with time zone default now()           not null,
    updated_at      timestamp with time zone default now()           not null
);

alter table etl_file_tracker
    owner to postgres;

create index idx_etl_file_tracker_status
    on etl_file_tracker (status);

create index idx_etl_file_tracker_etag
    on etl_file_tracker (etag);

create trigger trg_etl_file_tracker_updated_at
    before update
    on etl_file_tracker
    for each row
execute procedure set_updated_at();

