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
--  7. Adjuster Reports
--  8. Customer Invoices
--  9. Claim Decisions
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
--  7. ADJUSTER REPORTS
-- -----------------------------------------------------------------
INSERT INTO adjuster_reports (id, claim_id, adjuster_id, summary, findings, recommendations,
                              recommended_amount, status, report_pdf_path, original_pdf_filename,
                              created_at, updated_at)
VALUES
-- Auto Claim 1001 - Initial assessment by external adjuster
(2001, 1001, 20, 'Vehicle damage assessment completed. Rear bumper replacement required.',
 'Minor damage to rear bumper consistent with parking incident. Paint scratches and minor dent. No structural damage detected.',
 'Recommend approval for repair costs as estimated. Standard repair procedures sufficient.',
 750.00, 'SUBMITTED', 'adjuster-reports/claim-1001/report-2001/vehicle_assessment_20241121.pdf',
 'vehicle_assessment_20241121.pdf', '2024-11-21 15:30:00', '2024-11-21 15:30:00'),

-- Home Claim 1002 - Detailed water damage assessment
(2002, 1002, 22, 'Comprehensive water damage assessment. Extensive basement flooding confirmed.',
 'Water damage extends throughout basement area. Affected: concrete walls (moisture penetration), flooring (complete replacement needed), electrical installations (safety inspection required). Mold risk assessment: medium risk due to moisture retention in walls.',
 'Immediate water extraction and dehumidification required. Recommend partial approval pending additional contractor estimates for electrical work.',
 2800.00, 'APPROVED',
 'adjuster-reports/claim-1002/report-2002/water_damage_assessment_20250612.pdf',
 'water_damage_assessment_20250612.pdf', '2025-06-12 10:15:00', '2025-06-13 09:20:00'),

-- Health Claim 1003 - Medical procedure verification
(2003, 1003, 24, 'Dental procedure verification and cost analysis completed.',
 'Reviewed medical documentation for crown procedure. Treatment medically necessary due to tooth fracture. Provider credentials verified, procedure codes correct.',
 'Standard dental crown procedure with appropriate pricing. Recommend approval with standard policy deductible applied.',
 720.00, 'APPROVED', 'adjuster-reports/claim-1003/report-2003/dental_procedure_review_20250409.pdf',
 'dental_procedure_review_20250409.pdf', '2025-04-09 14:45:00', '2025-04-09 14:45:00'),

-- Auto Claim 1004 - Investigation for rejected claim
(2004, 1004, 21, 'Vehicle damage investigation. Suspicious circumstances identified.',
 'Scratch pattern inconsistent with reported incident. No corresponding damage to garage surfaces. Previous similar damage noted in photos. Lack of supporting evidence for external cause.',
 'Insufficient evidence to support external damage claim. Damage appears consistent with wear/tear or unreported incident. Recommend claim denial.',
 0.00, 'APPROVED', 'adjuster-reports/claim-1004/report-2004/damage_investigation_20241203.pdf',
 'damage_investigation_20241203.pdf', '2024-12-03 11:30:00', '2024-12-03 16:45:00');


-- -----------------------------------------------------------------
--  8. CUSTOMER INVOICES
-- -----------------------------------------------------------------
INSERT INTO customer_invoices (id, claim_id, vendor_name, invoice_amount, description, invoice_date,
                               invoice_pdf_path, original_pdf_filename, uploaded_at)
VALUES
-- Auto Claim 1001 - Repair shop invoice
(3001, 1001, 'AutoWerkstatt Schmidt & Söhne GmbH', 745.50,
 'Rear bumper replacement and paint work for vehicle damage repair',
 '2024-11-25', 'customer-invoices/claim-1001/invoice-3001/auto_repair_invoice_20241125.pdf',
 'auto_repair_invoice_20241125.pdf', '2024-11-26 09:15:00'),

-- Home Claim 1002 - Water damage restoration invoices (multiple vendors)
(3002, 1002, 'Wasserschaden Sanierung Berlin GmbH', 1850.00,
 'Water extraction, dehumidification, and initial damage assessment services',
 '2025-06-13', 'customer-invoices/claim-1002/invoice-3002/water_extraction_services_20250613.pdf',
 'water_extraction_services_20250613.pdf', '2025-06-14 08:30:00'),

(3003, 1002, 'Baumarkt ProFi', 420.75,
 'Building materials: waterproof membrane, concrete sealant, basement flooring materials',
 '2025-06-15', 'customer-invoices/claim-1002/invoice-3003/building_materials_20250615.pdf',
 'building_materials_20250615.pdf', '2025-06-16 14:20:00'),

(3004, 1002, 'Elektro Meyer Installations', 680.00,
 'Electrical safety inspection and wiring replacement in basement area affected by water damage',
 '2025-06-18', 'customer-invoices/claim-1002/invoice-3004/electrical_work_20250618.pdf',
 'electrical_work_20250618.pdf', '2025-06-19 10:45:00'),

-- Health Claim 1003 - Dental treatment invoice
(3005, 1003, 'Dr. Weiss Zahnklinik', 800.00,
 'Dental crown procedure including examination, preparation, temporary crown, and final crown placement',
 '2025-04-08', 'customer-invoices/claim-1003/invoice-3005/dental_crown_treatment_20250408.pdf',
 'dental_crown_treatment_20250408.pdf', '2025-04-10 16:30:00');


