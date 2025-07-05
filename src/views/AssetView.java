package views;

import javax.swing.*;
import javax.swing.table.*;
import database.DatabaseConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;

public class AssetView extends JFrame {
    private int userId;
    private DefaultTableModel model;
    private JTable table;

    public AssetView(int userId) {
        this.userId = userId;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("My Assets");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // اللوحة الرئيسية مع خلفية متدرجة
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(245, 240, 230), 0, getHeight(),
                        new Color(220, 235, 220));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // العنوان الرئيسي مع أيقونة
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);

        JLabel titleIcon = new JLabel("📋");
        titleIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        titleIcon.setForeground(new Color(34, 139, 34));
        titleIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JLabel titleLabel = new JLabel("My Assets");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(34, 139, 34));

        titlePanel.add(titleIcon);
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // إنشاء الجدول
        model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 6 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 7; // السماح بتعديل أعمدة الأزرار فقط
            }
        };

        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Type");
        model.addColumn("Quantity");
        model.addColumn("Price");
        model.addColumn("Date");
        model.addColumn("Halal");
        model.addColumn("Edit");
        model.addColumn("Delete");

        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(220, 235, 220));
        table.setSelectionForeground(Color.BLACK);

        // تخصيص مظهر الجدول
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Quantity
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Price
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Date
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Halal

        // تخصيص عمود "Halal"
        table.getColumnModel().getColumn(6).setCellRenderer(new TableCellRenderer() {
            private final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                if (value instanceof Boolean) {
                    Boolean isHalal = (Boolean) value;
                    String text = isHalal ? "✓ Halal" : "✗ Non-Halal";
                    Color color = isHalal ? new Color(0, 153, 51) : new Color(204, 0, 0);
                    ((JLabel) c).setText(text);
                    c.setForeground(color);
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                if (row % 2 == 0) {
                    c.setBackground(new Color(250, 245, 240));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        // إضافة أزرار "Edit" و"Delete"
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("Edit", new Color(34, 139, 34)));
        table.getColumnModel().getColumn(7)
                .setCellEditor(new ButtonEditor(new JCheckBox(), "Edit", new Color(34, 139, 34), table, row -> {
                    int assetId = (int) table.getValueAt(row, 0);
                    EditAssetView editView = new EditAssetView(userId, null, assetId);
                    editView.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            refreshTable(); // تحديث الجدول بعد الإغلاق
                        }
                    });
                    editView.setVisible(true);
                }));

        table.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer("Delete", new Color(183, 28, 28)));
        table.getColumnModel().getColumn(8)
                .setCellEditor(new ButtonEditor(new JCheckBox(), "Delete", new Color(183, 28, 28), table, row -> {
                    int assetId = (int) table.getValueAt(row, 0);
                    DeleteAssetView deleteView = new DeleteAssetView(userId, null, assetId);
                    deleteView.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            refreshTable(); // تحديث الجدول بعد الإغلاق
                        }
                    });
                    deleteView.setVisible(true);
                }));

        // ضبط عرض الأعمدة
        table.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Type
        table.getColumnModel().getColumn(3).setPreferredWidth(80); // Quantity
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Price
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Date
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Halal
        table.getColumnModel().getColumn(7).setPreferredWidth(80); // Edit
        table.getColumnModel().getColumn(8).setPreferredWidth(80); // Delete

        // تعبئة الجدول بالبيانات
        refreshTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void refreshTable() {
        model.setRowCount(0); // إفراغ الجدول
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id, name, type, quantity, purchase_price, purchase_date, is_halal " +
                                "FROM Assets WHERE user_id = ?")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            DecimalFormat df = new DecimalFormat("#,##0.00");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getDouble("quantity"),
                        df.format(rs.getDouble("purchase_price")),
                        rs.getDate("purchase_date"),
                        rs.getBoolean("is_halal"),
                        "Edit",
                        "Delete"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading assets: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // فئة لعرض الزر في الجدول
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text, Color bgColor) {
            setText(text);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBackground(bgColor);
            setForeground(Color.black);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(getBackground().darker());
            } else {
                setBackground(getBackground());
            }
            return this;
        }
    }

    // فئة لتحرير الجدول (لتفعيل الأزرار)
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private JTable table;
        private int row;
        private final java.util.function.Consumer<Integer> action; // استخدام Consumer لتمرير الصف

        public ButtonEditor(JCheckBox checkBox, String label, Color bgColor, JTable table,
                java.util.function.Consumer<Integer> action) {
            super(checkBox);
            this.label = label;
            this.table = table;
            this.action = action;

            button = new JButton(label);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBackground(bgColor);
            button.setForeground(Color.black);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            button.addActionListener(e -> {
                isPushed = true;
                fireEditingStopped();
                action.accept(row); // تمرير الصف مباشرة
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            if (isSelected) {
                button.setBackground(button.getBackground().darker());
            }
            button.setText(label);
            isPushed = false;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}
