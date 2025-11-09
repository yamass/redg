create table column_uniqueness_table (
    pk_column INT AUTO_INCREMENT PRIMARY KEY,
    single_column_unique integer unique,
    multi_column_unique_part_a integer,
    multi_column_unique_part_b integer,
    non_unique_column integer
);

create unique index column_uniqueness_table_multi_column_unique_idx
    on column_uniqueness_table (multi_column_unique_part_a, multi_column_unique_part_b);

