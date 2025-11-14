create type udt_status_enum as enum ('VALUE_A', 'VALUE_B');

create table udt_reference_table (
    udt_reference_id serial primary key,
    udt_status_column udt_status_enum not null
);

