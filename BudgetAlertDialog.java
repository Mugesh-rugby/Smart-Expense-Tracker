package view;

import model.Budget;
import utils.AppConstants;
import utils.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * BudgetAlertDialog - Shows budget limit warnings as a notification popup.
 * Called automatically when near-limit categories are detected.
 */
public class BudgetAlertDialog extends JDialog {

    public BudgetAlertDialog(Frame parent, List<Budget> alerts) {
        super(parent, "⚠ Budget Alerts", false); // non-modal
        initUI(alerts);
    }

    private void initUI(List<Budget> alerts) {
        setSize(380, Math.min(80 + alerts.size() * 70, 450));
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppConstants.WARNING);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel title = new JLabel("⚠  Budget Alerts");
        title.setFont(AppConstants.FONT_HEADING);
        title.setForeground(AppConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // Alert items
        JPanel alertsPanel = new JPanel();
        alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));
        alertsPanel.setBackground(Color.WHITE);
        alertsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        for (Budget b : alerts) {
            alertsPanel.add(buildAlertItem(b));
            alertsPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        root.add(new JScrollPane(alertsPanel), BorderLayout.CENTER);

        // Close button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        JButton close = UIHelper.primaryButton("Dismiss");
        close.addActionListener(e -> dispose());
        footer.add(close);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildAlertItem(Budget b) {
        JPanel panel = new JPanel(new BorderLayout(10, 4));
        panel.setBackground(b.isExceeded()
            ? new Color(255, 235, 240)
            : new Color(255, 250, 225));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0,
                b.isExceeded() ? AppConstants.DANGER : AppConstants.WARNING),
            new EmptyBorder(10, 14, 10, 14)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel icon = new JLabel(b.getCategory().getIcon());
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        String statusText = b.isExceeded()
            ? "EXCEEDED by " + CurrencyUtil.format(b.getSpent().subtract(b.getAmount()))
            : String.format("%.0f%% used — ", b.getUsagePercent())
              + CurrencyUtil.format(b.getRemaining()) + " remaining";

        JLabel name = new JLabel(b.getCategory().getName() + " Budget");
        name.setFont(AppConstants.FONT_BOLD);
        name.setForeground(b.isExceeded() ? AppConstants.DANGER : AppConstants.TEXT_PRIMARY);

        JLabel detail = new JLabel(statusText
            + " | Limit: " + CurrencyUtil.format(b.getAmount()));
        detail.setFont(AppConstants.FONT_SMALL);
        detail.setForeground(AppConstants.TEXT_SECONDARY);

        info.add(name);
        info.add(detail);

        // Progress bar
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.min(b.getUsagePercent(), 100));
        bar.setPreferredSize(new Dimension(0, 4));
        bar.setBorderPainted(false);
        bar.setBackground(new Color(220, 220, 220));
        bar.setForeground(b.isExceeded() ? AppConstants.DANGER : AppConstants.WARNING);

        panel.add(icon, BorderLayout.WEST);
        panel.add(info, BorderLayout.CENTER);
        panel.add(bar,  BorderLayout.SOUTH);
        return panel;
    }
}
