import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginUI extends JFrame {
    private String roleHint;

    public LoginUI(String roleHint) {
        this.roleHint = roleHint;

        setTitle("MatchPoint - Login as " + roleHint);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "SELECT role FROM users WHERE username = ? AND password = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, username);
                    stmt.setString(2, password);

                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String actualRole = rs.getString("role");
                        if (actualRole.equalsIgnoreCase(roleHint)) {
                            JOptionPane.showMessageDialog(LoginUI.this, "Login successful as " + actualRole + "!");

                            // Open the appropriate dashboard based on the role
                            if (actualRole.equalsIgnoreCase("referee")) {
                                new RefereeDashboard().setVisible(true);
                            } else if (actualRole.equalsIgnoreCase("admin")) {
                                new AdminDashboard().setVisible(true);
                            }

                            dispose(); // Close the login window
                        } else {
                            JOptionPane.showMessageDialog(LoginUI.this, "You are not authorized to login as " + roleHint, "Access Denied", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(LoginUI.this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginUI.this, "Error connecting to the database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);
    }

    public static void main(String[] args) {
        // Example usage for admin login
        new LoginUI("admin").setVisible(true);

        // Example usage for referee login
        // new LoginUI("referee").setVisible(true);
    }
}
