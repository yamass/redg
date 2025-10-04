create type my_enum as enum ('A', 'B', 'C');

create table t (
    c my_enum
)