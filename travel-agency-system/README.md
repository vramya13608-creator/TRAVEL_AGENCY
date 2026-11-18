# Travel Agency Booking and Tourism Trend Analysis System
Project-Based Assessment | Scenario 21 | Velalar College of Engineering and Technology, Dept. of IT | Class 25IT3A&B | AY 2026-27 (odd)
Register Nos.: 21, 51, 81, 111

## Description
| Subject | Part | Location |
|---|---|---|
| Software Engineering | SRS, use case / class / architecture / ER diagrams, test plan and results | `docs/` |
| Java Programming | Console booking application (customers, packages, booking, capacity check, payment, cancellation, reports) | `java/` |
| DBMS | PostgreSQL schema (3NF), sample data, 8 report queries, normalization | `sql/`, `docs/normalization.md` |
| Foundations of Data Science | Python retrieval from PostgreSQL, cleaning, 5 visualizations, observations | `python/` |
| Web dashboard | Website with every feature, backed by the same PostgreSQL database | `web/index.html`, `python/server.py` |

Data flow: Java app / website -> **PostgreSQL** (`travel_agency`) -> Python analysis -> charts in `python/output/`.

## Technologies
Java 17+ (JDBC), **PostgreSQL + pgAdmin**, Python 3.9+ (psycopg2, pandas, matplotlib, seaborn, Flask), HTML/CSS/JavaScript with Chart.js, Mermaid, Git/GitHub.

## Folder structure
```
docs/     SRS.md, design.md (diagrams), normalization.md, testing.md, test_results.txt
java/     src/travelagency/*.java, test/TestRunner.java
sql/      schema.sql, sample_data.sql, queries.sql (PostgreSQL);  sqlite/ = offline-testing copy only
python/   db.py, setup_database.py, analysis.py, server.py, output/
web/      index.html (dashboard)
lib/      put the PostgreSQL JDBC jar here
```

## 1. Database setup (pgAdmin)
1. In pgAdmin: right-click **Databases > Create > Database**, name it `travel_agency`.
2. Right-click the new database > **Query Tool**. Open `sql/schema.sql`, run it (F5). Then run `sql/sample_data.sql`.
3. Run any query from `sql/queries.sql` to see reports.
4. (Optional) load ~500 more bookings for the analysis. This drops and recreates the tables, then fills them:
```bash
pip install -r requirements.txt
# Windows PowerShell:  $env:PGPASSWORD="your_password"      Linux/macOS: export PGPASSWORD=your_password
# Optional: PGHOST, PGPORT (default 5432), PGDATABASE (default travel_agency), PGUSER (default postgres)
python python/setup_database.py
```

## 2. Java application
```bash
./build_and_test.sh                                   # compile + 31 unit tests (no database needed)
java -cp bin travelagency.Main demo                   # scripted walkthrough, in memory
```
To save to PostgreSQL, download the PostgreSQL JDBC driver (`postgresql-42.x.x.jar` from jdbc.postgresql.org) into `lib/`, then run from the repo root:
```bash
# set PG_PASSWORD (and optionally PG_URL, PG_USER)
export PG_PASSWORD=your_password
java -cp "bin:lib/*" travelagency.Main                # Windows: java -cp "bin;lib/*" travelagency.Main
```
Defaults: `jdbc:postgresql://localhost:5432/travel_agency`, user `postgres`. Without the driver it runs in memory only.

## 3. Data analysis (FDS)
```bash
python python/analysis.py
```
Writes five charts, `bookings_raw.csv`, `bookings_clean.csv` and `observations.md` to `python/output/`.

## 4. Website
```bash
python python/server.py        # open http://localhost:5000
```
The site loads and saves everything in PostgreSQL through `/api/state`. It also works as a plain static file (double-click `web/index.html`); then data stays in the browser only.

**Deploy:** a static host (GitHub Pages, Netlify) can serve `web/index.html` alone, browser-only data. To use PostgreSQL online, host `python/server.py` on Render/Railway/similar with a managed PostgreSQL, set `DATABASE_URL`, run `sql/schema.sql` on that database, and start with `gunicorn --chdir python server:app`.

## Business rules
- Capacity is enforced per package per travel date; cancelled bookings free their seats.
- Amount = price per person x travelers.
- Refund: 15+ days before travel 90%, 7-14 days 50%, under 7 days 0%.
- Payment status: PENDING, PAID, REFUNDED, CANCELLED.

## Notes
- The bulk dataset is synthetic (seeded) and deliberately contains duplicates and missing payment methods to demonstrate preprocessing.
- The website has no login, and its server writes the whole state per change: fine for a single-user project, not for many simultaneous users.
