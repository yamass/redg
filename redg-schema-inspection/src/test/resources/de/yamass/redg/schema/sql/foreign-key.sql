create table fk_target_table (
    target_id serial primary key
);

create table fk_source_table (
    source_id serial primary key,
    target_id_fk integer not null references fk_target_table(target_id)
);

