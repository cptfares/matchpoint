import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class RefereeDashboard extends JFrame {
    private JTextField matchIdField;
    private JButton loadButton, p1PointButton, p2PointButton, p1MinusButton, p2MinusButton, finishMatchButton;
    private JLabel matchInfoLabel, scoreLabel;
    private int matchId;
    private Connection conn;

    public RefereeDashboard() {
        setTitle("🏆 Referee Dashboard");
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "kataloni11");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        // Modern UI panel
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 245, 245));
    
        // Header
        JLabel titleLabel = new JLabel("Referee Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
    
        // Top Input
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setBackground(new Color(245, 245, 245));
    
        JLabel matchIdLabel = new JLabel("Enter Match ID:");
        matchIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        matchIdField = new JTextField(6);
        matchIdField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loadButton = new JButton("🔄 Load Match");
    
        topPanel.add(matchIdLabel);
        topPanel.add(matchIdField);
        topPanel.add(loadButton);
        panel.add(topPanel, BorderLayout.BEFORE_FIRST_LINE);
    
        // Center info
        matchInfoLabel = new JLabel("Match info will appear here", SwingConstants.CENTER);
        matchInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
    
        scoreLabel = new JLabel("Score: -", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(matchInfoLabel);
        centerPanel.add(scoreLabel);
        panel.add(centerPanel, BorderLayout.CENTER);
    
        // Bottom Controls
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomPanel.setBackground(new Color(245, 245, 245));
    
        p1PointButton = new JButton("+ Player 1");
        p2PointButton = new JButton("+ Player 2");
        p1MinusButton = new JButton("- Player 1");
        p2MinusButton = new JButton("- Player 2");
        finishMatchButton = new JButton(" Finish Match");
    
        JButton[] allButtons = { p1PointButton, p2PointButton, p1MinusButton, p2MinusButton, finishMatchButton };
        for (JButton btn : allButtons) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btn.setFocusPainted(false);
            btn.setBackground(new Color(220, 220, 220));
            btn.setEnabled(false);
            bottomPanel.add(btn);
        }
    
        panel.add(bottomPanel, BorderLayout.SOUTH);
    
        add(panel);
        setVisible(true);
    
        // Action Listeners
        loadButton.addActionListener(e -> loadMatch());
        p1PointButton.addActionListener(e -> updateScore(true));
        p2PointButton.addActionListener(e -> updateScore(false));
        p1MinusButton.addActionListener(e -> undoPoint(true));
        p2MinusButton.addActionListener(e -> undoPoint(false));
        finishMatchButton.addActionListener(e -> finishMatch());
    }
    

    private void loadMatch() {
        try {
            matchId = Integer.parseInt(matchIdField.getText());
            PreparedStatement psMatch = conn.prepareStatement("UPDATE matches SET status = 'NOW' WHERE id = ?");
            psMatch.setInt(1, matchId);
            psMatch.executeUpdate();

            PreparedStatement ps = conn.prepareStatement(
                "SELECT player1, player2, status FROM matches WHERE id = ?");
            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String p1 = rs.getString("player1");
                String p2 = rs.getString("player2");
                matchInfoLabel.setText("Match: " + p1 + " vs " + p2 + " | Status: NOW");
                p1PointButton.setEnabled(true);
                p2PointButton.setEnabled(true);
                p1MinusButton.setEnabled(true);
                p2MinusButton.setEnabled(true);
                finishMatchButton.setEnabled(true);
                updateScoreLabel();
            } else {
                JOptionPane.showMessageDialog(this, "Match not found!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Match ID.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading match: " + ex.getMessage());
        }
    }

    private void updateScore(boolean player1WonPoint) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM scores WHERE match_id = ?");
            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String p1Pts = rs.getString("player1_points");
                String p2Pts = rs.getString("player2_points");
                int p1Games = rs.getInt("player1_games");
                int p2Games = rs.getInt("player2_games");
                int p1Sets = rs.getInt("player1_sets");
                int p2Sets = rs.getInt("player2_sets");

                String[] points = {"0", "15", "30", "40"};
                int p1Idx = indexOf(points, p1Pts);
                int p2Idx = indexOf(points, p2Pts);

                if (p1Pts.equals("Deuce") || p2Pts.equals("Deuce") || p1Pts.equals("Adv") || p2Pts.equals("Adv")) {
                    if (p1Pts.equals("Deuce") && p2Pts.equals("Deuce")) {
                        if (player1WonPoint) {
                            p1Pts = "Adv";
                            p2Pts = "";
                        } else {
                            p2Pts = "Adv";
                            p1Pts = "";
                        }
                    } else if (p1Pts.equals("Adv")) {
                        if (player1WonPoint) {
                            p1Games++;
                            p1Pts = "0";
                            p2Pts = "0";
                        } else {
                            p1Pts = "Deuce";
                            p2Pts = "Deuce";
                        }
                    } else if (p2Pts.equals("Adv")) {
                        if (!player1WonPoint) {
                            p2Games++;
                            p1Pts = "0";
                            p2Pts = "0";
                        } else {
                            p1Pts = "Deuce";
                            p2Pts = "Deuce";
                        }
                    }
                } else if (p1Pts.equals("40") && p2Pts.equals("40")) {
                    p1Pts = "Deuce";
                    p2Pts = "Deuce";
                } else if (player1WonPoint) {
                    if (p1Pts.equals("40")) {
                        p1Games++;
                        p1Pts = "0";
                        p2Pts = "0";
                    } else {
                        p1Pts = points[Math.min(p1Idx + 1, 3)];
                    }
                } else {
                    if (p2Pts.equals("40")) {
                        p2Games++;
                        p1Pts = "0";
                        p2Pts = "0";
                    } else {
                        p2Pts = points[Math.min(p2Idx + 1, 3)];
                    }
                }

                // Check for Set win
                if (p1Games >= 6 && p1Games - p2Games >= 2) {
                    p1Sets++;
                    p1Games = 0;
                    p2Games = 0;
                } else if (p2Games >= 6 && p2Games - p1Games >= 2) {
                    p2Sets++;
                    p1Games = 0;
                    p2Games = 0;
                }

                PreparedStatement updatePs = conn.prepareStatement(
                        "UPDATE scores SET player1_points = ?, player2_points = ?, player1_games = ?, player2_games = ?, player1_sets = ?, player2_sets = ? WHERE match_id = ?");
                updatePs.setString(1, p1Pts);
                updatePs.setString(2, p2Pts);
                updatePs.setInt(3, p1Games);
                updatePs.setInt(4, p2Games);
                updatePs.setInt(5, p1Sets);
                updatePs.setInt(6, p2Sets);
                updatePs.setInt(7, matchId);
                updatePs.executeUpdate();

                updateScoreLabel();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating score: " + e.getMessage());
        }
    }

    private void undoPoint(boolean player1) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM scores WHERE match_id = ?");
            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] points = {"0", "15", "30", "40"};
                String p1Pts = rs.getString("player1_points");
                String p2Pts = rs.getString("player2_points");

                int p1Games = rs.getInt("player1_games");
                int p2Games = rs.getInt("player2_games");

                if (player1) {
                    int p1Idx = indexOf(points, p1Pts);
                    p1Pts = p1Idx > 0 ? points[p1Idx - 1] : "0";
                } else {
                    int p2Idx = indexOf(points, p2Pts);
                    p2Pts = p2Idx > 0 ? points[p2Idx - 1] : "0";
                }

                PreparedStatement updatePs = conn.prepareStatement(
                        "UPDATE scores SET player1_points = ?, player2_points = ? WHERE match_id = ?");
                updatePs.setString(1, p1Pts);
                updatePs.setString(2, p2Pts);
                updatePs.setInt(3, matchId);
                updatePs.executeUpdate();

                updateScoreLabel();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error undoing point: " + e.getMessage());
        }
    }

    private void finishMatch() {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE matches SET status = 'FINISHED' WHERE id = ?");
            ps.setInt(1, matchId);
            ps.executeUpdate();

            matchInfoLabel.setText("Match finished.");
            p1PointButton.setEnabled(false);
            p2PointButton.setEnabled(false);
            p1MinusButton.setEnabled(false);
            p2MinusButton.setEnabled(false);
            finishMatchButton.setEnabled(false);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error finishing match: " + e.getMessage());
        }
    }

    private void updateScoreLabel() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM scores WHERE match_id = ?");
            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String p1Pts = rs.getString("player1_points");
                String p2Pts = rs.getString("player2_points");
                int p1Games = rs.getInt("player1_games");
                int p2Games = rs.getInt("player2_games");
                int p1Sets = rs.getInt("player1_sets");
                int p2Sets = rs.getInt("player2_sets");

                scoreLabel.setText(String.format("Points: %s-%s | Games: %d-%d | Sets: %d-%d",
                        p1Pts, p2Pts, p1Games, p2Games, p1Sets, p2Sets));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RefereeDashboard::new);
    }
}
