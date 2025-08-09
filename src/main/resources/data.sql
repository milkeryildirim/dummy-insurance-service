INSERT INTO customers (first_name, last_name, date_of_birth, street_and_house_number, postal_code,
                       city, country, password, email, created_at, updated_at)
VALUES ('Max',
        'Mustermann',
        '1990-05-15',
        'Musterstrasse 1',
        '12345',
        'Musterstadt',
        'Germany',
        'hashed_password_123',
        'john.doe@example.com',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);