-- -----------------------------------------------------------------
--  9. CLAIM DECISIONS
-- -----------------------------------------------------------------
INSERT INTO claim_decisions (id, claim_id, decision_maker_id, decision_type, approved_amount,
                             rejection_reason, reasoning, additional_notes, decision_date,
                             updated_at)
VALUES
-- Auto Claim 1001 - Approved after assessment
(4001, 1001, 10, 'APPROVED', 745.50, NULL,
 'Claim approved based on adjuster report and submitted repair invoice. Damage consistent with reported parking incident.',
 'Standard bumper replacement claim. No additional investigation required.',
 '2024-11-27 14:20:00', '2024-11-27 14:20:00'),

-- Home Claim 1002 - Partially approved pending additional documentation
(4002, 1002, 10, 'PARTIALLY_APPROVED', 2500.00, NULL,
 'Partial approval granted for water extraction and basic repairs. Electrical work requires additional contractor certification before approval.',
 'Customer advised to submit certified electrical contractor estimates for remaining amount consideration.',
 '2025-06-20 11:30:00', '2025-06-20 11:30:00'),

-- Health Claim 1003 - Approved with standard deductible
(4003, 1003, 13, 'APPROVED', 720.00, NULL,
 'Medical procedure approved after review. Standard policy deductible of €80 applied to original claim amount.',
 'Routine dental procedure with appropriate documentation. Payment processed.',
 '2025-04-12 09:45:00', '2025-04-12 09:45:00'),

-- Auto Claim 1004 - Rejected due to insufficient evidence
(4004, 1004, 10, 'REJECTED', NULL, 'Insufficient evidence of external cause',
 'Investigation revealed inconsistencies in damage pattern and circumstances. No evidence of external incident causing reported damage.',
 'Customer notified of appeal process. Additional evidence may be submitted within 30 days for reconsideration.',
 '2024-12-05 16:00:00', '2024-12-05 16:00:00');


-- -----------------------------------------------------------------
--  10. UPDATE ID SEQUENCES
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
ALTER TABLE adjuster_reports
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM adjuster_reports);
ALTER TABLE customer_invoices
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM customer_invoices);
ALTER TABLE claim_decisions
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM claim_decisions);

-- -----------------------------------------------------------------
--  11. PARTNER VENDORS
-- -----------------------------------------------------------------
INSERT INTO partner_vendors (id, vendor_code, company_name, contact_person, email, phone_number,
                             address, city, postal_code, country, vendor_type, status,
                             specialization, notes, created_at, updated_at)
VALUES
-- Auto Repair Partners (existing vendors from customer invoices)
(5001, 'VND-AUTO-001', 'AutoWerkstatt Schmidt & Söhne GmbH', 'Hans Schmidt',
 'kontakt@auto-schmidt.de', '+49 30 555 2001', 'Berliner Str. 45', 'Berlin', '10115', 'Deutschland',
 'AUTO_REPAIR', 'ACTIVE', 'Collision repair, paint work, bodywork',
 'Preferred partner for auto claims in Berlin area', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Medical Partners
(5002, 'VND-MED-001', 'Dr. Weiss Zahnklinik', 'Dr. Thomas Weiss',
 'praxis@weiss-zahnarzt.de', '+49 89 555 3001', 'Leopoldstr. 12', 'München', '80331', 'Deutschland',
 'MEDICAL_PROVIDER', 'ACTIVE', 'General dentistry, oral surgery, implants',
 'Partner dental services provider', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Construction & Restoration Partners (existing vendors from customer invoices)
(5003, 'VND-REST-001', 'Wasserschaden Sanierung Berlin GmbH', 'Peter Bauer',
 'notfall@wasser-sanierung-berlin.de', '+49 30 555 4001', 'Potsdamer Platz 1', 'Berlin', '10115',
 'Deutschland',
 'RESTORATION_SERVICE', 'ACTIVE', 'Water damage restoration, mold remediation, drying services',
 '24/7 emergency water damage response team', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(5004, 'VND-SUPP-001', 'Baumarkt ProFi', 'Angela Meyer',
 'verkauf@baumarkt-profi.de', '+49 30 555 4002', 'Industrieweg 22', 'Berlin', '10115',
 'Deutschland',
 'PARTS_SUPPLIER', 'ACTIVE', 'Building materials, tools, construction supplies',
 'Bulk supplier for restoration projects', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(5005, 'VND-ELEC-001', 'Elektro Meyer Installations', 'Frank Meyer',
 'service@elektro-meyer.de', '+49 30 555 5001', 'Elektriker Str. 8', 'Berlin', '10115',
 'Deutschland',
 'ELECTRICAL_CONTRACTOR', 'ACTIVE',
 'Electrical installations, safety inspections, emergency repairs',
 'Licensed electrical contractor for insurance claims', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Additional Partners
(5006, 'VND-TOW-001', 'Abschleppdienst Express', 'Klaus Wagner',
 '24h@abschlepp-express.de', '+49 30 555 6001', 'Autobahnstr. 99', 'Berlin', '10115', 'Deutschland',
 'TOWING_SERVICE', 'ACTIVE', '24/7 towing, roadside assistance, vehicle transport',
 'Emergency towing service with citywide coverage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE partner_vendors
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM partner_vendors);