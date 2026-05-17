package view;

import controller.TransactionController;
import model.Category;
import model.Transaction;
import model.Transaction.TransactionType;
import utils.AppConstants;
import utils.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TransactionFormDialog - Modal dialog for adding or editing a transaction.
 * Shared by both ExpensePanel and IncomePanel.
 */
public class TransactionFormDialog extends JDialog {

    private TransactionController txController;
    private TransactionType       type;
    private Transaction           existing;   // null = add mode
    private boolean               saved = false;

    // Form components
    private JComboBox<Category>   categoryCombo;
    private JTextField            amountField;
    private JTextField            descriptionField;
    private JTextArea             notesArea;
    private JTextField            dateField;
    private JLabel                errorLabel;

    public TransactionFormDialog(Frame parent, TransactionController controller,
                                  TransactionType type, Transaction existing) {
        super(parent, (existing == null ? "Add " : "Edit ")
              + (type == TransactionType.EXPENSE ? "Expense" : "Income"), true);
        this.txController = controller;
        this.type         = type;
        this.existing     = existing;
        initUI();
        if (existing != null) prefillForm();
    }

    private void initUI() {
        setSize(480, 520);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(type == TransactionType.EXPENSE ? AppConstants.DANGER : AppConstants.SUCCESS);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel titleLbl = new JLabel(
            (existing == null ? "New " : "Edit ")
            + (type == TransactionType.EXPENSE ? "💸 Expense" : "💰 Income"));
        titleLbl.setFont(AppConstants.FONT_HEADING);
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // Form body
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridwidth = 2;
        g.insets = new Insets(5, 0, 5, 0);

        int row = 0;

        // Category
        g.gridy = row++; form.add(formLabel("Category *"), g);
        categoryCombo = UIHelper.styledComboBox();
        try {
            List<Category> cats = type == TransactionType.EXPENSE
                ? txController.getExpenseCategories()
                : txController.getIncomeCategories();
            for (Category c : cats) categoryCombo.addItem(c);
        } catch (Exception e) { /* handled elsewhere */ }
        g.gridy = row++; form.add(categoryCombo, g);

        // Amount
        g.gridy = row++; form.add(formLabel("Amount (₹) *"), g);
        amountField = UIHelper.styledTextField("");
        amountField.setForeground(AppConstants.TEXT_PRIMARY);
        g.gridy = row++; form.add(amountField, g);

        // Description
        g.gridy = row++; form.add(formLabel("Description"), g);
        descriptionField = UIHelper.styledTextField("");
        descriptionField.setForeground(AppConstants.TEXT_PRIMARY);
        g.gridy = row++; form.add(descriptionField, g);

        // Date
        g.gridy = row++; form.add(formLabel("Date * (dd MMM yyyy)"), g);
        dateField = UIHelper.styledTextField(DateUtil.toDisplay(LocalDate.now()));
        dateField.setForeground(AppConstants.TEXT_PRIMARY);
        dateField.setText(DateUtil.toDisplay(LocalDate.now()));
        g.gridy = row++; form.add(dateField, g);

        // Notes
        g.gridy = row++; form.add(formLabel("Notes"), g);
        notesArea = new JTextArea(3, 20);
        notesArea.setFont(AppConstants.FONT_BODY);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(null);
        g.gridy = row++; form.add(notesScroll, g);

        // Error label
        errorLabel = UIHelper.errorLabel(" ");
        g.gridy = row++; form.add(errorLabel, g);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppConstants.BORDER_COLOR));

        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = type == TransactionType.EXPENSE
            ? UIHelper.dangerButton(existing == null ? "Add Expense" : "Save Changes")
            : UIHelper.successButton(existing == null ? "Add Income" : "Save Changes");
        saveBtn.addActionListener(e -> handleSave());
        btnPanel.add(saveBtn);

        root.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(root);

        // Enter key on amount
        amountField.addActionListener(e -> handleSave());
    }

    private void prefillForm() {
        // Set category
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            Category c = categoryCombo.getItemAt(i);
            if (c.getId() == existing.getCategory().getId()) {
                categoryCombo.setSelectedIndex(i);
                break;
            }
        }
        amountField.setText(existing.getAmount().toPlainString());
        descriptionField.setText(existing.getDescription() != null ? existing.getDescription() : "");
        dateField.setText(DateUtil.toDisplay(existing.getTransactionDate()));
        notesArea.setText(existing.getNotes() != null ? existing.getNotes() : "");
    }

    private void handleSave() {
        errorLabel.setText(" ");
        Category cat = (Category) categoryCombo.getSelectedItem();
        String amount = amountField.getText().trim();
        String desc   = descriptionField.getText().trim();
        String notes  = notesArea.getText().trim();
        String dateStr = dateField.getText().trim();

        // Parse date
        LocalDate date;
        try {
            date = DateUtil.fromDisplay(dateStr);
            if (date == null) throw new Exception("Invalid date");
        } catch (Exception e) {
            errorLabel.setText("✗  Date must be in format: dd MMM yyyy  (e.g. 15 May 2025)");
            return;
        }

        try {
            if (existing == null) {
                // Add new
                if (type == TransactionType.EXPENSE)
                    txController.addExpense(cat, amount, desc, notes, date);
                else
                    txController.addIncome(cat, amount, desc, notes, date);
            } else {
                // Edit existing
                txController.updateTransaction(existing.getId(), cat, type, amount, desc, notes, date);
            }
            saved = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            errorLabel.setText("✗  " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving transaction: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }

    private JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_BOLD);
        lbl.setForeground(AppConstants.TEXT_PRIMARY);
        return lbl;
    }
}
