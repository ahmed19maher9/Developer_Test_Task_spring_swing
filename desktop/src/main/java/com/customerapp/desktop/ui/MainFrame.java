package com.customerapp.desktop.ui;

import com.customerapp.desktop.api.CustomerApiClient;
import com.customerapp.desktop.model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private final CustomerApiClient apiClient;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton searchButton;
    private JButton clearButton;
    private JButton previousButton;
    private JButton nextButton;
    private JTextField searchField;
    private JLabel statusLabel;
    private JLabel paginationLabel;
    private JProgressBar progressBar;
    private int currentPage = 0;
    private int totalPages = 1;
    private int totalItems = 0;
    private int pageSize = 10;
    private String currentSearch = null;

    public MainFrame() {
        this.apiClient = new CustomerApiClient();
        initialize();
        loadCustomers(null);
    }

    private void initialize() {
        setTitle("Customer Management Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(25);
        searchPanel.add(searchField);
        
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> handleClearSearch());
        searchPanel.add(clearButton);
        
        topPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addButton = new JButton("Add Customer");
        editButton = new JButton("Edit Customer");
        deleteButton = new JButton("Delete Customer");
        refreshButton = new JButton("Refresh");

        addButton.addActionListener(this::handleAdd);
        editButton.addActionListener(this::handleEdit);
        deleteButton.addActionListener(this::handleDelete);
        refreshButton.addActionListener(e -> loadCustomers(currentSearch, currentPage));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Name", "Email", "Phone", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleEdit(new ActionEvent(e, 0, "Edit"));
                }
            }
        });
        
        // Add selection listener to update button states when selection changes
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        // Pagination panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        previousButton = new JButton("Previous");
        previousButton.setEnabled(false);
        previousButton.addActionListener(e -> handlePreviousPage());
        
        paginationLabel = new JLabel("Page 1 of 1 (0 items)");
        
        nextButton = new JButton("Next");
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> handleNextPage());
        
        paginationPanel.add(previousButton);
        paginationPanel.add(paginationLabel);
        paginationPanel.add(nextButton);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(paginationPanel, BorderLayout.CENTER);
        statusPanel.add(progressBar, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        updateButtonStates();
    }

    private void loadCustomers(String search) {
        loadCustomers(search, 0);
    }

    private void loadCustomers(String search, int page) {
        setBusy(true, "Loading customers...");
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return apiClient.getAllCustomersWithPagination(search, page, pageSize);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> response = get();
                    List<Customer> customers = (List<Customer>) response.get("customers");
                    currentPage = (Integer) response.get("currentPage");
                    totalPages = (Integer) response.get("totalPages");
                    totalItems = (Integer) response.get("totalItems");
                    currentSearch = search;
                    
                    tableModel.setRowCount(0);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    
                    for (Customer customer : customers) {
                        Object[] row = {
                            customer.getId(),
                            customer.getName(),
                            customer.getEmail(),
                            customer.getPhone() != null ? customer.getPhone() : "",
                            customer.getCreatedAt() != null ? customer.getCreatedAt().format(formatter) : ""
                        };
                        tableModel.addRow(row);
                    }
                    statusLabel.setText("Loaded " + customers.size() + " customer(s)");
                    updatePaginationControls();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.this, 
                        "Error loading customers: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error loading customers");
                } finally {
                    setBusy(false, "Ready");
                    updateButtonStates();
                }
            }
        };
        worker.execute();
    }

    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        currentPage = 0;
        loadCustomers(searchTerm.isEmpty() ? null : searchTerm, 0);
    }

    private void handleClearSearch() {
        searchField.setText("");
        currentPage = 0;
        loadCustomers(null, 0);
    }

    private void handlePreviousPage() {
        if (currentPage > 0) {
            loadCustomers(currentSearch, currentPage - 1);
        }
    }

    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            loadCustomers(currentSearch, currentPage + 1);
        }
    }

    private void updatePaginationControls() {
        previousButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);
        paginationLabel.setText(String.format("Page %d of %d (%d items)", currentPage + 1, totalPages, totalItems));
    }

    private void handleAdd(ActionEvent e) {
        CustomerDialog dialog = new CustomerDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Customer customer = dialog.getCustomer();
            saveCustomer(customer, false);
        }
    }

    private void handleEdit(ActionEvent e) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a customer to edit", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String email = (String) tableModel.getValueAt(selectedRow, 2);
        String phone = (String) tableModel.getValueAt(selectedRow, 3);

        Customer customer = new Customer(name, email, phone);
        customer.setId(id);

        CustomerDialog dialog = new CustomerDialog(this, customer);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Customer updatedCustomer = dialog.getCustomer();
            saveCustomer(updatedCustomer, true);
        }
    }

    private void handleDelete(ActionEvent e) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a customer to delete", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete " + name + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            deleteCustomer(id);
        }
    }

    private void saveCustomer(Customer customer, boolean isUpdate) {
        setBusy(true, isUpdate ? "Updating customer..." : "Creating customer...");
        SwingWorker<Customer, Void> worker = new SwingWorker<>() {
            @Override
            protected Customer doInBackground() throws Exception {
                if (isUpdate) {
                    return apiClient.updateCustomer(customer.getId(), customer);
                } else {
                    return apiClient.createCustomer(customer);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(MainFrame.this, 
                        (isUpdate ? "Customer updated successfully!" : "Customer created successfully!"), 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    loadCustomers(currentSearch, currentPage);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.this, 
                        "Error saving customer: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error saving customer");
                } finally {
                    setBusy(false, "Ready");
                }
            }
        };
        worker.execute();
    }

    private void deleteCustomer(Integer id) {
        setBusy(true, "Deleting customer...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiClient.deleteCustomer(id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(MainFrame.this, 
                        "Customer deleted successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    loadCustomers(currentSearch, currentPage);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.this, 
                        "Error deleting customer: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error deleting customer");
                } finally {
                    setBusy(false, "Ready");
                }
            }
        };
        worker.execute();
    }

    private void setBusy(boolean busy, String message) {
        statusLabel.setText(message);
        progressBar.setVisible(busy);
        addButton.setEnabled(!busy);
        editButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
        searchButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);
        searchField.setEnabled(!busy);
        previousButton.setEnabled(!busy && currentPage > 0);
        nextButton.setEnabled(!busy && currentPage < totalPages - 1);
        customerTable.setEnabled(!busy);
    }

    private void updateButtonStates() {
        boolean hasSelection = customerTable.getSelectedRow() != -1;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }
}
