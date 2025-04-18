import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;

public class Scoreboard extends JFrame {
    private JLabel dateTimeLabel;
    private JLabel scoreLabel;
    private JLabel setScoreLabel;
    private JLabel gameScoreLabel;
    private JLabel player1Label;
    private JLabel player2Label;

    public Scoreboard(int matchId) {
        setTitle("MatchPoint - Live Scoreboard");
        setSize(700, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Top Time Label
        dateTimeLabel = new JLabel("17.04.2025 22:10", SwingConstants.CENTER);
        dateTimeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dateTimeLabel.setForeground(Color.LIGHT_GRAY);

        // Score Display
        scoreLabel = new JLabel("0 - 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 48));
        scoreLabel.setForeground(Color.RED);

        // Set Score (e.g., SET 1)
        setScoreLabel = new JLabel("SET 1", SwingConstants.CENTER);
        setScoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        setScoreLabel.setForeground(Color.PINK);

        // Game Score (e.g., 3:3 (15:0))
        gameScoreLabel = new JLabel("3 : 3 (15 : 0)", SwingConstants.CENTER);
        gameScoreLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gameScoreLabel.setForeground(Color.RED);

        // Players Panel
        JPanel playersPanel = new JPanel(new GridLayout(1, 2));
        playersPanel.setBackground(Color.BLACK);

        player1Label = new JLabel("<html><div style='text-align: center;'>Mejia N.<br/>ATP: 301</div></html>", SwingConstants.CENTER);
        player1Label.setForeground(Color.WHITE);
        player1Label.setFont(new Font("Arial", Font.PLAIN, 14));

        player2Label = new JLabel("<html><div style='text-align: center;'>Rodriguez Taverna S.<br/>ATP: 265</div></html>", SwingConstants.CENTER);
        player2Label.setForeground(Color.WHITE);
        player2Label.setFont(new Font("Arial", Font.PLAIN, 14));

        playersPanel.add(player1Label);
        playersPanel.add(player2Label);

        // Center Panel for scores
        JPanel centerPanel = new JPanel(new GridLayout(4, 1));
        centerPanel.setBackground(Color.BLACK);
        centerPanel.add(dateTimeLabel);
        centerPanel.add(scoreLabel);
        centerPanel.add(setScoreLabel);
        centerPanel.add(gameScoreLabel);

        add(playersPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateScoreboard(matchId);
            }
        }, 0, 2000);
    }

    private void updateScoreboard(int matchId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String matchQuery = "SELECT player1, player2, match_date, location FROM matches WHERE matches.id = ?";
            PreparedStatement matchStmt = conn.prepareStatement(matchQuery);
            matchStmt.setInt(1, matchId);
            ResultSet matchRs = matchStmt.executeQuery();

            if (matchRs.next()) {
                String player1 = matchRs.getString("player1");
                String player2 = matchRs.getString("player2");
                player1Label.setText("<html><div style='text-align: center;'>" + player1 + "<br/>ATP: 301</div></html>");
                player2Label.setText("<html><div style='text-align: center;'>" + player2 + "<br/>ATP: 265</div></html>");

                String scoreQuery = "SELECT player1_points, player2_points, player1_games, player2_games, player1_sets, player2_sets FROM scores WHERE match_id = ?";
                PreparedStatement scoreStmt = conn.prepareStatement(scoreQuery);
                scoreStmt.setInt(1, matchId);
                ResultSet scoreRs = scoreStmt.executeQuery();

                if (scoreRs.next()) {
                    String p1Points = scoreRs.getString("player1_points");
                    String p2Points = scoreRs.getString("player2_points");
                    int p1Games = scoreRs.getInt("player1_games");
                    int p2Games = scoreRs.getInt("player2_games");
                    int p1Sets = scoreRs.getInt("player1_sets");
                    int p2Sets = scoreRs.getInt("player2_sets");

                    scoreLabel.setText(p1Sets + " - " + p2Sets);
                    gameScoreLabel.setText(p1Games + " : " + p2Games + " (" + p1Points + " : " + p2Points + ")");
                }
            } else {
                dateTimeLabel.setText("Match not found");
                scoreLabel.setText("0 - 0");
                gameScoreLabel.setText("0 : 0 (0 : 0)");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            dateTimeLabel.setText("Error");
            scoreLabel.setText("X - X");
            gameScoreLabel.setText("Error loading scores");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            int matchId = Integer.parseInt(JOptionPane.showInputDialog("Enter Match ID:"));
            Scoreboard scoreboard = new Scoreboard(matchId);
            scoreboard.setVisible(true);
        });
    }
}
