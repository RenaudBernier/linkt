import sqlite3
from datetime import datetime
from pathlib import Path
from typing import Dict

BCRYPT_PASSWORD = "$2a$10$Ag0P81IIDefnGTk8cED4ee4z3G4tELoM2SX7R2/S7MEFu/m1BT0hy"


def resolve_db_path(db_path: str) -> Path:
    """Return an absolute path for the database file."""
    path = Path(db_path)
    if not path.is_absolute():
        path = Path(__file__).resolve().parent / path
    return path


def create_tables(cursor: sqlite3.Cursor) -> None:
    """Create the tables required by the Spring Boot JPA models."""
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS user (
            user_id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT NOT NULL UNIQUE,
            first_name TEXT NOT NULL,
            last_name TEXT NOT NULL,
            phone_number TEXT,
            password TEXT NOT NULL,
            user_type TEXT NOT NULL CHECK (user_type IN ('student', 'organizer', 'administrator')),
            is_approved INTEGER DEFAULT 0,
            organization_name TEXT
        );
        """
    )

    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS event (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            description TEXT,
            event_type TEXT,
            start_date_time TEXT NOT NULL,
            end_date_time TEXT NOT NULL,
            location TEXT,
            coordinates TEXT,
            capacity INTEGER,
            image_url TEXT,
            price REAL DEFAULT 0.0,
            organizer_id INTEGER NOT NULL,
            FOREIGN KEY (organizer_id) REFERENCES user (user_id) ON DELETE CASCADE
        );
        """
    )

    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS ticket (
            ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,
            qr_code TEXT NOT NULL UNIQUE,
            user_id INTEGER NOT NULL,
            event_id INTEGER NOT NULL,
            is_scanned INTEGER DEFAULT 0,
            scanned_at TEXT,
            scanned_by INTEGER,
            FOREIGN KEY (user_id) REFERENCES user (user_id) ON DELETE CASCADE,
            FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE,
            FOREIGN KEY (scanned_by) REFERENCES user (user_id) ON DELETE SET NULL
        );
        """
    )

    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS saved_event (
            saved_event_id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            event_id INTEGER NOT NULL,
            FOREIGN KEY (user_id) REFERENCES user (user_id) ON DELETE CASCADE,
            FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE,
            UNIQUE (user_id, event_id)
        );
        """
    )

    cursor.execute("CREATE INDEX IF NOT EXISTS idx_event_organizer ON event (organizer_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_ticket_user ON ticket (user_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_ticket_event ON ticket (event_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_ticket_scanned ON ticket (is_scanned);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_saved_event_user ON saved_event (user_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_saved_event_event ON saved_event (event_id);")


