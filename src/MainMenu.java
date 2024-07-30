import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame implements ActionListener {
    private JButton playButton;
    private JButton leaderboardButton;

    public MainMenu() {
        setTitle("RETRO SMASH");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("RETRO SMASH", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Jetbrains Mono", Font.BOLD, 50));
        titleLabel.setBounds(150, 200, 500, 100);
        panel.add(titleLabel);

        playButton = new JButton("PLAY");
        playButton.setFont(new Font("Jetbrains Mono", Font.BOLD, 30));
        playButton.setBounds(300, 350, 200, 50);
        playButton.addActionListener(this);
        panel.add(playButton);

        leaderboardButton = new JButton("LEADERBOARD");
        leaderboardButton.setFont(new Font("Jetbrains Mono", Font.BOLD, 30));
        leaderboardButton.setBounds(250, 450, 300, 50);
        leaderboardButton.addActionListener(this);
        panel.add(leaderboardButton);

        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == playButton) {
            String gamertag = JOptionPane.showInputDialog(this, "Enter your gamertag:");
            if (gamertag != null && !gamertag.trim().isEmpty()) {
                Gameplay gamePlay = new Gameplay(gamertag);
                JFrame gameFrame = new JFrame();
                gameFrame.setBounds(10, 10, 814, 700);
                gameFrame.setTitle("RETRO SMASH");
                gameFrame.setResizable(false);
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gameFrame.add(gamePlay);
                gameFrame.setVisible(true);
                gamePlay.startGame();
                dispose();
            }
        } else if (e.getSource() == leaderboardButton) {
            Leaderboard.showLeaderboard();
        }
    }

    public static void main(String[] args) {
        new MainMenu();
    }
}
