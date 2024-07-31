import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javazoom.jl.player.advanced.AdvancedPlayer;

public class Gameplay extends JPanel implements KeyListener, ActionListener {
    private boolean play = false;
    private int score = 0;
    private int totalBricks = 35; // Increased number of bricks
    private Timer timer;
    private int delay = 1000 / 60; // 60 FPS
    private int timeLeft = 30; // Timer for 30 seconds
    private Timer countdownTimer; // Timer for countdown

    private int playerX = 350;
    private int ballposX = 150;
    private int ballposY = 450;
    private int ballXdir = -4; // Increased ball speed
    private int ballYdir = -4; // Increased ball speed

    private Mapgen map;
    private boolean moveRight = false;
    private boolean moveLeft = false;
    private String gamertag;
    private int paddleWidth = 80; // Smaller paddle width
    private MusicPlayer musicPlayer;

    public Gameplay(String gamertag) {
        this.gamertag = gamertag;
        map = new Mapgen(5, 7); // Increased number of rows and columns
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
        startCountdown();
        musicPlayer = new MusicPlayer();
    }

    public void startGame() {
        play = true;
        musicPlayer.play("resources/musicfile.mp3"); // Adjust the path as needed
        repaint();
    }

    public void startCountdown() {
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (play) {
                    timeLeft--;
                    if (timeLeft <= 0) {
                        play = false;
                        countdownTimer.stop();
                        showEndGamePopup("TIME'S UP! SCORE: " + score);
                        saveScore(gamertag, score); // Save the score to the database
                    }
                    repaint();
                }
            }
        });
        countdownTimer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); // Call the superclass's paint method
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

        // timer
        g.drawString("TIMER: " + timeLeft, 50, 30);

        // the paddle
        g.setColor(Color.green);
        ((Graphics2D) g).fill(new RoundRectangle2D.Double(playerX, 600, paddleWidth, 8, 10, 10)); // Rounded corners

        // the ball
        g.setColor(Color.yellow);
        g.fillOval(ballposX, ballposY, 20, 20);

        if (ballposY > 670) {
            play = false;
            ballXdir = 0;
            ballYdir = 0;
            countdownTimer.stop();
            showEndGamePopup("GAME OVER, SCORE: " + score);
            saveScore(gamertag, score); // Save the score to the database
        }

        g.dispose();
    }

    private void showEndGamePopup(String message) {
        JOptionPane.showOptionDialog(this,
                message,
                "Game Over",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[] { "MAIN MENU" },
                "MAIN MENU");
        musicPlayer.stop(); // Stop background music
        returnToMainMenu();
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
        timeLeft = 30; // Reset the timer
        startCountdown();
        musicPlayer.play("resources/musicfile.mp3"); // Adjust the path as needed
        repaint();
    }

    private void returnToMainMenu() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
            javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (play) {
            // Ball and paddle collision detection
            RoundRectangle2D paddle = new RoundRectangle2D.Double(playerX, 600, paddleWidth, 8, 10, 10);
            Rectangle ball = new Rectangle(ballposX, ballposY, 20, 20);

            if (paddle.intersects(ball)) {
                if (ballposX + 19 >= paddle.getX() && ballposX <= paddle.getX() + paddle.getWidth()) {
                    ballYdir = -ballYdir;
                } else if (ballposX + 19 <= paddle.getX() || ballposX >= paddle.getX() + paddle.getWidth()) {
                    ballXdir = -ballXdir;
                }
            }

            // Brick collision detection and response
            A: for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        int brickWidth = map.brickWidth;
                        int brickHeight = map.brickHeight;

                        Rectangle brickRect = new Rectangle(brickX, brickY, brickWidth, brickHeight);

                        if (ball.intersects(brickRect)) {
                            map.setBrickValue(0, i, j);
                            totalBricks--;
                            score += 5;

                            // Ball collision with brick
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

            // Ball movement
            ballposX += ballXdir;
            ballposY += ballYdir;

            // Ball and wall collision detection
            if (ballposX < 0 || ballposX > 780) {
                ballXdir = -ballXdir;
            }
            if (ballposY < 0) {
                ballYdir = -ballYdir;
            }

            // Player paddle movement
            if (moveRight) {
                if (playerX >= 740 - paddleWidth) {
                    playerX = 740 - paddleWidth;
                } else {
                    playerX += 10; // Increased paddle speed
                }
            }
            if (moveLeft) {
                if (playerX < 10) {
                    playerX = 10;
                } else {
                    playerX -= 10; // Increased paddle speed
                }
            }

            // Repaint the game elements
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
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/retro_smash", "root", "root");
                PreparedStatement psSelect = conn.prepareStatement("SELECT score FROM leaderboard WHERE gamertag = ?");
                PreparedStatement psUpdate = conn
                        .prepareStatement("UPDATE leaderboard SET score = ? WHERE gamertag = ?");
                PreparedStatement psInsert = conn
                        .prepareStatement("INSERT INTO leaderboard (gamertag, score) VALUES (?, ?)")) {

            psSelect.setString(1, gamertag);
            ResultSet rs = psSelect.executeQuery();

            if (rs.next()) {
                // Update existing record
                psUpdate.setInt(1, score);
                psUpdate.setString(2, gamertag);
                psUpdate.executeUpdate();
            } else {
                // Insert new record
                psInsert.setString(1, gamertag);
                psInsert.setInt(2, score);
                psInsert.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class MusicPlayer {
        private AdvancedPlayer player;
        private java.io.InputStream inputStream;

        public void play(String filePath) {
            System.out.println("Attempting to play file at: " + filePath);
            new Thread(() -> {
                try {
                    inputStream = new java.io.FileInputStream(filePath);
                    player = new AdvancedPlayer(inputStream);
                    player.play();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        public void stop() {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
