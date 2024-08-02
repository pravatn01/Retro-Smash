import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Leaderboard {
    public static void showLeaderboard() {
        JFrame frame = new JFrame("Leaderboard");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        String[] columnNames = { "Gamertag", "Score" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/retro_smash", "root", "root");
                Statement stmt = conn.createStatement()) {

            String query = "SELECT gamertag, score FROM leaderboard ORDER BY score DESC LIMIT 10";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String gamertag = rs.getString("gamertag");
                int score = rs.getInt("score");
                tableModel.addRow(new Object[] { gamertag, score });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Leaderboard::showLeaderboard);
    }
}
