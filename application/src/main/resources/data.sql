-- =================================================================================
--  COMPREHENSIVE SEED DATA FOR DUMMY INSURANCE API
-- =================================================================================
--  Order of insertion:
--  1. Customers
--  2. Employees
--  3. Agencies
--  4. Policy Conditions & Rules
--  5. Policies
--  6. Claims
-- =================================================================================


-- -----------------------------------------------------------------
--  1. CUSTOMERS
-- -----------------------------------------------------------------
INSERT INTO customers (id, first_name, last_name, date_of_birth, street_and_house_number,
                       postal_code, city, country, password, email, created_at, updated_at)
VALUES (1, 'Max', 'Mustermann', '1985-03-15', 'Musterstraße 1A',
        '10115', 'Berlin', 'Deutschland',
        'testPassword123',
        'max.mustermann@example.com', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       (2, 'Erika', 'Mustermann', '1992-07-22', 'Beispielweg 25',
        '80331', 'München', 'Deutschland',
        'testPassword456',
        'erika.mustermann@example.com', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       (3, 'Klaus', 'Müller', '1978-11-02', 'Hauptplatz 10', '20095',
        'Hamburg', 'Deutschland', 'testPassword789',
        'k.mueller@example.de',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- -----------------------------------------------------------------
--  2. EMPLOYEES
-- -----------------------------------------------------------------

-- Internal Employees
INSERT INTO employees (id, employee_id, first_name, last_name, email, phone_number, role,
                       employment_type, company_name, specialization_area, availability_status,
                       created_at, updated_at)
VALUES
-- Internal Management and Staff
(10, 'EMP-1001', 'Sabine', 'Meier', 'sabine.meier@insurance.com', '+49 30 555 1001',
 'MANAGER', 'INTERNAL', 'InsuranceCorp GmbH', NULL, 'AVAILABLE', CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP),

(11, 'EMP-1002', 'Jürgen', 'Weber', 'jurgen.weber@insurance.com', '+49 30 555 1002',
 'CLAIMS_ADJUSTER', 'INTERNAL', 'InsuranceCorp GmbH', 'AUTO', 'AVAILABLE', CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP),

(12, 'EMP-1003', 'Anke', 'Huber', 'anke.huber@insurance.com', '+49 30 555 1003',
 'CLAIMS_ADJUSTER', 'INTERNAL', 'InsuranceCorp GmbH', 'HOME', 'AVAILABLE', CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP),

(13, 'EMP-1004', 'Stefan', 'Bauer', 'stefan.bauer@insurance.com', '+49 30 555 1004',
 'UNDERWRITER', 'INTERNAL', 'InsuranceCorp GmbH', NULL, 'AVAILABLE', CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP),

-- External Auto Adjusters
(20, 'EXT-ADJ-AUTO-001', 'Michael', 'Schmidt', 'michael.schmidt@kfz-gutachter-berlin.de',
 '+49 30 777 1001', 'CLAIMS_ADJUSTER', 'EXTERNAL', 'KFZ Gutachter Schmidt GmbH', 'AUTO',
 'AVAILABLE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(21, 'EXT-ADJ-AUTO-002', 'Sandra', 'Wagner', 'sandra.wagner@fahrzeug-experten-muenchen.de',
 '+49 89 777 1002', 'CLAIMS_ADJUSTER', 'EXTERNAL', 'Fahrzeug Experten München GmbH', 'AUTO',
 'AVAILABLE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- External Home Adjusters
(22, 'EXT-ADJ-HOME-001', 'Thomas', 'Bauer', 'thomas.bauer@immobilien-sachverstaendige-hamburg.de',
 '+49 40 777 2001', 'CLAIMS_ADJUSTER', 'EXTERNAL', 'Immobilien Sachverständige Hamburg GmbH',
 'HOME', 'AVAILABLE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(23, 'EXT-ADJ-HOME-002', 'Petra', 'Klein', 'petra.klein@gebaude-experten-berlin.de',
 '+49 30 777 2002', 'CLAIMS_ADJUSTER', 'EXTERNAL', 'Gebäude Experten Berlin GmbH', 'HOME', 'BUSY',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- External Health Adjusters
(24, 'EXT-ADJ-HEALTH-001', 'Dr. Andrea', 'Müller', 'andrea.mueller@med-gutachter-frankfurt.de',
 '+49 69 777 3001', 'CLAIMS_ADJUSTER', 'EXTERNAL', 'Medizinische Gutachter Frankfurt GmbH',
 'HEALTH', 'AVAILABLE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(25, 'EXT-ADJ-HEALTH-002', 'Dr. Frank', 'Weber', 'frank.weber@gesundheits-experten-koeln.de',
 '+49 221 777 3002', 'CLAIMS_ADJUSTER', 'EXTERNAL', 'Gesundheits Experten Köln GmbH', 'HEALTH',
 'AVAILABLE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- External LIABILITY Adjuster
(26, 'EXT-ADJ-LIABILITY-001', 'Jennifer', 'Hoffmann',
 'jennifer.hoffmann@haftpflicht-experten-muenchen.de',
 '+49 89 777 4001', 'CLAIMS_ADJUSTER', 'EXTERNAL', 'Haftpflicht Experten München GmbH', 'LIABILITY',
 'AVAILABLE',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- -----------------------------------------------------------------
--  3. AGENCIES
-- -----------------------------------------------------------------
INSERT INTO agencies (id, agency_code, name, street_and_house_number, postal_code, city, country,
                      contact_person, contact_email, contact_phone, created_at, updated_at)
VALUES (1, 'AG-BER-001', 'Sicher & Sicher GmbH', 'Kurfürstendamm 200', '10719', 'Berlin',
        'Deutschland', 'Herr Schmidt', 'kontakt@sicher-sicher.de', '+49 30 123 4567',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'AG-MUN-002', 'Klaro Versicherungen', 'Marienplatz 8', '80331', 'München', 'Deutschland',
        'Frau Huber', 'info@klaro-versicherung.de', '+49 89 765 4321', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);


-- -----------------------------------------------------------------
--  4. POLICY CONDITIONS & RULES
-- -----------------------------------------------------------------
INSERT INTO policy_conditions (id, free_cancellation_days, no_claim_bonus_percentage)
VALUES (1, 14, 0.05);

INSERT INTO cancellation_penalty_rules (id, months_remaining_threshold, penalty_percentage,
                                        policy_conditions_id)
VALUES (1, 6, 0.20, 1), -- 20% penalty if 6 or fewer months remain
       (2, 1, 0.05, 1);
-- 5% penalty if 1 or fewer months remain


-- -----------------------------------------------------------------
--  5. POLICIES
-- -----------------------------------------------------------------
INSERT INTO policies (id, policy_number, start_date, end_date, type, status, premium, customer_id,
                      agency_id, created_at, updated_at)
VALUES
-- Policies for Max Mustermann (Customer ID 1)
(101, 'POL-AUTO-2025-001', '2025-01-01', '2025-12-31', 'AUTO', 'PENDING', 850.50, 1, 1,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Sold by Agency 1, Pending
(102, 'POL-HOME-2025-002', '2025-02-15', '2026-02-14', 'HOME', 'ACTIVE', 450.00, 1, NULL,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Sold directly, Active

-- Policies for Erika Mustermann (Customer ID 2)
(103, 'POL-HEALTH-2025-003', '2025-03-01', '2025-12-31', 'HEALTH', 'ACTIVE', 1200.75, 2, 2,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Sold by Agency 2, Active
(104, 'POL-AUTO-2024-004', '2024-01-01', '2024-12-31', 'AUTO', 'EXPIRED', 780.00, 2, 1,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- Sold by Agency 1, Expired


-- -----------------------------------------------------------------
--  6. CLAIMS
-- -----------------------------------------------------------------
-- A submitted Auto Claim for Policy 104 (Erika's expired car policy)
INSERT INTO claims (id, claim_number, description, date_of_incident, status, estimated_amount,
                    paid_amount, policy_id, assigned_adjuster_id, date_reported, claim_type,
                    license_plate, vehicle_vin, accident_location, type_of_damage, damaged_items,
                    medical_provider, procedure_code)
VALUES (1001, 'CLM-2024-A001', 'Parkschaden am hinteren Stoßfänger.', '2024-11-20', 'SUBMITTED',
        750.00, NULL, 104, NULL, '2024-11-21 10:00:00', 'AUTO', 'M-XY-4321', 'VIN123XYZ...',
        'Supermarkt Parkplatz', NULL, NULL, NULL, NULL);

-- An "In Review" Home Claim for Policy 102 (Max's active home policy), assigned to an adjuster
INSERT INTO claims (id, claim_number, description, date_of_incident, status, estimated_amount,
                    paid_amount, policy_id, assigned_adjuster_id, date_reported, claim_type,
                    license_plate, vehicle_vin, accident_location, type_of_damage, damaged_items,
                    medical_provider, procedure_code)
VALUES (1002, 'CLM-2025-H001', 'Wasserschaden im Keller durch Rohrbruch.', '2025-06-10',
        'IN_REVIEW', 2500.00, NULL, 102, 11, '2025-06-11 14:30:00', 'HOME', NULL, NULL, NULL,
        'WATER', 'Kellerwände, Boden, einige Möbel', NULL, NULL);

-- A "Paid" Health Claim for Policy 103 (Erika's active health policy), assigned and resolved
INSERT INTO claims (id, claim_number, description, date_of_incident, status, estimated_amount,
                    paid_amount, policy_id, assigned_adjuster_id, date_reported, claim_type,
                    license_plate, vehicle_vin, accident_location, type_of_damage, damaged_items,
                    medical_provider, procedure_code)
VALUES (1003, 'CLM-2025-M001', 'Zahnärztliche Behandlung: Zahnkrone.', '2025-04-05', 'PAID', 800.00,
        720.00, 103, 12, '2025-04-08 09:00:00', 'HEALTH', NULL, NULL, NULL, NULL, NULL,
        'Dr. Weiss Zahnklinik', 'Z27.40');

-- A "Rejected" Auto Claim for Policy 104
INSERT INTO claims (id, claim_number, description, date_of_incident, status, estimated_amount,
                    paid_amount, policy_id, assigned_adjuster_id, date_reported, claim_type,
                    license_plate, vehicle_vin, accident_location, type_of_damage, damaged_items,
                    medical_provider, procedure_code)
VALUES (1004, 'CLM-2024-A002', 'Kratzer an der Tür, Ursache unklar.', '2024-12-01', 'REJECTED',
        400.00, NULL, 104, 11, '2024-12-02 11:20:00', 'AUTO', 'M-XY-4321', 'VIN123XYZ...',
        'Zu Hause in der Garage', NULL, NULL, NULL, NULL);


-- -----------------------------------------------------------------
--  7. UPDATE ID SEQUENCES
-- -----------------------------------------------------------------
-- After manually inserting data, reset the auto-increment sequences.
ALTER TABLE customers
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM customers);
ALTER TABLE employees
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM employees);
ALTER TABLE agencies
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM agencies);
ALTER TABLE cancellation_penalty_rules
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM cancellation_penalty_rules);
ALTER TABLE policies
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM policies);
ALTER TABLE claims
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM claims);