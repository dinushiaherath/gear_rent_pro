-- Schema for GearRent Pro (MySQL)

CREATE DATABASE IF NOT EXISTS gearrent_pro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gearrent_pro;

CREATE TABLE branch (
  id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  address VARCHAR(300),
  contact VARCHAR(100)
);

CREATE TABLE category (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  base_price_factor DOUBLE NOT NULL DEFAULT 1.0,
  weekend_multiplier DOUBLE NOT NULL DEFAULT 1.0,
  default_late_fee_per_day DOUBLE DEFAULT 0,
  active TINYINT(1) DEFAULT 1
);

CREATE TABLE equipment (
  id INT AUTO_INCREMENT PRIMARY KEY,
  equipment_code VARCHAR(100) NOT NULL UNIQUE,
  category_id INT NOT NULL,
  brand VARCHAR(100),
  model VARCHAR(150),
  purchase_year INT,
  base_daily_price DOUBLE NOT NULL,
  security_deposit DOUBLE NOT NULL,
  status VARCHAR(50) NOT NULL,
  branch_id INT NOT NULL,
  FOREIGN KEY (category_id) REFERENCES category(id),
  FOREIGN KEY (branch_id) REFERENCES branch(id)
);

CREATE TABLE customer (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_code VARCHAR(100) NOT NULL UNIQUE,
  name VARCHAR(200) NOT NULL,
  nic_passport VARCHAR(100),
  contact_no VARCHAR(100),
  email VARCHAR(150),
  address VARCHAR(300),
  membership VARCHAR(50) DEFAULT 'REGULAR'
);

CREATE TABLE reservation (
  id INT AUTO_INCREMENT PRIMARY KEY,
  reservation_code VARCHAR(100) NOT NULL UNIQUE,
  equipment_id INT NOT NULL,
  customer_id INT NOT NULL,
  branch_id INT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (equipment_id) REFERENCES equipment(id),
  FOREIGN KEY (customer_id) REFERENCES customer(id),
  FOREIGN KEY (branch_id) REFERENCES branch(id)
);

CREATE TABLE rental (
  id INT AUTO_INCREMENT PRIMARY KEY,
  rental_code VARCHAR(100) NOT NULL UNIQUE,
  equipment_id INT NOT NULL,
  customer_id INT NOT NULL,
  branch_id INT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  calculated_rental_amount DOUBLE DEFAULT 0,
  security_deposit DOUBLE DEFAULT 0,
  membership_discount DOUBLE DEFAULT 0,
  long_rental_discount DOUBLE DEFAULT 0,
  final_payable_amount DOUBLE DEFAULT 0,
  payment_status VARCHAR(50) DEFAULT 'UNPAID',
  rental_status VARCHAR(50) DEFAULT 'ACTIVE',
  actual_return_date DATE,
  damage_description TEXT,
  damage_charge DOUBLE DEFAULT 0,
  late_fee DOUBLE DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (equipment_id) REFERENCES equipment(id),
  FOREIGN KEY (customer_id) REFERENCES customer(id),
  FOREIGN KEY (branch_id) REFERENCES branch(id)
);

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  branch_id INT,
  FOREIGN KEY (branch_id) REFERENCES branch(id)
);

CREATE TABLE IF NOT EXISTS membership_config (
  level VARCHAR(50) PRIMARY KEY,
  discount_percent DOUBLE DEFAULT 0
);

-- Indexes for quick searches
CREATE INDEX idx_equipment_branch ON equipment(branch_id);
CREATE INDEX idx_rental_status ON rental(rental_status);
CREATE INDEX idx_reservation_dates ON reservation(start_date, end_date);

-- ============================================
-- 1. INSERT BRANCHES (3 branches)
-- ============================================
INSERT IGNORE INTO branch (code, name, address, contact) VALUES
('PND', 'Panadura', '123 Main St, Panadura', '011-1234567'),
('GLE', 'Galle', '45 Beach Rd, Galle', '091-7654321'),
('COL', 'Colombo', '78 Colombo Park, Colombo', '011-9876543');

