package views;

import javax.swing.*;

import database.DatabaseConnection;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginView extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckBox;

    public LoginView() {
        initializeUI();
    }

    private void initializeUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setTitle("Investment Management - Login");
        setSize(900, 510);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel);

        JPanel sidePanel = createSidePanel();
        mainPanel.add(sidePanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        // Set a subtle background color to differentiate from the header
        formPanel.setBackground(new Color(245, 245, 245)); // Very light gray
        formPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER; // Center-align components

        // Main Title: "Investment Management"
        JLabel titleLabel = new JLabel("Investment Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30)); // Larger and bold
        titleLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(titleLabel, gbc);

        // Add some vertical spacing between title and subtitle
        gbc.gridy = 1;
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)), gbc);

        // Subtitle: "Islamic Finance App"
        JLabel subtitleLabel = new JLabel("Islamic Finance App");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 20)); // Italic and smaller
        subtitleLabel.setForeground(new Color(100, 100, 100)); // Slightly lighter color
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        formPanel.add(subtitleLabel, gbc);

        // Add some vertical spacing before the email field
        gbc.gridy = 3;
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)), gbc);

        // Email Label and Field
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(Color.BLACK);
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST; // Align left for form fields
        formPanel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setForeground(Color.GRAY);
        emailField.setText("your@email.com");
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        emailField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (emailField.getText().equals("your@email.com")) {
                    emailField.setText("");
                    emailField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (emailField.getText().isEmpty()) {
                    emailField.setText("your@email.com");
                    emailField.setForeground(Color.GRAY);
                }
            }
        });
        gbc.gridy = 5;
        formPanel.add(emailField, gbc);

        // Password Label and Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(Color.BLACK);
        gbc.gridy = 6;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        gbc.gridy = 7;
        formPanel.add(passwordField, gbc);

        // Options Panel (Remember me and Forgot password)
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.setBackground(new Color(245, 245, 245)); // Match formPanel background
        rememberMeCheckBox = new JCheckBox("Remember me");
        rememberMeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeCheckBox.setBackground(new Color(245, 245, 245));
        optionsPanel.add(rememberMeCheckBox);

        JLabel forgotPasswordLabel = new JLabel("Forgot password?");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(new Color(0, 150, 136));
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        optionsPanel.add(forgotPasswordLabel);

        gbc.gridy = 8;
        formPanel.add(optionsPanel, gbc);

        // Sign-in Button
        JButton loginButton = createStyledButton("Sign in", new Color(0, 150, 136));
        loginButton.addActionListener(this::performLogin);
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        formPanel.add(loginButton, gbc);

        // Sign-up Link
        JLabel signupLabel = new JLabel("Don't have an account? Sign up");
        signupLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        signupLabel.setForeground(new Color(0, 150, 136));
        signupLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterView().setVisible(true);
                dispose();
            }
        });
        gbc.gridy = 10;
        formPanel.add(signupLabel, gbc);

        return formPanel;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new GradientPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(50, 30, 50, 30));

        JLabel sideTitle = new JLabel(
                "<html><div style='text-align: center;'>Manage your wealth according to islamic principles</div></html>");
        sideTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        sideTitle.setForeground(Color.WHITE);
        sideTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(sideTitle);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 30)));

        sidePanel.add(createPoint("🖥", "Calculate Zakat accurately based on islamic jurisprudence"));
        sidePanel.add(createPoint("📊", "Track multiple types of assets in one place"));
        sidePanel.add(createPoint("📄", "Generate detailed Zakat reports"));
        sidePanel.add(createPoint("✔", "Ensure compliance with islamic financial principles"));

        return sidePanel;
    }

    private JPanel createPoint(String icon, String text) {
        JPanel pointPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pointPanel.setOpaque(false);

        JLabel pointIcon = new JLabel(icon);
        pointIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        pointIcon.setForeground(Color.WHITE);

        JLabel pointText = new JLabel("<html><div style='width:250px;'>" + text + "</div></html>");
        pointText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pointText.setForeground(Color.WHITE);

        pointPanel.add(pointIcon);
        pointPanel.add(pointText);
        return pointPanel;
    }

    private void performLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password", "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT user_id, name FROM Users WHERE email = ? AND password = ?")) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new DashboardView(userId).setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "System Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        button.setOpaque(true);
        button.setBorderPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
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
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // Gradient Panel class
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color startColor = new Color(0, 150, 136);
            Color endColor = new Color(0, 105, 92);
            int width = getWidth();
            int height = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, startColor, 0, height, endColor);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    }
}
