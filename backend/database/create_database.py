import sqlite3
from pathlib import Path
from datetime import datetime, timedelta


def insert_dummy_data(cursor):
    """Insert dummy data into all tables."""

    # Insert Organizations
    organizations = [
        ("Tech Community Hub", "https://example.com/logos/tech.png"),
        ("Arts & Culture Society", "https://example.com/logos/arts.png"),
        ("Sports Events Inc", "https://example.com/logos/sports.png"),
        ("Music Festival Organizers", "https://example.com/logos/music.png"),
        ("Business Networking Group", "https://example.com/logos/business.png"),
    ]
    cursor.executemany("INSERT INTO Organization (name, logo_url) VALUES (?, ?)", organizations)

    # Insert Users (organizers, students, and administrators)
    users = [
        ("John", "Doe", "+1-555-0101", "organizer"),
        ("Jane", "Smith", "+1-555-0102", "organizer"),
        ("Mike", "Johnson", "+1-555-0103", "organizer"),
        ("Sarah", "Williams", "+1-555-0104", "organizer"),
        ("Tom", "Brown", "+1-555-0105", "organizer"),
        ("Emily", "Davis", "+1-555-0106", "student"),
        ("Chris", "Wilson", "+1-555-0107", "student"),
        ("Anna", "Martinez", "+1-555-0108", "student"),
        ("David", "Garcia", "+1-555-0109", "student"),
        ("Lisa", "Rodriguez", "+1-555-0110", "administrator"),
    ]
    cursor.executemany("INSERT INTO User (first_name, last_name, phone_number, user_type) VALUES (?, ?, ?, ?)", users)

    # Insert Organizers (linking first 5 users to organizations)
    organizers = [
        (1, True, 1),   # John Doe - Tech Community Hub - approved
        (2, True, 2),   # Jane Smith - Arts & Culture Society - approved
        (3, True, 3),   # Mike Johnson - Sports Events Inc - approved
        (4, False, 4),  # Sarah Williams - Music Festival Organizers - pending
        (5, True, 5),   # Tom Brown - Business Networking Group - approved
    ]
    cursor.executemany("INSERT INTO Organizer (id, is_approved, organization_id) VALUES (?, ?, ?)", organizers)

    # Insert Events
    now = datetime.now()
    events = [
        ("Tech Summit 2025", "Annual technology conference", "Conference",
         (now + timedelta(days=30)).isoformat(), (now + timedelta(days=31)).isoformat(),
         "San Francisco Convention Center", "POINT(37.7749 -122.4194)", 500, 1),

        ("Art Gallery Opening", "Contemporary art exhibition", "Exhibition",
         (now + timedelta(days=15)).isoformat(), (now + timedelta(days=45)).isoformat(),
         "Downtown Art Museum", "POINT(40.7128 -74.0060)", 200, 2),

        ("City Marathon", "Annual charity marathon", "Sports",
         (now + timedelta(days=60)).isoformat(), (now + timedelta(days=60, hours=6)).isoformat(),
         "Central Park", "POINT(40.7829 -73.9654)", 1000, 3),

        ("Summer Music Festival", "Three-day outdoor music event", "Festival",
         (now + timedelta(days=90)).isoformat(), (now + timedelta(days=92)).isoformat(),
         "Riverside Park", "POINT(34.0522 -118.2437)", 5000, 4),

        ("Business Networking Mixer", "Monthly networking event", "Networking",
         (now + timedelta(days=7)).isoformat(), (now + timedelta(days=7, hours=3)).isoformat(),
         "Grand Hotel Ballroom", "POINT(41.8781 -87.6298)", 150, 5),

        ("Coding Bootcamp", "Weekend programming workshop", "Workshop",
         (now + timedelta(days=20)).isoformat(), (now + timedelta(days=21)).isoformat(),
         "Tech Hub Coworking Space", "POINT(37.7749 -122.4194)", 50, 1),
    ]
    cursor.executemany("""
        INSERT INTO Event (title, description, event_type, start_date_time, end_date_time,
                          location, coordinates, capacity, organizer_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, events)

    # Insert Tickets (attendees registering for events)
    tickets = [
        ("QR-TECH-001-A1B2C3", 6, 1),   # Emily -> Tech Summit
        ("QR-ART-002-D4E5F6", 7, 2),    # Chris -> Art Gallery
        ("QR-MARA-003-G7H8I9", 8, 3),   # Anna -> City Marathon
        ("QR-MUSIC-004-J1K2L3", 9, 4),  # David -> Music Festival
        ("QR-BIZ-005-M4N5O6", 10, 5),   # Lisa -> Business Mixer
        ("QR-TECH-001-P7Q8R9", 7, 1),   # Chris -> Tech Summit
        ("QR-CODE-006-S1T2U3", 6, 6),   # Emily -> Coding Bootcamp
        ("QR-BIZ-005-V4W5X6", 9, 5),    # David -> Business Mixer
    ]
    cursor.executemany("INSERT INTO Ticket (qr_code, user_id, event_id) VALUES (?, ?, ?)", tickets)


def create_database(db_path: str = "linkt.db"):
    """
    Create SQLite database based on the ERD schema.

    Tables:
    - Organization
    - User
    - Organizer (links User to Organization)
    - Event (created by Organizers)
    - Ticket (users register for events)
    """

    # Connect to database (creates if doesn't exist)
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # Enable foreign key constraints
    cursor.execute("PRAGMA foreign_keys = ON;")

    # Create Organization table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Organization (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name VARCHAR NOT NULL,
            logo_url VARCHAR
        );
    """)

    # Create User table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS User (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            first_name VARCHAR NOT NULL,
            last_name VARCHAR NOT NULL,
            phone_number VARCHAR,
            user_type VARCHAR NOT NULL
        );
    """)

    # Create Organizer table (junction/relationship table)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Organizer (
            id INTEGER PRIMARY KEY,
            is_approved BOOLEAN DEFAULT 0,
            organization_id INTEGER NOT NULL,
            FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE,
            FOREIGN KEY (organization_id) REFERENCES Organization(id) ON DELETE CASCADE
        );
    """)

    # Create Event table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Event (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title VARCHAR NOT NULL,
            description VARCHAR,
            event_type VARCHAR,
            start_date_time TIMESTAMP NOT NULL,
            end_date_time TIMESTAMP NOT NULL,
            location VARCHAR,
            coordinates POINT,
            capacity INTEGER,
            organizer_id INTEGER NOT NULL,
            FOREIGN KEY (organizer_id) REFERENCES Organizer(id) ON DELETE CASCADE
        );
    """)

    # Create Ticket table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Ticket (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            qr_code VARCHAR UNIQUE NOT NULL,
            user_id INTEGER NOT NULL,
            event_id INTEGER NOT NULL,
            FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
            FOREIGN KEY (event_id) REFERENCES Event(id) ON DELETE CASCADE
        );
    """)

    # Create indexes for foreign keys to improve query performance
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_organizer_user ON Organizer(id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_organizer_org ON Organizer(organization_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_event_organizer ON Event(organizer_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_ticket_user ON Ticket(user_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_ticket_event ON Ticket(event_id);")

    # Insert dummy data
    insert_dummy_data(cursor)

    # Commit changes and close connection
    conn.commit()
    conn.close()

    print(f"Database created successfully at: {Path(db_path).absolute()}")
    print("Dummy data inserted successfully!")


if __name__ == "__main__":
    create_database()
