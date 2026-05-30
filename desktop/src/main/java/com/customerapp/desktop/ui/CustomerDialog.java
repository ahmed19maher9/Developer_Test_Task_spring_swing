package com.customerapp.desktop.ui;

import com.customerapp.desktop.model.Customer;

import javax.swing.*;
import java.awt.*;

public class CustomerDialog extends JDialog {
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private Customer customer;
    private boolean confirmed = false;

    public CustomerDialog(Frame parent, Customer customer) {
        super(parent, customer == null ? "Add Customer" : "Edit Customer", true);
        this.customer = customer;
        initialize();
    }

    private void initialize() {
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        if (customer != null) {
            nameField.setText(customer.getName());
            emailField.setText(customer.getEmail());
            phoneField.setText(customer.getPhone());
        }
    }

    private boolean validateInput() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public Customer getCustomer() {
        if (!confirmed) return null;
        
        Customer result = customer != null ? customer : new Customer();
        result.setName(nameField.getText().trim());
        result.setEmail(emailField.getText().trim());
        result.setPhone(phoneField.getText().trim());
        return result;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
