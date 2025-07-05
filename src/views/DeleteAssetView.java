package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import database.DatabaseConnection;

public class DeleteAssetView extends JFrame {
    private int userId, assetId;
    private PortfolioPanel portfolioPanel;
    private JLabel assetLabel;
    private JComboBox<String> assetCombo;
    private String selectedType; // لتخزين نوع الأصل المختار

    public DeleteAssetView(int userId, PortfolioPanel portfolioPanel, int assetId) {
        this.userId = userId;
        this.portfolioPanel = portfolioPanel;
        this.assetId = assetId;
        System.out.println("DeleteAssetView constructor called with userId: " + userId + ", assetId: " + assetId);
        setupUI();
        if (assetId != -1) {
            System.out.println("Loading asset details for assetId: " + assetId);
            loadAssetDetails();
        } else {
            System.out.println("Loading asset types for userId: " + userId);
            loadAssetTypes(); // تحميل أنواع الأصول بدلاً من الأصول الفردية
        }
    }

    public DeleteAssetView(int userId, PortfolioPanel portfolioPanel) {
        this(userId, portfolioPanel, -1);
    }

    private void setupUI() {
        System.out.println("Setting up UI for DeleteAssetView");
        setTitle("Delete Asset");
        setSize(500, 300); // زيادة الحجم لضمان العرض
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 247));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(198, 40, 40));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("Delete Asset", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 245, 247));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Asset Information
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // إضافة مسافات
        infoPanel.setBackground(new Color(245, 245, 247));
        System.out.println("Creating infoPanel with FlowLayout");

        if (assetId == -1) {
            System.out.println("assetId == -1, setting up ComboBox for asset types");
            JLabel comboLabel = new JLabel("Select Asset Type:");
            comboLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            comboLabel.setForeground(new Color(44, 44, 46));
            assetCombo = new JComboBox<>();
            assetCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            assetCombo.setBackground(Color.WHITE);
            assetCombo.setForeground(new Color(44, 44, 46));
            assetCombo.setPreferredSize(new Dimension(250, 50)); // زيادة الحجم
            styleComboBox(assetCombo);
            assetCombo.addActionListener(e -> {
                selectedType = (String) assetCombo.getSelectedItem();
                System.out.println("Selected asset type: " + selectedType);
            });
            infoPanel.add(comboLabel);
            infoPanel.add(assetCombo);
            System.out.println("ComboBox added to infoPanel with " + assetCombo.getItemCount() + " items");
        } else {
            System.out.println("assetId != -1, setting up label for individual asset");
            JLabel label = new JLabel("Asset to delete: ");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(new Color(44, 44, 46));
            assetLabel = new JLabel("Loading...");
            assetLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            assetLabel.setForeground(new Color(44, 44, 46));
            infoPanel.add(label);
            infoPanel.add(assetLabel);
            System.out.println("Label added to infoPanel");
        }

        mainPanel.add(infoPanel, BorderLayout.CENTER);
        System.out.println("infoPanel added to mainPanel");

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 245, 247));

        JButton deleteButton = new JButton("Delete");
        styleButton(deleteButton);
        deleteButton.addActionListener(e -> deleteAsset(e));

        JButton cancelButton = new JButton("Cancel");
        styleCancelButton(cancelButton);
        cancelButton.addActionListener(e -> {
            System.out.println("Cancel button clicked, closing DeleteAssetView");
            dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(deleteButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        System.out.println("UI setup completed for DeleteAssetView");
    }

    private void styleComboBox(JComboBox<String> combo) {
        System.out.println("Styling ComboBox");
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                if (isSelected) {
                    setBackground(new Color(198, 40, 40));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(44, 44, 46));
                }
                return this;
            }
        });
        System.out.println("ComboBox styled successfully");
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(198, 40, 40));
        button.setForeground(Color.black);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(178, 36, 36));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(198, 40, 40));
            }
        });
    }

    private void styleCancelButton(JButton button) {
        button.setBackground(new Color(200, 200, 200));
        button.setForeground(new Color(44, 44, 46));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(180, 180, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(200, 200, 200));
            }
        });
    }

    private void loadAssetDetails() {
        if (assetId != -1) {
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn
                            .prepareStatement("SELECT name FROM Assets WHERE id = ? AND user_id = ?")) {
                stmt.setInt(1, assetId);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    assetLabel.setText(rs.getString("name"));
                    System.out.println("Asset details loaded: name=" + rs.getString("name"));
                } else {
                    System.out.println("Asset not found for assetId: " + assetId);
                    JOptionPane.showMessageDialog(this, "Asset not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            } catch (SQLException ex) {
                System.out.println("Error loading asset: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Error loading asset: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                dispose();
            }
        }
    }

    private void loadAssetTypes() {
        System.out.println("Loading asset types for userId: " + userId);
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT type FROM Assets WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            assetCombo.removeAllItems();
            boolean hasTypes = false;
            while (rs.next()) {
                String type = rs.getString("type");
                if (type != null && !type.trim().isEmpty()) {
                    assetCombo.addItem(type);
                    hasTypes = true;
                    System.out.println("Loaded asset type: " + type);
                }
            }
            if (!hasTypes) {
                System.out.println("No asset types found for userId: " + userId);
                JOptionPane.showMessageDialog(this, "No asset types found for this user.", "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else if (assetCombo.getItemCount() > 0) {
                assetCombo.setSelectedIndex(0);
                selectedType = (String) assetCombo.getSelectedItem();
                System.out.println("Asset types loaded successfully, count: " + assetCombo.getItemCount());
            }
        } catch (SQLException ex) {
            System.out.println("Error loading asset types: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading asset types: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            dispose();
        }
    }

    private void deleteAsset(ActionEvent e) {
        System.out.println("Delete button clicked");
        if (assetId != -1) {
            // حذف أصل فردي (من AssetView)
            System.out.println("Deleting individual asset with assetId: " + assetId);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this asset?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement stmt = conn
                                .prepareStatement("DELETE FROM Assets WHERE user_id = ? AND id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, assetId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Asset deleted successfully for assetId: " + assetId);
                        JOptionPane.showMessageDialog(this, "Asset deleted successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        if (portfolioPanel != null) {
                            portfolioPanel.loadData();
                        }
                        dispose();
                    } else {
                        System.out.println("Failed to delete asset for assetId: " + assetId);
                        JOptionPane.showMessageDialog(this, "Failed to delete asset.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    System.out.println("Database error while deleting asset: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        } else {
            // حذف جميع الأصول من نوع معين (من PortfolioPanel)
            if (selectedType == null || selectedType.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a valid asset type.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            System.out.println("Deleting all assets of type: " + selectedType);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete all assets of type " + selectedType + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement stmt = conn
                                .prepareStatement("DELETE FROM Assets WHERE user_id = ? AND type = ?")) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, selectedType);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("All assets of type " + selectedType
                                + " deleted successfully, rows affected: " + rowsAffected);
                        JOptionPane.showMessageDialog(this,
                                "All assets of type " + selectedType + " deleted successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        if (portfolioPanel != null) {
                            portfolioPanel.loadData();
                        }
                        dispose();
                    } else {
                        System.out.println("No assets found for type: " + selectedType);
                        JOptionPane.showMessageDialog(this, "No assets found for type: " + selectedType, "Information",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    System.out.println(
                            "Database error while deleting assets of type " + selectedType + ": " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new DeleteAssetView(1, null, 1).setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
