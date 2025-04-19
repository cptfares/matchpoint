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
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Root panel with padding and background
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        rootPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Login as " + roleHint);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(40, 40, 40));
        rootPanel.add(titleLabel);
        rootPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JTextField userField = new JTextField(15);
        userField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        userField.setBorder(BorderFactory.createTitledBorder("Username"));
        rootPanel.add(userField);
        rootPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPasswordField passField = new JPasswordField(15);
        passField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passField.setBorder(BorderFactory.createTitledBorder("Password"));
        rootPanel.add(passField);
        rootPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(33, 150, 243));
        loginButton.setFocusPainted(false);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

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

                            if (actualRole.equalsIgnoreCase("referee")) {
                                new RefereeDashboard().setVisible(true);
                            } else if (actualRole.equalsIgnoreCase("admin")) {
                                new AdminDashboard().setVisible(true);
                            }

                            dispose();
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

        rootPanel.add(loginButton);
        add(rootPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI("admin").setVisible(true));
    }
}
