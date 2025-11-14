create type my_udt as (
    udt_int_column int,
    udt_text_column text
);

create table udt_structured_reference_table (
    udt_structured_reference_id serial primary key,
    udt_structured_column my_udt not null
);

