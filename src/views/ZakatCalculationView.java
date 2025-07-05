package views;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.DecimalFormat;

public class ZakatCalculationView extends JFrame {
    // UI Components
    private JTextField cashField, goldField, silverField, investmentsField, debtsField;
    private JLabel resultLabel;
    private int userId;

    // Strategy Pattern Components
    private ZakatCalculationStrategy calculationStrategy;

    // Interface for Strategy Pattern
    public interface ZakatCalculationStrategy {
        ZakatCalculationResult calculate(double cash, double gold, double silver, double investments, double debts);
    }

    // Default Strategy Implementation
    public class DefaultZakatCalculationStrategy implements ZakatCalculationStrategy {
        @Override
        public ZakatCalculationResult calculate(double cash, double gold, double silver, double investments,
                double debts) {
            double totalWealth = cash + (gold * 60) + (silver * 0.75) + investments;
            double netWealth = totalWealth - debts;
            double zakatAmount = (netWealth >= 5000) ? netWealth * 0.025 : 0;
            return new ZakatCalculationResult(totalWealth, netWealth, zakatAmount);
        }
    }

    // Result Container
    public class ZakatCalculationResult {
        private final double totalWealth;
        private final double netWealth;
        private final double zakatAmount;

        public ZakatCalculationResult(double totalWealth, double netWealth, double zakatAmount) {
            this.totalWealth = totalWealth;
            this.netWealth = netWealth;
            this.zakatAmount = zakatAmount;
        }

        public double getTotalWealth() {
            return totalWealth;
        }

        public double getNetWealth() {
            return netWealth;
        }

        public double getZakatAmount() {
            return zakatAmount;
        }
    }

    // Constructor
    public ZakatCalculationView(int userId) {
        this.userId = userId;
        this.calculationStrategy = new DefaultZakatCalculationStrategy();
        initializeUI();
    }

    // Strategy Setter
    public void setCalculationStrategy(ZakatCalculationStrategy strategy) {
        this.calculationStrategy = strategy;
    }

    // UI Initialization
    private void initializeUI() {
        setTitle("Zakat Calculator");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 51));
        JLabel headerLabel = new JLabel("Zakat Calculator", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        formPanel.setBackground(Color.WHITE);

        // Load Assets Button
        JButton loadAssetsBtn = new JButton("Load My Assets");
        styleButton(loadAssetsBtn, new Color(70, 130, 180));
        loadAssetsBtn.addActionListener(e -> loadAssetsFromDatabase());
        loadAssetsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(loadAssetsBtn);
        formPanel.add(Box.createVerticalStrut(20));

        // Form Fields
        formPanel.add(createFormField("Cash Savings:", cashField = new JTextField()));
        formPanel.add(createFormField("Gold Value (grams):", goldField = new JTextField()));
        formPanel.add(createFormField("Silver Value (grams):", silverField = new JTextField()));
        formPanel.add(createFormField("Investments Value:", investmentsField = new JTextField()));
        formPanel.add(createFormField("Debts/Liabilities:", debtsField = new JTextField()));

        // Calculate Button
        JButton calculateBtn = new JButton("Calculate Zakat");
        styleButton(calculateBtn, new Color(0, 102, 51));
        calculateBtn.addActionListener(e -> calculateZakat());
        calculateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(calculateBtn);

        // Result Label
        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(resultLabel);

        add(formPanel, BorderLayout.CENTER);
    }

    // Helper Methods
    private JPanel createFormField(String label, JTextField field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);

        JLabel jLabel = new JLabel(label);
        jLabel.setPreferredSize(new Dimension(180, 30));
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        field.setPreferredSize(new Dimension(200, 30));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(jLabel);
        panel.add(field);
        panel.add(Box.createHorizontalStrut(10));

        return panel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

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

    // Database Operations
    private void loadAssetsFromDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT SUM(purchase_price * quantity) as total_value " +
                                "FROM Assets WHERE user_id = ? AND is_halal = 1")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                investmentsField.setText(String.format("%.2f", rs.getDouble("total_value")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading assets: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Zakat Calculation
    private void calculateZakat() {
        try {
            double cash = parseDouble(cashField.getText());
            double gold = parseDouble(goldField.getText());
            double silver = parseDouble(silverField.getText());
            double investments = parseDouble(investmentsField.getText());
            double debts = parseDouble(debtsField.getText());

            ZakatCalculationResult result = calculationStrategy.calculate(cash, gold, silver, investments, debts);

            DecimalFormat df = new DecimalFormat("#,##0.00");
            String resultText = "<html><b>Zakat Calculation Results:</b><br><br>" +
                    "Total Wealth: $" + df.format(result.getTotalWealth()) + "<br>" +
                    "After Debts: $" + df.format(result.getNetWealth()) + "<br><br>" +
                    "<b>Zakat Due: $" + df.format(result.getZakatAmount()) + "</b></html>";

            resultLabel.setText(resultText);
            resultLabel.setForeground(new Color(0, 102, 51));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers in all fields",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private double parseDouble(String text) throws NumberFormatException {
        if (text == null || text.trim().isEmpty())
            return 0;
        return Double.parseDouble(text.trim());
    }
}
