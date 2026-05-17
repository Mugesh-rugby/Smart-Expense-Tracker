package view;

import model.User;
import utils.AppConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MainView - The main application shell with sidebar navigation.
 * Contains a sidebar + content area; child panels are swapped via CardLayout.
 */
public class MainView extends JFrame {

    private User            currentUser;
    private CardLayout      contentLayout;
    private JPanel          contentPanel;
    private boolean         darkMode = false;

    // Navigation buttons
    private Map<String, JButton> navButtons = new LinkedHashMap<>();

    // Child views (lazy-loaded)
    private DashboardPanel    dashboardPanel;
    private TransactionPanel  transactionPanel;
    private IncomePanel       incomePanel;
    private ReportsPanel      reportsPanel;

    // Panel names
    private static final String PANEL_DASHBOARD   = "DASHBOARD";
    private static final String PANEL_EXPENSES    = "EXPENSES";
    private static final String PANEL_INCOME      = "INCOME";
    private static final String PANEL_REPORTS     = "REPORTS";

    public MainView(User user) {
        this.currentUser = user;
        initUI();
    }

    private void initUI() {
        setTitle(AppConstants.APP_NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(),   BorderLayout.WEST);
        root.add(buildContent(),   BorderLayout.CENTER);

        setContentPane(root);
        showPanel(PANEL_DASHBOARD);
        setVisible(true);
    }

    // ── Sidebar ────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppConstants.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(AppConstants.SIDEBAR_WIDTH, 0));

        // Logo area
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 18));
        logoArea.setBackground(AppConstants.BG_SIDEBAR);
        logoArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel logoIcon = new JLabel("💰");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel logoText = new JLabel(AppConstants.APP_NAME);
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoText.setForeground(Color.WHITE);
        logoArea.add(logoIcon);
        logoArea.add(logoText);
        sidebar.add(logoArea);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 30));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Nav section label
        sidebar.add(sidebarSectionLabel("MENU"));

        // Navigation items
        addNavItem(sidebar, "🏠  Dashboard",      PANEL_DASHBOARD);
        addNavItem(sidebar, "💸  Expenses",        PANEL_EXPENSES);
        addNavItem(sidebar, "💰  Income",          PANEL_INCOME);
        addNavItem(sidebar, "📊  Reports",         PANEL_REPORTS);

        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(sidebarSectionLabel("SETTINGS"));

        // Dark mode toggle
        JButton darkModeBtn = navButtonBase("🌙  Dark Mode");
        darkModeBtn.addActionListener(e -> toggleDarkMode());
        sidebar.add(darkModeBtn);

        // Push remaining to bottom
        sidebar.add(Box.createVerticalGlue());

        // User profile section at bottom
        sidebar.add(buildUserSection());

        return sidebar;
    }

    private void addNavItem(JPanel sidebar, String label, String panelName) {
        JButton btn = navButtonBase(label);
        btn.addActionListener(e -> showPanel(panelName));
        navButtons.put(panelName, btn);
        sidebar.add(btn);
    }

    private JButton navButtonBase(String label) {
        JButton btn = new JButton(label);
        btn.setFont(AppConstants.FONT_BODY);
        btn.setForeground(AppConstants.TEXT_MUTED);
        btn.setBackground(AppConstants.BG_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(AppConstants.PRIMARY))
                    btn.setBackground(new Color(35, 45, 80));
            }
            public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(AppConstants.PRIMARY))
                    btn.setBackground(AppConstants.BG_SIDEBAR);
            }
        });
        return btn;
    }

    private JLabel sidebarSectionLabel(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(120, 130, 160));
        lbl.setBorder(new EmptyBorder(8, 14, 4, 14));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return lbl;
    }

    private JPanel buildUserSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        panel.setBackground(new Color(15, 22, 45));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Avatar circle
        JLabel avatar = new JLabel(String.valueOf(currentUser.getFirstName().charAt(0)).toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConstants.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(38, 38));

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        JLabel nameLabel = new JLabel(currentUser.getFirstName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(Color.WHITE);
        JLabel userBtn = new JLabel("<html><u>Logout</u></html>");
        userBtn.setFont(AppConstants.FONT_SMALL);
        userBtn.setForeground(new Color(150, 160, 200));
        userBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { handleLogout(); }
        });
        namePanel.add(nameLabel);
        namePanel.add(userBtn);

        panel.add(avatar);
        panel.add(namePanel);
        return panel;
    }

    // ── Content area ───────────────────────────────────────────────────────

    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(AppConstants.BG_MAIN);

        // Instantiate child panels
        try {
            dashboardPanel   = new DashboardPanel(currentUser, this);
            transactionPanel = new TransactionPanel(currentUser, this);
            incomePanel      = new IncomePanel(currentUser, this);
            reportsPanel     = new ReportsPanel(currentUser);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error initializing panels: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        contentPanel.add(dashboardPanel,   PANEL_DASHBOARD);
        contentPanel.add(transactionPanel, PANEL_EXPENSES);
        contentPanel.add(incomePanel,      PANEL_INCOME);
        contentPanel.add(reportsPanel,     PANEL_REPORTS);

        return contentPanel;
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    public void showPanel(String panelName) {
        contentLayout.show(contentPanel, panelName);

        // Update selected nav button
        navButtons.forEach((name, btn) -> {
            boolean selected = name.equals(panelName);
            btn.setBackground(selected ? AppConstants.PRIMARY : AppConstants.BG_SIDEBAR);
            btn.setForeground(selected ? Color.WHITE : AppConstants.TEXT_MUTED);
        });

        // Refresh panel data when switching
        try {
            switch (panelName) {
                case PANEL_DASHBOARD   -> dashboardPanel.refresh();
                case PANEL_EXPENSES    -> transactionPanel.refresh();
                case PANEL_INCOME      -> incomePanel.refresh();
                case PANEL_REPORTS     -> reportsPanel.refresh();
            }
        } catch (Exception e) {
            System.err.println("Refresh error: " + e.getMessage());
        }
    }

    // ── Dark mode ──────────────────────────────────────────────────────────

    private void toggleDarkMode() {
        darkMode = !darkMode;
        // Apply dark/light palette
        Color bg   = darkMode ? AppConstants.DARK_BG_MAIN   : AppConstants.BG_MAIN;
        Color card = darkMode ? AppConstants.DARK_BG_CARD   : AppConstants.BG_CARD;
        Color text = darkMode ? AppConstants.DARK_TEXT       : AppConstants.TEXT_PRIMARY;

        applyTheme(getContentPane(), bg, card, text);
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
        UIHelper.showToast(this, darkMode ? "Dark mode enabled" : "Light mode enabled", true);
    }

    private void applyTheme(Container container, Color bg, Color card, Color text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JPanel p) {
                if (!p.getBackground().equals(AppConstants.BG_SIDEBAR) &&
                    !p.getBackground().equals(new Color(15, 22, 45))) {
                    p.setBackground(bg);
                }
                applyTheme(p, bg, card, text);
            } else if (c instanceof JLabel lbl) {
                if (!lbl.getForeground().equals(Color.WHITE)) lbl.setForeground(text);
            }
        }
    }

    // ── Logout ─────────────────────────────────────────────────────────────

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            database.DBConnection.closeConnection();
            SwingUtilities.invokeLater(LoginView::new);
        }
    }

    /** Public accessor so child panels can trigger navigation. */
    public void navigate(String panel) { showPanel(panel); }

    public User getCurrentUser() { return currentUser; }
}
