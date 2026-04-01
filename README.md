# Delivery & Logistics Management System

A JavaFX desktop application built as part of a university group project at Brunel University London Pathway College.

## Overview
This component manages the complete delivery workflow for a Marketplace e-commerce system, including:
- Secure login for Administrators and Drivers
- Full delivery management dashboard (create, assign, filter, delete)
- Real-time activity log
- Simulated map view with canvas drawing
- Admin settings page (password management, driver deletion)

## Technologies Used
- Java 17
- JavaFX 17
- FXML + CSS
- SQLite (via sqlite-jdbc)
- Scene Builder
- Eclipse IDE

## Features
- Role-based access — Admin and Driver interfaces
- Live search, filter, and sort on delivery table
- Driver credential verification before map access
- Start Delivery button enforcement (one active delivery per driver)
- Full CRUD operations on deliveries table

## How to Run
1. Clone the repository
2. Open in Eclipse
3. Ensure JavaFX and sqlite-jdbc are on the build path
4. Run `StartPageController.java`

## Database
Uses SQLite (`DRIVERS.db`) with three tables:
- `deliveries` — Delivery_ID, Order_Ref, address, driver, status, eta
- `drivers` — username, password, name
- `admins` — username, password

## Project Structure
```
src/
└── main/java/GroupProjectB/Delivery/and/Logistics/
    ├── StartPageController.java
    ├── AdminLoginController.java
    ├── DriverLoginController.java
    ├── AdminDashboardController.java
    ├── DriverDashboardController.java
    └── AdminSettingsController.java
```
