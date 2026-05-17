package view;

import controller.TransactionController;
import model.Transaction;
import model.User;
import utils.AppConstants;
import utils.CurrencyUtil;
import utils.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DashboardPanel - The main overview screen showing summary cards, charts,
 * and recent transactions.
 */
public class DashboardPanel extends JPanel {

    private User                  currentUser;
    private MainView              mainView;
    private TransactionController txController;

    // Summary card labels
    private JLabel incomeValueLabel;
    private JLabel expenseValueLabel;
    private JLabel balanceValueLabel;
    private JLabel savingsRateLabel;

    // Charts
    private BarChartPanel  barChart;
    private PieChartPanel  pieChart;

    // Table
    private DefaultTableModel tableModel;
    private JLabel            monthLabel;

    public DashboardPanel(User user, MainView mainView) throws Exception {
        this.currentUser = user;
        this.mainView    = mainView;
        this.txController = new TransactionController(user.getId());
        initUI();
        refresh();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    // ── Header ─────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppConstants.BG_MAIN);
        header.setBorder(new EmptyBorder(24, 28, 8, 28));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel greet = new JLabel("Good day, " + currentUser.getFirstName() + "! 👋");
        greet.setFont(AppConstants.FONT_TITLE);
        greet.setForeground(AppConstants.TEXT_PRIMARY);
        monthLabel = new JLabel("  " + DateUtil.monthName(LocalDate.now().getMonthValue())
                              + " " + LocalDate.now().getYear());
        monthLabel.setFont(AppConstants.FONT_BODY);
        monthLabel.setForeground(AppConstants.TEXT_SECONDARY);
        left.add(greet);
        left.add(monthLabel);

        JButton addBtn = UIHelper.primaryButton("+ Add Transaction");
        addBtn.addActionListener(e -> mainView.navigate("EXPENSES"));

        header.add(left,   BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        return header;
    }

    // ── Body ───────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Top: summary cards
        body.add(buildSummaryCards(), BorderLayout.NORTH);

        // Middle: charts + recent tx
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                          buildChartsPanel(), buildRecentPanel());
        split.setDividerLocation(500);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerSize(8);
        body.add(split, BorderLayout.CENTER);

        return body;
    }

    // ── Summary cards ──────────────────────────────────────────────────────

    private JPanel buildSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 8, 16, 8));

        incomeValueLabel  = new JLabel("₹0.00");
        expenseValueLabel = new JLabel("₹0.00");
        balanceValueLabel = new JLabel("₹0.00");
        savingsRateLabel  = new JLabel("0%");

        panel.add(summaryCard("Total Income",    incomeValueLabel,  "💰", AppConstants.SUCCESS,   new Color(230, 255, 240)));
        panel.add(summaryCard("Total Expense",   expenseValueLabel, "💸", AppConstants.DANGER,    new Color(255, 230, 235)));
        panel.add(summaryCard("Net Balance",     balanceValueLabel, "🏦", AppConstants.PRIMARY,   new Color(230, 235, 255)));
        panel.add(summaryCard("Savings Rate",    savingsRateLabel,  "📈", AppConstants.WARNING,   new Color(255, 250, 220)));

        return panel;
    }

    private JPanel summaryCard(String title, JLabel valueLabel, String icon,
                                Color accentColor, Color bgColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 14, 14);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 14, 14);
                // Accent left bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 5, getHeight() - 4, 4, 4);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Icon top-right
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppConstants.FONT_SMALL);
        titleLabel.setForeground(AppConstants.TEXT_SECONDARY);

        // Value
        valueLabel.setFont(AppConstants.FONT_AMOUNT);
        valueLabel.setForeground(accentColor);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(iconLabel,  BorderLayout.EAST);

        card.add(topRow,     BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // ── Charts panel ───────────────────────────────────────────────────────

    private JPanel buildChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 8, 0, 8));

        // Bar chart card
        JPanel barCard = UIHelper.card();
        barCard.setLayout(new BorderLayout(0, 8));
        barCard.add(sectionTitle("📊 Monthly Trend (6 months)"), BorderLayout.NORTH);
        barChart = new BarChartPanel();
        barCard.add(barChart, BorderLayout.CENTER);

        // Pie chart card
        JPanel pieCard = UIHelper.card();
        pieCard.setLayout(new BorderLayout(0, 8));
        pieCard.add(sectionTitle("🍩 Expenses by Category"), BorderLayout.NORTH);
        pieChart = new PieChartPanel();
        pieCard.add(pieChart, BorderLayout.CENTER);

        panel.add(barCard);
        panel.add(pieCard);
        return panel;
    }

    // ── Recent transactions panel ──────────────────────────────────────────

    private JPanel buildRecentPanel() {
        JPanel card = UIHelper.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionTitle("🕐 Recent Transactions"), BorderLayout.WEST);
        JButton viewAll = UIHelper.outlineButton("View All");
        viewAll.addActionListener(e -> mainView.navigate("EXPENSES"));
        header.add(viewAll, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        String[] columns = {"Date", "Category", "Description", "Amount"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 255));
                }
                return c;
            }
        };
        UIHelper.styleTable(table);

        // Color amount column
        table.getColumnModel().getColumn(3).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    if (val != null) {
                        String s = val.toString();
                        lbl.setForeground(s.startsWith("+") ? AppConstants.SUCCESS : AppConstants.DANGER);
                        lbl.setFont(AppConstants.FONT_BOLD);
                    }
                    return lbl;
                }
            }
        );

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── Refresh ────────────────────────────────────────────────────────────

    public void refresh() {
        try {
            int month = LocalDate.now().getMonthValue();
            int year  = LocalDate.now().getYear();

            BigDecimal income  = txController.getMonthIncome(month, year);
            BigDecimal expense = txController.getMonthExpense(month, year);
            BigDecimal balance = income.subtract(expense);

            incomeValueLabel.setText(CurrencyUtil.format(income));
            expenseValueLabel.setText(CurrencyUtil.format(expense));
            balanceValueLabel.setText(CurrencyUtil.format(balance));

            // Savings rate
            if (income.compareTo(BigDecimal.ZERO) > 0) {
                double rate = balance.doubleValue() / income.doubleValue() * 100;
                savingsRateLabel.setText(String.format("%.1f%%", Math.max(0, rate)));
            } else {
                savingsRateLabel.setText("0%");
            }

            // Recent transactions table
            List<Transaction> recent = txController.getRecentTransactions(10);
            tableModel.setRowCount(0);
            for (Transaction t : recent) {
                String amountStr = (t.isExpense() ? "- " : "+ ") + CurrencyUtil.format(t.getAmount());
                tableModel.addRow(new Object[]{
                    DateUtil.toDisplay(t.getTransactionDate()),
                    t.getCategory().getIcon() + " " + t.getCategory().getName(),
                    t.getDescription() != null ? t.getDescription() : "",
                    amountStr
                });
            }

            // Bar chart – monthly trend
            Map<String, BigDecimal> trend = txController.getMonthlyTrend(6);
            barChart.setData(trend);

            // Pie chart – category expenses
            Map<String, BigDecimal> cats = txController.getCategoryExpenses(month, year);
            pieChart.setData(cats);

        } catch (Exception e) {
            System.err.println("Dashboard refresh error: " + e.getMessage());
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_HEADING);
        lbl.setForeground(AppConstants.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        return lbl;
    }
}
