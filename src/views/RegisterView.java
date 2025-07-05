package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.border.Border;

import database.DatabaseConnection;

public class RegisterView extends JFrame {
    private JTextField firstNameField, lastNameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel firstNameErrorLabel, lastNameErrorLabel, emailErrorLabel, passwordErrorLabel,
            confirmPasswordErrorLabel;

    public RegisterView() {
        initializeUI();
    }

    private void initializeUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setTitle("Zakat Calculator - Register");
        // ضبط الحجم بنسبة من حجم الشاشة
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.6); // 60% من عرض الشاشة
        int height = (int) (screenSize.height * 0.7); // 70% من ارتفاع الشاشة
        setSize(width, height);
        setMinimumSize(new Dimension(400, 300)); // الحد الأدنى للشاشات الصغيرة
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel formPanel = createFormPanel();
        mainPanel.add(new JScrollPane(formPanel)); // شريط تمرير لنموذج التسجيل

        JPanel sidePanel = createSidePanel();
        mainPanel.add(sidePanel); // بدون شريط تمرير للوحة الجانبية

        add(mainPanel, BorderLayout.CENTER);

        // ضبط حجم الخطوط عند تغيير حجم النافذة
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustFontSizes();
            }
        });
    }

    private void adjustFontSizes() {
        int width = getWidth();
        float fontScale = width < 500 ? 0.65f : width > 1000 ? 1.15f : 1.0f;

        Component[] components = ((JPanel) getContentPane().getComponent(0)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                Component view = ((JScrollPane) comp).getViewport().getView();
                if (view instanceof JPanel) {
                    for (Component subComp : ((JPanel) view).getComponents()) {
                        if (subComp instanceof JLabel) {
                            JLabel label = (JLabel) subComp;
                            float newSize = label.getFont().getSize() * fontScale;
                            label.setFont(label.getFont().deriveFont(newSize));
                        } else if (subComp instanceof JTextField || subComp instanceof JPasswordField) {
                            JTextField field = (JTextField) subComp;
                            float newSize = field.getFont().getSize() * fontScale;
                            field.setFont(field.getFont().deriveFont(newSize));
                        } else if (subComp instanceof JButton) {
                            JButton button = (JButton) subComp;
                            float newSize = button.getFont().getSize() * fontScale;
                            button.setFont(button.getFont().deriveFont(newSize));
                        }
                    }
                }
            } else if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JLabel) {
                        JLabel label = (JLabel) subComp;
                        float newSize = label.getFont().getSize() * fontScale;
                        label.setFont(label.getFont().deriveFont(newSize));
                    }
                }
            }
        }
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        formPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // العنوان الرئيسي
        JLabel titleLabel = new JLabel("Investment Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        // العنوان الفرعي
        JLabel subtitleLabel = new JLabel("Islamic Finance App");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        formPanel.add(subtitleLabel, gbc);

        // تسمية وحقل الاسم الأول
        JLabel firstNameLabel = new JLabel("First Name");
        firstNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        firstNameLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(firstNameLabel, gbc);

        firstNameField = new JTextField();
        firstNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        firstNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(firstNameField, gbc);

        firstNameErrorLabel = new JLabel("");
        firstNameErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        firstNameErrorLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(firstNameErrorLabel, gbc);

        // تسمية وحقل الاسم الأخير
        JLabel lastNameLabel = new JLabel("Last Name");
        lastNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lastNameLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(lastNameLabel, gbc);

        lastNameField = new JTextField();
        lastNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lastNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(lastNameField, gbc);

        lastNameErrorLabel = new JLabel("");
        lastNameErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lastNameErrorLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(lastNameErrorLabel, gbc);

        // تسمية وحقل البريد الإلكتروني
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(emailLabel, gbc);

        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        gbc.gridx = 0;
        gbc.gridy = 9;
        formPanel.add(emailField, gbc);

        emailErrorLabel = new JLabel("");
        emailErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        emailErrorLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 10;
        formPanel.add(emailErrorLabel, gbc);

        // تسمية وحقل كلمة المرور
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 11;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        gbc.gridx = 0;
        gbc.gridy = 12;
        formPanel.add(passwordField, gbc);

        passwordErrorLabel = new JLabel("");
        passwordErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        passwordErrorLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 13;
        formPanel.add(passwordErrorLabel, gbc);

        // تسمية وحقل تأكيد كلمة المرور
        JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        confirmPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        confirmPasswordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 14;
        formPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        gbc.gridx = 0;
        gbc.gridy = 15;
        formPanel.add(confirmPasswordField, gbc);

        confirmPasswordErrorLabel = new JLabel("");
        confirmPasswordErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        confirmPasswordErrorLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 16;
        formPanel.add(confirmPasswordErrorLabel, gbc);

        // إضافة KeyListener لحقول كلمة المرور
        KeyListener resetPasswordError = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                Border defaultBorder = BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6));
                passwordField.setBorder(defaultBorder);
                confirmPasswordField.setBorder(defaultBorder);
                passwordErrorLabel.setText("");
                confirmPasswordErrorLabel.setText("");
            }
        };
        passwordField.addKeyListener(resetPasswordError);
        confirmPasswordField.addKeyListener(resetPasswordError);

        // زر التسجيل
        JButton registerButton = createStyledButton("Register", new Color(0, 150, 136));
        registerButton.addActionListener(this::performRegistration);
        gbc.gridx = 0;
        gbc.gridy = 17;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(registerButton, gbc);

        // رابط العودة إلى تسجيل الدخول
        JLabel loginLabel = new JLabel("Already have an account? Sign in");
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginLabel.setForeground(new Color(0, 150, 136));
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginView().setVisible(true);
                dispose();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 18;
        formPanel.add(loginLabel, gbc);

        return formPanel;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new GradientPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(50, 8, 10, 8));

        JLabel sideTitle = new JLabel(
                "<html><div style='width: 90%; text-align: center;'>Manage your wealth according to islamic principles</div></html>");
        sideTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        sideTitle.setForeground(Color.WHITE);
        sideTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        sidePanel.add(sideTitle);
        sidePanel.add(Box.createVerticalGlue());

        sidePanel.add(createPoint("🖥", "Calculate Zakat accurately based on islamic jurisprudence."));
        sidePanel.add(createPoint("📊", "Track multiple types of assets in one place."));
        sidePanel.add(createPoint("📄", "Generate detailed Zakat reports."));
        sidePanel.add(createPoint("✔", "Ensure compliance with islamic financial principles."));
        sidePanel.add(createPoint("📈", "Plan your investments in line with Sharia-compliant strategies."));
        sidePanel.add(createPoint("💰", "Monitor your financial growth with islamic financial tools."));
        sidePanel.add(createPoint("🔒", "Secure your savings through interest-free financial solutions."));
        sidePanel.add(Box.createVerticalGlue());

        return sidePanel;
    }

    private JPanel createPoint(String icon, String text) {
        JPanel pointPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        pointPanel.setOpaque(false);
        pointPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel pointIcon = new JLabel(icon);
        pointIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        pointIcon.setForeground(Color.WHITE);

        JLabel pointText = new JLabel("<html><div style='width: 90%;'>" + text + "</div></html>");
        pointText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pointText.setForeground(Color.WHITE);

        pointPanel.add(pointIcon);
        pointPanel.add(pointText);
        return pointPanel;
    }

    private void performRegistration(ActionEvent e) {
        Border defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6));
        Border errorBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6));

        firstNameField.setBorder(defaultBorder);
        lastNameField.setBorder(defaultBorder);
        emailField.setBorder(defaultBorder);
        passwordField.setBorder(defaultBorder);
        confirmPasswordField.setBorder(defaultBorder);
        firstNameErrorLabel.setText("");
        lastNameErrorLabel.setText("");
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        boolean hasError = false;
        if (firstName.isEmpty()) {
            firstNameField.setBorder(errorBorder);
            firstNameErrorLabel.setText("Please enter first name");
            hasError = true;
        }
        if (lastName.isEmpty()) {
            lastNameField.setBorder(errorBorder);
            lastNameErrorLabel.setText("Please enter last name");
            hasError = true;
        }
        if (email.isEmpty()) {
            emailField.setBorder(errorBorder);
            emailErrorLabel.setText("Please enter email");
            hasError = true;
        }
        if (password.isEmpty()) {
            passwordField.setBorder(errorBorder);
            passwordErrorLabel.setText("Please enter password");
            hasError = true;
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordField.setBorder(errorBorder);
            confirmPasswordErrorLabel.setText("Please confirm password");
            hasError = true;
        }
        if (hasError) {
            return;
        }

        if (!isValidEmail(email)) {
            emailField.setBorder(errorBorder);
            emailErrorLabel.setText("Invalid email. Example: example@domain.com");
            return;
        }

        if (!password.equals(confirmPassword)) {
            passwordField.setBorder(errorBorder);
            confirmPasswordField.setBorder(errorBorder);
            confirmPasswordErrorLabel.setText("Passwords do not match");
            return;
        }

        if (!isValidPassword(password)) {
            passwordField.setBorder(errorBorder);
            confirmPasswordField.setBorder(errorBorder);
            JOptionPane.showMessageDialog(this,
                    "Invalid password. It must:\n" +
                            "- Be at least 8 characters long\n" +
                            "- Include at least one uppercase letter\n" +
                            "- Include at least one lowercase letter\n" +
                            "- Include at least one special character (e.g., !@#$%)",
                    "Password Requirements",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fullName = firstName + " " + lastName;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkEmailQuery = "SELECT COUNT(*) FROM Users WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailQuery)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    emailField.setBorder(errorBorder);
                    emailErrorLabel.setText("This email is already registered");
                    return;
                }
            }

            String insertQuery = "INSERT INTO Users (name, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, fullName);
                stmt.setString(2, email);
                stmt.setString(3, password);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Registration successful! You can now log in.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    new LoginView().setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Registration failed. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + ex.getMessage(),
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*]).{8,}$";
        return password.matches(passwordPattern);
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }

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

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 30, 6, 30));
        button.setOpaque(true);
        button.setBorderPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(bgColor.darker());
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(bgColor);
                button.setForeground(Color.WHITE);
            }
        });

        return button;
    }
}
