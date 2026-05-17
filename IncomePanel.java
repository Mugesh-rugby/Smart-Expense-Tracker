package view;

import controller.TransactionController;
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
 * IncomePanel - Income management screen (mirrors TransactionPanel for income).
 */
public class IncomePanel extends JPanel {

    private User                  currentUser;
    private MainView              mainView;
    private TransactionController txController;

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;
    private JLabel            totalLabel;
    private List<Transaction> currentList;

    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;

    private static final String[] MONTHS = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public IncomePanel(User user, MainView mainView) throws Exception {
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

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(UIHelper.titleLabel("💰 Income"));
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

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        searchField = UIHelper.styledTextField("🔍  Search income...");
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.setForeground(AppConstants.TEXT_SECONDARY);
        searchField.addActionListener(e -> handleSearch());
        right.add(searchField);

        JButton addBtn = UIHelper.successButton("+ Add Income");
        addBtn.addActionListener(e -> openAddDialog());
        right.add(addBtn);

        panel.add(left,  BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildTableCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel summaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 4));
        summaryRow.setOpaque(false);
        totalLabel = new JLabel("Total Income: ₹0.00");
        totalLabel.setFont(AppConstants.FONT_BOLD);
        totalLabel.setForeground(AppConstants.SUCCESS);
        summaryRow.add(totalLabel);
        summaryRow.add(UIHelper.bodyLabel("Double-click to edit"));
        card.add(summaryRow, BorderLayout.NORTH);

        String[] cols = {"#","Date","Source","Description","Notes","Amount","Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 255, 250));
                return c;
            }
        };
        UIHelper.styleTable(table);

        // Amount renderer – green
        table.getColumnModel().getColumn(5).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val,
                        boolean sel, boolean focus, int row, int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    lbl.setFont(AppConstants.FONT_BOLD);
                    lbl.setForeground(AppConstants.SUCCESS);
                    return lbl;
                }
            }
        );

        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionEditor(table));

        int[] widths = {40, 90, 110, 160, 140, 100, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

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

    public void refresh() {
        try {
            int month = monthCombo.getSelectedIndex() + 1;
            int year  = Integer.parseInt((String) yearCombo.getSelectedItem());
            currentList = txController.getMonthTransactions(month, year);
            currentList.removeIf(Transaction::isExpense);
            populateTable(currentList);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading income: " + e.getMessage(),
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
        totalLabel.setText("Total Income: " + CurrencyUtil.format(total)
                         + "  (" + list.size() + " entries)");
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty() || keyword.startsWith("🔍")) { refresh(); return; }
        try {
            List<Transaction> results = txController.search(keyword);
            results.removeIf(Transaction::isExpense);
            currentList = results;
            populateTable(results);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openAddDialog() {
        TransactionFormDialog dlg = new TransactionFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            txController, TransactionType.INCOME, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh();
            UIHelper.showToast((JFrame) SwingUtilities.getWindowAncestor(this),
                "Income added successfully", true);
        }
    }

    private void openEditDialog(Transaction t) {
        TransactionFormDialog dlg = new TransactionFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            txController, TransactionType.INCOME, t);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh();
            UIHelper.showToast((JFrame) SwingUtilities.getWindowAncestor(this),
                "Income updated", true);
        }
    }

    private void handleDelete(int tableRow) {
        if (currentList == null || tableRow >= currentList.size()) return;
        Transaction t = currentList.get(tableRow);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this income entry?\n" + t.getDescription()
            + "  " + CurrencyUtil.format(t.getAmount()),
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                txController.deleteTransaction(t.getId());
                refresh();
                UIHelper.showToast((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Income entry deleted", true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Action column renderer & editor (identical pattern to TransactionPanel)
    private class ActionRenderer implements javax.swing.table.TableCellRenderer {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private JButton e = UIHelper.outlineButton("✏");
        private JButton d = UIHelper.dangerButton("🗑");
        { panel.setOpaque(true); panel.add(e); panel.add(d); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            panel.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 255, 250));
            return panel;
        }
    }

    private class ActionEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private JButton editBtn   = UIHelper.outlineButton("✏");
        private JButton deleteBtn = UIHelper.dangerButton("🗑");
        private int currentRow;
        {
            panel.setOpaque(true); panel.setBackground(Color.WHITE);
            panel.add(editBtn); panel.add(deleteBtn);
            editBtn.addActionListener(e -> { fireEditingStopped();
                if (currentList != null && currentRow < currentList.size())
                    openEditDialog(currentList.get(currentRow)); });
            deleteBtn.addActionListener(e -> { fireEditingStopped(); handleDelete(currentRow); });
        }
        public ActionEditor(JTable t) { super(new JCheckBox()); setClickCountToStart(1); }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) { currentRow = row; return panel; }
        @Override public Object getCellEditorValue() { return ""; }
    }
}
