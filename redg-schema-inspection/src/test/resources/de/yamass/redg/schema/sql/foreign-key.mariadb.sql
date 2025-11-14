create table fk_target_table (
    target_id INT AUTO_INCREMENT PRIMARY KEY
);

create table fk_source_table (
    source_id INT AUTO_INCREMENT PRIMARY KEY,
    target_id_fk integer not null,
    foreign key (target_id_fk) references fk_target_table(target_id)
);

