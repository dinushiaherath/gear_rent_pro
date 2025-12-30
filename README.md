# GearRent Pro - Equipment Rental Management System

A comprehensive JavaFX-based equipment rental management system designed for multi-branch operations. Manage rentals, reservations, customers, inventory, and financial settlements with role-based access control.

## Table of Contents

- [Features](#features)
- [System Requirements](#system-requirements)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Default Login Credentials](#default-login-credentials)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Key Features & Workflows](#key-features--workflows)

---

## Features

### Core Functionality
- **Multi-Branch Management**: Support for multiple rental branches with independent inventory and staff
- **Equipment Inventory**: Track equipment across categories (cameras, lenses, drones, lighting, audio)
- **Rental & Reservation System**: Create, manage, and track equipment rentals and reservations
- **Dynamic Pricing**: 
  - Per-day pricing with category-based factors
  - Weekend multiplier pricing
  - Membership-based discounts (Regular/Silver/Gold)
  - Long-rental discounts (7+ days)
- **Customer Management**: Maintain customer profiles with membership levels
- **Settlement & Return Processing**: Process equipment returns with damage assessment and late fees
- **Membership Configuration**: Admin-managed discount percentages per membership level
- **Overdue Tracking**: Automatic identification of overdue rentals
- **Damage & Late Fee Management**: Record and calculate damage charges and late fees

### Role-Based Access Control
- **Admin**: Full system access, user management, membership configuration
- **Branch Manager**: Branch-level operations, staff management, reporting
- **Staff**: Daily operations (rentals, reservations, returns, customer service)

---

## System Requirements

### Software
- **Java 17+** (OpenJDK or Oracle JDK)
- **Maven 3.8+** (for building and running)
- **MySQL 8.0+** (or compatible database)

### Operating System
- Windows, Linux, or macOS with Java and Maven installed

### Hardware
- Minimum 2GB RAM
- 500MB disk space (including database)

---

## Database Setup

### Step 1: Create the Database

Open your MySQL client and run the schema creation script:

```bash
mysql -u root -p < database/dbScript.sql
```

Or execute in MySQL Workbench/CLI:
```sql
source database/dbScript.sql;
```

This will:
- Create the `gearrent_pro` database
- Create all necessary tables (branch, category, equipment, customer, rental, reservation, users, membership_config)
- Create indexes for performance optimization

### Step 2: Populate Sample Data (Optional)

To populate the database with test data (branches, equipment, customers, sample rentals, and reservations):

```bash
mysql -u root -p gearrent_pro < database/sample_data.sql
```

**Sample Data Includes:**
- 3 branches (Panadura, Galle, Colombo)
- 5 equipment categories
- 24+ equipment items distributed across branches
- 12 customers with mixed membership levels (Gold/Silver/Regular)
- 10 sample rentals (active, completed, overdue, with damages)
- 5 sample reservations

### Step 3: Configure Database Connection

Update the database connection properties in:
```
src/main/resources/database.properties
```

Example configuration:
```properties
db.url=jdbc:mysql://localhost:3306/gearrent_pro
db.user=root
db.password=your_password
```

---

## Running the Application

### Option 1: Using Maven (Recommended)

**Clean Build & Run:**
```bash
cd gear_rent_pro
mvn clean javafx:run
```

**Just Run (without clean):**
```bash
mvn javafx:run
```

### Option 2: Build an Executable JAR

```bash
mvn clean package
java -jar target/gear_rent_pro-1.0-SNAPSHOT.jar
```

### First-Time Setup

When the application starts for the first time:
1. It automatically initializes default users (if they don't exist)
2. All test data from sample_data.sql is ready to use
3. The login screen will appear

---

## Default Login Credentials

Use these credentials to log in to the application:

### Admin Access
- **Username:** `admin`
- **Password:** `admin123`
- **Role:** Administrator (full system access)
- **Permissions:** User management, membership configuration, system administration

### Branch Manager Access

**Panadura Branch:**
- **Username:** `manager_pnd`
- **Password:** `manager123`
- **Role:** Branch Manager
- **Branch:** Panadura

**Galle Branch:**
- **Username:** `manager_gle`
- **Password:** `manager123`
- **Role:** Branch Manager
- **Branch:** Galle

**Colombo Branch:**
- **Username:** `manager_col`
- **Password:** `manager123`
- **Role:** Branch Manager
- **Branch:** Colombo

### Staff Access

**Panadura Branch:**
- **Username:** `staff_pnd`
- **Password:** `staff123`
- **Role:** Staff
- **Branch:** Panadura

**Galle Branch:**
- **Username:** `staff_gle`
- **Password:** `staff123`
- **Role:** Staff
- **Branch:** Galle

**Colombo Branch:**
- **Username:** `staff_col`
- **Password:** `staff123`
- **Role:** Staff
- **Branch:** Colombo

---

## Project Structure

```
gear_rent_pro/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── lk/ijse/gear_rent_pro/
│   │   │       ├── AppInitializer.java          (JavaFX entry point)
│   │   │       ├── controller/                  (UI controllers)
│   │   │       ├── dao/                         (Data access layer)
│   │   │       ├── model/                       (Data models)
│   │   │       ├── service/                     (Business logic)
│   │   │       └── util/                        (Utilities, DatabaseInitializer)
│   │   └── resources/
│   │       ├── lk/ijse/
│   │       │   ├── ui/                          (FXML UI files)
│   │       │   │   ├── login/
│   │       │   │   ├── dashboard/
│   │       │   │   ├── rentals/
│   │       │   │   ├── reservations/
│   │       │   │   ├── customers/
│   │       │   │   ├── equipments/
│   │       │   │   ├── categories/
│   │       │   │   ├── branches/
│   │       │   │   ├── users/
│   │       │   │   ├── side-menu/
│   │       │   │   ├── membership-config/
│   │       │   │   └── overdue/
│   │       │   ├── styles/
│   │       │   │   └── global.css               (Global styles)
│   │       │   └── images/                      (UI images)
│   │       └── database.properties              (DB connection config)
│   └── test/
│       └── java/                                (Unit tests)
├── database/
│   ├── dbScript.sql                               (Database schema)
│   ├── sample_data.sql                          (Sample test data)
│   └── update_passwords.sql                     (Password management)
├── pom.xml                                      (Maven configuration)
└── README.md                                    (This file)
```

---

## Technology Stack

### Backend
- **Language:** Java 17+
- **Build Tool:** Maven 3.8+
- **Database:** MySQL 8.0+
- **JDBC:** MySQL Connector/J

### Frontend
- **UI Framework:** JavaFX 21
- **Layout:** FXML (XML-based UI markup)
- **Styling:** CSS (global.css)

### Architecture
- **Pattern:** MVC (Model-View-Controller)
- **Layers:** 
  - Controller layer (UI logic)
  - Service layer (business logic)
  - DAO layer (data persistence)
  - Model layer (data objects)

---

## Key Features & Workflows

### 1. Rental Management
- **Create Rental:** Select equipment, customer, branch, and dates
- **Dynamic Pricing:** Automatic calculation based on equipment, category, dates, and membership
- **Process Return:** Record return, calculate late fees, assess damage charges
- **View Breakdown:** Detailed pricing breakdown showing all factors

### 2. Reservation System
- **Create Reservation:** Reserve equipment for future dates
- **Availability Check:** Equipment availability filtered by branch and date
- **Overlap Prevention:** System prevents double-booking

### 3. Equipment Management
- **Track Status:** AVAILABLE, RESERVED, RENTED. UNDER_MAINTENANCE
- **Category-Based Pricing:** Base price factors and weekend multipliers per category
- **Multi-Branch Distribution:** Equipment tracked per branch with availability

### 4. Customer Management
- **Membership Levels:** REGULAR, SILVER, GOLD
- **Member Discount:** Automatically applied based on membership level
- **Admin Configuration:** Adjust membership discount percentages in real-time

### 5. Settlement & Returns
- **Damage Assessment:** Record damage and calculate charges
- **Late Fee Calculation:** Automatic calculation based on overdue days
- **Payment Processing:** Track payment status (PAID, UNPAID, PARTIALLY_PAID)

### 6. Overdue Management
- **Overdue Tracking:** Automatic marking of overdue rentals
- **Notification Dashboard:** View all overdue rentals at a glance
- **Late Fee Application:** Configurable late fees per category

---

## Troubleshooting

### Database Connection Issues
- **Error:** `Connection refused`
  - **Solution:** Ensure MySQL is running and `database.properties` has correct credentials

### Build Failures
- **Error:** `Module not found`
  - **Solution:** Run `mvn clean install` to download all dependencies

---

## Development Notes

### Adding New Features
1. Create model class in `model/` package
2. Create DAO interface and implementation in `dao/` package
3. Create service interface and implementation in `service/` package
4. Create controller in `controller/` package
5. Create FXML layout in `src/main/resources/lk/ijse/ui/` folder
6. Wire controller and FXML together

### Database Modifications
- Update `database/dbScript.sql` for schema changes
- Create migration script in `database/` for data migrations
- Update model classes to reflect schema changes

### Styling
- Edit `src/main/resources/lk/ijse/styles/global.css` for global style changes
- Use CSS classes in FXML files (e.g., `styleClass="btn-common"`)

---

## License

This project is developed for educational purposes.

---

## Support & Contact

For issues, questions, or feature requests, please contact the development team or refer to the project documentation.

---

## Changelog

### Version 1.0 (Current)
- Multi-branch equipment rental system
- Role-based access control (Admin, Manager, Staff)
- Dynamic pricing with multiple discount types
- Equipment reservation and rental management
- Return settlement with damage and late fee calculation
- Membership configuration and discount management
- Overdue rental tracking
- Sample data population scripts

