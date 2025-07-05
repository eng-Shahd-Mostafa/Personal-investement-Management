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

public class ViewAllTransactionsView extends JFrame {
    private JTable transactionsTable;
    private final Color PRIMARY_COLOR = new Color(52, 73, 94); // لون رئيسي
    private final Color SECONDARY_COLOR = new Color(44, 62, 80); // لون ثانوي
    private final Color POSITIVE_AMOUNT = new Color(46, 204, 113); // أخضر للمبالغ الموجبة
    private final Color NEGATIVE_AMOUNT = new Color(231, 76, 60); // أحمر للمبالغ السالبة

    public ViewAllTransactionsView() {
        setupUI();
        loadTransactions();
    }

    private void setupUI() {
        setTitle("All Transactions");
        setSize(800, 550); // زيادة حجم النافذة قليلاً
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header Panel - بنفس لون الهيدر السابق
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel headerLabel = new JLabel("All Transactions", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.white);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.white);

        // Transactions Table
        String[] columnNames = { "Date", "Description", "Amount" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // جعل الجدول غير قابل للتعديل
            }
        };

        transactionsTable = new JTable(tableModel) {
            // تلوين الصفوف الزوجية والفردية
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                // تلوين الصفوف
                if (!isRowSelected(row)) {
                    Color color = row % 2 == 0 ? new Color(248, 248, 248) : Color.WHITE;
                    c.setBackground(color);
                }

                // تلوين عمود المبلغ حسب القيمة
                if (column == 2) {
                    String value = getValueAt(row, column).toString();
                    if (value.startsWith("-")) {
                        c.setForeground(NEGATIVE_AMOUNT);
                    } else {
                        c.setForeground(POSITIVE_AMOUNT);
                    }
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    c.setForeground(new Color(60, 60, 60));
                }

                return c;
            }
        };

        // تحسين مظهر الجدول
        transactionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        transactionsTable.setRowHeight(30);
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionsTable.setGridColor(new Color(230, 230, 230));
        transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // تحديد عرض عمود المبلغ

        // تحسين رأس الجدول بنفس التنسيق السابق
        JTableHeader header = transactionsTable.getTableHeader();
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(Color.black);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel - بنفس التنسيق السابق
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(250, 250, 250));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, new Color(149, 165, 166), Color.black); // رمادي
        refreshButton.addActionListener(e -> loadTransactions());

        JButton closeButton = new JButton("Close");
        styleButton(closeButton, new Color(189, 195, 199), Color.black); // رمادي فاتح
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
        });
    }

    private void loadTransactions() {
        DefaultTableModel model = (DefaultTableModel) transactionsTable.getModel();
        model.setRowCount(0); // إفراغ الجدول

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT transaction_date, description, amount " +
                    "FROM Transactions " +
                    "WHERE user_id = ? " +
                    "ORDER BY transaction_date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1); // user_id مؤقت
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getTimestamp("transaction_date").toString());
                row.add(rs.getString("description"));

                // تنسيق المبلغ مع إضافة علامة + للمبالغ الموجبة
                float amount = rs.getFloat("amount");
                if (amount >= 0) {
                    row.add(String.format("+$%,.2f", amount));
                } else {
                    row.add(String.format("-$%,.2f", Math.abs(amount)));
                }

                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ViewAllTransactionsView view = new ViewAllTransactionsView();
            view.setVisible(true);
        });
    }
}