-- ============================================
-- 2. INSERT USERS (1 admin, 3 managers, 3 staff)
-- ============================================
INSERT INTO users (username, password_hash, role, branch_id)
VALUES
('admin', '5GSwtnbJNRDkxWGYcptqhYWpPP/3m1R6Bhu49HiiI0U3aZIM7FMA7lhswm5Qi1By', 'ADMIN', NULL),
('manager_pnd', 'cwadmA+gYxYr9PDbHVTsAo1nRTtHUzK2Lpygx7AY3WTi39DWKMIgmLlh1tzf6LCF', 'BRANCH_MANAGER', 1),
('manager_gle', 'cwadmA+gYxYr9PDbHVTsAo1nRTtHUzK2Lpygx7AY3WTi39DWKMIgmLlh1tzf6LCF', 'BRANCH_MANAGER', 2),
('manager_col', 'cwadmA+gYxYr9PDbHVTsAo1nRTtHUzK2Lpygx7AY3WTi39DWKMIgmLlh1tzf6LCF', 'BRANCH_MANAGER', 3),
('staff_pnd', 'UzRDQnNszZni4usR8cXoogUyiQ19xGQfSyX39smSpTLm/8amaWUtf9Qtwuq6EDIh', 'STAFF', 1),
('staff_gle', 'UzRDQnNszZni4usR8cXoogUyiQ19xGQfSyX39smSpTLm/8amaWUtf9Qtwuq6EDIh', 'STAFF', 2),
('staff_col', 'UzRDQnNszZni4usR8cXoogUyiQ19xGQfSyX39smSpTLm/8amaWUtf9Qtwuq6EDIh', 'STAFF', 3);

-- ============================================
-- 3. INSERT CATEGORIES (5 categories)
-- ============================================
INSERT IGNORE INTO category (name, description, base_price_factor, weekend_multiplier, default_late_fee_per_day) VALUES
('Camera', 'Digital stills cameras (DSLR, Mirrorless)', 1.0, 1.0, 500),
('Lens', 'Interchangeable lenses', 0.8, 1.0, 300),
('Drone', 'Aerial drones and accessories', 1.5, 1.2, 1500),
('Lighting', 'Lighting kits and accessories', 0.9, 1.0, 400),
('Audio', 'Microphones and audio recorders', 0.7, 1.0, 200);

-- ============================================
-- 4. INSERT EQUIPMENT (20+ items distributed across branches and categories)
-- ============================================
INSERT IGNORE INTO equipment (equipment_code, category_id, brand, model, purchase_year, base_daily_price, security_deposit, status, branch_id) VALUES
-- Cameras at Panadura (5 items)
('CAM-001', 1, 'Canon', 'EOS 5D Mark IV', 2022, 5000, 25000, 'AVAILABLE', 1),
('CAM-002', 1, 'Nikon', 'D850', 2021, 4500, 22500, 'AVAILABLE', 1),
('CAM-003', 1, 'Sony', 'A7R IV', 2022, 5500, 27500, 'AVAILABLE', 1),
('CAM-004', 1, 'Canon', 'EOS R5', 2023, 6000, 30000, 'AVAILABLE', 1),
('CAM-005', 1, 'GoPro', 'Hero 11', 2023, 1500, 7500, 'AVAILABLE', 1),

-- Lenses at Panadura (4 items)
('LEN-001', 2, 'Canon', '70-200mm f/2.8L', 2021, 2000, 10000, 'AVAILABLE', 1),
('LEN-002', 2, 'Nikon', '24-70mm f/2.8G', 2020, 1800, 9000, 'AVAILABLE', 1),
('LEN-003', 2, 'Sony', '35mm f/1.4 GM', 2022, 2200, 11000, 'AVAILABLE', 1),
('LEN-004', 2, 'Tamron', '100-400mm', 2021, 1500, 7500, 'UNDER_MAINTENANCE', 1),

-- Drones at Panadura (3 items)
('DRN-001', 3, 'DJI', 'Air 2S', 2022, 4000, 20000, 'AVAILABLE', 1),
('DRN-002', 3, 'DJI', 'Mini 3 Pro', 2023, 2500, 12500, 'AVAILABLE', 1),
('DRN-003', 3, 'Freefly', 'Alta X', 2021, 3500, 17500, 'AVAILABLE', 1),

-- Lighting at Galle (4 items)
('LGT-001', 4, 'Neewer', 'LED Studio Soft', 2022, 3000, 15000, 'AVAILABLE', 2),
('LGT-002', 4, 'Godox', 'SL-60W', 2021, 3500, 17500, 'AVAILABLE', 2),
('LGT-003', 4, 'Aputure', 'MC 4-Light', 2023, 5000, 25000, 'AVAILABLE', 2),
('LGT-004', 4, 'Neewer', 'RGB LED Panel', 2022, 2000, 10000, 'AVAILABLE', 2),

