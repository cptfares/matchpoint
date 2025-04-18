import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MatchPoint");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 800);
            frame.setLayout(new BorderLayout());

            // Header Panel
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(30, 144, 255));
            JLabel titleLabel = new JLabel("Welcome to MatchPoint", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            headerPanel.add(titleLabel, BorderLayout.CENTER);
            frame.add(headerPanel, BorderLayout.NORTH);

            // Button Panel
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 20));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            JButton adminButton = new JButton("Login as Tournament Manager");
            JButton refereeButton = new JButton("Login as Referee");
            for (JButton btn : new JButton[]{adminButton, refereeButton}) {
                btn.setFont(new Font("Arial", Font.PLAIN, 16));
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
            }
            adminButton.setBackground(new Color(60, 179, 113));
            adminButton.addActionListener(e -> {
                frame.dispose();
                new LoginUI("admin").setVisible(true);
            });
            refereeButton.setBackground(new Color(255, 99, 71));
            refereeButton.addActionListener(e -> {
                frame.dispose();
                new LoginUI("referee").setVisible(true);
            });
            buttonPanel.add(adminButton);
            buttonPanel.add(refereeButton);
            frame.add(buttonPanel, BorderLayout.BEFORE_FIRST_LINE);

            // Match Tables Panel
            String[] titles = {"Today's Matches", "Tomorrow's Matches", "Other Matches", "Finished Matches"};
            String[] whereClauses = {
                "DATE(m.match_date) = CURRENT_DATE",
                "DATE(m.match_date) = CURRENT_DATE + INTERVAL '1 day'",
                "DATE(m.match_date) < CURRENT_DATE - INTERVAL '1 day' OR DATE(m.match_date) > CURRENT_DATE + INTERVAL '1 day'",
                "m.status = 'FINISHED'"
            };
            boolean[] showTimeOnly = {true, true, false, false};

            JPanel tablesPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            List<DefaultTableModel> models = new ArrayList<>();

            for (int i = 0; i < titles.length; i++) {
                DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Player 1", "Player 2", "Score/Date", "Status"}, 0) {
                    @Override public boolean isCellEditable(int row, int column) { return false; }
                };
                JTable table = new JTable(model);
                table.setFont(new Font("Arial", Font.PLAIN, 14));
                table.setRowHeight(25);

                // Renderer
                table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                    private boolean flash = false;
                    @Override public Component getTableCellRendererComponent(
                            JTable tbl, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                        Component c = super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
                        String status = (String) tbl.getValueAt(row, tbl.getColumnCount()-1);
                        if ("NOW".equalsIgnoreCase(status)) {
                            flash = !flash;
                            c.setBackground(flash ? Color.YELLOW : Color.ORANGE);
                        } else if ("FINISHED".equalsIgnoreCase(status)) {
                            c.setBackground(new Color(144, 238, 144));
                        } else c.setBackground(Color.WHITE);
                        return c;
                    }
                });

                table.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        int row = table.getSelectedRow();
                        if (row != -1 && "NOW".equalsIgnoreCase((String) table.getValueAt(row, 3))) {
                            int matchId = getMatchIdFromRow(table, row);
                            SwingUtilities.invokeLater(() -> new Scoreboard(matchId).setVisible(true));
                        }
                    }
                });

                populateMatchTable(model, whereClauses[i], showTimeOnly[i]);
                tablesPanel.add(new JScrollPane(table));
                models.add(model);
            }

            // Refresh Button
            JButton refreshButton = new JButton("Refresh Matches");
            refreshButton.setFont(new Font("Arial", Font.PLAIN, 16));
            refreshButton.setBackground(new Color(70, 130, 180));
            refreshButton.setForeground(Color.WHITE);
            refreshButton.setFocusPainted(false);
            refreshButton.addActionListener(e -> {
                for (int j = 0; j < models.size(); j++) {
                    populateMatchTable(models.get(j), whereClauses[j], showTimeOnly[j]);
                }
            });
            JPanel refreshPanel = new JPanel();
            refreshPanel.add(refreshButton);

            frame.add(tablesPanel, BorderLayout.CENTER);
            frame.add(refreshPanel, BorderLayout.SOUTH);
            frame.setVisible(true);
        });
    }

    private static void populateMatchTable(
        DefaultTableModel tableModel, String whereClause, boolean showTimeOnly) {

        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query =
                "SELECT m.id, m.player1, m.player2, m.match_date, m.time, m.location, m.status, " +
                "s.player1_sets, s.player2_sets, s.player1_games, s.player2_games, s.player1_points, s.player2_points " +
                "FROM matches m LEFT JOIN scores s ON m.id = s.match_id " +
                "WHERE " + whereClause + " " +
                "ORDER BY CASE WHEN m.status = 'NOW' THEN 1 ELSE 2 END, m.match_date, m.time";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");
                String p1 = rs.getString("player1");
                String p2 = rs.getString("player2");

                if ("FINISHED".equalsIgnoreCase(status)) {
                    int sets1 = rs.getInt("player1_sets");
                    int sets2 = rs.getInt("player2_sets");
                    tableModel.addRow(new Object[]{p1, p2, sets1 + "-" + sets2, status});

                } else if ("NOW".equalsIgnoreCase(status)) {
                    String pts1 = rs.getString("player1_points");
                    String pts2 = rs.getString("player2_points");
                    int games1 = rs.getInt("player1_games");
                    int games2 = rs.getInt("player2_games");
                    int sets1 = rs.getInt("player1_sets");
                    int sets2 = rs.getInt("player2_sets");
                    String liveScore = String.format(
                        "Pts: %s-%s | Gms: %d-%d | Sets: %d-%d",
                        pts1, pts2, games1, games2, sets1, sets2
                    );
                    tableModel.addRow(new Object[]{p1, p2, liveScore, status});

                } else {
                    String dateTime = showTimeOnly ? rs.getString("time")
                        : rs.getString("match_date") + " " + rs.getString("time");
                    tableModel.addRow(new Object[]{p1, p2, dateTime, status});
                }
            }
        } catch (Exception ex) {
            System.err.println("Error fetching matches: " + ex.getMessage());
        }
    }

    private static int getMatchIdFromRow(JTable matchTable, int row) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String p1 = (String) matchTable.getValueAt(row, 0);
            String p2 = (String) matchTable.getValueAt(row, 1);
            String query =
                "SELECT id FROM matches " +
                "WHERE player1 = ? AND player2 = ? " +
                "ORDER BY match_date DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, p1);
            stmt.setString(2, p2);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }
}