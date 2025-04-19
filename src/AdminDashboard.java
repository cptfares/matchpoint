import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Calendar;          // ← add this

import java.util.Random;

public class AdminDashboard extends JFrame {
    private JTable matchTable;
    private DefaultTableModel tableModel;

    public AdminDashboard() {
        setTitle("🎾 Admin Dashboard");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(new Color(245, 245, 245));

        // Top panel
// In the AdminDashboard constructor, modify the top panel section:
JPanel topPanel = new JPanel(new BorderLayout());
topPanel.setBackground(getContentPane().getBackground());
topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

JLabel title = new JLabel("All Scheduled Matches");
title.setFont(new Font("Segoe UI", Font.BOLD, 24));
topPanel.add(title, BorderLayout.WEST);

// Create button panel
JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
buttonPanel.setOpaque(false);
JButton manageUsersBtn = new JButton(" Manage Users");
styleButton(manageUsersBtn);
manageUsersBtn.addActionListener(e -> openUserManagementPopup());
buttonPanel.add(manageUsersBtn);


JButton createUserBtn = new JButton(" Create User");
styleButton(createUserBtn);
createUserBtn.addActionListener(e -> openCreateUserPopup());
buttonPanel.add(createUserBtn);

JButton createMatchBtn = new JButton("Create Match");
styleButton(createMatchBtn);
createMatchBtn.addActionListener(e -> openCreateMatchPopup());
buttonPanel.add(createMatchBtn);

topPanel.add(buttonPanel, BorderLayout.EAST);

        // Table model & table
        tableModel = new DefaultTableModel(new String[]{
            "Match ID","Player 1","Player 2","Date","Time","Location","Status"
        }, 0);

        matchTable = new JTable(tableModel);
        matchTable.setRowHeight(28);
        matchTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        matchTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        matchTable.setGridColor(new Color(220,220,220));

        // ensure only rows are selectable
        matchTable.setRowSelectionAllowed(true);
        matchTable.setColumnSelectionAllowed(false);
        matchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // double-click listener using rowAtPoint
        matchTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = matchTable.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        // select that row
                        matchTable.setRowSelectionInterval(row, row);
                        int matchId    = (int) tableModel.getValueAt(row, 0);
                        String p1      = tableModel.getValueAt(row, 1).toString();
                        String p2      = tableModel.getValueAt(row, 2).toString();
                        String date    = tableModel.getValueAt(row, 3).toString();
                        String time    = tableModel.getValueAt(row, 4).toString();
                        String loc     = tableModel.getValueAt(row, 5).toString();
                        String status  = tableModel.getValueAt(row, 6).toString();
                        openEditMatchPopup(matchId, p1, p2, date, time, loc, status);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(matchTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        loadMatches();
    }

    private void styleButton(JButton b){
        b.setFocusPainted(false);
        b.setBackground(new Color(66,133,244));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    private void styleCancelButton(JButton b){
        b.setFocusPainted(false);
        b.setBackground(new Color(220,53,69));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void loadMatches(){
        tableModel.setRowCount(0);
        try(Connection conn = DatabaseConnection.getConnection()){
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM matches");
            while(rs.next()){
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
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "Error loading matches: "+ex.getMessage());
        }
    }

    private void openCreateMatchPopup(){
        showMatchForm(null,"","", new java.util.Date(), new java.util.Date(), "", "scheduled", true);
    }

    private void openEditMatchPopup(int id, String p1, String p2, String date, String time, String loc, String status){
        try {
            java.util.Date d = java.sql.Date.valueOf(date);
            java.util.Date t = java.sql.Time.valueOf(time);
            showMatchForm(id, p1, p2, d, t, loc, status, false);
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this,"Invalid date/time format");
        }
    }

    private void showMatchForm(Integer id, String p1, String p2,
                               java.util.Date d, java.util.Date t,
                               String loc, String status, boolean isCreate){
        JDialog dlg = new JDialog(this, isCreate?"Create Match":"Edit Match", true);
        dlg.setSize(400,400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridLayout(9,2,10,10));
        dlg.getContentPane().setBackground(new Color(250,250,250));
        dlg.setResizable(false);

        JTextField f1 = new JTextField(p1);
        JTextField f2 = new JTextField(p2);
        JSpinner spDate = new JSpinner(new SpinnerDateModel(d,null,null,Calendar.DAY_OF_MONTH));
        spDate.setEditor(new JSpinner.DateEditor(spDate,"yyyy-MM-dd"));
        JSpinner spTime = new JSpinner(new SpinnerDateModel(t,null,null,Calendar.MINUTE));
        spTime.setEditor(new JSpinner.DateEditor(spTime,"HH:mm"));
        JTextField fLoc = new JTextField(loc);

        dlg.add(new JLabel("Player 1:")); dlg.add(f1);
        dlg.add(new JLabel("Player 2:")); dlg.add(f2);
        dlg.add(new JLabel("Date:"));     dlg.add(spDate);
        dlg.add(new JLabel("Time:"));     dlg.add(spTime);
        dlg.add(new JLabel("Location:")); dlg.add(fLoc);

        JButton cancel = new JButton("Cancel");
        JButton action = new JButton(isCreate?"Submit":"Update");
        JButton delete = new JButton("Delete Match");
        styleCancelButton(cancel);
        styleButton(action);
        styleCancelButton(delete);

        dlg.add(cancel); dlg.add(action);
        dlg.add(new JLabel()); dlg.add(delete);

        cancel.addActionListener(e -> dlg.dispose());
        action.addActionListener(e -> {
            String np1 = f1.getText().trim(), np2 = f2.getText().trim(), nloc = fLoc.getText().trim();
            java.sql.Date sd = new java.sql.Date(((java.util.Date)spDate.getValue()).getTime());
            java.sql.Time st = new java.sql.Time(((java.util.Date)spTime.getValue()).getTime());
            if(np1.isEmpty()||np2.isEmpty()||nloc.isEmpty()){
                JOptionPane.showMessageDialog(dlg,"Fill all fields");
                return;
            }
            try(Connection conn = DatabaseConnection.getConnection()){
                if(isCreate){
                    int newId = new Random().nextInt(1_000_000);
                    PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO matches(id,player1,player2,match_date,time,location,status) VALUES(?,?,?,?,?,?,?)");
                    ins.setInt(1,newId); ins.setString(2,np1); ins.setString(3,np2);
                    ins.setDate(4,sd); ins.setTime(5,st);
                    ins.setString(6,nloc); ins.setString(7,"scheduled");
                    ins.executeUpdate();
                    // initialize scores
                    PreparedStatement sci = conn.prepareStatement(
                        "INSERT INTO scores(match_id,player1_points,player2_points,player1_games,player2_games,player1_sets,player2_sets)"+
                        " VALUES(?, '0','0',0,0,0,0)");
                    sci.setInt(1,newId); sci.executeUpdate();
                    JOptionPane.showMessageDialog(dlg,"Match created: "+newId);
                } else {
                    PreparedStatement upd = conn.prepareStatement(
                        "UPDATE matches SET player1=?,player2=?,match_date=?,time=?,location=? WHERE id=?");
                    upd.setString(1,np1); upd.setString(2,np2);
                    upd.setDate(3,sd); upd.setTime(4,st);
                    upd.setString(5,nloc); upd.setInt(6,id);
                    upd.executeUpdate();
                    JOptionPane.showMessageDialog(dlg,"Match updated");
                }
                dlg.dispose();
                loadMatches();
            } catch(SQLException ex){
                JOptionPane.showMessageDialog(dlg,"DB error: "+ex.getMessage());
            }
        });

        delete.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(dlg,"Delete this match?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
                try(Connection conn = DatabaseConnection.getConnection()){
                    PreparedStatement del = conn.prepareStatement("DELETE FROM matches WHERE id=?");
                    del.setInt(1, id);
                    del.executeUpdate();
                    JOptionPane.showMessageDialog(dlg,"Match deleted");
                    dlg.dispose();
                    loadMatches();
                } catch(SQLException ex){
                    JOptionPane.showMessageDialog(dlg,"DB error: "+ex.getMessage());
                }
            }
        });

        dlg.setVisible(true);
    }
    private void openCreateUserPopup() {
        JDialog dlg = new JDialog(this, "Create User", true);
        dlg.setSize(350, 250);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridLayout(5, 2, 10, 10));
        dlg.getContentPane().setBackground(new Color(250, 250, 250));
    
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"admin", "referee"});
    
        dlg.add(new JLabel("Username:"));
        dlg.add(usernameField);
        dlg.add(new JLabel("Password:"));
        dlg.add(passwordField);
        dlg.add(new JLabel("Role:"));
        dlg.add(roleCombo);
    
        JButton cancel = new JButton("Cancel");
        JButton create = new JButton("Create");
        styleCancelButton(cancel);
        styleButton(create);
    
        dlg.add(cancel);
        dlg.add(create);
    
        cancel.addActionListener(e -> dlg.dispose());
        create.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleCombo.getSelectedItem();
    
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Username and password are required");
                return;
            }
    
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES (?, ?, ?)"
                );
                stmt.setString(1, username);
                stmt.setString(2, password); // In real applications, hash the password
                stmt.setString(3, role);
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(dlg, "User created successfully!");
                dlg.dispose();
            } catch (SQLException ex) {
                if (ex.getSQLState().equals("23505")) {
                    JOptionPane.showMessageDialog(dlg, "Username already exists!");
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage());
                }
            }
        });
    
        dlg.setVisible(true);
    }
    private void openUserManagementPopup() {
        JDialog dlg = new JDialog(this, "Manage Users", true);
        dlg.setSize(600, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getContentPane().setBackground(Color.WHITE);
    
        DefaultTableModel userModel = new DefaultTableModel(new String[]{"ID", "Username", "Role"}, 0);
        JTable userTable = new JTable(userModel);
        userTable.setRowHeight(25);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
    
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT id, username, role FROM users");
            while (rs.next()) {
                userModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage());
        }
    
        JButton deleteBtn = new JButton("❌ Delete Selected User");
        styleCancelButton(deleteBtn);
        deleteBtn.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                int userId = (int) userModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(dlg,
                    "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?");
                        stmt.setInt(1, userId);
                        stmt.executeUpdate();
                        userModel.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(dlg, "User deleted successfully.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dlg, "Error deleting user: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(dlg, "Please select a user to delete.");
            }
        });
    
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(deleteBtn);
    
        dlg.add(new JScrollPane(userTable), BorderLayout.CENTER);
        dlg.add(bottomPanel, BorderLayout.SOUTH);
    
        dlg.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}
