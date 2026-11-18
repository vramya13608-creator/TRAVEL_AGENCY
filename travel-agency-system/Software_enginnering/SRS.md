# Software Requirements Specification (SRS)
**Project:** Travel Agency Booking and Tourism Trend Analysis System
**Scenario:** 21 | **Class:** 25IT3A&B | **Dept:** Information Technology, Velalar College of Engineering and Technology
**Version:** 1.0

## 1. Introduction
### 1.1 Purpose
This document specifies the requirements of a system that lets a travel agency manage customers, travel packages, bookings and payments, and analyse tourism trends.
### 1.2 Scope
The system has three cooperating parts: a **Java** console application (business operations), a **PostgreSQL relational database** and a **Python** analytics module (Pandas, Matplotlib, Seaborn). It replaces manual record keeping, prevents overbooking and reveals popular destinations and seasonal demand.
### 1.3 Definitions
| Term | Meaning |
|---|---|
| Package | A priced tour to one destination with a duration and a per-departure capacity |
| Departure | One package on one travel date; capacity is enforced per departure |
| Net revenue | Amount paid minus refunds |

## 2. Overall Description
- **Users / actors:** *Admin* (agency staff: manages packages, customers, bookings, reports) and *Customer* (books, pays, cancels).
- **Environment:** Java 17+, PostgreSQL (managed with pgAdmin) via JDBC, Python 3.9+ with psycopg2, pandas, matplotlib, seaborn, Flask; a web browser for the dashboard.
- **Assumptions:** one currency (INR); capacity applies per package per travel date; refund policy as in FR-9; payments are recorded by staff (no live payment gateway).
- **Constraints:** console UI; single-agency, single-user at a time.

## 3. Functional Requirements
| ID | Requirement |
|---|---|
| FR-1 | Maintain customer details: ID, name, 10-digit phone, unique valid email. |
| FR-2 | Maintain travel packages: ID, name, destination, duration, price per person, maximum capacity. |
| FR-3 | Allow a customer to book a package for a travel date, specifying the number of travelers. |
| FR-4 | Check availability and reject bookings that exceed the departure's maximum capacity. |
| FR-5 | Record booking date, travel date (must be after booking date), payment status and cancellation details. |
| FR-6 | Calculate booking amount = price per person x travelers. |
| FR-7 | Record payments (UPI, CARD, CASH, NET_BANKING) and keep payment status: PENDING, PAID, REFUNDED, CANCELLED. |
| FR-8 | Cancel a booking, free its seats and record date and reason. |
| FR-9 | Refund policy: 15+ days before travel = 90%, 7-14 days = 50%, under 7 days = 0%; unpaid bookings simply cancel their payment. |
| FR-10 | Generate reports: bookings per package, destination popularity, booking revenue. |
| FR-11 | Retrieve booking data from the database with Python, clean it and produce visualizations (bar, pie, line, heatmap). |

## 4. Non-Functional Requirements
| ID | Category | Requirement |
|---|---|---|
| NFR-1 | Correctness | A departure must never hold more confirmed travelers than its capacity. |
| NFR-2 | Data integrity | Primary/foreign keys, CHECK and UNIQUE constraints enforced by the database. |
| NFR-3 | Usability | Clear menu, validation messages for every invalid input. |
| NFR-4 | Performance | Typical operations and reports complete in under 2 seconds for up to 10,000 bookings. |
| NFR-5 | Portability | Runs on Windows/Linux/macOS with standard Java and Python. |
| NFR-6 | Maintainability | Layered design (entities, business logic, persistence); automated tests. |
| NFR-7 | Reliability | Business-rule violations raise a controlled error and never corrupt stored data. |

## 5. External Interfaces
- **User interface:** text menu on the console and a web dashboard.
- **Database interface:** JDBC (Java) and psycopg2 (Python) on the PostgreSQL database `travel_agency`; JSON API for the web dashboard.
- **Output:** console reports, PNG charts, CSV datasets, `observations.md`.

## 6. Use Case Summary
UC-1 Register customer, UC-2 Manage packages, UC-3 Check availability, UC-4 Book package, UC-5 Make payment, UC-6 Cancel booking, UC-7 View booking details, UC-8 Generate reports, UC-9 Analyse trends. Diagram: see `design.md`.
