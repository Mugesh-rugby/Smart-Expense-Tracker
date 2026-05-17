package view;

import controller.TransactionController;
import model.Category;
import model.Transaction;
import model.Transaction.TransactionType;
import model.User;
import utils.AppConstants;
import utils.CurrencyUtil;
import utils.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TransactionPanel - Expense management screen with CRUD, search, and date filter.
 */
public class TransactionPanel extends JPanel {

    private User                  currentUser;
    private MainView              mainView;
    private TransactionController txController;

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;
    private JLabel            totalLabel;
    private List<Transaction> currentList;

    // Filter combos
    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;

    private static final String[] MONTHS = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public TransactionPanel(User user, MainView mainView) throws Exception {
        this.currentUser  = user;
        this.mainView     = mainView;
        this.txController = new TransactionController(user.getId());
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    // ── Top bar ────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 14, 0));

        // Left: title + filters
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel title = UIHelper.titleLabel("💸 Expenses");
        left.add(title);

        left.add(Box.createHorizontalStrut(10));

        monthCombo = UIHelper.styledComboBox();
        for (String m : MONTHS) monthCombo.addItem(m);
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthCombo.setPreferredSize(new Dimension(120, 34));
        monthCombo.addActionListener(e -> refresh());
        left.add(monthCombo);

        yearCombo = UIHelper.styledComboBox();
        int thisYear = LocalDate.now().getYear();
        for (int y = thisYear; y >= thisYear - 5; y--) yearCombo.addItem(String.valueOf(y));
        yearCombo.setPreferredSize(new Dimension(80, 34));
        yearCombo.addActionListener(e -> refresh());
        left.add(yearCombo);

        // Right: search + add button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        searchField = UIHelper.styledTextField("🔍  Search transactions...");
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.setForeground(AppConstants.TEXT_SECONDARY);
        searchField.addActionListener(e -> handleSearch());
        right.add(searchField);

        JButton searchBtn = UIHelper.outlineButton("Search");
        searchBtn.addActionListener(e -> handleSearch());
        right.add(searchBtn);

        JButton addBtn = UIHelper.primaryButton("+ Add Expense");
        addBtn.addActionListener(e -> openAddDialog(TransactionType.EXPENSE));
        right.add(addBtn);

        panel.add(left,  BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // ── Table card ─────────────────────────────────────────────────────────

    private JPanel buildTableCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BorderLayout(0, 10));

        // Summary row
        JPanel summaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 4));
        summaryRow.setOpaque(false);
        totalLabel = new JLabel("Total Expense: ₹0.00");
        totalLabel.setFont(AppConstants.FONT_BOLD);
        totalLabel.setForeground(AppConstants.DANGER);
        summaryRow.add(totalLabel);

        JLabel hint = UIHelper.bodyLabel("Double-click a row to edit");
        summaryRow.add(hint);
        card.add(summaryRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"#","Date","Category","Description","Notes","Amount","Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 255));
                return c;
            }
        };
        UIHelper.styleTable(table);

        // Amount column renderer
        table.getColumnModel().getColumn(5).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val,
                        boolean sel, boolean focus, int row, int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    lbl.setFont(AppConstants.FONT_BOLD);
                    lbl.setForeground(AppConstants.DANGER);
                    return lbl;
                }
            }
        );

        // Action buttons column
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionEditor(table));

        // Column widths
        int[] widths = {40, 90, 110, 160, 140, 100, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && currentList != null && row < currentList.size())
                        openEditDialog(currentList.get(row));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ── Refresh ────────────────────────────────────────────────────────────

    public void refresh() {
        try {
            int month = monthCombo.getSelectedIndex() + 1;
            int year  = Integer.parseInt((String) yearCombo.getSelectedItem());
            currentList = txController.getMonthTransactions(month, year);
            // Filter only expenses
            currentList.removeIf(Transaction::isIncome);
            populateTable(currentList);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading expenses: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Transaction> list) {
        tableModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        int i = 1;
        for (Transaction t : list) {
            tableModel.addRow(new Object[]{
                i++,
                DateUtil.toDisplay(t.getTransactionDate()),
                t.getCategory().getIcon() + " " + t.getCategory().getName(),
                t.getDescription() != null ? t.getDescription() : "",
                t.getNotes() != null ? t.getNotes() : "",
                CurrencyUtil.format(t.getAmount()),
                "Edit | Delete"
            });
            total = total.add(t.getAmount());
        }
        totalLabel.setText("Total Expense: " + CurrencyUtil.format(total)
                         + "  (" + list.size() + " transactions)");
    }

    // ── Search ─────────────────────────────────────────────────────────────

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty() || keyword.equals("🔍  Search transactions...")) {
            refresh();
            return;
        }
        try {
            List<Transaction> results = txController.search(keyword);
            results.removeIf(Transaction::isIncome);
            currentList = results;
            populateTable(results);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Search Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── Add / Edit dialogs ─────────────────────────────────────────────────

    private void openAddDialog(TransactionType type) {
        TransactionFormDialog dlg = new TransactionFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            txController, type, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh();
            UIHelper.showToast((JFrame) SwingUtilities.getWindowAncestor(this),
                "Expense added successfully", true);
        }
    }

    private void openEditDialog(Transaction t) {
        TransactionFormDialog dlg = new TransactionFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            txController, t.getType(), t);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh();
            UIHelper.showToast((JFrame) SwingUtilities.getWindowAncestor(this),
                "Transaction updated", true);
        }
    }

    private void handleDelete(int tableRow) {
        if (currentList == null || tableRow >= currentList.size()) return;
        Transaction t = currentList.get(tableRow);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this expense?\n" + t.getDescription()
            + "  " + CurrencyUtil.format(t.getAmount()),
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                txController.deleteTransaction(t.getId());
                refresh();
                UIHelper.showToast((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Expense deleted", true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Action column renderers/editors ────────────────────────────────────

    private class ActionRenderer implements javax.swing.table.TableCellRenderer {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private JButton editBtn   = UIHelper.outlineButton("✏");
        private JButton deleteBtn = UIHelper.dangerButton("🗑");
        { panel.setOpaque(true); panel.add(editBtn); panel.add(deleteBtn); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            panel.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 255));
            return panel;
        }
    }

    private class ActionEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private JButton editBtn   = UIHelper.outlineButton("✏");
        private JButton deleteBtn = UIHelper.dangerButton("🗑");
        private int currentRow;
        {
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);
            panel.add(editBtn);
            panel.add(deleteBtn);
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                if (currentList != null && currentRow < currentList.size())
                    openEditDialog(currentList.get(currentRow));
            });
            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                handleDelete(currentRow);
            });
        }
        public ActionEditor(JTable t) { super(new JCheckBox()); setClickCountToStart(1); }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            currentRow = row;
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}
