import sqlite3
# USE THIS TO CREATE AN ADMIN ACCOUNT - ADMIN ACCESS
# BCrypt hash for password: "password123"
BCRYPT_PASSWORD = "$2b$10$xBw2R3M/vwQcgNbA58VbDuZ1J4tTx/RBxdtrciiS67UVGr8atCQwC"

conn = sqlite3.connect('linkt.db')
cursor = conn.cursor()
cursor.execute("""
    INSERT INTO user (email, first_name, last_name, phone_number, password, user_type)
    VALUES (?, ?, ?, ?, ?, ?)
""", (
    "customadmin@linkt.dev",
    "Custom",
    "Admin",
    "+1-555-9999",
    BCRYPT_PASSWORD,
    "administrator"
))
conn.commit()
conn.close()
print("Admin user created!")
