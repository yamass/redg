create table "A" (
    id int primary key
);

CREATE TABLE "user" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    description varchar(255),
    a_id int references A (id)
);

