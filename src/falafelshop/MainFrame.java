package falafelshop;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class MainFrame extends JFrame {
    private static final String SHOP_NAME = "Falafel House";
    private static final double TAX_RATE = 0.05;

    private final List<ShopMenuItem> menuItems;
    private final OrderManager orderManager;
    private final CashRegister cashRegister;
    private final Set<Integer> refundedOrderIds;

    private Order currentOrder;

    private final JComboBox<String> categoryCombo;
    private final DefaultListModel<ShopMenuItem> menuListModel;
    private final JList<ShopMenuItem> menuList;
    private final JTextArea menuDescriptionArea;

    private final DefaultTableModel orderTableModel;
    private final JTable orderTable;

    private final DefaultTableModel dailyOrdersTableModel;
    private final JTable dailyOrdersTable;

    private final JTextField cashierNameField;
    private final JTextField customerNameField;
    private final JTextField customerPhoneField;
    private final JComboBox<OrderType> orderTypeCombo;
    private final JComboBox<OrderStatus> statusCombo;

    private final JComboBox<DiscountType> discountTypeCombo;
    private final JTextField discountValueField;

    private final JLabel subtotalValueLabel;
    private final JLabel discountValueLabel;
    private final JLabel taxValueLabel;
    private final JLabel totalValueLabel;

    private final JComboBox<PaymentMethod> paymentMethodCombo;
    private final JTextField paidAmountField;
    private final JLabel changeValueLabel;

    private final JLabel currentOrderInfoLabel;

    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public MainFrame() {
        this.menuItems = createDefaultMenuItems();
        this.orderManager = new OrderManager();
        this.cashRegister = new CashRegister(200.00);
        this.refundedOrderIds = new HashSet<>();

        setTitle(SHOP_NAME + " - Cashier System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        this.menuListModel = new DefaultListModel<>();
        this.menuList = new JList<>(menuListModel);
        this.menuDescriptionArea = new JTextArea(4, 20);
        this.categoryCombo = new JComboBox<>();

        this.orderTableModel = new DefaultTableModel(new Object[]{"Item", "Qty", "Unit", "Line Total", "Notes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.orderTable = new JTable(orderTableModel);

        this.dailyOrdersTableModel = new DefaultTableModel(new Object[]{"Order #", "Time", "Customer", "Type", "Total", "Payment", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.dailyOrdersTable = new JTable(dailyOrdersTableModel);

        this.cashierNameField = new JTextField("Cashier 1", 12);
        this.customerNameField = new JTextField(12);
        this.customerPhoneField = new JTextField(12);
        this.orderTypeCombo = new JComboBox<>(OrderType.values());
        this.statusCombo = new JComboBox<>(OrderStatus.values());

        this.discountTypeCombo = new JComboBox<>(DiscountType.values());
        this.discountValueField = new JTextField("0", 8);

        this.subtotalValueLabel = new JLabel("$0.00");
        this.discountValueLabel = new JLabel("$0.00");
        this.taxValueLabel = new JLabel("$0.00");
        this.totalValueLabel = new JLabel("$0.00");

        this.paymentMethodCombo = new JComboBox<>(PaymentMethod.values());
        this.paidAmountField = new JTextField(8);
        this.changeValueLabel = new JLabel("$0.00");

        this.currentOrderInfoLabel = new JLabel();

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);
        add(createDailyOrdersPanel(), BorderLayout.SOUTH);

        initializeMenuArea();
        wireActions();
        startNewOrder();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel titleLabel = new JLabel(SHOP_NAME + " Cashier POS", SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel cashierPanel = new JPanel();
        cashierPanel.add(new JLabel("Cashier:"));
        cashierPanel.add(cashierNameField);
        headerPanel.add(cashierPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JSplitPane createMainContentPanel() {
        JPanel menuPanel = createMenuPanel();
        JPanel currentOrderPanel = createCurrentOrderPanel();
        JPanel controlsPanel = createControlsPanel();

        JSplitPane leftCenterSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPanel, currentOrderPanel);
        leftCenterSplit.setResizeWeight(0.35);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCenterSplit, controlsPanel);
        mainSplit.setResizeWeight(0.75);

        return mainSplit;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Menu"));

        JPanel topFilterPanel = new JPanel(new BorderLayout(5, 5));
        topFilterPanel.add(new JLabel("Category:"), BorderLayout.WEST);
        topFilterPanel.add(categoryCombo, BorderLayout.CENTER);
        panel.add(topFilterPanel, BorderLayout.NORTH);

        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                java.awt.Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ShopMenuItem) {
                    ShopMenuItem item = (ShopMenuItem) value;
                    setText(item.getName() + " - " + String.format("$%.2f", item.getPrice()));
                }
                return component;
            }
        });

        panel.add(new JScrollPane(menuList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        JButton addItemButton = new JButton("Add Item");
        bottomPanel.add(addItemButton, BorderLayout.NORTH);

        menuDescriptionArea.setEditable(false);
        menuDescriptionArea.setLineWrap(true);
        menuDescriptionArea.setWrapStyleWord(true);
        menuDescriptionArea.setBorder(BorderFactory.createTitledBorder("Item Description"));
        bottomPanel.add(menuDescriptionArea, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        addItemButton.addActionListener(e -> addSelectedMenuItem());

        return panel;
    }

    private JPanel createCurrentOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Current Order"));

        currentOrderInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(currentOrderInfoLabel, BorderLayout.NORTH);

        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(1, 4, 6, 0));
        JButton increaseButton = new JButton("Increase Qty");
        JButton decreaseButton = new JButton("Decrease Qty");
        JButton removeButton = new JButton("Remove Selected");
        JButton noteButton = new JButton("Edit Note");

        controls.add(increaseButton);
        controls.add(decreaseButton);
        controls.add(removeButton);
        controls.add(noteButton);
        panel.add(controls, BorderLayout.SOUTH);

        increaseButton.addActionListener(e -> increaseSelectedItemQuantity());
        decreaseButton.addActionListener(e -> decreaseSelectedItemQuantity());
        removeButton.addActionListener(e -> removeSelectedItem());
        noteButton.addActionListener(e -> editSelectedItemNote());

        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Order Controls"));

        panel.add(createCustomerSection());
        panel.add(Box.createVerticalStrut(8));
        panel.add(createDiscountSection());
        panel.add(Box.createVerticalStrut(8));
        panel.add(createTotalsSection());
        panel.add(Box.createVerticalStrut(8));
        panel.add(createPaymentSection());
        panel.add(Box.createVerticalStrut(8));
        panel.add(createActionButtonsSection());

        return panel;
    }

    private JPanel createCustomerSection() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Customer & Order"));

        panel.add(new JLabel("Customer Name:"));
        panel.add(customerNameField);
        panel.add(new JLabel("Phone:"));
        panel.add(customerPhoneField);
        panel.add(new JLabel("Order Type:"));
        panel.add(orderTypeCombo);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);

        return panel;
    }

    private JPanel createDiscountSection() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Discount"));

        JButton applyDiscountButton = new JButton("Apply Discount");
        panel.add(new JLabel("Discount Type:"));
        panel.add(discountTypeCombo);
        panel.add(new JLabel("Value:"));
        panel.add(discountValueField);
        panel.add(new JLabel(""));
        panel.add(applyDiscountButton);

        applyDiscountButton.addActionListener(e -> applyDiscount());

        return panel;
    }

    private JPanel createTotalsSection() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Totals"));

        panel.add(new JLabel("Subtotal:"));
        panel.add(subtotalValueLabel);
        panel.add(new JLabel("Discount:"));
        panel.add(discountValueLabel);
        panel.add(new JLabel("Tax (5%):"));
        panel.add(taxValueLabel);
        panel.add(new JLabel("Grand Total:"));
        panel.add(totalValueLabel);

        return panel;
    }

    private JPanel createPaymentSection() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Payment"));

        panel.add(new JLabel("Method:"));
        panel.add(paymentMethodCombo);
        panel.add(new JLabel("Amount Paid:"));
        panel.add(paidAmountField);
        panel.add(new JLabel("Change:"));
        panel.add(changeValueLabel);

        return panel;
    }

    private JPanel createActionButtonsSection() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton completeButton = new JButton("Complete Order");
        JButton cancelButton = new JButton("Cancel Order");
        JButton newOrderButton = new JButton("New Order");
        JButton historyButton = new JButton("View Order History");
        JButton summaryButton = new JButton("View Register Summary");
        JButton exportHistoryButton = new JButton("Export History");

        panel.add(completeButton);
        panel.add(cancelButton);
        panel.add(newOrderButton);
        panel.add(historyButton);
        panel.add(summaryButton);
        panel.add(exportHistoryButton);

        completeButton.addActionListener(e -> completeOrder());
        cancelButton.addActionListener(e -> cancelCurrentOrder());
        newOrderButton.addActionListener(e -> handleNewOrderButton());
        historyButton.addActionListener(e -> openOrderHistoryDialog());
        summaryButton.addActionListener(e -> showRegisterSummaryDialog());
        exportHistoryButton.addActionListener(e -> exportOrderHistory());

        return panel;
    }

    private JPanel createDailyOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(1000, 200));
        panel.setBorder(BorderFactory.createTitledBorder("Daily Orders (Session)"));

        dailyOrdersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(dailyOrdersTable), BorderLayout.CENTER);

        JButton openSummaryButton = new JButton("Open Selected Summary");
        openSummaryButton.addActionListener(e -> openSelectedDailyOrderSummary());
        panel.add(openSummaryButton, BorderLayout.EAST);

        return panel;
    }

    private void initializeMenuArea() {
        categoryCombo.addItem("All");
        Set<String> categories = new LinkedHashSet<>();
        for (ShopMenuItem item : menuItems) {
            categories.add(item.getCategory());
        }
        for (String category : categories) {
            categoryCombo.addItem(category);
        }

        refreshMenuList();
    }

    private void wireActions() {
        categoryCombo.addActionListener(e -> refreshMenuList());

        menuList.addListSelectionListener(e -> {
            ShopMenuItem selected = menuList.getSelectedValue();
            menuDescriptionArea.setText(selected == null ? "" : selected.getDescription());
        });

        statusCombo.addActionListener(e -> {
            if (currentOrder != null) {
                OrderStatus selectedStatus = (OrderStatus) statusCombo.getSelectedItem();
                if (selectedStatus != null && selectedStatus != OrderStatus.COMPLETED && selectedStatus != OrderStatus.CANCELLED) {
                    currentOrder.setStatus(selectedStatus);
                    updateCurrentOrderInfoLabel();
                    refreshDailyOrdersTable();
                }
            }
        });

        paymentMethodCombo.addActionListener(e -> {
            updatePaymentFieldBehavior();
            updateChangePreview();
        });

        paidAmountField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateChangePreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateChangePreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateChangePreview();
            }
        });

        discountValueField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTotals();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTotals();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshTotals();
            }
        });

        discountTypeCombo.addActionListener(e -> refreshTotals());

        dailyOrdersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedDailyOrderSummary();
                }
            }
        });
    }

    private void refreshMenuList() {
        String selectedCategory = (String) categoryCombo.getSelectedItem();
        menuListModel.clear();

        for (ShopMenuItem item : menuItems) {
            if (selectedCategory == null || "All".equals(selectedCategory) || item.getCategory().equals(selectedCategory)) {
                menuListModel.addElement(item);
            }
        }

        if (!menuListModel.isEmpty()) {
            menuList.setSelectedIndex(0);
        }
    }

    private void addSelectedMenuItem() {
        ShopMenuItem selected = menuList.getSelectedValue();
        if (selected == null) {
            showError("Please select a menu item to add.");
            return;
        }

        currentOrder.addItem(selected);
        refreshOrderTable();
        refreshTotals();
    }

    private void increaseSelectedItemQuantity() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            showError("Select an item from the current order first.");
            return;
        }

        currentOrder.increaseQuantity(row);
        refreshOrderTable();
        refreshTotals();
    }

    private void decreaseSelectedItemQuantity() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            showError("Select an item from the current order first.");
            return;
        }

        currentOrder.decreaseQuantity(row);
        refreshOrderTable();
        refreshTotals();
    }

    private void removeSelectedItem() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            showError("Select an item to remove.");
            return;
        }

        currentOrder.removeItem(row);
        refreshOrderTable();
        refreshTotals();
    }

    private void editSelectedItemNote() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            showError("Select an item to edit notes.");
            return;
        }

        String currentNote = currentOrder.getItems().get(row).getNote();
        String newNote = JOptionPane.showInputDialog(this,
            "Enter item note/customization:",
            currentNote);

        if (newNote != null) {
            currentOrder.setItemNote(row, newNote);
            refreshOrderTable();
        }
    }

    private void applyDiscount() {
        if (!applyDiscountValuesFromInput(true)) {
            return;
        }

        refreshTotals();
        JOptionPane.showMessageDialog(this, "Discount applied.", "Discount", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean applyDiscountValuesFromInput(boolean showErrors) {
        DiscountType discountType = (DiscountType) discountTypeCombo.getSelectedItem();
        if (discountType == null) {
            currentOrder.setDiscount(DiscountType.NONE, 0);
            return true;
        }

        if (discountType == DiscountType.NONE) {
            currentOrder.setDiscount(DiscountType.NONE, 0);
            return true;
        }

        String rawValue = discountValueField.getText() == null ? "" : discountValueField.getText().trim();
        if (rawValue.isEmpty()) {
            if (showErrors) {
                showError("Please enter a discount value.");
            }
            return false;
        }

        double parsed;
        try {
            parsed = Double.parseDouble(rawValue);
        } catch (NumberFormatException ex) {
            if (showErrors) {
                showError("Invalid discount value. Please enter a valid number.");
            }
            return false;
        }

        if (parsed < 0) {
            if (showErrors) {
                showError("Discount cannot be negative.");
            }
            return false;
        }

        if (discountType == DiscountType.PERCENT && parsed > 100) {
            if (showErrors) {
                showError("Percentage discount must be between 0 and 100.");
            }
            return false;
        }

        currentOrder.calculateTotals(TAX_RATE);
        if (discountType == DiscountType.FIXED && parsed > currentOrder.getSubtotal()) {
            if (showErrors) {
                showError("Fixed discount cannot exceed subtotal.");
            }
            return false;
        }

        currentOrder.setDiscount(discountType, parsed);
        return true;
    }

    private void refreshOrderTable() {
        orderTableModel.setRowCount(0);

        for (OrderItem item : currentOrder.getItems()) {
            orderTableModel.addRow(new Object[]{
                item.getMenuItem().getName(),
                item.getQuantity(),
                formatMoney(item.getMenuItem().getPrice()),
                formatMoney(item.calculateLineTotal()),
                item.getNote()
            });
        }

        updateCurrentOrderInfoLabel();
    }

    private void refreshTotals() {
        applyDiscountValuesFromInput(false);
        currentOrder.calculateTotals(TAX_RATE);

        subtotalValueLabel.setText(formatMoney(currentOrder.getSubtotal()));
        discountValueLabel.setText("-" + formatMoney(currentOrder.getDiscountAmount()));
        taxValueLabel.setText(formatMoney(currentOrder.getTax()));
        totalValueLabel.setText(formatMoney(currentOrder.getTotal()));

        updatePaymentFieldBehavior();
        updateChangePreview();
    }

    private void updatePaymentFieldBehavior() {
        PaymentMethod method = (PaymentMethod) paymentMethodCombo.getSelectedItem();
        boolean cashMode = method != null && method.isCash();

        paidAmountField.setEnabled(cashMode);
        if (!cashMode) {
            paidAmountField.setText(String.format("%.2f", currentOrder.getTotal()));
        }
    }

    private void updateChangePreview() {
        PaymentMethod method = (PaymentMethod) paymentMethodCombo.getSelectedItem();
        if (method == null) {
            changeValueLabel.setText(formatMoney(0));
            return;
        }

        if (!method.isCash()) {
            changeValueLabel.setText(formatMoney(0));
            return;
        }

        try {
            double amountPaid = Double.parseDouble(paidAmountField.getText().trim());
            double change = amountPaid - currentOrder.getTotal();
            changeValueLabel.setText(formatMoney(Math.max(0, change)));
        } catch (Exception ex) {
            changeValueLabel.setText(formatMoney(0));
        }
    }

    private void completeOrder() {
        if (currentOrder.getItems().isEmpty()) {
            showError("Cannot complete an empty order.");
            return;
        }

        if (!applyDiscountValuesFromInput(true)) {
            return;
        }

        currentOrder.setCustomerName(customerNameField.getText());
        currentOrder.setCustomerPhone(customerPhoneField.getText());
        currentOrder.setOrderType((OrderType) orderTypeCombo.getSelectedItem());
        currentOrder.calculateTotals(TAX_RATE);

        PaymentMethod paymentMethod = (PaymentMethod) paymentMethodCombo.getSelectedItem();
        if (paymentMethod == null) {
            showError("Please choose a payment method.");
            return;
        }

        double paidAmount;
        if (paymentMethod.isCash()) {
            String amountText = paidAmountField.getText() == null ? "" : paidAmountField.getText().trim();
            if (amountText.isEmpty()) {
                showError("Please enter amount paid for cash payment.");
                return;
            }

            try {
                paidAmount = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
                showError("Paid amount must be numeric.");
                return;
            }
        } else {
            paidAmount = currentOrder.getTotal();
        }

        Payment payment = new Payment(paymentMethod, paidAmount);
        if (!payment.processPayment(currentOrder.getTotal())) {
            showError(payment.getMessage());
            return;
        }

        currentOrder.setPaymentDetails(payment.getPaymentMethod(), payment.getAmountPaid(), payment.getChange());
        currentOrder.setStatus(OrderStatus.COMPLETED);

        orderManager.addOrder(currentOrder);
        cashRegister.addSale(currentOrder);

        refreshDailyOrdersTable();

        String receipt = ReceiptGenerator.generateReceipt(currentOrder, SHOP_NAME, cashierNameField.getText());
        showReceiptDialog(receipt, currentOrder.getOrderId());

        startNewOrder();
    }

    private void cancelCurrentOrder() {
        if (currentOrder.getItems().isEmpty()) {
            startNewOrder();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Cancel this order?",
            "Confirm Cancel",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        currentOrder.setCustomerName(customerNameField.getText());
        currentOrder.setCustomerPhone(customerPhoneField.getText());
        currentOrder.setOrderType((OrderType) orderTypeCombo.getSelectedItem());
        currentOrder.calculateTotals(TAX_RATE);
        currentOrder.setStatus(OrderStatus.CANCELLED);

        orderManager.addOrder(currentOrder);
        refreshDailyOrdersTable();
        startNewOrder();
    }

    private void handleNewOrderButton() {
        if (!currentOrder.getItems().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Start a new order and clear the current one?",
                "New Order",
                JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        startNewOrder();
    }

    private void startNewOrder() {
        currentOrder = orderManager.createNewOrder();

        customerNameField.setText("");
        customerPhoneField.setText("");
        orderTypeCombo.setSelectedItem(OrderType.TAKEAWAY);
        statusCombo.setSelectedItem(OrderStatus.PENDING);

        discountTypeCombo.setSelectedItem(DiscountType.NONE);
        discountValueField.setText("0");

        paymentMethodCombo.setSelectedItem(PaymentMethod.CASH);
        paidAmountField.setText("");

        currentOrder.setStatus(OrderStatus.PENDING);
        refreshOrderTable();
        refreshTotals();
    }

    private void updateCurrentOrderInfoLabel() {
        currentOrderInfoLabel.setText("Order #" + currentOrder.getOrderId() + " | Status: " + currentOrder.getStatus());
    }

    private void refreshDailyOrdersTable() {
        dailyOrdersTableModel.setRowCount(0);

        for (Order order : orderManager.getOrders()) {
            dailyOrdersTableModel.addRow(new Object[]{
                order.getOrderId(),
                order.getDateTime().format(ORDER_TIME_FORMATTER),
                order.getCustomerName().isBlank() ? "Walk-in" : order.getCustomerName(),
                order.getOrderType(),
                formatMoney(order.getTotal()),
                order.getPaymentMethod() == null ? "N/A" : order.getPaymentMethod(),
                order.getStatus()
            });
        }
    }

    private void openSelectedDailyOrderSummary() {
        int selectedRow = dailyOrdersTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Select an order from the daily orders table.");
            return;
        }

        int modelRow = dailyOrdersTable.convertRowIndexToModel(selectedRow);
        int orderId = Integer.parseInt(dailyOrdersTableModel.getValueAt(modelRow, 0).toString());
        Order order = orderManager.findById(orderId);

        if (order == null) {
            showError("Order not found.");
            return;
        }

        String receipt = ReceiptGenerator.generateReceipt(order, SHOP_NAME, cashierNameField.getText());
        showReceiptDialog(receipt, order.getOrderId());
    }

    private void openOrderHistoryDialog() {
        JDialog dialog = new JDialog(this, "Order History", true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        DefaultTableModel historyModel = new DefaultTableModel(new Object[]{"Order #", "Date/Time", "Customer", "Type", "Total", "Payment", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = new JTable(historyModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(historyModel);
        historyTable.setRowSorter(sorter);

        JPanel topPanel = new JPanel(new BorderLayout(6, 6));
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");

        JPanel searchButtons = new JPanel(new GridLayout(1, 2, 6, 0));
        searchButtons.add(searchButton);
        searchButtons.add(clearButton);

        topPanel.add(new JLabel("Search by Order # or Customer:"), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchButtons, BorderLayout.EAST);

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 4, 6, 0));
        JButton openSummaryButton = new JButton("Open Summary");
        JComboBox<OrderStatus> updateStatusCombo = new JComboBox<>(OrderStatus.values());
        JButton updateStatusButton = new JButton("Update Status");
        JButton closeButton = new JButton("Close");

        bottomPanel.add(openSummaryButton);
        bottomPanel.add(updateStatusCombo);
        bottomPanel.add(updateStatusButton);
        bottomPanel.add(closeButton);

        dialog.add(bottomPanel, BorderLayout.SOUTH);

        Runnable loadAllOrders = () -> loadOrdersIntoTable(historyModel, orderManager.getOrders());
        loadAllOrders.run();

        searchButton.addActionListener(e -> {
            String query = searchField.getText();
            List<Order> results = orderManager.searchOrders(query);
            loadOrdersIntoTable(historyModel, results);
            if (query != null && !query.isBlank()) {
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
            } else {
                sorter.setRowFilter(null);
            }
        });

        clearButton.addActionListener(e -> {
            searchField.setText("");
            sorter.setRowFilter(null);
            loadAllOrders.run();
        });

        openSummaryButton.addActionListener(e -> {
            int selected = historyTable.getSelectedRow();
            if (selected < 0) {
                showError("Select an order to open summary.");
                return;
            }

            int modelIndex = historyTable.convertRowIndexToModel(selected);
            int orderId = Integer.parseInt(historyModel.getValueAt(modelIndex, 0).toString());
            Order order = orderManager.findById(orderId);

            if (order == null) {
                showError("Order not found.");
                return;
            }

            String receipt = ReceiptGenerator.generateReceipt(order, SHOP_NAME, cashierNameField.getText());
            showReceiptDialog(receipt, order.getOrderId());
        });

        updateStatusButton.addActionListener(e -> {
            int selected = historyTable.getSelectedRow();
            if (selected < 0) {
                showError("Select an order to update status.");
                return;
            }

            int modelIndex = historyTable.convertRowIndexToModel(selected);
            int orderId = Integer.parseInt(historyModel.getValueAt(modelIndex, 0).toString());
            Order order = orderManager.findById(orderId);

            if (order == null) {
                showError("Order not found.");
                return;
            }

            OrderStatus newStatus = (OrderStatus) updateStatusCombo.getSelectedItem();
            if (newStatus == null) {
                return;
            }

            if (newStatus == OrderStatus.REFUNDED && !refundedOrderIds.contains(orderId)) {
                cashRegister.addRefund(order.getTotal());
                refundedOrderIds.add(orderId);
            }

            order.setStatus(newStatus);
            loadAllOrders.run();
            refreshDailyOrdersTable();
        });

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void loadOrdersIntoTable(DefaultTableModel model, List<Order> orders) {
        model.setRowCount(0);
        for (Order order : new ArrayList<>(orders)) {
            model.addRow(new Object[]{
                order.getOrderId(),
                order.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                order.getCustomerName().isBlank() ? "Walk-in" : order.getCustomerName(),
                order.getOrderType(),
                formatMoney(order.getTotal()),
                order.getPaymentMethod() == null ? "N/A" : order.getPaymentMethod(),
                order.getStatus()
            });
        }
    }

    private void showRegisterSummaryDialog() {
        JTextArea textArea = new JTextArea(cashRegister.getSummaryText());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Register Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportOrderHistory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("order_history.txt"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            orderManager.exportOrderHistory(chooser.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Order history exported successfully.", "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError("Failed to export order history: " + ex.getMessage());
        }
    }

    private void showReceiptDialog(String receiptText, int orderId) {
        JTextArea receiptArea = new JTextArea(receiptText);
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        Object[] options = {"Close", "Save Receipt"};
        int choice = JOptionPane.showOptionDialog(
            this,
            new JScrollPane(receiptArea),
            "Receipt - Order #" + orderId,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice == 1) {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("receipt_order_" + orderId + ".txt"));

            int saveResult = chooser.showSaveDialog(this);
            if (saveResult == JFileChooser.APPROVE_OPTION) {
                try {
                    ReceiptGenerator.saveReceiptToFile(receiptText, chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Receipt saved.", "Receipt", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    showError("Failed to save receipt: " + ex.getMessage());
                }
            }
        }
    }

    private String formatMoney(double amount) {
        return String.format("$%.2f", amount);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private List<ShopMenuItem> createDefaultMenuItems() {
        List<ShopMenuItem> items = new ArrayList<>();

        items.add(new ShopMenuItem("S1", "Falafel Sandwich", "Sandwiches", 6.00, "Crispy falafel in pita with veggies and tahini."));
        items.add(new ShopMenuItem("S2", "Falafel Plate", "Plates / Meals", 12.00, "Falafel plate served with hummus, salad, and bread."));
        items.add(new ShopMenuItem("S3", "Falafel Box", "Plates / Meals", 10.00, "Falafel box meal for takeaway."));
        items.add(new ShopMenuItem("E1", "Hummus Plate", "Extras", 8.00, "Creamy hummus with olive oil and bread."));
        items.add(new ShopMenuItem("E2", "Fries", "Extras", 5.00, "Golden crispy fries."));
        items.add(new ShopMenuItem("E3", "Extra Falafel (4 pieces)", "Extras", 3.00, "Four extra falafel pieces."));
        items.add(new ShopMenuItem("SA1", "Tahini Sauce", "Sauces", 1.00, "Classic tahini sauce."));
        items.add(new ShopMenuItem("SA2", "Spicy Sauce", "Sauces", 1.00, "House spicy chili sauce."));
        items.add(new ShopMenuItem("D1", "Soft Drink", "Drinks", 2.50, "Assorted canned soft drinks."));
        items.add(new ShopMenuItem("D2", "Water", "Drinks", 1.50, "Bottled water."));

        return items;
    }
}
