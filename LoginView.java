package view;

import controller.AuthController;
import model.User;
import utils.AppConstants;
import utils.PasswordUtil;
import utils.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * LoginView - Login, Registration, and Password Reset screen.
 * Uses a card-flip style to switch between panels.
 */
public class LoginView extends JFrame {

    private AuthController authController;

    // Panels
    private JPanel    cardContainer;
    private CardLayout cardLayout;

    // Login form fields
    private JTextField     loginUsernameField;
    private JPasswordField loginPasswordField;
    private JLabel         loginErrorLabel;

    // Register form fields
    private JTextField     regFullNameField;
    private JTextField     regUsernameField;
    private JTextField     regEmailField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmField;
    private JProgressBar   strengthBar;
    private JLabel         strengthLabel;
    private JLabel         regErrorLabel;

    // Reset password fields
    private JTextField     resetEmailField;
    private JPasswordField resetNewPassField;
    private JPasswordField resetConfirmField;
    private JLabel         resetErrorLabel;

    private static final String PANEL_LOGIN    = "LOGIN";
    private static final String PANEL_REGISTER = "REGISTER";
    private static final String PANEL_RESET    = "RESET";

    public LoginView() {
        try {
            authController = new AuthController();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to database.\n" + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        initUI();
    }

    private void initUI() {
        setTitle(AppConstants.APP_NAME + " — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(false);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(null);
        setResizable(false);

        // Root panel: gradient left + white right
        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };

        root.add(buildBrandPanel());
        root.add(buildFormPanel());

        setContentPane(root);
        setVisible(true);
    }

    // ── Left brand panel ───────────────────────────────────────────────────

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(67, 97, 238),
                    getWidth(), getHeight(), new Color(114, 9, 183));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Logo emoji
        JLabel logo = new JLabel("💰");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // App name
        JLabel name = new JLabel(AppConstants.APP_NAME);
        name.setFont(new Font("Segoe UI", Font.BOLD, 26));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tagline
        JLabel tagline = new JLabel("<html><center>Track · Analyze · Save<br>Your money, your control.</center></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(new Color(255, 255, 255, 200));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);

        // Features list
        String[] features = {"✓  Income & Expense Tracking", "✓  Category Analysis",
                             "✓  Monthly Reports", "✓  Dark Mode Support",
                             "✓  PDF Export", "✓  Budget Alerts"};
        JPanel featurePanel = new JPanel();
        featurePanel.setLayout(new BoxLayout(featurePanel, BoxLayout.Y_AXIS));
        featurePanel.setOpaque(false);
        featurePanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        for (String f : features) {
            JLabel fl = new JLabel(f);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            fl.setForeground(new Color(255, 255, 255, 180));
            fl.setAlignmentX(Component.CENTER_ALIGNMENT);
            featurePanel.add(fl);
            featurePanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        content.add(logo);
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(name);
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(tagline);
        content.add(featurePanel);

        panel.add(content);
        return panel;
    }

    // ── Right form panel ───────────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(0, 30, 0, 30));

        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(Color.WHITE);
        cardContainer.add(buildLoginForm(),    PANEL_LOGIN);
        cardContainer.add(buildRegisterForm(), PANEL_REGISTER);
        cardContainer.add(buildResetForm(),    PANEL_RESET);

        wrapper.add(cardContainer, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Login form ─────────────────────────────────────────────────────────

    private JPanel buildLoginForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1;
        gbc.gridwidth = 2;

        int row = 0;

        // Title
        JLabel title = new JLabel("Welcome back 👋");
        title.setFont(AppConstants.FONT_TITLE);
        title.setForeground(AppConstants.TEXT_PRIMARY);
        gbc.gridy = row++; panel.add(title, gbc);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(AppConstants.FONT_BODY);
        sub.setForeground(AppConstants.TEXT_SECONDARY);
        gbc.gridy = row++; panel.add(sub, gbc);

        gbc.gridy = row++; panel.add(Box.createRigidArea(new Dimension(0, 10)), gbc);

        // Username
        gbc.gridy = row++; panel.add(formLabel("Username"), gbc);
        loginUsernameField = UIHelper.styledTextField("");
        loginUsernameField.setForeground(AppConstants.TEXT_PRIMARY);
        gbc.gridy = row++; panel.add(loginUsernameField, gbc);

        // Password
        gbc.gridy = row++; panel.add(formLabel("Password"), gbc);
        loginPasswordField = UIHelper.styledPasswordField("");
        gbc.gridy = row++; panel.add(loginPasswordField, gbc);

        // Error label
        loginErrorLabel = UIHelper.errorLabel(" ");
        gbc.gridy = row++; panel.add(loginErrorLabel, gbc);

        // Login button
        JButton loginBtn = UIHelper.primaryButton("Sign In  →");
        loginBtn.setPreferredSize(new Dimension(300, 42));
        loginBtn.addActionListener(e -> handleLogin());
        gbc.gridy = row++; panel.add(loginBtn, gbc);

        // Enter key support
        loginPasswordField.addActionListener(e -> handleLogin());

        // Divider
        gbc.gridy = row++; panel.add(UIHelper.divider(), gbc);

        // Register link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkPanel.setBackground(Color.WHITE);
        linkPanel.add(UIHelper.bodyLabel("Don't have an account?"));
        JButton regLink = linkButton("Create one");
        regLink.addActionListener(e -> cardLayout.show(cardContainer, PANEL_REGISTER));
        linkPanel.add(regLink);
        gbc.gridy = row++; panel.add(linkPanel, gbc);

        // Forgot password
        JButton forgotBtn = linkButton("Forgot password?");
        forgotBtn.addActionListener(e -> cardLayout.show(cardContainer, PANEL_RESET));
        JPanel fp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fp.setBackground(Color.WHITE);
        fp.add(forgotBtn);
        gbc.gridy = row++; panel.add(fp, gbc);

        // Demo hint
        JLabel demoHint = new JLabel("Demo: username=demo  password=Demo@1234");
        demoHint.setFont(AppConstants.FONT_SMALL);
        demoHint.setForeground(new Color(150, 150, 180));
        demoHint.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = row++; panel.add(demoHint, gbc);

        return panel;
    }

    // ── Register form ──────────────────────────────────────────────────────

    private JPanel buildRegisterForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.weightx = 1;
        gbc.gridwidth = 2;

        int row = 0;

        JLabel title = new JLabel("Create Account");
        title.setFont(AppConstants.FONT_TITLE);
        title.setForeground(AppConstants.TEXT_PRIMARY);
        gbc.gridy = row++; panel.add(title, gbc);

        JLabel sub = new JLabel("Start tracking your finances today");
        sub.setFont(AppConstants.FONT_BODY);
        sub.setForeground(AppConstants.TEXT_SECONDARY);
        gbc.gridy = row++; panel.add(sub, gbc);

        gbc.gridy = row++; panel.add(formLabel("Full Name"), gbc);
        regFullNameField = UIHelper.styledTextField("");
        regFullNameField.setForeground(AppConstants.TEXT_PRIMARY);
        gbc.gridy = row++; panel.add(regFullNameField, gbc);

        gbc.gridy = row++; panel.add(formLabel("Username"), gbc);
        regUsernameField = UIHelper.styledTextField("");
        regUsernameField.setForeground(AppConstants.TEXT_PRIMARY);
        gbc.gridy = row++; panel.add(regUsernameField, gbc);

        gbc.gridy = row++; panel.add(formLabel("Email"), gbc);
        regEmailField = UIHelper.styledTextField("");
        regEmailField.setForeground(AppConstants.TEXT_PRIMARY);
        gbc.gridy = row++; panel.add(regEmailField, gbc);

        gbc.gridy = row++; panel.add(formLabel("Password"), gbc);
        regPasswordField = UIHelper.styledPasswordField("");
        regPasswordField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { updateStrengthBar(); }
        });
        gbc.gridy = row++; panel.add(regPasswordField, gbc);

        // Strength bar
        strengthBar = new JProgressBar(0, 100);
        strengthBar.setPreferredSize(new Dimension(300, 6));
        strengthBar.setBorderPainted(false);
        strengthBar.setBackground(AppConstants.BORDER_COLOR);
        strengthBar.setForeground(AppConstants.DANGER);
        gbc.gridy = row++; panel.add(strengthBar, gbc);

        strengthLabel = UIHelper.errorLabel(" ");
        gbc.gridy = row++; panel.add(strengthLabel, gbc);

        gbc.gridy = row++; panel.add(formLabel("Confirm Password"), gbc);
        regConfirmField = UIHelper.styledPasswordField("");
        gbc.gridy = row++; panel.add(regConfirmField, gbc);

        regErrorLabel = UIHelper.errorLabel(" ");
        gbc.gridy = row++; panel.add(regErrorLabel, gbc);

        JButton registerBtn = UIHelper.successButton("Create Account  →");
        registerBtn.setPreferredSize(new Dimension(300, 42));
        registerBtn.addActionListener(e -> handleRegister());
        gbc.gridy = row++; panel.add(registerBtn, gbc);

        gbc.gridy = row++; panel.add(UIHelper.divider(), gbc);

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backPanel.setBackground(Color.WHITE);
        backPanel.add(UIHelper.bodyLabel("Already have an account?"));
        JButton backLink = linkButton("Sign in");
        backLink.addActionListener(e -> cardLayout.show(cardContainer, PANEL_LOGIN));
        backPanel.add(backLink);
        gbc.gridy = row++; panel.add(backPanel, gbc);

        return panel;
    }

    // ── Reset password form ────────────────────────────────────────────────

    private JPanel buildResetForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1;
        gbc.gridwidth = 2;

        int row = 0;

        JLabel title = new JLabel("Reset Password 🔑");
        title.setFont(AppConstants.FONT_TITLE);
        title.setForeground(AppConstants.TEXT_PRIMARY);
        gbc.gridy = row++; panel.add(title, gbc);

        JLabel sub = new JLabel("Enter your registered email to reset");
        sub.setFont(AppConstants.FONT_BODY);
        sub.setForeground(AppConstants.TEXT_SECONDARY);
        gbc.gridy = row++; panel.add(sub, gbc);

        gbc.gridy = row++; panel.add(Box.createRigidArea(new Dimension(0, 10)), gbc);

        gbc.gridy = row++; panel.add(formLabel("Email Address"), gbc);
        resetEmailField = UIHelper.styledTextField("");
        resetEmailField.setForeground(AppConstants.TEXT_PRIMARY);
        gbc.gridy = row++; panel.add(resetEmailField, gbc);

        gbc.gridy = row++; panel.add(formLabel("New Password"), gbc);
        resetNewPassField = UIHelper.styledPasswordField("");
        gbc.gridy = row++; panel.add(resetNewPassField, gbc);

        gbc.gridy = row++; panel.add(formLabel("Confirm New Password"), gbc);
        resetConfirmField = UIHelper.styledPasswordField("");
        gbc.gridy = row++; panel.add(resetConfirmField, gbc);

        resetErrorLabel = UIHelper.errorLabel(" ");
        gbc.gridy = row++; panel.add(resetErrorLabel, gbc);

        JButton resetBtn = UIHelper.primaryButton("Reset Password");
        resetBtn.setPreferredSize(new Dimension(300, 42));
        resetBtn.addActionListener(e -> handleResetPassword());
        gbc.gridy = row++; panel.add(resetBtn, gbc);

        gbc.gridy = row++; panel.add(UIHelper.divider(), gbc);

        JButton backLink = linkButton("← Back to Login");
        backLink.addActionListener(e -> cardLayout.show(cardContainer, PANEL_LOGIN));
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bp.setBackground(Color.WHITE);
        bp.add(backLink);
        gbc.gridy = row++; panel.add(bp, gbc);

        return panel;
    }

    // ── Action handlers ────────────────────────────────────────────────────

    private void handleLogin() {
        loginErrorLabel.setText(" ");
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        try {
            User user = authController.login(username, password);
            dispose();
            SwingUtilities.invokeLater(() -> new MainView(user));
        } catch (Exception ex) {
            loginErrorLabel.setText("✗  " + ex.getMessage());
            loginPasswordField.setText("");
        }
    }

    private void handleRegister() {
        regErrorLabel.setText(" ");
        try {
            authController.register(
                regFullNameField.getText().trim(),
                regUsernameField.getText().trim(),
                regEmailField.getText().trim(),
                new String(regPasswordField.getPassword()),
                new String(regConfirmField.getPassword())
            );
            JOptionPane.showMessageDialog(this,
                "Account created successfully! Please sign in.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(cardContainer, PANEL_LOGIN);
            loginUsernameField.setText(regUsernameField.getText().trim());
        } catch (Exception ex) {
            regErrorLabel.setText("✗  " + ex.getMessage());
        }
    }

    private void handleResetPassword() {
        resetErrorLabel.setText(" ");
        try {
            authController.resetPassword(
                resetEmailField.getText().trim(),
                new String(resetNewPassField.getPassword()),
                new String(resetConfirmField.getPassword())
            );
            JOptionPane.showMessageDialog(this,
                "Password reset successfully! Please sign in with your new password.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(cardContainer, PANEL_LOGIN);
        } catch (Exception ex) {
            resetErrorLabel.setText("✗  " + ex.getMessage());
        }
    }

    private void updateStrengthBar() {
        String pass = new String(regPasswordField.getPassword());
        int score = ValidationUtil.passwordStrengthScore(pass);
        strengthBar.setValue(score);
        if (score < 40) {
            strengthBar.setForeground(AppConstants.DANGER);
            strengthLabel.setText("Weak password");
            strengthLabel.setForeground(AppConstants.DANGER);
        } else if (score < 70) {
            strengthBar.setForeground(AppConstants.WARNING);
            strengthLabel.setText("Moderate password");
            strengthLabel.setForeground(AppConstants.WARNING);
        } else {
            strengthBar.setForeground(AppConstants.SUCCESS);
            strengthLabel.setText(PasswordUtil.getPasswordStrengthMessage(pass));
            strengthLabel.setForeground(AppConstants.SUCCESS);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_BOLD);
        lbl.setForeground(AppConstants.TEXT_PRIMARY);
        return lbl;
    }

    private JButton linkButton(String text) {
        JButton btn = new JButton("<html><u>" + text + "</u></html>");
        btn.setFont(AppConstants.FONT_BODY);
        btn.setForeground(AppConstants.PRIMARY);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }
}