def insert_seed_data(cursor: sqlite3.Cursor) -> None:
    """Populate the database with sample users, events, tickets and saved events."""
    users = [
        {
            "email": "john.organizer@linkt.dev",
            "first_name": "John",
            "last_name": "Doe",
            "phone_number": "+1-555-0101",
            "password": BCRYPT_PASSWORD,
            "user_type": "organizer",
            "is_approved": 1,
            "organization_name": "Tech Community Hub",
        },
        {
            "email": "jane.organizer@linkt.dev",
            "first_name": "Jane",
            "last_name": "Smith",
            "phone_number": "+1-555-0102",
            "password": BCRYPT_PASSWORD,
            "user_type": "organizer",
            "is_approved": 1,
            "organization_name": "Arts & Culture Society",
        },
        {
            "email": "sarah.organizer@linkt.dev",
            "first_name": "Sarah",
            "last_name": "Williams",
            "phone_number": "+1-555-0104",
            "password": BCRYPT_PASSWORD,
            "user_type": "organizer",
            "is_approved": 0,
            "organization_name": "Campus Wellness Collective",
        },
        {
            "email": "emily.student@linkt.dev",
            "first_name": "Emily",
            "last_name": "Davis",
            "phone_number": "+1-555-0106",
            "password": BCRYPT_PASSWORD,
            "user_type": "student",
        },
        {
            "email": "chris.student@linkt.dev",
            "first_name": "Chris",
            "last_name": "Wilson",
            "phone_number": "+1-555-0107",
            "password": BCRYPT_PASSWORD,
            "user_type": "student",
        },
        {
            "email": "anna.student@linkt.dev",
            "first_name": "Anna",
            "last_name": "Martinez",
            "phone_number": "+1-555-0108",
            "password": BCRYPT_PASSWORD,
            "user_type": "student",
        },
        {
            "email": "admin@linkt.dev",
            "first_name": "Alex",
            "last_name": "Admin",
            "phone_number": "+1-555-0110",
            "password": BCRYPT_PASSWORD,
            "user_type": "administrator",
        },
    ]

    user_ids: Dict[str, int] = {}
    for user in users:
        cursor.execute(
            """
            INSERT INTO user (email, first_name, last_name, phone_number, password, user_type, is_approved, organization_name)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                user["email"],
                user["first_name"],
                user["last_name"],
                user.get("phone_number"),
                user["password"],
                user["user_type"],
                user.get("is_approved"),
                user.get("organization_name"),
            ),
        )
        user_ids[user["email"]] = cursor.lastrowid

    events = [
        {
            "title": "Frosh Night",
            "description": "Welcome party for new students with games and music.",
            "event_type": "Social",
            "start": datetime(2025, 10, 15, 19, 0),
            "end": datetime(2025, 10, 15, 23, 0),
            "location": "Student Union Building",
            "coordinates": "POINT(37.7749 -122.4194)",
            "capacity": 200,
            "image_url": "https://images.unsplash.com/photo-1511795409834-ef04bbd61622",
            "price": 0.00,
            "organizer_email": "john.organizer@linkt.dev",
        },
        {
            "title": "DJ Night",
            "description": "Campus EDM night featuring local DJs.",
            "event_type": "Music",
            "start": datetime(2025, 10, 22, 20, 0),
            "end": datetime(2025, 10, 23, 2, 0),
            "location": "Campus Arena",
            "coordinates": "POINT(40.7128 -74.0060)",
            "capacity": 500,
            "image_url": "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3",
            "price": 25.00,
            "organizer_email": "jane.organizer@linkt.dev",
        },
        {
            "title": "Yoga & Wellness Workshop",
            "description": "Guided yoga and mindfulness session to unwind.",
            "event_type": "Wellness",
            "start": datetime(2025, 10, 28, 17, 0),
            "end": datetime(2025, 10, 28, 19, 0),
            "location": "Recreation Center",
            "coordinates": "POINT(40.7829 -73.9654)",
            "capacity": 50,
            "image_url": "https://images.unsplash.com/photo-1506126613408-eca07ce68773",
            "price": 10.00,
            "organizer_email": "sarah.organizer@linkt.dev",
        },
    ]

    event_ids: Dict[str, int] = {}
    for event in events:
        organizer_id = user_ids[event["organizer_email"]]
        cursor.execute(
            """
            INSERT INTO event (
                title, description, event_type, start_date_time, end_date_time,
                location, coordinates, capacity, image_url, price, organizer_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                event["title"],
                event["description"],
                event["event_type"],
                event["start"].isoformat(),
                event["end"].isoformat(),
                event["location"],
                event.get("coordinates"),
                event.get("capacity"),
                event.get("image_url"),
                event.get("price", 0.0),
                organizer_id,
            ),
        )
        event_ids[event["title"]] = cursor.lastrowid

    tickets = [
        {"qr": "QR-FROSH-001", "student_email": "emily.student@linkt.dev", "event_title": "Frosh Night"},
        {"qr": "QR-FROSH-002", "student_email": "chris.student@linkt.dev", "event_title": "Frosh Night"},
        {"qr": "QR-DJ-001", "student_email": "anna.student@linkt.dev", "event_title": "DJ Night"},
    ]

    for ticket in tickets:
        cursor.execute(
            """
            INSERT INTO ticket (qr_code, user_id, event_id)
            VALUES (?, ?, ?)
            """,
            (
                ticket["qr"],
                user_ids[ticket["student_email"]],
                event_ids[ticket["event_title"]],
            ),
        )

    saved_events = [
        {"student_email": "emily.student@linkt.dev", "event_title": "DJ Night"},
        {"student_email": "chris.student@linkt.dev", "event_title": "Yoga & Wellness Workshop"},
        {"student_email": "anna.student@linkt.dev", "event_title": "Frosh Night"},
    ]

    for saved in saved_events:
        cursor.execute(
            """
            INSERT OR IGNORE INTO saved_event (user_id, event_id)
            VALUES (?, ?)
            """,
            (
                user_ids[saved["student_email"]],
                event_ids[saved["event_title"]],
            ),
        )


def create_database(db_path: str = "linkt.db") -> None:
    """Create the SQLite database and seed it with initial data."""
    db_file = resolve_db_path(db_path)
    if db_file.exists():
        db_file.unlink()

    db_file.parent.mkdir(parents=True, exist_ok=True)

    conn = sqlite3.connect(db_file)
    try:
        cursor = conn.cursor()
        cursor.execute("PRAGMA foreign_keys = ON;")
        create_tables(cursor)
        insert_seed_data(cursor)
        conn.commit()
    finally:
        conn.close()

    print(f"Database created successfully at: {db_file}")
    print("Seed data inserted successfully!")


if __name__ == "__main__":
    create_database()
