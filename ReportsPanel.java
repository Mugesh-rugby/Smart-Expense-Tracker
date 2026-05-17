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
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ReportsPanel - Monthly reports, category breakdown, charts, and PDF export.
 */
public class ReportsPanel extends JPanel {

    private User                  currentUser;
    private TransactionController txController;

    // Filter controls
    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;

    // Summary labels
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel balanceLabel;
    private JLabel txCountLabel;

    // Charts
    private PieChartPanel  pieChart;
    private BarChartPanel  trendChart;

    // Category breakdown table
    private DefaultTableModel catTableModel;

    // All transactions table
    private DefaultTableModel txTableModel;

    private static final String[] MONTHS = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public ReportsPanel(User user) throws Exception {
        this.currentUser  = user;
        this.txController = new TransactionController(user.getId());
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(),  BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(14, 14));
        body.setOpaque(false);
        body.add(buildLeftColumn(),  BorderLayout.WEST);
        body.add(buildRightColumn(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    // ── Header ─────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(UIHelper.titleLabel("📊 Reports & Analytics"));

        left.add(Box.createHorizontalStrut(16));
        monthCombo = UIHelper.styledComboBox();
        for (String m : MONTHS) monthCombo.addItem(m);
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthCombo.setPreferredSize(new Dimension(130, 34));
        monthCombo.addActionListener(e -> refresh());
        left.add(monthCombo);

        yearCombo = UIHelper.styledComboBox();
        int y = LocalDate.now().getYear();
        for (int i = y; i >= y - 5; i--) yearCombo.addItem(String.valueOf(i));
        yearCombo.setPreferredSize(new Dimension(80, 34));
        yearCombo.addActionListener(e -> refresh());
        left.add(yearCombo);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton exportBtn = UIHelper.primaryButton("📄 Export to PDF");
        exportBtn.addActionListener(e -> handleExport());
        right.add(exportBtn);
        JButton printBtn = UIHelper.outlineButton("🖨 Print");
        printBtn.addActionListener(e -> handlePrint());
        right.add(printBtn);

        panel.add(left,  BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // ── Left column: summary + pie + category table ────────────────────────

    private JPanel buildLeftColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.setPreferredSize(new Dimension(380, 0));

        // Summary card
        JPanel summaryCard = UIHelper.card();
        summaryCard.setLayout(new GridLayout(2, 2, 10, 10));
        summaryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        incomeLabel  = bigStatLabel("₹0.00", AppConstants.SUCCESS);
        expenseLabel = bigStatLabel("₹0.00", AppConstants.DANGER);
        balanceLabel = bigStatLabel("₹0.00", AppConstants.PRIMARY);
        txCountLabel = bigStatLabel("0",      AppConstants.INFO);

        summaryCard.add(statBox("Total Income",   incomeLabel));
        summaryCard.add(statBox("Total Expense",  expenseLabel));
        summaryCard.add(statBox("Net Balance",    balanceLabel));
        summaryCard.add(statBox("Transactions",   txCountLabel));

        col.add(summaryCard);
        col.add(Box.createRigidArea(new Dimension(0, 14)));

        // Pie chart card
        JPanel pieCard = UIHelper.card();
        pieCard.setLayout(new BorderLayout(0, 8));
        pieCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        pieCard.add(sectionLabel("🍩 Expense by Category"), BorderLayout.NORTH);
        pieChart = new PieChartPanel();
        pieCard.add(pieChart, BorderLayout.CENTER);
        col.add(pieCard);
        col.add(Box.createRigidArea(new Dimension(0, 14)));

        // Category breakdown table
        JPanel catCard = UIHelper.card();
        catCard.setLayout(new BorderLayout(0, 8));
        catCard.add(sectionLabel("📋 Category Breakdown"), BorderLayout.NORTH);

        String[] catCols = {"Category", "Amount", "% Share"};
        catTableModel = new DefaultTableModel(catCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable catTable = new JTable(catTableModel);
        UIHelper.styleTable(catTable);
        catTable.setRowHeight(34);
        JScrollPane catScroll = new JScrollPane(catTable);
        catScroll.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER_COLOR));
        catCard.add(catScroll, BorderLayout.CENTER);
        col.add(catCard);

        return col;
    }

    // ── Right column: trend chart + full transaction list ──────────────────

    private JPanel buildRightColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 14));
        col.setOpaque(false);

        // Trend chart card
        JPanel trendCard = UIHelper.card();
        trendCard.setLayout(new BorderLayout(0, 8));
        trendCard.setPreferredSize(new Dimension(0, 220));
        trendCard.add(sectionLabel("📈 6-Month Expense Trend"), BorderLayout.NORTH);
        trendChart = new BarChartPanel();
        trendCard.add(trendChart, BorderLayout.CENTER);
        col.add(trendCard, BorderLayout.NORTH);

        // All transactions card
        JPanel txCard = UIHelper.card();
        txCard.setLayout(new BorderLayout(0, 8));
        txCard.add(sectionLabel("📄 All Transactions This Month"), BorderLayout.NORTH);

        String[] txCols = {"Date","Type","Category","Description","Amount"};
        txTableModel = new DefaultTableModel(txCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable txTable = new JTable(txTableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    // Color income rows green-tinted, expense rows red-tinted
                    Object type = getModel().getValueAt(row, 1);
                    if ("INCOME".equals(type))
                        c.setBackground(new Color(240, 255, 245));
                    else
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(255, 245, 248));
                }
                return c;
            }
        };
        UIHelper.styleTable(txTable);

        // Amount renderer
        txTable.getColumnModel().getColumn(4).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val,
                        boolean sel, boolean focus, int row, int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    lbl.setFont(AppConstants.FONT_BOLD);
                    String s = val != null ? val.toString() : "";
                    lbl.setForeground(s.startsWith("+") ? AppConstants.SUCCESS : AppConstants.DANGER);
                    return lbl;
                }
            }
        );

        // Hide type column (used for row coloring)
        txTable.getColumnModel().getColumn(1).setMinWidth(0);
        txTable.getColumnModel().getColumn(1).setMaxWidth(0);
        txTable.getColumnModel().getColumn(1).setWidth(0);

        JScrollPane scroll = new JScrollPane(txTable);
        scroll.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER_COLOR));
        txCard.add(scroll, BorderLayout.CENTER);
        col.add(txCard, BorderLayout.CENTER);

        return col;
    }

    // ── Refresh ────────────────────────────────────────────────────────────

    public void refresh() {
        try {
            int month = monthCombo.getSelectedIndex() + 1;
            int year  = Integer.parseInt((String) yearCombo.getSelectedItem());

            BigDecimal income  = txController.getMonthIncome(month, year);
            BigDecimal expense = txController.getMonthExpense(month, year);
            BigDecimal balance = income.subtract(expense);

            incomeLabel.setText(CurrencyUtil.format(income));
            expenseLabel.setText(CurrencyUtil.format(expense));
            balanceLabel.setText(CurrencyUtil.format(balance));

            // Category breakdown
            Map<String, BigDecimal> cats = txController.getCategoryExpenses(month, year);
            catTableModel.setRowCount(0);
            double totalExp = expense.doubleValue();
            for (Map.Entry<String, BigDecimal> e : cats.entrySet()) {
                double pct = totalExp > 0 ? e.getValue().doubleValue() / totalExp * 100 : 0;
                catTableModel.addRow(new Object[]{
                    e.getKey(),
                    CurrencyUtil.format(e.getValue()),
                    String.format("%.1f%%", pct)
                });
            }
            pieChart.setData(cats);

            // All transactions
            List<Transaction> txList = txController.getMonthTransactions(month, year);
            txCountLabel.setText(String.valueOf(txList.size()));
            txTableModel.setRowCount(0);
            for (Transaction t : txList) {
                String sign = t.isIncome() ? "+" : "-";
                txTableModel.addRow(new Object[]{
                    DateUtil.toDisplay(t.getTransactionDate()),
                    t.getType().name(),
                    t.getCategory().getIcon() + " " + t.getCategory().getName(),
                    t.getDescription() != null ? t.getDescription() : "",
                    sign + CurrencyUtil.format(t.getAmount())
                });
            }

            // Trend chart
            Map<String, BigDecimal> trend = txController.getMonthlyTrend(6);
            trendChart.setData(trend);

        } catch (Exception e) {
            System.err.println("Reports refresh error: " + e.getMessage());
        }
    }

    // ── Export & Print ─────────────────────────────────────────────────────

    private void handleExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Report as PDF");
        chooser.setSelectedFile(new java.io.File("SmartExpenseReport_"
            + MONTHS[monthCombo.getSelectedIndex()] + "_"
            + yearCombo.getSelectedItem() + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PDFExporter.export(chooser.getSelectedFile(), currentUser,
                    monthCombo.getSelectedIndex() + 1,
                    Integer.parseInt((String) yearCombo.getSelectedItem()),
                    txController);
                UIHelper.showToast((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Report exported successfully", true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handlePrint() {
        // Build a printable text report
        JTextArea printArea = new JTextArea();
        printArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        printArea.setText(buildTextReport());
        try {
            printArea.print(null, new MessageFormat(
                AppConstants.APP_NAME + " - Report  Page {0}"));
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Print error: " + e.getMessage(),
                "Print Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildTextReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("  ").append(AppConstants.APP_NAME).append(" - Monthly Report\n");
        sb.append("  Month: ").append(MONTHS[monthCombo.getSelectedIndex()])
          .append(" ").append(yearCombo.getSelectedItem()).append("\n");
        sb.append("  User: ").append(currentUser.getFullName()).append("\n");
        sb.append("  Generated: ").append(LocalDate.now()).append("\n");
        sb.append("=".repeat(60)).append("\n\n");
        sb.append(String.format("%-20s %s\n", "Income:",  incomeLabel.getText()));
        sb.append(String.format("%-20s %s\n", "Expense:", expenseLabel.getText()));
        sb.append(String.format("%-20s %s\n", "Balance:", balanceLabel.getText()));
        sb.append("\n--- Category Breakdown ---\n");
        for (int i = 0; i < catTableModel.getRowCount(); i++) {
            sb.append(String.format("%-20s %-14s %s\n",
                catTableModel.getValueAt(i, 0),
                catTableModel.getValueAt(i, 1),
                catTableModel.getValueAt(i, 2)));
        }
        sb.append("\n--- Transactions ---\n");
        for (int i = 0; i < txTableModel.getRowCount(); i++) {
            sb.append(String.format("%-12s %-18s %-24s %s\n",
                txTableModel.getValueAt(i, 0),
                txTableModel.getValueAt(i, 2),
                txTableModel.getValueAt(i, 3),
                txTableModel.getValueAt(i, 4)));
        }
        return sb.toString();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private JPanel statBox(String title, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(AppConstants.FONT_SMALL);
        t.setForeground(AppConstants.TEXT_SECONDARY);
        p.add(t,          BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private JLabel bigStatLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(color);
        return lbl;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_HEADING);
        lbl.setForeground(AppConstants.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        return lbl;
    }
}
