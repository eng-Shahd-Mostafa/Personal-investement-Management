package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.Vector;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import database.DatabaseConnection;

public class DashboardView extends JFrame {
    private int userId;

    public DashboardView(int userId) {
        this.userId = userId;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Islamic Finance Manager - Dashboard");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));

        // Apply modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create tabbed pane with modern styling
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(240, 240, 240));
        tabbedPane.setForeground(new Color(70, 70, 70));

        // Create tabs
        tabbedPane.addTab("Home", createIcon("../resources/icons/home_bar.png"), createHomePanel());
        tabbedPane.addTab("Portfolio", createIcon("../resources/icons/portfolio_bar.png"), createPortfolioPanel());
        tabbedPane.addTab("Zakat", createIcon("../resources/icons/zakat_bar.png"), createZakatPanel());
        tabbedPane.addTab("Bank", createIcon("../resources/icons/bank_bar.png"), createBankPanel());
        tabbedPane.addTab("About", createIcon("../resources/icons/About_bar.png"), createAboutPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
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
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setOpaque(false);

        JLabel logoLabel = new JLabel();
        // logoLabel.setIcon(createIcon("../resources/icons/home.png"));

        JLabel dashboardTitle = new JLabel("Your Dashboard");
        dashboardTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        dashboardTitle.setForeground(new Color(0, 70, 70));
        dashboardTitle.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(logoLabel, BorderLayout.WEST);
        titlePanel.add(dashboardTitle, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Log out");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoutBtn.setBackground(new Color(183, 28, 28));
        logoutBtn.setForeground(Color.black);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setPreferredSize(new Dimension(120, 40));
        logoutBtn.addActionListener(e -> {
            new LoginView().setVisible(true);
            dispose();
        });

        // إضافة زر Refresh
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        refreshBtn.setBackground(new Color(34, 139, 34)); // لون أخضر
        refreshBtn.setForeground(Color.black);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setPreferredSize(new Dimension(120, 40));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshBtn); // إضافة زر Refresh
        buttonPanel.add(logoutBtn);

        navPanel.add(titlePanel, BorderLayout.CENTER);
        navPanel.add(buttonPanel, BorderLayout.EAST);
        navPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(navPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBackground(new Color(255, 255, 255, 220));
        innerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel userLabel = new JLabel("Welcome, Loading User...");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        userLabel.setForeground(new Color(50, 50, 50));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(userLabel);

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT name FROM Users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    userLabel.setText("Welcome, " + rs.getString("name"));
                else
                    userLabel.setText("Welcome, Unknown User");
            }
        } catch (SQLException ex) {
            userLabel.setText("Welcome, Error Loading User");
            ex.printStackTrace();
        }

