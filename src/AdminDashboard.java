import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Random;

public class AdminDashboard extends JFrame {
    private JTable matchTable;
    private DefaultTableModel tableModel;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top panel with title and create match button
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("All Scheduled Matches", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        JButton createMatchBtn = new JButton("Create Match");
        createMatchBtn.addActionListener(e -> openCreateMatchPopup());
        topPanel.add(title, BorderLayout.CENTER);
        topPanel.add(createMatchBtn, BorderLayout.EAST);

        // Match table
        tableModel = new DefaultTableModel(new String[]{
                "Match ID", "Player 1", "Player 2", "Date", "Time", "Location", "Status"
        }, 0);
        matchTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(matchTable);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadMatches();
    }

    private void loadMatches() {
        tableModel.setRowCount(0); // Clear table
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM matches");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("player1"),
                        rs.getString("player2"),
                        rs.getDate("match_date"),
                        rs.getTime("time"),
                        rs.getString("location"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading matches: " + ex.getMessage());
        }
    }

    private void openCreateMatchPopup() {
        JDialog dialog = new JDialog(this, "Create New Match", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(8, 2, 10, 10));

        JTextField player1Field = new JTextField();
        JTextField player2Field = new JTextField();
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);

        JTextField locationField = new JTextField();

        dialog.add(new JLabel("Player 1:"));
        dialog.add(player1Field);
        dialog.add(new JLabel("Player 2:"));
        dialog.add(player2Field);
        dialog.add(new JLabel("Date:"));
        dialog.add(dateSpinner);
        dialog.add(new JLabel("Time:"));
        dialog.add(timeSpinner);
        dialog.add(new JLabel("Location:"));
        dialog.add(locationField);

        JButton submitBtn = new JButton("Submit");
        JButton cancelBtn = new JButton("Cancel");
        dialog.add(cancelBtn);
        dialog.add(submitBtn);

        submitBtn.addActionListener(e -> {
            String player1 = player1Field.getText().trim();
            String player2 = player2Field.getText().trim();
            String location = locationField.getText().trim();
            java.util.Date date = (java.util.Date) dateSpinner.getValue();
            java.util.Date time = (java.util.Date) timeSpinner.getValue();

            if (player1.isEmpty() || player2.isEmpty() || location.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.");
                return;
            }

            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            java.sql.Time sqlTime = new java.sql.Time(time.getTime());
            int matchId = new Random().nextInt(1_000_000);

            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO matches (id, player1, player2, match_date, time, location, status) VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                stmt.setInt(1, matchId);
                stmt.setString(2, player1);
                stmt.setString(3, player2);
                stmt.setDate(4, sqlDate);
                stmt.setTime(5, sqlTime);
                stmt.setString(6, location);
                stmt.setString(7, "scheduled");
                stmt.executeUpdate();

                PreparedStatement scoreStmt = conn.prepareStatement(
                        "INSERT INTO scores (match_id, player1_points, player2_points, player1_games, player2_games, player1_sets, player2_sets) VALUES (?, '0', '0', 0, 0, 0, 0)"
                );
                scoreStmt.setInt(1, matchId);
                scoreStmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Match created with ID: " + matchId);
                dialog.dispose();
                loadMatches(); // refresh table
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving match: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}
