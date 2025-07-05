package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import database.DatabaseConnection;

public class EditAssetView extends JFrame {
    private JTextField nameField, valueField;
    private JComboBox<String> typeComboBox, assetCombo; // أضفنا assetCombo للسماح باختيار الأصل
    private JButton updateButton;
    private int userId, assetId;
    private PortfolioPanel portfolioPanel;

    // البنائي الجديد مع assetId
    public EditAssetView(int userId, PortfolioPanel portfolioPanel, int assetId) {
        this.userId = userId;
        this.portfolioPanel = portfolioPanel;
        this.assetId = assetId;
        System.out.println("Initializing EditAssetView for userId: " + userId + ", assetId: " + assetId);
        setupUI();
        if (assetId != -1) {
            loadAssetDetails(); // تحميل تفاصيل الأصل تلقائيًا إذا كان assetId موجودًا
        } else {
            loadAssets(); // تحميل قائمة الأصول إذا لم يكن هناك assetId
        }
    }

    // البنائي البديل (بدون assetId) لدعم الاستدعاءات القديمة
    public EditAssetView(int userId, PortfolioPanel portfolioPanel) {
        this(userId, portfolioPanel, -1); // يستدعي البنائي الرئيسي مع assetId افتراضي
    }

    private void setupUI() {
        setTitle("Edit Asset");
        setSize(500, 670);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 248, 255));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(10, 132, 255));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("Edit Asset");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Asset Selection (يظهر فقط إذا لم يكن assetId محدد)
        if (assetId == -1) {
            gbc.gridx = 0;
            gbc.gridy = 0;
            JLabel comboLabel = new JLabel("Select Asset:");
            comboLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            comboLabel.setForeground(new Color(44, 44, 46));
            mainPanel.add(comboLabel, gbc);

            gbc.gridy = 1;
            assetCombo = new JComboBox<>();
            assetCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            assetCombo.setBackground(Color.WHITE);
            assetCombo.setForeground(new Color(44, 44, 46));
            styleComboBox(assetCombo);
            assetCombo.addActionListener(e -> {
                if (assetCombo.getSelectedItem() != null) {
                    loadAssetDetails((String) assetCombo.getSelectedItem());
                }
            });
            mainPanel.add(assetCombo, gbc);
        }

        // Asset Name
        gbc.gridy = assetId == -1 ? 2 : 0;
        JLabel nameLabel = new JLabel("Asset Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(44, 44, 46));
        mainPanel.add(nameLabel, gbc);

        gbc.gridy = assetId == -1 ? 3 : 1;
        nameField = createTextField();
        mainPanel.add(nameField, gbc);

        // Value (L.E)
        gbc.gridy = assetId == -1 ? 4 : 2;
        JLabel valueLabel = new JLabel("Value (L.E):");
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(44, 44, 46));
        mainPanel.add(valueLabel, gbc);

        gbc.gridy = assetId == -1 ? 5 : 3;
        valueField = createTextField();
        mainPanel.add(valueField, gbc);

        // Type
        gbc.gridy = assetId == -1 ? 6 : 4;
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeLabel.setForeground(new Color(44, 44, 46));
        mainPanel.add(typeLabel, gbc);

        gbc.gridy = assetId == -1 ? 7 : 5;
        typeComboBox = new JComboBox<>();
        typeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeComboBox.setBackground(Color.WHITE);
        typeComboBox.setForeground(new Color(44, 44, 46));
        loadAssetTypes();
        styleComboBox(typeComboBox);
        mainPanel.add(typeComboBox, gbc);

        // Button Panel
        gbc.gridy = assetId == -1 ? 8 : 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(240, 248, 255));

        updateButton = new JButton("Update");
        styleButton(updateButton);
        updateButton.addActionListener(this::updateAsset);

        JButton cancelButton = new JButton("Cancel");
        styleCancelButton(cancelButton);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(updateButton);
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(44, 44, 46));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        return field;
    }

    private void styleComboBox(JComboBox<String> combo) {
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
                    setBackground(new Color(10, 132, 255));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(44, 44, 46));
                }
                return this;
            }
        });
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(10, 132, 255));
        button.setForeground(Color.black);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(9, 113, 217));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(10, 132, 255));
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
        System.out.println("Loading details for assetId: " + assetId);
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT name, purchase_price, type FROM Assets WHERE id = ? AND user_id = ?")) {
            stmt.setInt(1, assetId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                valueField.setText(String.valueOf(rs.getDouble("purchase_price")));
                typeComboBox.setSelectedItem(rs.getString("type"));
                System.out.println("Asset details loaded: id=" + assetId);
            } else {
                System.out.println("No details found for asset id: " + assetId);
                JOptionPane.showMessageDialog(this, "Asset not found.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        } catch (SQLException ex) {
            System.out.println("Error loading asset details: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading asset details: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            dispose();
        }
    }

    private void loadAssets() {
        System.out.println("Loading assets for userId: " + userId);
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM Assets WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            assetCombo.removeAllItems();
            boolean hasAssets = false;
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                assetCombo.addItem(name + " (ID: " + id + ")"); // عرض الاسم مع الـ ID
                hasAssets = true;
                System.out.println("Asset loaded: id=" + id + ", name=" + name);
            }
            if (!hasAssets) {
                System.out.println("No assets found for userId: " + userId);
                JOptionPane.showMessageDialog(this, "No assets found for this user. Please add assets first.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                updateButton.setEnabled(false);
            } else {
                updateButton.setEnabled(true);
                if (assetCombo.getItemCount() > 0) {
                    assetCombo.setSelectedIndex(0);
                    loadAssetDetails(assetCombo.getSelectedItem().toString());
                }
                System.out.println("Assets loaded successfully, count: " + assetCombo.getItemCount());
            }
        } catch (SQLException ex) {
            System.out.println("Error loading assets: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading assets: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            updateButton.setEnabled(false);
        }
    }

    private void loadAssetDetails(String assetDisplayName) {
        System.out.println("Loading details for asset: " + assetDisplayName);
        int idStart = assetDisplayName.indexOf("(ID: ") + 5;
        int idEnd = assetDisplayName.indexOf(")");
        if (idStart > 0 && idEnd > idStart) {
            String idStr = assetDisplayName.substring(idStart, idEnd);
            try {
                assetId = Integer.parseInt(idStr);
                try (Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(
                                "SELECT name, purchase_price, type FROM Assets WHERE id = ? AND user_id = ?")) {
                    stmt.setInt(1, assetId);
                    stmt.setInt(2, userId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        nameField.setText(rs.getString("name"));
                        valueField.setText(String.valueOf(rs.getDouble("purchase_price")));
                        typeComboBox.setSelectedItem(rs.getString("type"));
                        System.out.println("Asset details loaded: id=" + assetId);
                    } else {
                        System.out.println("No details found for asset id: " + assetId);
                        clearFields();
                    }
                }
            } catch (NumberFormatException | SQLException ex) {
                System.out.println("Error parsing asset ID or loading details: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Error loading asset details: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                clearFields();
            }
        } else {
            System.out.println("Invalid asset display name format: " + assetDisplayName);
            clearFields();
        }
    }

    private void clearFields() {
        nameField.setText("");
        valueField.setText("");
        typeComboBox.setSelectedIndex(0);
        assetId = -1;
    }

    private void loadAssetTypes() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT DISTINCT type FROM Assets")) {

            typeComboBox.addItem(""); // خيار فارغ كافتراضي
            while (rs.next()) {
                String type = rs.getString("type");
                if (type != null && !type.trim().isEmpty()) {
                    typeComboBox.addItem(type);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading asset types: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateAsset(ActionEvent e) {
        System.out.println("Updating asset with id: " + assetId);
        if (assetId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a valid asset.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String valueText = valueField.getText().trim();
        String type = (String) typeComboBox.getSelectedItem();

        if (name.isEmpty() || valueText.isEmpty() || type == null || type.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double value = Double.parseDouble(valueText);
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE Assets SET name = ?, purchase_price = ?, type = ? WHERE user_id = ? AND id = ?")) {
                stmt.setString(1, name);
                stmt.setDouble(2, value);
                stmt.setString(3, type);
                stmt.setInt(4, userId);
                stmt.setInt(5, assetId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Asset updated successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    if (portfolioPanel != null) {
                        portfolioPanel.loadData();
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update asset.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for value.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            System.out.println("Error updating asset: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new EditAssetView(1, null, 1).setVisible(true);
            } catch (Exception e) {
                System.err.println("Error initializing EditAssetView: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
