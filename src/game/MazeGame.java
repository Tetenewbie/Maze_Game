package game;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import launcher.Launcher;
import logic.GameModel;
import logic.Level;

/**
 * Main gameplay panel for the Maze/Hacking game.
 * Handles:
 * Rendering walls, fog of war, items, enemies, and playerr
 *  Player movement
 *  Timers for clock and enemy movement
 *   UI side panel (score/time/sound/restart)
 *   Image loading for sprites/items
 */
public class MazeGame extends JPanel implements KeyListener {

    // --- Layout and drawing constants ---
    private static final int SIDE_PANEL_WIDTH = 200;
    private static final int MARGIN = 4;
    private static final int CORNER_RADIUS = 12;
    private static final int ENEMY_SPEED_MS = 600;

    // colors of the game
    private static final Color WALL_COLOR = new Color(40, 40, 40);
    private static final Color FLOOR_COLOR = new Color(80, 80, 80);
    private static final Color EXIT_COLOR = new Color(255, 215, 0);
    private static final Color PLAYER_COLOR = new Color(0, 255, 0, 200);
    private static final Color VIRUS_COLOR = new Color(255, 0, 0, 180);
    private static final Color BITCOIN_COLOR = new Color(0, 0, 255, 180);

    /**
     * References to launcher and game logic
     */
    private final Launcher launcher;
    private final Level level;
    private final String playerName;
    private GameModel model;

    //  UI components 
    private JLabel timeLabel, scoreLabel, nameLabel;

    // Timers for game clock and enemy movement
    private final javax.swing.Timer gameClock;
    private final javax.swing.Timer enemyTimer;

    // Used for cheat code detection (GODMODE)
    private StringBuilder cheatBuffer = new StringBuilder();

    // Cell size for rendering
    private int cellSize = 0;

    // Sprites and textures
    private BufferedImage[][] playerSprites;
    private BufferedImage virusImage;
    private BufferedImage bitcoinImage;

    // Keeps track of player walking direction
    private boolean facingRight = true;

    /**
     * Constructor initializes the game panel:
     * loads sprites, sets up sidebar, attaches key listener, starts timers.
     */
    public MazeGame(Launcher launcher, Level level, String playerName) {
        this.launcher = launcher;
        this.level = level;
        this.playerName = playerName;
        this.model = new GameModel(level);

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        loadImages();
        initSidePanel();

        setFocusable(true);
        addKeyListener(this);

        // Clock updates once per second
        gameClock = new javax.swing.Timer(1000, e -> {
            model.updateTime();
            timeLabel.setText("Time: " + model.getTimeSeconds() + "s");
        });
        gameClock.start();

        // Enemies move periodically
        enemyTimer = new javax.swing.Timer(ENEMY_SPEED_MS, e -> {
            model.moveEnemies();
            checkGameStatus();
            repaint();
        });
        enemyTimer.start();
    }

