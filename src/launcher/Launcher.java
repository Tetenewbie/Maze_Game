package launcher;

import game.MazeGame;
import game.SoundManager;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import logic.Level;

public class Launcher {

    // Main application window
    public JFrame frame;

    // Card layout to switch between welcome screen and game screen
    private JPanel cards;

    private static final String WELCOME = "WELCOME";
    private static final String GAME = "GAME";

    private static final int FRAME_SIZE = 900;

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 64);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 36);

    private String playerNickname;
    private Image welcomeImage;   // background image for the welcome screen

    // UI elements we need to show / hide depending on the step
    private JButton startButton;
    private JPanel nameInputPanel;
    private JLabel chooseLabel;
    private JPanel levelPanel;
    private JTextField nicknameField;

    public Launcher() {

        // Create the main window
        frame = new JFrame("Maze Adventure - Virenjagd");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(FRAME_SIZE, FRAME_SIZE);
        frame.setMinimumSize(new Dimension(800, 800));
        frame.setLocationRelativeTo(null);

        //  Load welcome background image 
        try {
            URL imgUrl = getClass().getResource("/resources/welcome.jpg");
            if (imgUrl != null) {
                welcomeImage = ImageIO.read(imgUrl);
            } else {
                System.err.println("Warnung: welcome.jpg nicht gefunden!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start background music
        try {
            SoundManager.getInstance().playMusic("/resources/robot_radio.wav");
        } catch (Exception e) {
            System.err.println("Music failed to load: " + e.getMessage());
        }

        // CardLayout allows switching UI screens
        cards = new JPanel(new CardLayout());
        cards.add(createWelcomePanel(), WELCOME);

        frame.add(cards);
        frame.setVisible(true);
    }

    /**
     * Returns to the welcome screen.
     * Resets everything so the player can start again.
     */
    public void showWelcomePanel() {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, WELCOME);

        if (cards.getComponentCount() > 1) {
            cards.remove(1); // remove game panel
        }

        if (startButton != null) startButton.setVisible(true);
        if (nameInputPanel != null) nameInputPanel.setVisible(false);
        if (chooseLabel != null) chooseLabel.setVisible(false);
        if (levelPanel != null) levelPanel.setVisible(false);
        if (nicknameField != null) nicknameField.setText("");

        playerNickname = null;
    }

    /**
     * Creates the first screen the player sees:
     * - background image
     * - start button
     * - nickname input (hidden at first)
     * - difficulty selection (hidden at first)
     */
    private JPanel createWelcomePanel() {

        // Panel with custom painting for the background image
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (welcomeImage != null) {
                    g.drawImage(welcomeImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        // Center content container
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalStrut(250));

        // Start button
        startButton = createStyledButton("START", new Color(0, 153, 255));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(startButton);
        centerPanel.add(Box.createVerticalStrut(40));

        // Nickname input section, initially hidden
        nameInputPanel = createNameInputPanel();
        nameInputPanel.setVisible(false);
        centerPanel.add(nameInputPanel);
        centerPanel.add(Box.createVerticalStrut(40));

        // Difficulty text + difficulty buttons
        chooseLabel = new JLabel("Choose Difficulty Level:");
        chooseLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        chooseLabel.setForeground(Color.WHITE);
        chooseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chooseLabel.setOpaque(true);
        chooseLabel.setBackground(new Color(0, 0, 0, 150));
        chooseLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        levelPanel = createLevelSelectionPanel();
        chooseLabel.setVisible(false);
        levelPanel.setVisible(false);

        centerPanel.add(chooseLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(levelPanel);

        // When clicking START → show nickname input
        startButton.addActionListener(e -> {
            startButton.setVisible(false);
            nameInputPanel.setVisible(true);
            frame.revalidate();
            frame.repaint();
            nicknameField.requestFocusInWindow();
        });

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the panel where the player types their nickname.
     */
    private JPanel createNameInputPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel prompt = new JLabel("Enter your nickname:");
        prompt.setFont(new Font("SansSerif", Font.BOLD, 20));
        prompt.setForeground(Color.WHITE);
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        prompt.setOpaque(true);
        prompt.setBackground(new Color(0, 0, 0, 150));
        prompt.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Text field for entering name
        nicknameField = new JTextField(15);
        nicknameField.setMaximumSize(new Dimension(200, 30));
        nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button to confirm nickname
        JButton confirmButton = createStyledButton("Confirm", new Color(50, 205, 50));
        confirmButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        confirmButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // What happens when confirming nickname
        ActionListener confirmAction = e -> {
            String input = nicknameField.getText().trim();
            if (!input.isEmpty()) {
                playerNickname = input;
                nameInputPanel.setVisible(false);
                chooseLabel.setVisible(true);
                levelPanel.setVisible(true);
                frame.revalidate();
                frame.repaint();
            } else {
                JOptionPane.showMessageDialog(frame, "Nickname cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        confirmButton.addActionListener(confirmAction);
        nicknameField.addActionListener(confirmAction);

        p.add(prompt);
        p.add(Box.createVerticalStrut(10));
        p.add(nicknameField);
        p.add(Box.createVerticalStrut(15));
        p.add(confirmButton);

        return p;
    }

    /**
     * Difficulty selection buttons (Easy / Medium / Hard()
     */
    private JPanel createLevelSelectionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        p.setOpaque(false);

        JButton easy = createLevelButton(Level.EASY, new Color(76, 175, 80));
        JButton medium = createLevelButton(Level.MEDIUM, new Color(255, 193, 7));
        JButton hard = createLevelButton(Level.HARD, new Color(244, 67, 54));

        p.add(easy);
        p.add(medium);
        p.add(hard);

        return p;
    }

    /** Creates a general styled button */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(15, 40, 15, 40)
        ));
        return button;
    }

    /** Creates a difficulty button */
    private JButton createLevelButton(Level level, Color color) {
        JButton button = new JButton(level.getTitle());
        button.setFont(new Font("SansSerif", Font.BOLD, 20));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));

        button.addActionListener(e -> confirmAndStart(level));

        return button;
    }

    /**
     * Shows a confirmation popup before starting the game.
     */
    private void confirmAndStart(Level level) {
        int result = JOptionPane.showConfirmDialog(frame,
                "Start " + level.getTitle() + " level as " + playerNickname + "?",
                "Confirm Level",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            startGame(level, playerNickname);
        }
    }

    /**
     * Actually starts the MazeGame by replacing the welcome screen with the game panel.
     */
    private void startGame(Level level, String nickname) {
        MazeGame gamePanel = new MazeGame(this, level, nickname);

        // Remove old game panel (if any)
        if (cards.getComponentCount() > 1) {
            cards.remove(1);
        }

        cards.add(gamePanel, GAME);
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, GAME);

        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Launcher::new);
    }
}


