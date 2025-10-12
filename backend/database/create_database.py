import sqlite3
from pathlib import Path
from datetime import datetime


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
    cursor.executemany("INSERT INTO organization (name, logo_url) VALUES (?, ?)", organizations)

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
    cursor.executemany("INSERT INTO user (first_name, last_name, phone_number, user_type) VALUES (?, ?, ?, ?)", users)

    # Insert Organizers (linking first 5 users to organizations)
    organizers = [
        (1, True, 1),   # John Doe - Tech Community Hub - approved
        (2, True, 2),   # Jane Smith - Arts & Culture Society - approved
        (3, True, 3),   # Mike Johnson - Sports Events Inc - approved
        (4, False, 4),  # Sarah Williams - Music Festival Organizers - pending
        (5, True, 5),   # Tom Brown - Business Networking Group - approved
    ]
    cursor.executemany("INSERT INTO organizer (id, is_approved, organization_id) VALUES (?, ?, ?)", organizers)

    # Insert Events
    events = [
        ("Frosh Night", "New to school and don't know where to start? Have some drinks, play games and meet some new people at the school's frosh night!", "Social",
         datetime(2025, 10, 15, 19, 0, 0).isoformat(), datetime(2025, 10, 15, 23, 0, 0).isoformat(),
         "Student Union Building", "POINT(37.7749 -122.4194)", 200, "https://images.unsplash.com/photo-1511795409834-ef04bbd61622", 0.00, 1),

        ("DJ Night", "The EDM Club is organizing an all-night dance festival on the 22nd of October! Click for more details!", "Music",
         datetime(2025, 10, 22, 20, 0, 0).isoformat(), datetime(2025, 10, 23, 2, 0, 0).isoformat(),
         "Campus Arena", "POINT(40.7128 -74.0060)", 500, "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3", 25.00, 2),

        ("Campus Museum Tour", "Join us for a tour of the campus museum where you can browse artifacts of some of the school's greatest alumni!", "Educational",
         datetime(2025, 10, 18, 14, 0, 0).isoformat(), datetime(2025, 10, 18, 16, 0, 0).isoformat(),
         "Campus Museum", "POINT(40.7829 -73.9654)", 30, "https://images.unsplash.com/photo-1577896851231-70ef18881754", 0.00, 3),

        ("Tech Career Fair", "Connect with leading tech companies and explore internship and job opportunities. Meet recruiters from top firms!", "Career",
         datetime(2025, 10, 25, 10, 0, 0).isoformat(), datetime(2025, 10, 25, 16, 0, 0).isoformat(),
         "Convention Center", "POINT(34.0522 -118.2437)", 1000, "https://images.unsplash.com/photo-1540575467063-178a50c2df87", 0.00, 1),

        ("Open Mic Night", "Showcase your talent or enjoy performances from fellow students. Poetry, music, comedy - all welcome!", "Arts",
         datetime(2025, 10, 20, 18, 0, 0).isoformat(), datetime(2025, 10, 20, 22, 0, 0).isoformat(),
         "Student Cafe", "POINT(41.8781 -87.6298)", 80, "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7", 5.00, 5),

        ("Hackathon 2025", "24-hour coding marathon! Build innovative solutions, win prizes, and network with industry professionals.", "Technology",
         datetime(2025, 11, 1, 9, 0, 0).isoformat(), datetime(2025, 11, 2, 9, 0, 0).isoformat(),
         "Engineering Building", "POINT(37.7749 -122.4194)", 150, "https://images.unsplash.com/photo-1504384308090-c894fdcc538d", 15.00, 1),

        ("Halloween Costume Party", "Get ready for the spookiest night of the year! Costume contest with amazing prizes, DJ, and themed refreshments.", "Social",
         datetime(2025, 10, 31, 20, 0, 0).isoformat(), datetime(2025, 11, 1, 1, 0, 0).isoformat(),
         "Student Union Ballroom", "POINT(40.7128 -74.0060)", 300, "https://images.unsplash.com/photo-1509557965875-b88c97052f0e", 10.00, 2),

        ("Yoga & Wellness Workshop", "Destress and recharge with guided yoga sessions, meditation, and wellness tips from certified instructors.", "Wellness",
         datetime(2025, 10, 28, 17, 0, 0).isoformat(), datetime(2025, 10, 28, 19, 0, 0).isoformat(),
         "Recreation Center", "POINT(40.7829 -73.9654)", 50, "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b", 8.00, 3),

        ("International Food Festival", "Taste cuisines from around the world! Student organizations showcase their cultural dishes and traditions.", "Cultural",
         datetime(2025, 11, 5, 12, 0, 0).isoformat(), datetime(2025, 11, 5, 18, 0, 0).isoformat(),
         "Campus Quad", "POINT(34.0522 -118.2437)", 500, "https://images.unsplash.com/photo-1555939594-58d7cb561ad1", 0.00, 4),

        ("Guest Speaker: AI & The Future", "Renowned AI researcher Dr. Sarah Chen discusses the impact of artificial intelligence on society and careers.", "Educational",
         datetime(2025, 11, 8, 18, 30, 0).isoformat(), datetime(2025, 11, 8, 20, 0, 0).isoformat(),
         "Auditorium Hall", "POINT(41.8781 -87.6298)", 400, "https://images.unsplash.com/photo-1485827404703-89b55fcc595e", 0.00, 5),

        ("Basketball Tournament Finals", "Cheer for your team at the intramural basketball championship! Exciting finals with live commentary.", "Sports",
         datetime(2025, 11, 12, 19, 0, 0).isoformat(), datetime(2025, 11, 12, 21, 0, 0).isoformat(),
         "Sports Complex", "POINT(37.7749 -122.4194)", 800, "https://images.unsplash.com/photo-1546519638-68e109498ffc", 7.00, 1),

        ("Film Festival Screening", "Student-produced short films premiere at our annual film festival. Q&A with filmmakers afterwards.", "Arts",
         datetime(2025, 11, 15, 19, 0, 0).isoformat(), datetime(2025, 11, 15, 22, 0, 0).isoformat(),
         "Media Arts Theater", "POINT(40.7128 -74.0060)", 120, "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba", 12.00, 2),
    ]
    cursor.executemany("""
        INSERT INTO event (title, description, event_type, start_date_time, end_date_time,
                          location, coordinates, capacity, image_url, price, organizer_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
    cursor.executemany("INSERT INTO ticket (qr_code, user_id, event_id) VALUES (?, ?, ?)", tickets)


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

    # Create organization table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS organization (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name VARCHAR NOT NULL,
            logo_url VARCHAR
        );
    """)

    # Create user table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS user (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            first_name VARCHAR NOT NULL,
            last_name VARCHAR NOT NULL,
            phone_number VARCHAR,
            user_type VARCHAR NOT NULL
        );
    """)

    # Create organizer table (junction/relationship table)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS organizer (
            id INTEGER PRIMARY KEY,
            is_approved BOOLEAN DEFAULT 0,
            organization_id INTEGER NOT NULL,
            FOREIGN KEY (id) REFERENCES user(id) ON DELETE CASCADE,
            FOREIGN KEY (organization_id) REFERENCES organization(id) ON DELETE CASCADE
        );
    """)

    # Create event table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS event (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title VARCHAR NOT NULL,
            description VARCHAR,
            event_type VARCHAR,
            start_date_time TIMESTAMP NOT NULL,
            end_date_time TIMESTAMP NOT NULL,
            location VARCHAR,
            coordinates POINT,
            capacity INTEGER,
            image_url VARCHAR,
            price DECIMAL(10,2) DEFAULT 0.00,
            organizer_id INTEGER NOT NULL,
            FOREIGN KEY (organizer_id) REFERENCES organizer(id) ON DELETE CASCADE
        );
    """)

    # Create ticket table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS ticket (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            qr_code VARCHAR UNIQUE NOT NULL,
            user_id INTEGER NOT NULL,
            event_id INTEGER NOT NULL,
            FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
            FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE
        );
    """)

    # Create indexes for foreign keys to improve query performance
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_organizer_user ON organizer(id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_organizer_org ON organizer(organization_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_event_organizer ON event(organizer_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_ticket_user ON ticket(user_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_ticket_event ON ticket(event_id);")

    # Insert dummy data
    insert_dummy_data(cursor)

    # Commit changes and close connection
    conn.commit()
    conn.close()

    print(f"Database created successfully at: {Path(db_path).absolute()}")
    print("Dummy data inserted successfully!")


if __name__ == "__main__":
    create_database()
