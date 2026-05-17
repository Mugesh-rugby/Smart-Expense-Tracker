package view;

import utils.AppConstants;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * UIHelper - Reusable factory methods for styled Swing components.
 * Abstraction layer that keeps views clean and consistent.
 */
public final class UIHelper {

    private UIHelper() {}

    // ── Buttons ────────────────────────────────────────────────────────────

    /** Solid primary button with hover animation. */
    public static JButton primaryButton(String text) {
        JButton btn = createRoundButton(text, AppConstants.PRIMARY, Color.WHITE);
        addHoverEffect(btn, AppConstants.PRIMARY, AppConstants.PRIMARY_DARK, Color.WHITE, Color.WHITE);
        return btn;
    }

    /** Danger (red) button. */
    public static JButton dangerButton(String text) {
        JButton btn = createRoundButton(text, AppConstants.DANGER, Color.WHITE);
        addHoverEffect(btn, AppConstants.DANGER, new Color(200, 20, 100), Color.WHITE, Color.WHITE);
        return btn;
    }

    /** Outlined/secondary button. */
    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppConstants.FONT_BOLD);
        btn.setForeground(AppConstants.PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.PRIMARY, 2, true),
            new EmptyBorder(6, 18, 6, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(230, 235, 255));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    /** Success (green) button. */
    public static JButton successButton(String text) {
        JButton btn = createRoundButton(text, AppConstants.SUCCESS, Color.WHITE);
        addHoverEffect(btn, AppConstants.SUCCESS, new Color(50, 170, 110), Color.WHITE, Color.WHITE);
        return btn;
    }

    /** Icon sidebar navigation button. */
    public static JButton sidebarButton(String text, boolean selected) {
        JButton btn = new JButton(text);
        btn.setFont(AppConstants.FONT_BODY);
        btn.setForeground(selected ? Color.WHITE : AppConstants.TEXT_MUTED);
        btn.setBackground(selected ? AppConstants.PRIMARY : AppConstants.BG_SIDEBAR);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    // ── Text fields ────────────────────────────────────────────────────────

    /** Styled rounded text field. */
    public static JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setFont(AppConstants.FONT_BODY);
        field.setBackground(AppConstants.BG_MAIN);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        field.setPreferredSize(new Dimension(200, AppConstants.INPUT_HEIGHT));
        addPlaceholder(field, placeholder);
        return field;
    }

    /** Styled password field. */
    public static JPasswordField styledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(20);
        field.setFont(AppConstants.FONT_BODY);
        field.setBackground(AppConstants.BG_MAIN);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        field.setPreferredSize(new Dimension(200, AppConstants.INPUT_HEIGHT));
        return field;
    }

    /** Styled combo box. */
    public static <T> JComboBox<T> styledComboBox() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(AppConstants.FONT_BODY);
        combo.setBackground(Color.WHITE);
        combo.setBorder(new EmptyBorder(4, 8, 4, 8));
        combo.setPreferredSize(new Dimension(200, AppConstants.INPUT_HEIGHT));
        return combo;
    }

    // ── Labels ─────────────────────────────────────────────────────────────

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_TITLE);
        lbl.setForeground(AppConstants.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel headingLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_HEADING);
        lbl.setForeground(AppConstants.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel bodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_BODY);
        lbl.setForeground(AppConstants.TEXT_SECONDARY);
        return lbl;
    }

    public static JLabel errorLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_SMALL);
        lbl.setForeground(AppConstants.DANGER);
        return lbl;
    }

    // ── Cards ──────────────────────────────────────────────────────────────

    /** Returns a panel styled as a card with drop shadow border. */
    public static JPanel card() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, AppConstants.CARD_ARC, AppConstants.CARD_ARC);
                // Card
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, AppConstants.CARD_ARC, AppConstants.CARD_ARC);
                g2.dispose();
            }
        };
        panel.setBackground(AppConstants.BG_CARD);
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        return panel;
    }

    // ── Dividers ───────────────────────────────────────────────────────────

    public static JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppConstants.BORDER_COLOR);
        return sep;
    }

    // ── Notification toast ─────────────────────────────────────────────────

    /** Shows a temporary notification at the bottom of the parent frame. */
    public static void showToast(JFrame parent, String message, boolean success) {
        JWindow toast = new JWindow(parent);
        JLabel label  = new JLabel("  " + (success ? "✓ " : "✗ ") + message + "  ");
        label.setFont(AppConstants.FONT_BODY);
        label.setForeground(Color.WHITE);
        label.setBackground(success ? new Color(30, 150, 90) : new Color(200, 50, 80));
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(10, 20, 10, 20));
        toast.add(label);
        toast.pack();
        Point loc = parent.getLocation();
        Dimension sz = parent.getSize();
        toast.setLocation(loc.x + sz.width/2 - toast.getWidth()/2,
                          loc.y + sz.height - toast.getHeight() - 60);
        toast.setVisible(true);
        new Timer(2800, e -> toast.dispose()).start();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    public static void setGlobalFont() {
        Font defaultFont = AppConstants.FONT_BODY;
        UIManager.put("Label.font",       defaultFont);
        UIManager.put("Button.font",      defaultFont);
        UIManager.put("TextField.font",   defaultFont);
        UIManager.put("ComboBox.font",    defaultFont);
        UIManager.put("Table.font",       defaultFont);
        UIManager.put("TableHeader.font", AppConstants.FONT_BOLD);
        UIManager.put("OptionPane.messageFont", defaultFont);
    }

    /** Colors a row of a JTable alternately. */
    public static void styleTable(JTable table) {
        table.setRowHeight(AppConstants.TABLE_ROW_HEIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 237, 255));
        table.setSelectionForeground(AppConstants.TEXT_PRIMARY);
        table.getTableHeader().setBackground(AppConstants.BG_MAIN);
        table.getTableHeader().setForeground(AppConstants.TEXT_SECONDARY);
        table.getTableHeader().setFont(AppConstants.FONT_BOLD);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppConstants.BORDER_COLOR));
        table.setFillsViewportHeight(true);
    }

    // ── Private factory helpers ────────────────────────────────────────────

    private static JButton createRoundButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(AppConstants.FONT_BOLD);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static void addHoverEffect(JButton btn, Color normal, Color hover, Color fg, Color fgHover) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); btn.setForeground(fgHover); }
            public void mouseExited (MouseEvent e) { btn.setBackground(normal); btn.setForeground(fg); }
        });
    }

    private static void addPlaceholder(JTextField field, String placeholder) {
        if (placeholder == null || placeholder.isBlank()) return;
        field.setForeground(AppConstants.TEXT_SECONDARY);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(AppConstants.TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isBlank()) {
                    field.setForeground(AppConstants.TEXT_SECONDARY);
                    field.setText(placeholder);
                }
            }
        });
    }
}
