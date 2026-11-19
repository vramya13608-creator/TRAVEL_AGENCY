# SOFTWARE REQUIREMENTS SPECIFICATION (SRS)

**PROJECT TITLE: TRAVEL AGENCY BOOKING AND TOURISM TREND ANALYSIS SYSTEM**

## 1. Introduction

### 1.1 Purpose
To develop a system that manages customers, travel packages, bookings, payments, and tourism-related data efficiently.

### 1.2 Scope
WanderWise simplifies travel agency operations through a web application. It reduces manual work, manages bookings, and supports tourism trend analysis.

## 2. Overall Description

### 2.1 Technologies Used

- Frontend: HTML, CSS, JavaScript
- Backend: Python Flask
- Database: PostgreSQL
- Database Connectivity: psycopg2
- Business Logic Module: Core Java
- Data Analysis: Python, Pandas, Matplotlib
- Deployment: Render

### 2.2 Users
- **Admin:** Manages customers, packages, bookings, and payments.
- **Customer:** Registers, views packages, and makes bookings.

## 3. Functional Requirements

1. **Customer Management:** Register and view customer details.
2. **Destination Management:** Maintain tourist destination information.
3. **Package Management:** Add and manage travel packages.
4. **Booking Management:** Create bookings for registered customers.
5. **Availability Management:** Check package capacity and prevent overbooking.
6. **Payment Management:** Maintain payment details and status.
7. **Cancellation:** Cancel bookings and update booking status.
8. **Trend Analysis:** Analyse booking data and generate visualizations using Python.

## 4. Non-Functional Requirements

- **Performance:** Process requests efficiently.
- **Reliability:** Maintain accurate database records.
- **Usability:** Provide a simple user-friendly interface.
- **Security:** Protect database credentials and validate inputs.
- **Maintainability:** Organize the application into separate modules.
- **Data Integrity:** Maintain database relationships using keys and constraints.

## 5. Database Requirements

The system uses PostgreSQL with the following tables:

1. Customer
2. Destination
3. Travel_Package
4. Booking
5. Payment

Primary keys and foreign keys maintain relationships between tables.

## 6. System Architecture

The frontend communicates with the Flask backend through API requests. Flask connects to PostgreSQL using psycopg2.

**Architecture Flow:**

User → HTML/CSS/JavaScript → Flask Backend → PostgreSQL Database

Python is used for tourism data analysis, while Core Java is maintained as a separate business logic module.

## 7. Future Enhancements

- Customer login and authentication.
- Online payment gateway.
- Email and SMS notifications.
- Advanced tourism prediction.
- Mobile application support.