    /**
     * Loads all image assets (sprites, enemies, items) using getResource(),
     * which works inside JAR files.
     */
    private void loadImages() {
        try {
            // Load robot sprite sheet
            URL sheetUrl = getClass().getResource("/resources/robot_sheet.png");
            if (sheetUrl != null) {
                BufferedImage sheet = ImageIO.read(sheetUrl);
                int cols = 4, rows = 4;
                int fw = sheet.getWidth() / cols;
                int fh = sheet.getHeight() / rows;

                playerSprites = new BufferedImage[rows][cols];
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < cols; c++)
                        playerSprites[r][c] = sheet.getSubimage(c * fw, r * fh, fw, fh);

            } else {
                System.err.println("Warnung: robot_sheet.png nicht gefunden!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            playerSprites = null;
        }

        // Load malware enemy image
        try {
            URL virusUrl = getClass().getResource("/resources/malware.png");
            if (virusUrl != null) virusImage = ImageIO.read(virusUrl);
        } catch (Exception e) {
            virusImage = null;
        }

        // Load Bitcoin image
        try {
            URL coinUrl = getClass().getResource("/resources/bitcoin.png");
            if (coinUrl != null) bitcoinImage = ImageIO.read(coinUrl);
        } catch (Exception e) {
            bitcoinImage = null;
        }
    }

    // INPUT
    /**
     * Handles keyboard movement + triggers re-render.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameClock.isRunning()) return;

        int key = e.getKeyCode();

        // Player movement handling
        if (key == KeyEvent.VK_LEFT) {
            model.movePlayer(-1, 0);
            facingRight = false;
        }
        else if (key == KeyEvent.VK_RIGHT) {
            model.movePlayer(1, 0);
            facingRight = true;
        }
        else if (key == KeyEvent.VK_UP) model.movePlayer(0, -1);
        else if (key == KeyEvent.VK_DOWN) model.movePlayer(0, 1);

        checkGameStatus();
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {
        // Track typed characters for cheat codes
        cheatBuffer.append(Character.toUpperCase(e.getKeyChar()));
        if (cheatBuffer.length() > 10) cheatBuffer.delete(0, 1);

        // Cheat: GODMODE
        if (cheatBuffer.toString().endsWith("GODMODE")) {
            model.toggleGodMode();
            if (model.isGodMode()) {
                nameLabel.setForeground(Color.YELLOW);
                nameLabel.setText("GOD MODE ACTIVE");
            } else {
                nameLabel.setForeground(Color.CYAN);
                nameLabel.setText("Hacker: " + playerName);
            }
            repaint();
        }
    }

    // Status check if complet or not

    /**
     * Updates score label and checks if the level is complete.
     */
    private void checkGameStatus() {
        scoreLabel.setText("Bitcoins: " + model.getScore());

        if (model.isLevelComplete()) {
            gameClock.stop();
            enemyTimer.stop();
            new VictoryPanel(launcher, playerName, level.getTitle(),
                    model.getScore(), model.getTimeSeconds());
        }
    }

    //
    //  RENDERING

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Playfield dimensions
        int panelWidth = getWidth() - SIDE_PANEL_WIDTH;
        int panelHeight = getHeight();

        int rows = model.getRows();
        int cols = model.getCols();
        if (rows == 0 || cols == 0) return;

        // Compute cell size based on window dimensions
        int cw = panelWidth / cols;
        int ch = panelHeight / rows;
        cellSize = Math.min(cw, ch);

        // Background gradient
        GradientPaint gp = new GradientPaint(0, 0, Color.DARK_GRAY, panelWidth, panelHeight, Color.BLACK);
        g2.setPaint(gp);
        g2.fillRect(0, 0, panelWidth, panelHeight);

        drawMaze(g2, rows, cols);
        drawItems(g2);
        drawPlayer(g2);

        // Special effects
        if (level == Level.HARD) drawFogOfWar(g2, panelWidth, panelHeight, rows, cols);

        // GODMODE highlight overlay
        if (model.isGodMode()) {
            g2.setColor(new Color(255, 215, 0, 50));
            g2.fillRect(0, 0, panelWidth, panelHeight);
        }
    }

