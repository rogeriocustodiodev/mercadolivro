CREATE TABLE customer_roles(
    customer_id int not null,
    role varchar(255) not null,
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);