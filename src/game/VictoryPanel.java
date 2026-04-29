package game;

import java.awt.*;
import javax.swing.*;
import launcher.Launcher;

public class VictoryPanel extends JDialog {

    public VictoryPanel(Launcher launcher, String playerName, String levelTitle, int score, int timeSeconds) {
        // Set the dialog to appear over the game window
        super(launcher.frame, "Victory!", true);

        // Basic window settings
        setSize(500, 350);
        setLocationRelativeTo(launcher.frame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 30)); // Dark background

        // Title at the top 
        JLabel title = new JLabel("LEVEL COMPLETE!", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(new Color(0, 255, 0)); // Green text
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Stats panel in the center
        JPanel statsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));

        // Helper font
        Font labelFont = new Font("SansSerif", Font.PLAIN, 18);
        Font valueFont = new Font("SansSerif", Font.BOLD, 20);

        statsPanel.add(createStatRow("Player:", playerName, labelFont, valueFont, Color.CYAN));
        statsPanel.add(createStatRow("Difficulty:", levelTitle, labelFont, valueFont, Color.ORANGE));
        statsPanel.add(createStatRow("Score:", String.valueOf(score), labelFont, valueFont, Color.YELLOW));
        statsPanel.add(createStatRow("Time Taken:", timeSeconds + " seconds", labelFont, valueFont, Color.WHITE));

        add(statsPanel, BorderLayout.CENTER);

        // Button panel at the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JButton restartButton = new JButton("START NEW GAME");
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        restartButton.setBackground(new Color(0, 153, 255));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.setPreferredSize(new Dimension(250, 50));

        restartButton.addActionListener(e -> {
            dispose(); // Close this popup
            launcher.showWelcomePanel(); // Go back to start screen
        });

        buttonPanel.add(restartButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Show the window
        setVisible(true);
    }

    // Helper to create a nice row with Label on left and Value on right
    private JPanel createStatRow(String labelText, String valueText, Font lblFont, Font valFont, Color valColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(lblFont);
        lbl.setForeground(Color.LIGHT_GRAY);

        JLabel val = new JLabel(valueText, SwingConstants.RIGHT);
        val.setFont(valFont);
        val.setForeground(valColor);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);

        // Add a dotted line or separator at the bottom of the row
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 80, 80)));

        return row;
    }
}