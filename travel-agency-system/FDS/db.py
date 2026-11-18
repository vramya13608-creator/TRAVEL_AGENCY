"""Database connection helper: PostgreSQL (default) with an optional SQLite fallback for offline testing.

PostgreSQL settings come from environment variables (or DATABASE_URL):
  PGHOST=localhost PGPORT=5432 PGDATABASE=travel_agency PGUSER=postgres PGPASSWORD=...
Set USE_SQLITE=1 only for offline testing without PostgreSQL.
"""
import os
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
IS_SQLITE = bool(os.getenv("USE_SQLITE"))
SQLITE_FILE = ROOT / "data" / "travel_agency.db"
SCHEMA = ROOT / "sql" / ("sqlite/schema.sql" if IS_SQLITE else "schema.sql")


def connect():
    if IS_SQLITE:
        SQLITE_FILE.parent.mkdir(exist_ok=True)
        con = sqlite3.connect(SQLITE_FILE)
        con.execute("PRAGMA foreign_keys = ON")
        return con
    import psycopg2
    url = os.getenv("DATABASE_URL")
    if url:
        return psycopg2.connect(url)
    return psycopg2.connect(
        host=os.getenv("PGHOST", "localhost"), port=os.getenv("PGPORT", "5432"),
        dbname=os.getenv("PGDATABASE", "travel_agency"), user=os.getenv("PGUSER", "postgres"),
        password=os.getenv("PGPASSWORD", ""))


def q(con, sql, params=()):
    """Run one statement written with ? placeholders; returns a cursor."""
    cur = con.cursor()
    cur.execute(sql if IS_SQLITE else sql.replace("?", "%s"), params)
    return cur


def run_script(con, text):
    if IS_SQLITE:
        con.executescript(text)
    else:
        cur = con.cursor()
        cur.execute(text)


def reset_tables(con):
    """Drop all project tables so setup can start clean."""
    if IS_SQLITE:
        return
    cur = con.cursor()
    cur.execute("DROP TABLE IF EXISTS payment, booking, travel_package, destination, customer CASCADE")
    con.commit()
