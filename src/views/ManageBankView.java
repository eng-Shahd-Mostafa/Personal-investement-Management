package views;

import database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ManageBankView extends JFrame {
    private JTable bankTable;
    private final Color HEADER_COLOR = new Color(50, 100, 150); // لون الهيدر
    private final Color EVEN_ROW_COLOR = new Color(240, 248, 255); // لون الصف الزوجي
    private final Color ODD_ROW_COLOR = Color.WHITE; // لون الصف الفردي

    public ManageBankView() {
        setupUI();
        loadBankAccounts();
    }

    private void setupUI() {
        setTitle("Manage Bank Accounts");
        setSize(650, 500); // زيادة حجم النافذة قليلاً
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        JLabel headerLabel = new JLabel("Manage Bank Accounts", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Connected Banks Table
        String[] columnNames = { "Bank Name", "Account Number", "Last Sync" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bankTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
                }
                return c;
            }
        };

        // تحسين مظهر الجدول
        bankTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bankTable.setRowHeight(30);
        bankTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bankTable.setGridColor(new Color(220, 220, 220));

        // تحسين رأس الجدول
        JTableHeader header = bankTable.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.black);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(bankTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton syncButton = new JButton("Sync Now");
        styleButton(syncButton, new Color(65, 131, 215), Color.black); // أزرق

        JButton disconnectButton = new JButton("Disconnect");
        styleButton(disconnectButton, new Color(220, 53, 69), Color.black); // أحمر

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, new Color(108, 117, 125), Color.black); // رمادي

        syncButton.addActionListener(this::syncAccounts);
        disconnectButton.addActionListener(this::disconnectAccount);
        refreshButton.addActionListener(e -> refreshAccounts());

        buttonPanel.add(syncButton);
        buttonPanel.add(disconnectButton);
        buttonPanel.add(refreshButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        // تأثيرات عند التمرير والضغط
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
        });
    }

    private void loadBankAccounts() {
        DefaultTableModel model = (DefaultTableModel) bankTable.getModel();
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT b.bank_name, uba.account_number, uba.last_sync_date " +
                    "FROM UserBankAccounts uba " +
                    "JOIN Banks b ON uba.bank_id = b.bank_id " +
                    "WHERE uba.user_id = ? AND uba.is_active = 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("bank_name"));
                row.add(rs.getString("account_number"));
                row.add(rs.getTimestamp("last_sync_date") != null ? rs.getTimestamp("last_sync_date").toString()
                        : "Not Synced");
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void syncAccounts(ActionEvent e) {
        int selectedRow = bankTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account to sync",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String accountNumber = (String) bankTable.getValueAt(selectedRow, 1);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String updateQuery = "UPDATE UserBankAccounts SET last_sync_date = CURRENT_TIMESTAMP " +
                    "WHERE account_number = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, accountNumber);
            stmt.setInt(2, 1);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Account synced successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAccounts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to sync account",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnectAccount(ActionEvent e) {
        int selectedRow = bankTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account to disconnect",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String accountNumber = (String) bankTable.getValueAt(selectedRow, 1);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String updateQuery = "UPDATE UserBankAccounts SET is_active = 0 " +
                    "WHERE account_number = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, accountNumber);
            stmt.setInt(2, 1);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Account disconnected successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAccounts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to disconnect account",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAccounts() {
        loadBankAccounts();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManageBankView view = new ManageBankView();
            view.setVisible(true);
        });
    }
}
