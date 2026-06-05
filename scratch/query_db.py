import sqlite3

conn = sqlite3.connect("deutrust.db")
cursor = conn.cursor()

# Get table names
cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
tables = cursor.fetchall()
print("Tables:", tables)

for table in tables:
    name = table[0]
    print(f"\n--- Table {name} ---")
    cursor.execute(f"PRAGMA table_info({name});")
    print("Columns:", cursor.fetchall())
    
    cursor.execute(f"SELECT * FROM {name} LIMIT 10;")
    rows = cursor.fetchall()
    print("Rows:")
    for row in rows:
        print(row)

conn.close()
