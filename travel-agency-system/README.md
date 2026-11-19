# Travel Agency Booking and Tourism Trend Analysis System

## Description

| **Subject** | **Part** | **Location** |
|---|---|---|
| Software Engineering | SRS, use case, class and system architecture diagrams, test plan and results | `Software_enginnering/` |
| Java Programming | Console application for customers, packages, bookings, capacity checking, payments, cancellation and reports | `java/` |
| DBMS | PostgreSQL schema, sample data, report queries, ER diagram and normalization | `DBMS/` |
| Foundations of Data Science | Python data retrieval, cleaning, five visualizations and observations | `FDS/` |
| Web Dashboard | Website for booking and viewing travel information, connected to PostgreSQL through Flask | `web/`, `FDS/server.py` |

https://travel-agency-jdf8.onrender.com/#dashboard
 
**Data flow:** Java console application and web dashboard → PostgreSQL (`travel_agency`) → Python analysis → charts and reports.

## Technologies

- Java 17+ with JDBC
- PostgreSQL and pgAdmin
- Python with psycopg2, pandas, matplotlib and Flask
- HTML, CSS and JavaScript
- Git and GitHub

## Folder Structure

```text
travel-agency-system/
│
├── DBMS/
│   ├── sqlite/                  # Offline-testing copy
│   ├── ER_Diagram.png
│   ├── relational_model.png
│   ├── schema.sql
│   ├── sample_data.sql
│   ├── queries.sql
│   └── normalization.md
│
├── FDS/
│   ├── output/
│   │   ├── 1_popular_destinations_bar.png
│   │   ├── 2_booking_distribution_pie.png
│   │   ├── 3_monthly_trend_line.png
│   │   ├── 4_revenue_by_package_bar.png
│   │   ├── 5_seasonal_heatmap.png
│   │   ├── bookings_raw.csv
│   │   ├── bookings_clean.csv
│   │   └── observations.md
│   ├── analysis.py
│   ├── db.py
│   ├── server.py
│   └── setup_database.py
│
├── Software_enginnering/
│   ├── SRS.md
│   ├── use_case_diagram.png
│   ├── class_diagram.png
│   ├── system_architecture.png
│   ├── testing.md
│   └── test_results.txt
│
├── java/
│   ├── src/travelagency/
│   │   ├── Booking.java
│   │   ├── BookingException.java
│   │   ├── Customer.java
│   │   ├── Database.java
│   │   ├── Main.java
│   │   ├── Payment.java
│   │   ├── TravelAgency.java
│   │   └── TravelPackage.java
│   └── test/
│       ├── sampleInput.docx
│       └── SampleOutput.docx
│
└── web/
    ├── index.html
    ├── README.md
    └── requirements.txt
```

## 1. Database Setup

1. Open pgAdmin.
2. Right-click **Databases → Create → Database**.
3. Enter `travel_agency` as the database name.
4. Open the Query Tool for the new database.
5. Run `DBMS/schema.sql`.
6. Run `DBMS/sample_data.sql`.
7. Run queries from `DBMS/queries.sql` to view the reports.

## 2. Java Application

The Java module provides a menu-driven console application.

It supports:
- Customer registration
- Travel package management
- Package availability checking
- Booking creation
- Payment processing
- Booking cancellation
- Booking details and reports

To run the application, compile the Java source files and include the PostgreSQL JDBC driver when connecting to PostgreSQL.

Default database URL:

```text
jdbc:postgresql://localhost:5432/travel_agency
```

Database connection settings are read from the environment variables `PG_URL`, `PG_USER` and `PG_PASSWORD`.

Without a working PostgreSQL connection, the application can run in memory.

Sample input and output documents are available in `java/test/`.

## 3. Data Analysis (FDS)

Run the analysis module from the project root:

```bash
python FDS/analysis.py
```

The module retrieves booking data from PostgreSQL, preprocesses the data and generates:

- Five visualizations
- `bookings_raw.csv`
- `bookings_clean.csv`
- `observations.md`

The generated files are saved in `FDS/output/`.

## 4. Website

Install the required Python packages using the requirements file:

```bash
pip install -r web/requirements.txt
```

Run the Flask server:

```bash
python FDS/server.py
```

Open the website at:

```text
http://localhost:5000
```

The website uses PostgreSQL through the Flask backend. Opening `web/index.html` directly may provide static browser-only functionality; database-backed operations require the Flask server.

## Business Rules

- Package capacity is checked for each package and travel date.
- Booking amounts are calculated using price per person multiplied by the number of travelers.
- The Java console module includes booking cancellation and refund calculations.
- Payment and booking statuses are maintained by the application.
state to the database. It is intended for a single-user academic demonstration rather than a high-concurrency production system.
