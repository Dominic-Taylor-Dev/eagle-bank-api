CREATE TABLE users (
   id VARCHAR(40) PRIMARY KEY,
   password_hash VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,
   email VARCHAR(255) NOT NULL UNIQUE,
   phone_number VARCHAR(255) NOT NULL,
   address_line_1 VARCHAR(255) NOT NULL,
   address_line_2 VARCHAR(255),
   address_line_3 VARCHAR(255),
   town VARCHAR(255) NOT NULL,
   county VARCHAR(255) NOT NULL,
   postcode VARCHAR(255) NOT NULL,
   created_timestamp TIMESTAMP NOT NULL,
   updated_timestamp TIMESTAMP NOT NULL
);