-- Audio at Galle (3 items)
('AUD-001', 5, 'Rode', 'Wireless GO II', 2023, 1500, 7500, 'AVAILABLE', 2),
('AUD-002', 5, 'Shure', 'SM7B', 2021, 2000, 10000, 'AVAILABLE', 2),
('AUD-003', 5, 'Audio-Technica', 'AT4050', 2022, 1800, 9000, 'AVAILABLE', 2),

-- Equipment at Colombo (4 items)
('CAM-006', 1, 'Panasonic', 'Lumix S1H', 2022, 4000, 20000, 'AVAILABLE', 3),
('DRN-004', 3, 'Auterion', 'Skynode', 2023, 3000, 15000, 'UNDER_MAINTENANCE', 3),
('LGT-005', 4, 'Arri', 'SkyPanel', 2023, 6000, 30000, 'AVAILABLE', 3),
('LEN-005', 2, 'Sigma', '14-24mm f/2.8', 2021, 1700, 8500, 'AVAILABLE', 3);

-- ============================================
-- 5. INSERT CUSTOMERS (10+ customers with different membership levels)
-- ============================================
INSERT IGNORE INTO customer (customer_code, name, nic_passport, contact_no, email, address, membership) VALUES
('CUST-001', 'John Silva', '123456789V', '077-1234567', 'john@email.com', '10 Main St, Colombo', 'GOLD'),
('CUST-002', 'Maria Garcia', 'P123456789', '076-2345678', 'maria@email.com', '25 Park Ave, Galle', 'SILVER'),
('CUST-003', 'Ahmed Hassan', '987654321V', '070-3456789', 'ahmed@email.com', '30 Beach Rd, Panadura', 'REGULAR'),
('CUST-004', 'Lisa Wong', 'P987654321', '077-4567890', 'lisa@email.com', '40 City Center, Colombo', 'GOLD'),
('CUST-005', 'David Kumar', '456789123V', '071-5678901', 'david@email.com', '50 South Ln, Galle', 'SILVER'),
('CUST-006', 'Emma Thompson', 'P456789123', '078-6789012', 'emma@email.com', '60 North Rd, Panadura', 'REGULAR'),
('CUST-007', 'Raj Patel', '321654987V', '072-7890123', 'raj@email.com', '70 East St, Colombo', 'GOLD'),
('CUST-008', 'Sophie Martin', 'P321654987', '076-8901234', 'sophie@email.com', '80 West Ave, Galle', 'SILVER'),
('CUST-009', 'Marco Rossi', '654987321V', '077-9012345', 'marco@email.com', '90 Old Town, Panadura', 'REGULAR'),
('CUST-010', 'Nina Andersson', 'P654987321', '070-0123456', 'nina@email.com', '100 Harbor St, Colombo', 'GOLD'),
('CUST-011', 'Carlos Lopez', '789123456V', '071-1123456', 'carlos@email.com', '110 Market Rd, Galle', 'SILVER'),
('CUST-012', 'Anna Kowalski', 'P789123456', '078-2123456', 'anna@email.com', '120 Industrial, Panadura', 'REGULAR');

-- ============================================
-- 6. INSERT MEMBERSHIP CONFIG (Discount percentages)
-- ============================================
INSERT IGNORE INTO membership_config (level, discount_percent) VALUES
('REGULAR', 0),
('SILVER', 5),
('GOLD', 10);

-- ============================================
-- 7. INSERT SAMPLE RESERVATIONS
-- ============================================
-- Current/Future reservations
INSERT IGNORE INTO reservation (reservation_code, equipment_id, customer_id, branch_id, start_date, end_date) VALUES
('RES-001', 1, 1, 1, CURDATE() + INTERVAL 5 DAY, CURDATE() + INTERVAL 10 DAY),
('RES-002', 5, 2, 1, CURDATE() + INTERVAL 3 DAY, CURDATE() + INTERVAL 7 DAY),
('RES-003', 11, 3, 2, CURDATE() + INTERVAL 2 DAY, CURDATE() + INTERVAL 6 DAY),
('RES-004', 15, 4, 2, CURDATE() + INTERVAL 7 DAY, CURDATE() + INTERVAL 12 DAY),
('RES-005', 22, 5, 3, CURDATE() + INTERVAL 4 DAY, CURDATE() + INTERVAL 9 DAY);

