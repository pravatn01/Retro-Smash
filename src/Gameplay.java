import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Gameplay extends JPanel implements KeyListener, ActionListener {
    private boolean play = false;
    private int score = 0;
    private int totalBricks = 35; // Increased number of bricks
    private Timer timer;
    private int delay = 1000 / 60; // 60 FPS

    private int playerX = 350;
    private int ballposX = 150;
    private int ballposY = 450;
    private int ballXdir = -5; // Increased ball speed
    private int ballYdir = -6; // Increased ball speed

    private Mapgen map;
    private boolean moveRight = false;
    private boolean moveLeft = false;
    private String gamertag;

    public Gameplay(String gamertag) {
        this.gamertag = gamertag;
        map = new Mapgen(5, 7); // Increased number of rows and columns
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    public void startGame() {
        play = true;
        repaint();
    }

    public void paint(Graphics g) {
        // background
        g.setColor(Color.black);
        g.fillRect(1, 1, 800, 700);

        // drawing map
        map.draw((Graphics2D) g);

        // borders
        g.setColor(Color.yellow);
        g.fillRect(0, 0, 800, 3);
        g.fillRect(0, 0, 3, 700);
        g.fillRect(797, 0, 3, 700);

        // scores
        g.setColor(Color.white);
        g.setFont(new Font("Jetbrains Mono", Font.BOLD, 25));
        g.drawString("SCORE: " + score, 650, 30);

        // the paddle
        g.setColor(Color.green);
        g.fillRect(playerX, 600, 100, 8);

        // the ball
        g.setColor(Color.yellow);
        g.fillOval(ballposX, ballposY, 20, 20);

        if (totalBricks <= 0) {
            play = false;
            ballXdir = 0;
            ballYdir = 0;
            showEndGamePopup("YOU WON!");
            saveScore(gamertag, score); // Save the score to the database
        }

        if (ballposY > 670) {
            play = false;
            ballXdir = 0;
            ballYdir = 0;
            showEndGamePopup("GAME OVER, SCORE: " + score);
            saveScore(gamertag, score); // Save the score to the database
        }

        g.dispose();
    }

    private void showEndGamePopup(String message) {
        int option = JOptionPane.showOptionDialog(this,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[] { "REPLAY", "MAIN MENU" },
                "REPLAY");

        if (option == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            returnToMainMenu();
        }
    }

    private void restartGame() {
        play = true;
        ballposX = 150;
        ballposY = 450;
        ballXdir = -5;
        ballYdir = -6;
        playerX = 350;
        score = 0;
        totalBricks = 35; // Reset number of bricks
        map = new Mapgen(5, 7); // Reset the map

        repaint();
    }

    private void returnToMainMenu() {
        // Close the game window and show the main menu
        javax.swing.SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
            javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();

        if (play) {
            if (new Rectangle(ballposX, ballposY, 20, 20).intersects(new Rectangle(playerX, 600, 100, 8))) {
                ballYdir = -ballYdir;
            }

            A: for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        int brickWidth = map.brickWidth;
                        int brickHeight = map.brickHeight;

                        Rectangle rect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
                        Rectangle ballRect = new Rectangle(ballposX, ballposY, 20, 20);
                        Rectangle brickRect = rect;

                        if (ballRect.intersects(brickRect)) {
                            map.setBrickValue(0, i, j);
                            totalBricks--;
                            score += 5;

                            if (ballposX + 19 <= brickRect.x || ballposX + 1 >= brickRect.x + brickRect.width) {
                                ballXdir = -ballXdir;
                            } else {
                                ballYdir = -ballYdir;
                            }

                            break A;
                        }
                    }
                }
            }

            ballposX += ballXdir;
            ballposY += ballYdir;
            if (ballposX < 0) {
                ballXdir = -ballXdir;
            }
            if (ballposY < 0) {
                ballYdir = -ballYdir;
            }
            if (ballposX > 780) {
                ballXdir = -ballXdir;
            }

            if (moveRight) {
                if (playerX >= 700) {
                    playerX = 700;
                } else {
                    playerX += 15; // Increased paddle speed
                }
            }
            if (moveLeft) {
                if (playerX < 10) {
                    playerX = 10;
                } else {
                    playerX -= 15; // Increased paddle speed
                }
            }

            repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!play) {
                restartGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft = false;
        }
    }

    private void saveScore(String gamertag, int score) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/retro_smash", "root",
                "root");
                PreparedStatement ps = conn
                        .prepareStatement("INSERT INTO leaderboard (gamertag, score) VALUES (?, ?)")) {
            ps.setString(1, gamertag);
            ps.setInt(2, score);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
