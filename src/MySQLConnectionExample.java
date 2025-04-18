import java.sql.*;

public class MySQLConnectionExample {
    public static void main(String[] args) {

        String url = "jdbc:postgresql://localhost:5432/postgres"; // Change 'testdb' to your PostgreSQL database name
        String username = "postgres"; // Your PostgreSQL username
        String password = "kataloni11"; // Your PostgreSQL password

        Connection conn = null;

        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connection to PostgreSQL established successfully!");
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Database Product Name: " + meta.getDatabaseProductName());
            System.out.println("Database Product Version: " + meta.getDatabaseProductVersion());
            System.out.println("User: " + meta.getUserName());

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT version();");
            if (rs.next()) {
                System.out.println("PostgreSQL Version: " + rs.getString(1));
            }

        } catch (SQLException e) {
            System.out.println("❌ Connection failed!");
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());

            if (e.getSQLState().equals("28000")) {
                System.out.println("❌ Authentication failed: Incorrect username or password.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ JDBC Driver not found!");
            e.printStackTrace();
        } finally {
            // Step 4: Close the connection properly
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("🔒 Connection closed.");
                }
            } catch (SQLException ex) {
                System.out.println("Error closing the connection.");
                ex.printStackTrace();
            }
        }
    }
}