-- ============================================
-- 8. INSERT SAMPLE RENTALS (including overdue and with damage)
-- ============================================
-- Active rentals
INSERT IGNORE INTO rental (rental_code, equipment_id, customer_id, branch_id, start_date, end_date, calculated_rental_amount, security_deposit, membership_discount, long_rental_discount, final_payable_amount, payment_status, rental_status, created_at) VALUES
('RNT-001', 2, 2, 1, CURDATE() - INTERVAL 3 DAY, CURDATE() + INTERVAL 2 DAY, 18000, 22500, 900, 0, 39600, 'PAID', 'ACTIVE', NOW() - INTERVAL 3 DAY),
('RNT-002', 3, 5, 1, CURDATE() - INTERVAL 5 DAY, CURDATE() + INTERVAL 5 DAY, 55000, 27500, 5500, 0, 77000, 'PAID', 'ACTIVE', NOW() - INTERVAL 5 DAY);

-- Completed rentals (returned)
INSERT IGNORE INTO rental (rental_code, equipment_id, customer_id, branch_id, start_date, end_date, calculated_rental_amount, security_deposit, membership_discount, long_rental_discount, final_payable_amount, payment_status, rental_status, actual_return_date, late_fee, damage_charge, created_at) VALUES
('RNT-003', 4, 7, 1, CURDATE() - INTERVAL 15 DAY, CURDATE() - INTERVAL 10 DAY, 60000, 30000, 6000, 9000, 75000, 'PAID', 'RETURNED', CURDATE() - INTERVAL 9 DAY, 0, 0, NOW() - INTERVAL 15 DAY),
('RNT-004', 6, 10, 1, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 15 DAY, 16000, 10000, 1600, 2400, 22400, 'PAID', 'RETURNED', CURDATE() - INTERVAL 14 DAY, 0, 0, NOW() - INTERVAL 20 DAY),
('RNT-005', 8, 3, 1, CURDATE() - INTERVAL 12 DAY, CURDATE() - INTERVAL 8 DAY, 18000, 9000, 0, 0, 27000, 'PAID', 'RETURNED', CURDATE() - INTERVAL 8 DAY, 0, 0, NOW() - INTERVAL 12 DAY);

-- Overdue rentals
INSERT IGNORE INTO rental (rental_code, equipment_id, customer_id, branch_id, start_date, end_date, calculated_rental_amount, security_deposit, membership_discount, long_rental_discount, final_payable_amount, payment_status, rental_status, damage_description, created_at) VALUES
('RNT-006', 9, 6, 1, CURDATE() - INTERVAL 8 DAY, CURDATE() - INTERVAL 3 DAY, 32000, 20000, 0, 0, 52000, 'UNPAID', 'OVERDUE', NULL, NOW() - INTERVAL 8 DAY),
('RNT-007', 14, 11, 2, CURDATE() - INTERVAL 10 DAY, CURDATE() - INTERVAL 4 DAY, 15000, 15000, 750, 2250, 27000, 'UNPAID', 'OVERDUE', NULL, NOW() - INTERVAL 10 DAY);

-- Rental with damage charges
INSERT IGNORE INTO rental (rental_code, equipment_id, customer_id, branch_id, start_date, end_date, calculated_rental_amount, security_deposit, membership_discount, long_rental_discount, final_payable_amount, payment_status, rental_status, actual_return_date, late_fee, damage_charge, damage_description, created_at) VALUES
('RNT-008', 7, 1, 1, CURDATE() - INTERVAL 25 DAY, CURDATE() - INTERVAL 20 DAY, 9000, 7500, 900, 1350, 15250, 'PARTIALLY_PAID', 'RETURNED', CURDATE() - INTERVAL 19 DAY, 0, 5000, 'Minor scratches on lens mount', NOW() - INTERVAL 25 DAY),
('RNT-009', 12, 4, 2, CURDATE() - INTERVAL 30 DAY, CURDATE() - INTERVAL 25 DAY, 4000, 20000, 400, 600, 23600, 'PARTIALLY_PAID', 'RETURNED', CURDATE() - INTERVAL 24 DAY, 2500, 8000, 'Propeller damaged, gimbal malfunction', NOW() - INTERVAL 30 DAY);

-- Long-term rental (7+ days with discount)
INSERT IGNORE INTO rental (rental_code, equipment_id, customer_id, branch_id, start_date, end_date, calculated_rental_amount, security_deposit, membership_discount, long_rental_discount, final_payable_amount, payment_status, rental_status, actual_return_date, late_fee, created_at) VALUES
('RNT-010', 10, 8, 1, CURDATE() - INTERVAL 40 DAY, CURDATE() - INTERVAL 33 DAY, 28000, 12500, 1400, 4200, 34300, 'PAID', 'RETURNED', CURDATE() - INTERVAL 32 DAY, 0, NOW() - INTERVAL 40 DAY);
