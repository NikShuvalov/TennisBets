package org.example.database

import java.lang.AutoCloseable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/** Stores the player list in a local SQLite file.  */
class PlayerRepository(dbPath: String?) : AutoCloseable {
    private val conn: Connection

    init {
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)
        conn.createStatement().use { st ->
            st.execute(
                """
                    CREATE TABLE IF NOT EXISTS players (
                        id         INTEGER PRIMARY KEY AUTOINCREMENT,
                        name       TEXT NOT NULL UNIQUE,
                        first_seen TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """.trimIndent()
            )
        }
    }

    /** Adds the player if new. Returns true if a row was inserted.  */
    @Throws(SQLException::class)
    fun add(name: String): Boolean {
        conn.prepareStatement("INSERT OR IGNORE INTO players(name) VALUES (?)").use { ps ->
            ps.setString(1, name.trim { it <= ' ' })
            return ps.executeUpdate() > 0
        }
    }

    @Throws(SQLException::class)
    fun all(): MutableList<String?> {
        val names: MutableList<String?> = ArrayList<String?>()
        conn.createStatement().use { st ->
            st.executeQuery("SELECT name FROM players ORDER BY name").use { rs ->
                while (rs.next()) names.add(rs.getString("name"))
            }
        }
        return names
    }

    @Throws(SQLException::class)
    fun count(): Int {
        conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM players").use { rs ->
                return if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    @Throws(SQLException::class)
    override fun close() {
        conn.close()
    }
}