import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres"; // Replace with your DB URL
        String user = "postgres"; // Replace with your DB username
        String password = "kataloni11"; // Replace with your DB password

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // Drop existing tables
            stmt.execute("DROP TABLE IF EXISTS scores");
            stmt.execute("DROP TABLE IF EXISTS matches");
            stmt.execute("DROP TABLE IF EXISTS users");

            // Create users table
            stmt.execute("CREATE TABLE users ("
                    + "id SERIAL PRIMARY KEY, "
                    + "username VARCHAR(50) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'referee'))"
                    + ")");

            // Create matches table
            stmt.execute("CREATE TABLE matches ("
                    + "id SERIAL PRIMARY KEY, "
                    + "player1 VARCHAR(50) NOT NULL, "
                    + "player2 VARCHAR(50) NOT NULL, "
                    + "match_date DATE NOT NULL, "
                    + "time TIME NOT NULL, "
                    + "location VARCHAR(100) NOT NULL, "
                    + "status VARCHAR(20) NOT NULL DEFAULT 'scheduled'"
                    + ")");

            // Create scores table
            stmt.execute("CREATE TABLE scores ("
                    + "id SERIAL PRIMARY KEY, "
                    + "match_id INT NOT NULL REFERENCES matches(id) ON DELETE CASCADE, "
                    + "player1_points VARCHAR(10) NOT NULL DEFAULT '0', "
                    + "player2_points VARCHAR(10) NOT NULL DEFAULT '0', "
                    + "player1_games INT NOT NULL DEFAULT 0, "
                    + "player2_games INT NOT NULL DEFAULT 0, "
                    + "player1_sets INT NOT NULL DEFAULT 0, "
                    + "player2_sets INT NOT NULL DEFAULT 0"
                    + ")");

            // Insert sample users (admins and referees)
            stmt.execute(
                "INSERT INTO users (username, password, role) VALUES " +
                "('admin1', 'adminpass1', 'admin'), " +
                "('admin2', 'adminpass2', 'admin'), " +
                "('referee1', 'referee1', 'referee'), " +
                "('referee2', 'refpass2', 'referee'), " +
                "('referee3', 'refpass3', 'referee')"
            );

            // Insert sample matches
            stmt.execute("INSERT INTO matches (id, player1, player2, match_date, time, location, status) VALUES" +
                    "(1, 'Alice', 'Bob', '2025-04-18', '09:30:00', 'Court 1', 'NOW')," +
                    "(2, 'Charlie', 'David', '2025-04-17', '10:00:00', 'Court 2', 'scheduled')," +
                    "(3, 'Eve', 'Frank', '2025-04-16', '12:00:00', 'Court 3', 'scheduled')," +
                    "(4, 'Grace', 'Hank', '2025-04-18', '14:00:00', 'Court 4', 'scheduled')," +
                    "(5, 'Ivy', 'Jack', '2025-04-15', '09:00:00', 'Court 5', 'scheduled')," +
                    "(6, 'Karen', 'Leo', '2025-04-19', '16:30:00', 'Court 6', 'scheduled')");

            // Insert tennis-style scores
            stmt.execute("INSERT INTO scores (match_id, player1_points, player2_points, player1_games, player2_games, player1_sets, player2_sets) " +
                    "VALUES (1, '40', '30', 3, 2, 1, 0)");
            stmt.execute("INSERT INTO scores (match_id, player1_points, player2_points, player1_games, player2_games, player1_sets, player2_sets) " +
                    "VALUES (2, '15', '15', 1, 1, 0, 0)");
            stmt.execute("INSERT INTO scores (match_id, player1_points, player2_points, player1_games, player2_games, player1_sets, player2_sets) " +
                    "VALUES (3, '0', 'Adv', 0, 2, 0, 1)");
            stmt.execute("INSERT INTO scores (match_id, player1_points, player2_points, player1_games, player2_games, player1_sets, player2_sets) " +
                    "VALUES (4, 'Deuce', 'Deuce', 4, 4, 2, 2)");
            stmt.execute("INSERT INTO scores (match_id, player1_points, player2_points, player1_games, player2_games, player1_sets, player2_sets) " +
                    "VALUES (5, '30', '0', 2, 0, 0, 0)");
            stmt.execute("INSERT INTO scores (match_id, player1_points, player2_points, player1_games, player2_games, player1_sets, player2_sets) " +
                    "VALUES (6, '15', '40', 0, 3, 0, 1)");

            System.out.println("Database schema and seed data applied successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
