create table fk_composite_target_table (
    target_part_a integer not null,
    target_part_b integer not null,
    primary key (target_part_a, target_part_b)
);

create table fk_composite_source_table (
    source_id serial primary key,
    target_part_a_fk integer not null,
    target_part_b_fk integer not null,
    foreign key (target_part_a_fk, target_part_b_fk) references fk_composite_target_table(target_part_a, target_part_b)
);

