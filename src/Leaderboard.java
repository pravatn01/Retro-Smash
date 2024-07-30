import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Leaderboard {
    public static void showLeaderboard() {
        JFrame frame = new JFrame("Leaderboard");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/retro_smash", "root",
                "root");
                Statement stmt = conn.createStatement()) {

            String query = "SELECT gamertag, score FROM leaderboard ORDER BY score DESC LIMIT 10";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String entry = rs.getString("gamertag") + " - " + rs.getInt("score");
                listModel.addElement(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        frame.add(new JScrollPane(list));
        frame.setVisible(true);
    }
}
