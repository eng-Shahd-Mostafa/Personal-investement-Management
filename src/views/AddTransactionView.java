package views;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddTransactionView extends JFrame {
    private JTextField dateField;
    private JTextField descriptionField;
    private JTextField amountField;
    private JComboBox<Integer> accountCombo;

    public AddTransactionView() {
        setupUI();
    }

    private void setupUI() {
        setTitle("Add Transaction");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Account Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Bank Account:"), gbc);
        accountCombo = new JComboBox<>();
        loadAccounts();
        gbc.gridx = 1;
        mainPanel.add(accountCombo, gbc);

        // Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        gbc.gridx = 1;
        mainPanel.add(dateField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Description:"), gbc);
        descriptionField = new JTextField();
        gbc.gridx = 1;
        mainPanel.add(descriptionField, gbc);

        // Amount
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Amount ($):"), gbc);
        amountField = new JTextField();
        gbc.gridx = 1;
        mainPanel.add(amountField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveTransaction());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAccounts() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT account_id FROM UserBankAccounts WHERE user_id = ? AND is_active = 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1); // user_id مؤقت
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                accountCombo.addItem(rs.getInt("account_id"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTransaction() {
        String date = dateField.getText().trim();
        String description = descriptionField.getText().trim();
        String amountStr = amountField.getText().trim();
        Integer accountId = (Integer) accountCombo.getSelectedItem();

        if (date.isEmpty() || description.isEmpty() || amountStr.isEmpty() || accountId == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO Transactions (user_id, account_id, transaction_date, description, amount) "
                        +
                        "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, 1); // user_id مؤقت
                stmt.setInt(2, accountId);
                stmt.setString(3, date);
                stmt.setString(4, description);
                stmt.setDouble(5, amount);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Error adding transaction", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error in database: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddTransactionView view = new AddTransactionView();
            view.setVisible(true);
        });
    }
}
