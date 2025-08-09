-- =================================================================
--  PRE-LOADED DATA FOR THE DUMMY INSURANCE API
-- =================================================================

-- -----------------------------------------------------------------
--  CUSTOMERS
-- -----------------------------------------------------------------
INSERT INTO customers (id, first_name, last_name, date_of_birth, street_and_house_number,
                       postal_code, city, country, password, email, created_at, updated_at)
VALUES (1, 'Max', 'Mustermann', '1985-03-15', 'Musterstraße 1', '10115', 'Berlin', 'Deutschland',
        '$2a$10$tZNo.pOFbWkCCG4WqD/nAO/BmxDi2/859a0dFrQKo.i2n2.i8SC/y',
        'max.mustermann@example.com',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       (2, 'Erika', 'Musterfrau', '1992-07-22', 'Musterstraße 22B', '80331', 'München',
        'Deutschland',
        '$2a$10$r6g1qY09j8L.P8e.qFzMweLqO6m/hO3iN.d/l/9bU.e/mJ6h.qM6G',
        'erika.musterfrau@example.com',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);


-- -----------------------------------------------------------------
--  POLICIES
-- -----------------------------------------------------------------
INSERT INTO policies (id, policy_number, start_date, end_date, type, status, premium, customer_id,
                      created_at, updated_at)
VALUES
-- Policies for John Doe (Customer ID 1)
(101, 'POL-AUTO-2025-001', '2025-01-01', '2025-12-31', 'AUTO', 'PENDING', 850.50, 1,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(102, 'POL-HOME-2025-002', '2025-02-15', '2026-02-14', 'HOME', 'ACTIVE', 450.00, 1,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Policies for Jane Smith (Customer ID 2)
(103, 'POL-HEALTH-2025-003', '2025-03-01', '2025-12-31', 'HEALTH', 'ACTIVE', 1200.75, 2,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(104, 'POL-AUTO-2024-004', '2024-01-01', '2024-12-31', 'AUTO', 'EXPIRED', 780.00, 2,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- -----------------------------------------------------------------
--  UPDATE ID SEQUENCES
-- -----------------------------------------------------------------
ALTER TABLE customers
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM customers);
ALTER TABLE policies
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM policies);