package payroll;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddEmployeePanel extends JPanel {
    private final JTextField nameField;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JTextField departmentField;
    private final JTextField salaryField;
    private final JTextField allowancesField;
    private final JTextField deductionsField;

    public AddEmployeePanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titleLabel = new JLabel("Add New Employee");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));

        nameField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        departmentField = new JTextField();
        salaryField = new JTextField();
        allowancesField = new JTextField();
        deductionsField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Department:"));
        formPanel.add(departmentField);

        formPanel.add(new JLabel("Base Salary:"));
        formPanel.add(salaryField);

        formPanel.add(new JLabel("Allowances:"));
        formPanel.add(allowancesField);

        formPanel.add(new JLabel("Deductions:"));
        formPanel.add(deductionsField);

        add(formPanel, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Employee");
        add(addButton, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addEmployee());
    }

    private void addEmployee() {
        String name = nameField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String department = departmentField.getText();

        double salary, allowances, deductions;

        try {
            salary = Double.parseDouble(salaryField.getText());
            allowances = Double.parseDouble(allowancesField.getText());
            deductions = Double.parseDouble(deductionsField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for salary, allowances, and deductions.");
            return;
        }

        Connection conn = null;
        PreparedStatement psUser = null;
        PreparedStatement psSalary = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            // Step 1: Insert into users
            String insertUser = "INSERT INTO users(name, username, password, role, department) VALUES (?, ?, ?, ?, ?)";
            psUser = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            psUser.setString(1, name);
            psUser.setString(2, username);
            psUser.setString(3, password);
            psUser.setString(4, "employee");
            psUser.setString(5, department);
            psUser.executeUpdate();

            rs = psUser.getGeneratedKeys();
            int empId = -1;
            if (rs.next()) {
                empId = rs.getInt(1);
            }

            // Step 2: Insert into employee_salary
            String insertSalary = "INSERT INTO employee_salary(emp_id, name, base_salary, allowances, deductions) VALUES (?, ?, ?, ?, ?)";
            psSalary = conn.prepareStatement(insertSalary);
            psSalary.setInt(1, empId);
            psSalary.setString(2, name);
            psSalary.setDouble(3, salary);
            psSalary.setDouble(4, allowances);
            psSalary.setDouble(5, deductions);
            psSalary.executeUpdate();

            JOptionPane.showMessageDialog(this, "Employee added successfully!");

            // Clear the form
            nameField.setText("");
            usernameField.setText("");
            passwordField.setText("");
            departmentField.setText("");
            salaryField.setText("");
            allowancesField.setText("");
            deductionsField.setText("");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding employee: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (psSalary != null) psSalary.close(); } catch (Exception ignored) {}
            try { if (psUser != null) psUser.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
}
