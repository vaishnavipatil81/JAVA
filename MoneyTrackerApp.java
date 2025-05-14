import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class MoneyTrackerApp extends JFrame {
    private DefaultTableModel tableModel;
    private JLabel balanceLabel;
    private List<Transaction> transactions = new ArrayList<>();
    private final String FILE_NAME = "transactions.txt";
    private JComboBox<String> monthComboBox;

    public MoneyTrackerApp() {
        setTitle("💰 Money Tracker");
        setSize(800, 500);
 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 255));

        // Top Panel with buttons
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(200, 220, 255));
        JButton addButton = new JButton("➕ Add Transaction");
        JButton reportButton = new JButton("📊 Monthly Report");
        topPanel.add(addButton);
        topPanel.add(reportButton);

        // Balance label
        balanceLabel = new JLabel("Balance: ₹0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(balanceLabel);

        // Month filter
        monthComboBox = new JComboBox<>(getMonthOptions());
        topPanel.add(new JLabel("Filter Month:"));
        topPanel.add(monthComboBox);

        // Transaction Table
        tableModel = new DefaultTableModel(new String[]{"Date", "Type", "Amount", "Category", "Description"}, 0);
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        // Layout
  add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load existing transactions
        loadTransactions();
        refreshTable(null);

        // Add button listener
        addButton.addActionListener(e -> addTransaction());
        reportButton.addActionListener(e -> showMonthlyReport());

        // Filter by month
        monthComboBox.addActionListener(e -> {
            String selected = (String) monthComboBox.getSelectedItem();
            refreshTable(selected.equals("All") ? null : selected);
        });
    }

    private void addTransaction() {
        String[] types = {"Income", "Expense"};
        String type = (String) JOptionPane.showInputDialog(this, "Type:", "Transaction Type",
                JOptionPane.PLAIN_MESSAGE, null, types, "Expense");

        if (type == null) return;

        String amountStr = JOptionPane.showInputDialog(this, "Amount:");
        if (amountStr == null || amountStr.isEmpty()) return;

        String[] categories = {"Food", "Transport", "Bills", "Shopping", "Salary", "Other","Mess","Room rent",};
        String category = (String) JOptionPane.showInputDialog(this, "Category:", "Transaction Category",
   JOptionPane.PLAIN_MESSAGE, null, categories, "Other");

        String desc = JOptionPane.showInputDialog(this, "Description:");

        try {
            double amount = Double.parseDouble(amountStr);
            Transaction t = new Transaction(type, amount, category, desc, LocalDate.now());
            transactions.add(t);
            saveTransaction(t);
            refreshTable(null);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable(String monthFilter) {
        tableModel.setRowCount(0);
        double balance = 0.0;

        for (Transaction t : transactions) {
            String month = t.date.getMonth().toString();
            if (monthFilter == null || month.equalsIgnoreCase(monthFilter)) {
                tableModel.addRow(new Object[]{
                        t.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        t.type,
                        "₹" + String.format("%.2f", t.amount),
                        t.category,
                        t.description
                });
                balance += t.type.equals("Income") ?  t.amount : -t.amount;
            }
        }

        balanceLabel.setText("Balance: ₹" + String.format("%.2f", balance));
    }

    private void showMonthlyReport() {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.type.equals("Expense")) {
                categoryTotals.put(t.category,
                        categoryTotals.getOrDefault(t.category, 0.0) + t.amount);
            }
        }
 StringBuilder report = new StringBuilder("🧾 Expense by Category:\n\n");
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            report.append(String.format("%-10s : ₹%.2f\n", entry.getKey(), entry.getValue()));
        }

        JOptionPane.showMessageDialog(this, report.toString(), "Monthly Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadTransactions() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Transaction t = Transaction.fromString(line);
                if (t != null) transactions.add(t);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load data.");
        }
    }

    private void saveTransaction(Transaction t) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(t.toString());
            bw.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save data.");
        }
 }

    private String[] getMonthOptions() {
        return new String[]{"All", "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY",
                "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MoneyTrackerApp().setVisible(true));
    }

    static class Transaction {
        String type;
        double amount;
        String category;
        String description;
        LocalDate date;

        public Transaction(String type, double amount, String category, String description, LocalDate date) {
            this.type = type;
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
        }

        public static Transaction fromString(String line) {
            try {
                String[] p = line.split(",");
return new Transaction(p[0], Double.parseDouble(p[1]), p[2], p[3], LocalDate.parse(p[4]));
            } catch (Exception e) {
                return null;
            }
        }

        public String toString() {
            return type + "," + amount + "," + category + "," + description + "," + date;
 }}
}
