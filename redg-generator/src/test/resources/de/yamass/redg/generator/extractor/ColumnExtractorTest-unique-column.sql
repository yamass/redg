create type my_enum as enum ('A', 'B', 'C');

create table t (
    e my_enum not null unique,
    i int not null unique,
    t text not null unique,
    b boolean not null unique,
    partial_b boolean
);

CREATE UNIQUE INDEX partial_b_uniq
    ON t (partial_b)
    WHERE partial_b is not null;