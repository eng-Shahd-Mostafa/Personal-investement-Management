package views;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ConnectBankView extends JFrame {
    private JComboBox<String> bankCombo;
    private JTextField accountNumberField;
    private JPasswordField passwordField;
    private JButton connectButton;

    public ConnectBankView() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Connect Bank Account");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // إعداد لوحة العنوان
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel headerLabel = new JLabel("Connect Your Bank", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // إعداد اللوحة الرئيسية باستخدام GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0); // مسافات عمودية بين العناصر

        // إعداد تسمية ومربع اختيار البنك
        JLabel bankLabel = new JLabel("Select Bank:");
        bankLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START; // محاذاة إلى اليسار
        mainPanel.add(bankLabel, gbc);

        String[] banks = { "National Bank of Egypt", "Banque Misr", "Commercial International Bank", "QNB Al Ahli" };
        bankCombo = new JComboBox<>(banks);
        bankCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bankCombo.setPreferredSize(new Dimension(350, 30));
        bankCombo.setMaximumSize(new Dimension(350, 30));
        gbc.gridy = 1;
        mainPanel.add(bankCombo, gbc);

        // إعداد تسمية وحقل رقم الحساب
        JLabel accNumLabel = new JLabel("Account Number:");
        accNumLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 2;
        mainPanel.add(accNumLabel, gbc);

        accountNumberField = new JTextField();
        accountNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        accountNumberField.setPreferredSize(new Dimension(350, 30));
        accountNumberField.setMaximumSize(new Dimension(350, 30));
        gbc.gridy = 3;
        mainPanel.add(accountNumberField, gbc);

        // إعداد تسمية وحقل كلمة المرور
        JLabel passwordLabel = new JLabel("Online Banking Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 4;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(350, 30));
        passwordField.setMaximumSize(new Dimension(350, 30));
        gbc.gridy = 5;
        mainPanel.add(passwordField, gbc);

        // إعداد زر Connect Bank Account
        connectButton = new JButton("Connect Bank Account");
        styleButton(connectButton, new Color(46, 204, 113)); // لون أخضر زمردي
        connectButton.setPreferredSize(new Dimension(350, 40));
        connectButton.setMaximumSize(new Dimension(350, 40));
        connectButton.addActionListener(this::connectBank);
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.LINE_END; // محاذاة إلى اليمين
        mainPanel.add(connectButton, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(39, 174, 96), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    private void connectBank(ActionEvent e) {
        if (bankCombo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bank.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String bankName = (String) bankCombo.getSelectedItem();
        String accountNumber = accountNumberField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (accountNumber.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter account number and password.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String otp = JOptionPane.showInputDialog(this,
                "Please enter the OTP sent to your registered mobile number:",
                "OTP Verification",
                JOptionPane.PLAIN_MESSAGE);

        if (otp != null && otp.equals("1234")) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String bankQuery = "SELECT bank_id FROM Banks WHERE bank_name = ?";
                PreparedStatement bankStmt = conn.prepareStatement(bankQuery);
                bankStmt.setString(1, bankName);
                ResultSet rs = bankStmt.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Bank not found.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int bankId = rs.getInt("bank_id");

                String insertQuery = "INSERT INTO UserBankAccounts (user_id, bank_id, account_number, account_holder_name, last_sync_date, is_active) "
                        +
                        "VALUES (?, ?, ?, ?, GETDATE(), 1)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, 1);
                insertStmt.setInt(2, bankId);
                insertStmt.setString(3, accountNumber);
                insertStmt.setString(4, "Shosho");
                int rowsAffected = insertStmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                            "The bank account has been successfully linked.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to link the bank account.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error connecting bank account: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid OTP. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConnectBankView view = new ConnectBankView();
            view.setVisible(true);
        });
    }
}
