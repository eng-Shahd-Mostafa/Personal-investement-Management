package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import database.DatabaseConnection;

public class PortfolioPanel extends JPanel {
    private int userId;
    private JLabel totalAssetsLabel, totalValueLabel, halalRatioLabel;

    public PortfolioPanel(int userId) {
        this.userId = userId;
        System.out.println("Initializing PortfolioPanel for userId: " + userId);
        setupUI();
        loadData();
    }

    private void setupUI() {
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 248, 255));

        // 1. Summary Cards Panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        summaryPanel.setBackground(new Color(240, 248, 255));

        totalAssetsLabel = createSummaryCard("Total Assets", "0");
        totalValueLabel = createSummaryCard("Total Value", "$0.00");
        halalRatioLabel = createSummaryCard("Halal Ratio", "0%");

        summaryPanel.add(totalAssetsLabel);
        summaryPanel.add(totalValueLabel);
        summaryPanel.add(halalRatioLabel);

        add(summaryPanel, BorderLayout.NORTH);

        // 2. Buttons Panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(new Color(240, 248, 255));
        buttonsPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        // View Assets Button
        JButton viewBtn = createActionButton("View Assets", new Color(70, 130, 180));
        viewBtn.addActionListener(e -> {
            System.out.println("View Assets button clicked for userId: " + userId);
            try {
                new AssetView(userId).setVisible(true);
                System.out.println("AssetView opened successfully");
            } catch (Exception ex) {
                System.out.println("Error opening AssetView: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Error opening AssetView: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Add Asset Button
        JButton addBtn = createActionButton("Add Asset", new Color(56, 142, 60));
        addBtn.addActionListener(e -> {
            System.out.println("Add Asset button clicked for userId: " + userId);
            try {
                new AddAssetView(userId).setVisible(true);
                loadData();
                System.out.println("AddAssetView opened successfully");
            } catch (Exception ex) {
                System.out.println("Error opening AddAssetView: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Error opening AddAssetView: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Edit Asset Button
        JButton editBtn = createActionButton("Edit Asset", new Color(255, 165, 0));
        editBtn.addActionListener(e -> {
            System.out.println("Edit Asset button clicked for userId: " + userId);
            if (hasAssets()) {
                System.out.println("Assets found, attempting to open EditAssetView");
                try {
                    EditAssetView editView = new EditAssetView(userId, this);
                    editView.setVisible(true);
                    System.out.println("EditAssetView opened successfully");
                } catch (Exception ex) {
                    System.out.println("Error opening EditAssetView: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Error opening EditAssetView: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                System.out.println("No assets found for userId: " + userId);
                JOptionPane.showMessageDialog(this, "No assets available to edit. Please add assets first.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Delete Asset Button
        JButton deleteBtn = createActionButton("Delete Asset", new Color(220, 53, 69));
        deleteBtn.addActionListener(e -> {
            System.out.println("Delete Asset button clicked for userId: " + userId);
            if (hasAssets()) {
                System.out.println("Assets found, attempting to open DeleteAssetView");
                SwingUtilities.invokeLater(() -> {
                    try {
                        DeleteAssetView deleteView = new DeleteAssetView(userId, this);
                        deleteView.setVisible(true);
                        System.out.println("DeleteAssetView opened successfully");
                    } catch (Exception ex) {
                        System.out.println("Error opening DeleteAssetView: " + ex.getMessage());
                        JOptionPane.showMessageDialog(this, "Error opening DeleteAssetView: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                });
            } else {
                System.out.println("No assets found for userId: " + userId);
                JOptionPane.showMessageDialog(this, "No assets available to delete. Please add assets first.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonsPanel.add(viewBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(addBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(editBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(deleteBtn);

        add(buttonsPanel, BorderLayout.CENTER);
    }

    private JLabel createSummaryCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(100, 100, 100));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(new Color(50, 50, 50));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return new JLabel() {
            {
                setLayout(new BorderLayout());
                add(card, BorderLayout.CENTER);
            }
        };
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 45));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(brighterColor(bgColor, 0.1f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private Color brighterColor(Color color, float factor) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1], Math.min(1.0f, hsb[2] + factor));
    }

    public void loadData() {
        System.out.println("Loading portfolio data for userId: " + userId);
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) as total_assets, " +
                                "SUM(purchase_price * quantity) as total_value, " +
                                "SUM(CASE WHEN is_halal = 1 THEN purchase_price * quantity ELSE 0 END) as halal_value "
                                +
                                "FROM Assets WHERE user_id = ?")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int totalAssets = rs.getInt("total_assets");
                double totalValue = rs.getDouble("total_value");
                double halalValue = rs.getDouble("halal_value");
                double halalRatio = totalValue > 0 ? (halalValue / totalValue) * 100 : 0;

                DecimalFormat df = new DecimalFormat("#,##0.00");

                updateCard(totalAssetsLabel, String.valueOf(totalAssets));
                updateCard(totalValueLabel, "$" + df.format(totalValue));
                updateCard(halalRatioLabel, df.format(halalRatio) + "%");
                System.out.println("Portfolio data loaded: total_assets=" + totalAssets + ", total_value=$" +
                        df.format(totalValue));
            } else {
                System.out.println("No portfolio data found for userId: " + userId);
                updateCard(totalAssetsLabel, "0");
                updateCard(totalValueLabel, "$0.00");
                updateCard(halalRatioLabel, "0%");
            }
        } catch (SQLException ex) {
            System.out.println("Error loading portfolio data: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading portfolio data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateCard(JLabel card, String value) {
        JPanel innerPanel = (JPanel) ((JLabel) card.getComponent(0)).getComponent(0);
        JLabel valueLabel = (JLabel) innerPanel.getComponent(1);
        valueLabel.setText(value);
    }

    private boolean hasAssets() {
        System.out.println("Checking for assets for userId: " + userId);
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Assets WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Asset count: " + count);
                return count > 0;
            } else {
                System.out.println("No assets found for userId: " + userId);
                return false;
            }
        } catch (SQLException ex) {
            System.out.println("Error checking assets: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error checking assets: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JFrame frame = new JFrame("Portfolio Panel");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 600);
                frame.add(new PortfolioPanel(1));
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception e) {
                System.out.println("Error initializing PortfolioPanel: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error initializing PortfolioPanel: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