    /**
     * Draws floor tiles and walls of the maze.
     */
    private void drawMaze(Graphics2D g2, int rows, int cols) {
        boolean[][] north = model.getNorthWalls();
        boolean[][] east = model.getEastWalls();

        // Draw each tile
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * cellSize;
                int y = i * cellSize;

                g2.setColor(FLOOR_COLOR);
                g2.fillRoundRect(x, y, cellSize, cellSize, CORNER_RADIUS, CORNER_RADIUS);

                g2.setColor(WALL_COLOR);
                g2.setStroke(new BasicStroke(MARGIN));

                // Maze wall logic
                if (north[i][j] && i > 0) g2.drawLine(x, y, x + cellSize, y);
                if (east[i][j] && j < cols - 1) g2.drawLine(x + cellSize, y, x + cellSize, y + cellSize);

                // Borders
                if (i == 0) g2.drawLine(x, y, x + cellSize, y);
                if (j == 0) g2.drawLine(x, y, x, y + cellSize);
                if (i == rows - 1) g2.drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                if (j == cols - 1) g2.drawLine(x + cellSize, y, x + cellSize, y + cellSize);
            }
        }

        // Draw exit tile
        g2.setColor(EXIT_COLOR);
        int is = cellSize - 2 * MARGIN;
        g2.fillOval(model.getExitX() * cellSize + MARGIN, model.getExitY() * cellSize + MARGIN, is, is);
    }

    /**
     * Draws Bitcoins  and Viruses on the map.
     */
    private void drawItems(Graphics2D g2) {
        int is = cellSize - 2 * MARGIN;
        boolean[][] visited = model.getVisitedTiles();

        // Bitcoins (only visible if visited)
        for (java.awt.Point p : model.getBitcoins()) {
            if (visited[p.y][p.x]) {
                int x = p.x * cellSize + MARGIN;
                int y = p.y * cellSize + MARGIN;
                if (bitcoinImage != null)
                    g2.drawImage(bitcoinImage, x, y, is, is, null);
                else {
                    g2.setColor(BITCOIN_COLOR);
                    g2.fillOval(x, y, is, is);
                }
            }
        }

        // Viruses (always visible)
        for (java.awt.Point p : model.getViruses()) {
            int x = p.x * cellSize + MARGIN;
            int y = p.y * cellSize + MARGIN;

            if (virusImage != null)
                g2.drawImage(virusImage, x, y, is, is, null);
            else {
                g2.setColor(VIRUS_COLOR);
                g2.fillOval(x, y, is, is);
            }
        }
    }

    /**
     * Draws the player using sprite sheet or fallback circle.
     */
    private void drawPlayer(Graphics2D g2) {
        int is = cellSize - 2 * MARGIN;
        int x = model.getPlayerX() * cellSize + MARGIN;
        int y = model.getPlayerY() * cellSize + MARGIN;

        if (playerSprites != null) {
            int frame = (model.getPlayerX() + model.getPlayerY()) % 2;
            BufferedImage sprite = playerSprites[1][frame];

            if (facingRight)
                g2.drawImage(sprite, x, y, is, is, null);
            else
                g2.drawImage(sprite, x + is, y, -is, is, null);

        } else {
            g2.setColor(PLAYER_COLOR);
            g2.fillOval(x, y, is, is);
        }
    }

    /**
     * Fog-of-war for HARD difficulty:
     * - Darkness everywhere except visited tiles
     * - Small radial light around player
     */
    private void drawFogOfWar(Graphics2D g2, int w, int h, int rows, int cols) {
        boolean[][] visited = model.getVisitedTiles();

        Area darkness = new Area(new Rectangle2D.Double(0, 0, w, h));

        // Cut holes for visited tiles
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (visited[i][j]) {
                    Rectangle2D tile = new Rectangle2D.Double(j * cellSize, i * cellSize, cellSize, cellSize);
                    darkness.subtract(new Area(tile));
                }
            }
        }

        // Apply dark overlay
        g2.setColor(new Color(0, 0, 0, 230));
        g2.fill(darkness);

        // Draw radial light around player
        float radius = cellSize * 3.0f;
        if (radius <= 0) radius = 10;

        Point2D center = new Point2D.Float(
                model.getPlayerX() * cellSize + cellSize / 2f,
                model.getPlayerY() * cellSize + cellSize / 2f
        );

        float[] dist = {0.0f, 1.0f};
        Color[] colors = { new Color(0,0,0,0), new Color(0,0,0,100) };

        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(p);
        g2.fillOval((int)(center.getX() - radius), (int)(center.getY() - radius), (int)(radius * 2), (int)(radius * 2));
    }

    // UI and side panel

    
    /**
     * Creates side UI panel with labels + buttons.
     */
    private void initSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, 0));
        sidePanel.setBackground(new Color(30, 30, 30));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        nameLabel = styleLabel(new JLabel("Hacker: " + playerName));
        scoreLabel = styleLabel(new JLabel("Bitcoins: " + model.getScore()));
        timeLabel = styleLabel(new JLabel("Time: 0s"));

        sidePanel.add(nameLabel);
        sidePanel.add(Box.createVerticalStrut(15));
        sidePanel.add(scoreLabel);
        sidePanel.add(Box.createVerticalStrut(15));
        sidePanel.add(timeLabel);
        sidePanel.add(Box.createVerticalStrut(30));

        // Sound toggle button
        boolean isMuted = game.SoundManager.getInstance().isMuted();
        JButton soundBtn = new JButton(isMuted ? "SOUND: OFF" : "SOUND: ON");
        styleButton(soundBtn, new Color(80, 80, 80));
        soundBtn.addActionListener(e -> {
            game.SoundManager.getInstance().toggleMute();
            soundBtn.setText(game.SoundManager.getInstance().isMuted() ? "SOUND: OFF" : "SOUND: ON");
            this.requestFocusInWindow();
        });
        sidePanel.add(soundBtn);
        sidePanel.add(Box.createVerticalStrut(10));

        // Restart system (reset level)
        JButton restart = new JButton("RESTART SYSTEM");
        styleButton(restart, new Color(200, 50, 50));
        restart.addActionListener(e -> {
            model.initLevel();
            scoreLabel.setText("Bitcoins: 0");

            if (!gameClock.isRunning()) {
                gameClock.start();
                enemyTimer.start();
            }

            repaint();
            this.requestFocusInWindow();
        });
        sidePanel.add(restart);
        sidePanel.add(Box.createVerticalStrut(10));

        // Quit to main menu
        JButton quit = new JButton("LOGOUT");
        styleButton(quit, new Color(80, 80, 80));
        quit.addActionListener(e -> {
            gameClock.stop();
            enemyTimer.stop();
            launcher.showWelcomePanel();
        });
        sidePanel.add(quit);

        add(sidePanel, BorderLayout.EAST);
    }

    // Styling helpers
    private JLabel styleLabel(JLabel l) {
        l.setForeground(Color.CYAN);
        l.setFont(new Font("Consolas", Font.BOLD, 16));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void styleButton(JButton b, Color bg) {
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Consolas", Font.BOLD, 14));
    }
}
