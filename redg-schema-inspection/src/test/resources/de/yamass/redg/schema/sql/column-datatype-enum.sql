create type custom_enum_type as enum ('VALUE_A', 'VALUE_B', 'VALUE_C');

create table column_datatype_enum_table (
    column_enum custom_enum_type not null
);