        // جلب البيانات من قاعدة البيانات أولاً
        double totalAssets = 0.0;
        double totalValue = 0.0;
        double halalValue = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) AS asset_count, " +
                                "SUM(purchase_price * quantity) AS total_value, " +
                                "SUM(CASE WHEN is_halal = 1 THEN purchase_price * quantity ELSE 0 END) AS halal_value "
                                +
                                "FROM Assets WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalAssets = rs.getInt("asset_count");
                    totalValue = rs.getDouble("total_value");
                    halalValue = rs.getDouble("halal_value");
                    System.out.println("Initial data loaded - Total Assets: " + totalAssets + ", Total Value: "
                            + totalValue + ", Halal Value: " + halalValue);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        double halalRatio = (totalValue > 0) ? (halalValue / totalValue) * 100 : 0.0;

        // Row 1 - 3 cards
        JPanel row1 = new JPanel(new GridLayout(1, 3, 20, 20));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // إنشاء البطاقات باستخدام القيم الفعلية
        JPanel totalAssetsCard = createEnhancedSummaryCard("Total Assets", String.format("%.0f", totalAssets),
                new Color(2, 136, 209), "\uD83D\uDCCA");
        JPanel totalValueCard = createEnhancedSummaryCard("Total Value", String.format("$%.2f", totalValue),
                new Color(46, 125, 50), "\uD83D\uDCB0");
        JPanel halalRatioCard = createEnhancedSummaryCard("Halal Ratio", String.format("%.1f%%", halalRatio),
                new Color(255, 179, 0), "\u2705");

        row1.add(totalAssetsCard);
        row1.add(totalValueCard);
        row1.add(halalRatioCard);

        // Row 2 - 2 cards
        JPanel row2 = new JPanel(new GridLayout(1, 2, 20, 20));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        row2.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        row2.add(createEnhancedSummaryCard("Zakat Due", "Calculate Now", new Color(34, 139, 34), "\uD83D\uDCB8"));
        row2.add(createEnhancedSummaryCard("Bank Accounts", "Manage", new Color(74, 20, 140), "\uD83C\uDFE6"));

        innerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        innerPanel.add(row1);
        innerPanel.add(row2);

        contentPanel.add(innerPanel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        // دالة لتحديث البيانات
        Runnable refreshData = () -> {
            double newTotalAssets = 0.0;
            double newTotalValue = 0.0;
            double newHalalValue = 0.0;

            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT COUNT(*) AS asset_count, " +
                                    "SUM(purchase_price * quantity) AS total_value, " +
                                    "SUM(CASE WHEN is_halal = 1 THEN purchase_price * quantity ELSE 0 END) AS halal_value "
                                    +
                                    "FROM Assets WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        newTotalAssets = rs.getInt("asset_count");
                        newTotalValue = rs.getDouble("total_value");
                        newHalalValue = rs.getDouble("halal_value");
                        System.out.println("Refreshed data - Total Assets: " + newTotalAssets + ", Total Value: "
                                + newTotalValue + ", Halal Value: " + newHalalValue);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error refreshing data: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            double newHalalRatio = (newTotalValue > 0) ? (newHalalValue / newTotalValue) * 100 : 0.0;

            // تحديث البطاقات مع تمرير العنوان لتتبع أي بطاقة يتم تحديثها
            updateSummaryCard(totalAssetsCard, "Total Assets", String.format("%.0f", newTotalAssets));
            updateSummaryCard(totalValueCard, "Total Value", String.format("$%.2f", newTotalValue));
            updateSummaryCard(halalRatioCard, "Halal Ratio", String.format("%.1f%%", newHalalRatio));
        };

        // إضافة ActionListener لزر Refresh
        refreshBtn.addActionListener(e -> {
            System.out.println("Refresh button clicked, updating dashboard data...");
            refreshData.run();
        });

        return panel;
    }

    // دالة مساعدة لتحديث محتوى البطاقة
    private void updateSummaryCard(JPanel card, String cardTitle, String newValue) {
        boolean valueUpdated = false;

        // طباعة جميع النصوص الموجودة في البطاقة لتتبع المشكلة
        System.out.println("Inspecting card: " + cardTitle);
        for (Component comp : card.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                System.out.println("Found JLabel with text: " + label.getText());
                String currentText = label.getText();
                // تحقق إذا كان النص يحتوي على أرقام أو رموز خاصة (مثل $ أو %)
                if (currentText.matches(".\\d+.") || currentText.contains("$") || currentText.contains("%")) {
                    System.out.println("Updating " + cardTitle + " from '" + currentText + "' to '" + newValue + "'");
                    label.setText(newValue);
                    valueUpdated = true;
                    break;
                }
            }

            if (comp instanceof JPanel) {
                for (Component innerComp : ((JPanel) comp).getComponents()) {
                    if (innerComp instanceof JLabel) {
                        JLabel innerLabel = (JLabel) innerComp;
                        System.out.println("Found inner JLabel with text: " + innerLabel.getText());
                        String innerText = innerLabel.getText();
                        if (innerText.matches(".\\d+.") || innerText.contains("$") || innerText.contains("%")) {
                            System.out.println(
                                    "Updating " + cardTitle + " from '" + innerText + "' to '" + newValue + "'");
                            innerLabel.setText(newValue);
                            valueUpdated = true;
                            break;
                        }
                    }
                }
            }
        }
        if (!valueUpdated) {
            System.out.println("Failed to update " + cardTitle + ": Could not find the value label.");
        }
        card.revalidate();
        card.repaint();
    }

    private JPanel createPortfolioPanel() {
        // اللوحة الرئيسية مع خلفية متدرجة موحدة
        JPanel panel = new JPanel(new BorderLayout()) {
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
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // لوحة المحتوى المركزية مع ظل
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(34, 139, 34), 2),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 4, new Color(0, 0, 0, 80)),
                contentPanel.getBorder()));

        // أيقونة وعنوان في الأعلى
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);

        JLabel portfolioIcon = new JLabel("📊");
        portfolioIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        portfolioIcon.setForeground(new Color(34, 139, 34));
        portfolioIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JLabel titleLabel = new JLabel("Portfolio Overview");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 36));
        titleLabel.setForeground(new Color(34, 139, 34));

        titlePanel.add(portfolioIcon);
        titlePanel.add(titleLabel);
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // بطاقات الإحصائيات
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 30, 30));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        double totalAssets = 0.0;
        double totalValue = 0.0;
        double halalRatio = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT SUM(purchase_price * quantity) AS total_value, " +
                                "SUM(CASE WHEN is_halal = 1 THEN purchase_price * quantity ELSE 0 END) AS halal_value, "
                                +
                                "COUNT(*) AS total_assets " +
                                "FROM Assets WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalAssets = rs.getDouble("total_assets");
                    totalValue = rs.getDouble("total_value");
                    double halalValue = rs.getDouble("halal_value");
                    halalRatio = totalValue > 0 ? (halalValue / totalValue) * 100 : 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JPanel totalAssetsCard = createEnhancedSummaryCard("Total Assets", String.format("%.0f", totalAssets),
                new Color(13, 71, 161), "\uD83D\uDCC8");
        JPanel totalValueCard = createEnhancedSummaryCard("Total Value", String.format("$%.2f", totalValue),
                new Color(56, 142, 60), "\uD83D\uDCB0");
        JPanel halalRatioCard = createEnhancedSummaryCard("Halal Ratio", String.format("%.1f%%", halalRatio),
                new Color(249, 168, 37), "\u2705");

        summaryPanel.add(totalAssetsCard);
        summaryPanel.add(totalValueCard);
        summaryPanel.add(halalRatioCard);

        contentPanel.add(summaryPanel, BorderLayout.CENTER);

        // لوحة الأزرار (عمودين وصفين)
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(40, 100, 40, 100));

        JButton viewBtn = createEnhancedPortfolioButton("View Assets", new Color(34, 139, 34), "👁");
        JButton addBtn = createEnhancedPortfolioButton("Add Asset", new Color(34, 139, 34), "➕");
        JButton editBtn = createEnhancedPortfolioButton("Edit Assets", new Color(34, 139, 34), "✏");
        JButton deleteBtn = createEnhancedPortfolioButton("Delete Assets", new Color(34, 139, 34), "🗑");

        viewBtn.addActionListener(e -> new AssetView(userId).setVisible(true));
        addBtn.addActionListener(e -> new AddAssetView(userId).setVisible(true));
        editBtn.addActionListener(e -> {
            if (hasAssets()) {
                new EditAssetView(userId, null).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(DashboardView.this,
                        "No assets available to edit. Please add assets first.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        deleteBtn.addActionListener(e -> {
            if (hasAssets()) {
                new DeleteAssetView(userId, null).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(DashboardView.this,
                        "No assets available to delete. Please add assets first.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonPanel.add(viewBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createZakatPanel() {
        // اللوحة الرئيسية مع خلفية متدرجة أنيقة
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(232, 245, 233), 0, getHeight(),
                        new Color(245, 245, 245)); // تدرج أخضر فاتح إلى رمادي فاتح
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(20, 40, 20, 40)); // تقليل المسافة العلوية والسفلية

        // لوحة المحتوى الرئيسية باستخدام GridBagLayout
        JPanel contentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 255, 255, 200), 0, getHeight(),
                        new Color(245, 255, 245, 200));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(8, 8, 8, 8, new Color(0, 0, 0, 50)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(46, 125, 50), 2, true),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20))));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // تقليل المسافات الداخلية لتوازن أفضل
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // العنوان الرئيسي (داخل الكارت)
        JLabel titleLabel = new JLabel("Zakat Calculator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(46, 125, 50));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridy = 0;
        contentPanel.add(titleLabel, gbc);

        // فاصل هندسي
        gbc.gridy = 1;
        JPanel separatorPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 215, 0)); // ذهبي
                g2d.setStroke(new BasicStroke(2));
                int[] xPoints = { getWidth() / 2 - 50, getWidth() / 2, getWidth() / 2 + 50 };
                int[] yPoints = { 10, 0, 10 };
                g2d.drawPolyline(xPoints, yPoints, 3);
            }
        };
        separatorPanel.setOpaque(false);
        separatorPanel.setPreferredSize(new Dimension(150, 20));
        contentPanel.add(separatorPanel, gbc);

        // صورة توضيحية
        JLabel imageLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("../resources/icons/zakat_icon.png"));
            Image scaledImage = originalIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            imageLabel.setIcon(scaledIcon);
        } catch (Exception e) {
            imageLabel.setText("💰");
            imageLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            e.printStackTrace();
        }
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = 2;
        contentPanel.add(imageLabel, gbc);

        // نص الوصف
        JTextArea infoText = new JTextArea();
        infoText.setText(
                "Zakat is one of the five pillars of Islam. It is obligatory for every Muslim who meets the necessary criteria of wealth. Calculate your zakat obligations based on your assets and liabilities with ease.");
        infoText.setFont(new Font("Arial", Font.PLAIN, 18));
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);
        infoText.setEditable(false);
        infoText.setBackground(new Color(255, 255, 255, 0));
        infoText.setForeground(new Color(50, 50, 50));
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        contentPanel.add(infoText, gbc);

        // فاصل هندسي
        gbc.gridy = 4;
        JPanel separatorPanel2 = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 215, 0)); // ذهبي
                g2d.setStroke(new BasicStroke(2));
                int[] xPoints = { getWidth() / 2 - 50, getWidth() / 2, getWidth() / 2 + 50 };
                int[] yPoints = { 10, 0, 10 };
                g2d.drawPolyline(xPoints, yPoints, 3);
            }
        };
        separatorPanel2.setOpaque(false);
        separatorPanel2.setPreferredSize(new Dimension(150, 20));
        contentPanel.add(separatorPanel2, gbc);

        // بطاقة معلومات إضافية
        JPanel infoCard = new JPanel();
        infoCard.setBackground(new Color(245, 255, 245));
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 125, 50), 2, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        infoCard.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel infoCardText = new JLabel(
                "<html><b>Tip:</b> Ensure you include all cash, investments, and liabilities for an accurate calculation.</html>");
        infoCardText.setFont(new Font("Arial", Font.BOLD, 16));
        infoCard.add(infoCardText);
        gbc.gridy = 5;
        contentPanel.add(infoCard, gbc);

        // زر الحساب (داخل الكارت)
        JButton calcBtn = createStyledButton("Calculate Zakat", new Color(46, 125, 50));
        calcBtn.setPreferredSize(new Dimension(300, 60));
        calcBtn.setFont(new Font("Arial", Font.BOLD, 18));
        calcBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calcBtn.addActionListener(e -> new ZakatCalculationView(userId).setVisible(true));
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridy = 6;
        contentPanel.add(calcBtn, gbc);

        // إضافة الكارت للوحة الرئيسية مع ضبط الحجم
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBankPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
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
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // العنوان الرئيسي مع أيقونة
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Bank Account Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(34, 139, 34));

        // أيقونة بجانب العنوان
        JLabel bankIcon = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/bank_icon.png"));
            if (icon.getImage() != null) {
                Image scaledImage = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                bankIcon.setIcon(new ImageIcon(scaledImage));
            } else {
                bankIcon.setText("🏦");
                bankIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
                bankIcon.setForeground(new Color(34, 139, 34));
            }
        } catch (Exception e) {
            bankIcon.setText("🏦");
            bankIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            bankIcon.setForeground(new Color(34, 139, 34));
        }
        bankIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        titlePanel.add(bankIcon);
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);

        // لوحة المحتوى الرئيسية
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(250, 245, 240));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(34, 139, 34), 2),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 4, new Color(0, 0, 0, 80)),
                contentPanel.getBorder()));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // بطاقات الإحصائيات
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        statsPanel.setOpaque(false);

        // جلب البيانات من قاعدة البيانات
        int connectedAccounts = getConnectedAccountsCount();
        int totalTransactions = getTotalTransactionsCount();
        String importedFunds = String.format("$%,.2f", getImportedFunds());

        statsPanel.add(createStatCard("Connected Accounts", String.valueOf(connectedAccounts), new Color(0, 102, 204)));
        statsPanel.add(createStatCard("Total Transactions", String.valueOf(totalTransactions), new Color(56, 142, 60)));
        statsPanel.add(createStatCard("Imported Funds", importedFunds, new Color(255, 167, 38)));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        contentPanel.add(statsPanel, gbc);
        contentPanel.add(Box.createVerticalStrut(20), gbc);

        // لوحة Connect وManage (بجانب بعض)
        JPanel actionsPanel = new JPanel(new GridLayout(1, 2, 30, 30));
        actionsPanel.setOpaque(false);

        // Connect Panel
        JPanel connectPanel = new JPanel(new BorderLayout(10, 10));
        connectPanel.setBackground(new Color(250, 245, 240));
        connectPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 1),
                        "Connect New Account"),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel connectIcon = new JLabel("🔗");
        connectIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        connectIcon.setForeground(new Color(34, 139, 34));
        connectIcon.setHorizontalAlignment(JLabel.CENTER);
        connectIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        connectPanel.add(connectIcon, BorderLayout.NORTH);

        JTextArea connectInfo = new JTextArea();
        connectInfo.setText(
                "Connect your bank account to automatically import transactions and keep track of your finances in one place.");
        connectInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        connectInfo.setLineWrap(true);
        connectInfo.setWrapStyleWord(true);
        connectInfo.setEditable(false);
        connectInfo.setBackground(new Color(250, 245, 240));
        connectInfo.setForeground(new Color(50, 50, 50));
        connectPanel.add(connectInfo, BorderLayout.CENTER);

        JButton connectBtn = createStyledButton("Connect Bank", new Color(34, 139, 34));
        connectBtn.addActionListener(e -> new ConnectBankView().setVisible(true));
        connectPanel.add(connectBtn, BorderLayout.SOUTH);

        actionsPanel.add(connectPanel);

        // Manage Panel
        JPanel managePanel = new JPanel(new BorderLayout(10, 10));
        managePanel.setBackground(new Color(250, 245, 240));
        managePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 1),
                        "Manage Accounts"),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel manageIcon = new JLabel("🛠");
        manageIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        manageIcon.setForeground(new Color(34, 139, 34));
        manageIcon.setHorizontalAlignment(JLabel.CENTER);
        manageIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        managePanel.add(manageIcon, BorderLayout.NORTH);

        JTextArea manageInfo = new JTextArea();
        manageInfo.setText(
                "View and manage your connected bank accounts, update credentials, or disconnect accounts you no longer use.");
        manageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        manageInfo.setLineWrap(true);
        manageInfo.setWrapStyleWord(true);
        manageInfo.setEditable(false);
        manageInfo.setBackground(new Color(250, 245, 240));
        manageInfo.setForeground(new Color(50, 50, 50));
        managePanel.add(manageInfo, BorderLayout.CENTER);

        JButton manageBtn = createStyledButton("Manage Accounts", new Color(34, 139, 34));
        manageBtn.addActionListener(e -> new ManageBankView().setVisible(true));
        managePanel.add(manageBtn, BorderLayout.SOUTH);

        actionsPanel.add(managePanel);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        contentPanel.add(actionsPanel, gbc);
        contentPanel.add(Box.createVerticalStrut(20), gbc);

        // قسم المعاملات الأخيرة (جدول)
        JPanel recentTransactionsPanel = new JPanel(new BorderLayout(10, 10));
        recentTransactionsPanel.setBackground(new Color(250, 245, 240));
        recentTransactionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 1),
                        "Transactions"),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // جلب المعاملات من قاعدة البيانات
        String[] columnNames = { "Date", "Description", "Amount" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable transactionsTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row % 2 == 0) {
                    c.setBackground(new Color(250, 245, 240));
                } else {
                    c.setBackground(new Color(240, 248, 255));
                }
                return c;
            }
        };
        transactionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionsTable.setRowHeight(30);
        transactionsTable.setGridColor(new Color(200, 200, 200));
        transactionsTable.setShowGrid(true);

        // تحميل المعاملات الأخيرة
        loadRecentTransactions(tableModel);

        JScrollPane tableScrollPane = new JScrollPane(transactionsTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 120));
        recentTransactionsPanel.add(tableScrollPane, BorderLayout.CENTER);

        // لوحة الأزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(250, 245, 240));

        JButton viewAllBtn = createStyledButton("View All Transactions", new Color(34, 139, 34));
        viewAllBtn.addActionListener(e -> new ViewAllTransactionsView().setVisible(true));
        buttonPanel.add(viewAllBtn);

        JButton addTransactionBtn = createStyledButton("Add Transaction", new Color(34, 139, 34));
        addTransactionBtn.addActionListener(e -> new AddTransactionView().setVisible(true));
        buttonPanel.add(addTransactionBtn);

        recentTransactionsPanel.add(buttonPanel, BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        contentPanel.add(recentTransactionsPanel, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // دالة لتحميل المعاملات الأخيرة
    private void loadRecentTransactions(DefaultTableModel model) {
        model.setRowCount(0); // إفراغ الجدول
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT TOP 3 transaction_date, description, amount " +
                    "FROM Transactions " +
                    "WHERE user_id = ? " +
                    "ORDER BY transaction_date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1); // user_id مؤقت
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("transaction_date"));
                row.add(rs.getString("description"));
                row.add(String.format("$%,.2f", rs.getFloat("amount")));
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading recent transactions: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // دالة لجلب عدد الحسابات المرتبطة
    private int getConnectedAccountsCount() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM UserBankAccounts WHERE user_id = ? AND is_active = 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1); // user_id مؤقت
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    // دالة لجلب عدد المعاملات
    private int getTotalTransactionsCount() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM Transactions WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1); // user_id مؤقت
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    // دالة لجلب إجمالي الأموال المستوردة
    private double getImportedFunds() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT SUM(amount) FROM Transactions WHERE user_id = ? AND amount > 0";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1); // user_id مؤقت
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }

    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 240, 240), 0, getHeight(),
                        new Color(220, 215, 210));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header (About Us)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("About Us") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(34, 139, 34), getWidth(), 0,
                        new Color(0, 105, 92));
                g2d.setPaint(gradient);
                g2d.setFont(getFont());
                g2d.drawString(getText(), 0, getHeight() - 5);
            }
        };
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setOpaque(false);
        headerPanel.add(titleLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // القسم العلوي (الجامعة والكلية)
        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel universityPanel = new JPanel();
        universityPanel.setLayout(new BoxLayout(universityPanel, BoxLayout.Y_AXIS));
        universityPanel.setOpaque(false);

        JLabel universityLabel = new JLabel("Cairo University");
        universityLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        universityLabel.setForeground(new Color(34, 139, 34));
        universityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel facultyLabel = new JLabel("Faculty of Computers and Artificial Intelligence");
        facultyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        facultyLabel.setForeground(new Color(51, 51, 51));
        facultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        universityPanel.add(universityLabel);
        universityPanel.add(Box.createVerticalStrut(10));
        universityPanel.add(facultyLabel);

        // خطوط فاصلة على الجانبين
        JPanel leftLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(34, 139, 34));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
            }
        };
        leftLine.setOpaque(false);
        leftLine.setPreferredSize(new Dimension(20, 0));

        JPanel rightLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(34, 139, 34));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
            }
        };
        rightLine.setOpaque(false);
        rightLine.setPreferredSize(new Dimension(20, 0));

        topPanel.add(leftLine, BorderLayout.WEST);
        topPanel.add(universityPanel, BorderLayout.CENTER);
        topPanel.add(rightLine, BorderLayout.EAST);

        contentPanel.add(topPanel, gbc);

        // القسم الوسط (اسم المشروع)
        gbc.gridy = 1;
        JPanel projectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        projectPanel.setOpaque(false);
        JLabel projectLabel = new JLabel("Personal Investment Management") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(34, 139, 34), getWidth(), 0,
                        new Color(0, 105, 92));
                g2d.setPaint(gradient);
                g2d.setFont(getFont());
                g2d.drawString(getText(), 0, getHeight() - 5);
            }
        };
        projectLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        projectLabel.setOpaque(false);
        projectPanel.add(projectLabel);
        contentPanel.add(projectPanel, gbc);

        // القسم السفلي (الفريق والمشرف)
        gbc.gridy = 2;
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);

        JTextPane teamDetails = new JTextPane();
        teamDetails.setContentType("text/html");
        teamDetails.setEditable(false);
        teamDetails.setOpaque(false);
        teamDetails.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        teamDetails.setText("""
                <html>
                <body style='text-align:center; color:#333333;'>
                    <h3 style='color:#00695C; font-size:20px;'>Development Team</h3>
                    <ul style='list-style-type:none; padding:0;'>
                        <li>Mohamed Abdelhamid Rafaat - ID: 20240820</li>
                        <li>Shahd Mostafa Farouk - ID: 20240806</li>
                        <li>Shrouk Mahmoud Gomaa - ID: 20210176</li>
                    </ul>
                </body>
                </html>
                """);
        teamDetails.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(34, 139, 34));
        separator.setPreferredSize(new Dimension(200, 2));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel supervisorLabel = new JLabel("Supervised By");
        supervisorLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        supervisorLabel.setForeground(new Color(34, 139, 34));
        supervisorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel supervisorName = new JLabel("Dr. Mohamed El-Ramly") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(34, 139, 34), getWidth(), 0,
                        new Color(0, 105, 92));
                g2d.setPaint(gradient);
                g2d.setFont(getFont());
                g2d.drawString(getText(), 0, getHeight() - 5);
            }
        };
        supervisorName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        supervisorName.setOpaque(false);
        supervisorName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel copyrightLabel = new JLabel("© 2025 Personal Investment Manager. All rights reserved.");
        copyrightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        copyrightLabel.setForeground(new Color(51, 51, 51));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(teamDetails);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(separator);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(supervisorLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(supervisorName);
        bottomPanel.add(Box.createVerticalStrut(20));
        bottomPanel.add(copyrightLabel);

        // إضافة Scroll
        JScrollPane scrollPane = new JScrollPane(bottomPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(scrollPane, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // دالة مساعدة لإنشاء بطاقات أنيقة
    // private JPanel createModernCard(String emoji, String title, String content,
    // Color color) {
    // JPanel card = new JPanel();
    // card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    // card.setBackground(new Color(255, 255, 255, 200));
    // card.setBorder(BorderFactory.createCompoundBorder(
    // BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(),
    // color.getBlue(), 100), 1,
    // true),
    // BorderFactory.createEmptyBorder(20, 25, 20, 25)));

    // // إضافة تأثير ظل
    // card.setBorder(BorderFactory.createCompoundBorder(
    // BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(0, 0, 0, 15)),
    // card.getBorder()));

    // JLabel emojiLabel = new JLabel(emoji);
    // emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
    // emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    // card.add(emojiLabel);

    // JLabel titleLabel = new JLabel(title);
    // titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
    // titleLabel.setForeground(color);
    // titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    // card.add(titleLabel);
    // card.add(Box.createRigidArea(new Dimension(0, 10)));

    // JTextArea contentArea = new JTextArea(content);
    // contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    // contentArea.setEditable(false);
    // contentArea.setOpaque(false);
    // contentArea.setLineWrap(true);
    // contentArea.setWrapStyleWord(true);
    // contentArea.setAlignmentX(Component.CENTER_ALIGNMENT);
    // card.add(contentArea);

    // return card;
    // }

    // // دالة مساعدة لإنشاء الزخارف الجانبية
    // private JPanel createDecorPanel() {
    // return new JPanel() {
    // @Override
    // protected void paintComponent(Graphics g) {
    // super.paintComponent(g);
    // Graphics2D g2d = (Graphics2D) g;
    // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    // RenderingHints.VALUE_ANTIALIAS_ON);

    // int centerY = getHeight() / 2;
    // int centerX = getWidth() / 2;

    // // رسم خطوط زخرفية
    // g2d.setColor(new Color(0, 120, 215, 50));
    // g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
    // BasicStroke.JOIN_ROUND,
    // 0, new float[] { 5, 5 }, 0));
    // g2d.drawLine(centerX, centerY - 20, centerX, centerY + 20);

    // // نقاط زخرفية
    // g2d.setColor(new Color(0, 120, 215, 80));
    // g2d.fillOval(centerX - 3, centerY - 25, 6, 6);
    // g2d.fillOval(centerX - 3, centerY + 19, 6, 6);
    // }
    // };
    // }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 3, new Color(0, 0, 0, 60)),
                card.getBorder()));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // private JPanel createSummaryCard(String title, String value, Color color) {
    // JPanel card = new JPanel(new BorderLayout());
    // card.setBackground(Color.WHITE);
    // card.setBorder(BorderFactory.createCompoundBorder(
    // BorderFactory.createLineBorder(new Color(220, 220, 220)),
    // BorderFactory.createEmptyBorder(20, 20, 20, 20)));

    // JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
    // titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    // titleLabel.setForeground(new Color(100, 100, 100));

    // JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
    // valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
    // valueLabel.setForeground(color);

    // card.add(titleLabel, BorderLayout.NORTH);
    // card.add(valueLabel, BorderLayout.CENTER);

    // return card;
    // }

    private JPanel createEnhancedSummaryCard(String title, String value, Color color, String iconText) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(new Color(255, 255, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 4, new Color(0, 0, 0, 80)),
                card.getBorder()));
        card.setPreferredSize(new Dimension(200, 150));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(color);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33));

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JButton createEnhancedPortfolioButton(String text, Color bgColor, String iconText) {
        JButton button = new JButton();
        button.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1, true),
                BorderFactory.createEmptyBorder(15, 40, 15, 40)));
        button.setPreferredSize(new Dimension(200, 60));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(Color.WHITE);

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        textLabel.setForeground(Color.black); // توحيد اللون مع الأيقونة

        button.add(iconLabel);
        button.add(textLabel);

        Color originalColor = bgColor;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(originalColor.darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });

        return button;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.black); // تغيير لون النص للأبيض ليكون أكثر وضوحًا
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        Color originalColor = bgColor;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(originalColor.darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });

        button.setBorder(BorderFactory.createLineBorder(new Color(0, 80, 160), 1));
        button.setOpaque(true);
        button.setBorderPainted(true);

        return button;
    }

    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel userLabel = new JLabel("User ID: " + userId);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(userLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private boolean hasAssets() {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Assets WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking assets: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
    }

    private ImageIcon createIcon(String path) {
        try {
            return new ImageIcon(new ImageIcon(getClass().getResource(path))
                    .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("Error loading icon: " + path);
            return createTextIcon(path.replace("/icons/", "").replace(".png", ""));
        }
    }

    private ImageIcon createTextIcon(String text) {
        BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(0, 102, 204));
        g2d.fillRect(0, 0, 20, 20);
        g2d.setColor(Color.black);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString(text.substring(0, 1), 6, 15);
        g2d.dispose();
        return new ImageIcon(img);
    }
}
