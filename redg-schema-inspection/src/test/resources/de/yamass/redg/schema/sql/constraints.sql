create table constraint_metadata_table (
    constraint_primary_key serial primary key,
    constraint_unique_column text not null,
    constraint_positive_value integer not null
);

alter table constraint_metadata_table
    add constraint constraint_unique_column_unique unique (constraint_unique_column);

alter table constraint_metadata_table
    add constraint constraint_positive_value_check check (constraint_positive_value > 0);

