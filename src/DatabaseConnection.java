import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    static String URL = "jdbc:postgresql://localhost:5432/postgres"; // Change 'testdb' to your PostgreSQL database name
    static String USER = "postgres"; // Your PostgreSQL username
    static String PASSWORD = "kataloni11"; // Your PostgreSQL password
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}