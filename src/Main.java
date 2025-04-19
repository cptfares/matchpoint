import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🎾 MatchPoint");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 850);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            // Set Look & Feel
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.put("Button.arc", 15);
                UIManager.put("Component.arc", 15);
                UIManager.put("Table.showVerticalLines", false);
                UIManager.put("Table.showHorizontalLines", true);
            } catch (Exception ignored) {}

            // Header Panel
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(33, 150, 243));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel("Welcome to MatchPoint", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titleLabel.setForeground(Color.WHITE);
            headerPanel.add(titleLabel, BorderLayout.CENTER);

            frame.add(headerPanel, BorderLayout.NORTH);

            // Button Panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
            buttonPanel.setBackground(Color.WHITE);

            JButton adminButton = createModernButton("Login as Tournament Manager", new Color(76, 175, 80));
            JButton refereeButton = createModernButton("Login as Referee", new Color(244, 67, 54));

            adminButton.addActionListener(e -> {
                frame.dispose();
                new LoginUI("admin").setVisible(true);
            });
            refereeButton.addActionListener(e -> {
                frame.dispose();
                new LoginUI("referee").setVisible(true);
            });

            buttonPanel.add(adminButton);
            buttonPanel.add(refereeButton);
            frame.add(buttonPanel, BorderLayout.BEFORE_FIRST_LINE);

            // Match Tables Panel
            JPanel tablesPanel = new JPanel();
            tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
            tablesPanel.setBackground(Color.WHITE);
            tablesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            String[] titles = {"Today's Matches", "Tomorrow's Matches", "Other Matches", "Finished Matches"};
            String[] whereClauses = {
                    "DATE(m.match_date) = CURRENT_DATE",
                    "DATE(m.match_date) = CURRENT_DATE + INTERVAL '1 day'",
                    "DATE(m.match_date) < CURRENT_DATE - INTERVAL '1 day' OR DATE(m.match_date) > CURRENT_DATE + INTERVAL '1 day'",
                    "m.status = 'FINISHED'"
            };
            boolean[] showTimeOnly = {true, true, false, false};

            List<DefaultTableModel> models = new ArrayList<>();

            for (int i = 0; i < titles.length; i++) {
                JLabel sectionTitle = new JLabel(titles[i]);
                sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
                sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                tablesPanel.add(sectionTitle);
                tablesPanel.add(Box.createRigidArea(new Dimension(0, 8)));

                DefaultTableModel model = new DefaultTableModel(
                        new String[]{"Player 1", "Player 2", "Score/Date", "Status"}, 0) {
                    @Override public boolean isCellEditable(int row, int column) { return false; }
                };

                JTable table = new JTable(model);
                table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                table.setRowHeight(28);
                table.setFillsViewportHeight(true);
                table.setSelectionBackground(new Color(232, 234, 246));

                table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                    private boolean flash = false;
                    @Override
                    public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean hasFocus, int row, int col) {
                        Component c = super.getTableCellRendererComponent(tbl, val, sel, hasFocus, row, col);
                        String status = (String) tbl.getValueAt(row, 3);
                        if ("NOW".equalsIgnoreCase(status)) {
                            flash = !flash;
                            c.setBackground(flash ? new Color(255, 235, 59) : new Color(255, 204, 0));
                        } else if ("FINISHED".equalsIgnoreCase(status)) {
                            c.setBackground(new Color(200, 230, 201));
                        } else {
                            c.setBackground(Color.WHITE);
                        }
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
                models.add(model);

                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                tablesPanel.add(scrollPane);
                tablesPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            }

            frame.add(new JScrollPane(tablesPanel), BorderLayout.CENTER);

            // Refresh Panel
            JButton refreshButton = createModernButton("🔄 Refresh Matches", new Color(30, 136, 229));
            refreshButton.addActionListener(e -> {
                for (int j = 0; j < models.size(); j++) {
                    populateMatchTable(models.get(j), whereClauses[j], showTimeOnly[j]);
                }
            });

            JPanel refreshPanel = new JPanel();
            refreshPanel.setBackground(Color.WHITE);
            refreshPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            refreshPanel.add(refreshButton);

            frame.add(refreshPanel, BorderLayout.SOUTH);
            frame.setVisible(true);
        });
    }

    private static JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private static void populateMatchTable(DefaultTableModel tableModel, String whereClause, boolean showTimeOnly) {
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
                    String liveScore = String.format("Pts: %s-%s | Gms: %d-%d | Sets: %d-%d", pts1, pts2, games1, games2, sets1, sets2);
                    tableModel.addRow(new Object[]{p1, p2, liveScore, status});
                } else {
                    String dateTime = showTimeOnly ? rs.getString("time") : rs.getString("match_date") + " " + rs.getString("time");
                    tableModel.addRow(new Object[]{p1, p2, dateTime, status});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getMatchIdFromRow(JTable table, int row) {
        String player1 = (String) table.getValueAt(row, 0);
        String player2 = (String) table.getValueAt(row, 1);
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM matches WHERE player1 = ? AND player2 = ? ORDER BY match_date DESC LIMIT 1");
            stmt.setString(1, player1);
            stmt.setString(2, player2);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
