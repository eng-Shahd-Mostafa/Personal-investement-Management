package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import database.DatabaseConnection;

public class AddAssetView extends JFrame {
    private JTextField nameField, valueField, quantityField;
    private JComboBox<String> typeComboBox;
    private int userId;

    public AddAssetView(int userId) {
        this.userId = userId;
        setupUI();
    }

    private void setupUI() {
        setTitle("Add New Asset");
        setSize(450, 470);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 0, 255));
        JLabel headerLabel = new JLabel("Add New Asset");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // تباعد جانبي وعامودي
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // للسماح للحقول بأخذ العرض الكامل

        // Asset Name
        JLabel nameLabel = new JLabel("Asset Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(nameLabel, gbc);

        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(nameField, gbc);

        // Quantity
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(quantityLabel, gbc);

        quantityField = new JTextField();
        quantityField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(quantityField, gbc);

        // Value (L.E)
        JLabel valueLabel = new JLabel("Value (L.E):");
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(valueLabel, gbc);

        valueField = new JTextField();
        valueField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(valueField, gbc);

        // Type (JComboBox)
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(typeLabel, gbc);

        typeComboBox = new JComboBox<>();
        typeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadAssetTypes();
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(typeComboBox, gbc);

        // Save Button
        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(0, 102, 204));
        saveButton.setForeground(Color.black);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(this::saveAsset);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0; // زر Save لا يحتاج أن يأخذ العرض الكامل
        formPanel.add(saveButton, gbc);

        add(formPanel, BorderLayout.CENTER);
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

    private void saveAsset(ActionEvent e) {
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double purchasePrice = Double.parseDouble(valueField.getText().trim());
            String selectedType = (String) typeComboBox.getSelectedItem();
            if (selectedType == null || selectedType.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select an asset type");
                return;
            }
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO Assets (user_id, type, name, quantity, purchase_price, purchase_date, is_halal) VALUES (?, ?, ?, ?, ?, ?, 1)")) {

                stmt.setInt(1, userId);
                stmt.setString(2, selectedType);
                stmt.setString(3, nameField.getText().trim());
                stmt.setInt(4, quantity);
                stmt.setDouble(5, purchasePrice);
                stmt.setDate(6, new java.sql.Date(System.currentTimeMillis()));
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Asset added successfully!");
                dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and value");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
