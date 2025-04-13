package payroll;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageEmployeesPanel extends JPanel {

    JTable employeeTable;
    DefaultTableModel model;
    JTextField nameField, deptField, salaryField, allowField, deductField;
    JButton updateBtn, deleteBtn;

    public ManageEmployeesPanel() {
        setLayout(new BorderLayout());

        // Table Model
        model = new DefaultTableModel(new String[]{"ID", "Name", "Username", "Department", "Salary", "Allowances", "Deductions"}, 0);
        employeeTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        add(scrollPane, BorderLayout.CENTER);

        // Form for editing
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        nameField = new JTextField();
        deptField = new JTextField();
        salaryField = new JTextField();
        allowField = new JTextField();
        deductField = new JTextField();

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Department:"));
        form.add(deptField);
        form.add(new JLabel("Base Salary:"));
        form.add(salaryField);
        form.add(new JLabel("Allowances:"));
        form.add(allowField);
        form.add(new JLabel("Deductions:"));
        form.add(deductField);

        updateBtn = new JButton("Update");
        deleteBtn = new JButton("Delete");
        form.add(updateBtn);
        form.add(deleteBtn);

        add(form, BorderLayout.SOUTH);

        loadEmployees();

        employeeTable.getSelectionModel().addListSelectionListener(e -> populateForm());

        updateBtn.addActionListener(e -> updateEmployee());
        deleteBtn.addActionListener(e -> deleteEmployee());
    }

    public void loadEmployees() {
        model.setRowCount(0); // Clear old data
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT u.id, u.name, u.username, u.department, s.base_salary, s.allowances, s.deductions " +
                    "FROM users u JOIN employee_salary s ON u.id = s.emp_id WHERE u.role = 'employee'";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("department"),
                        rs.getDouble("base_salary"),
                        rs.getDouble("allowances"),
                        rs.getDouble("deductions")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load employees" + ex.getMessage());
        }
    }

    private void populateForm() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow >= 0) {
            nameField.setText(model.getValueAt(selectedRow, 1).toString());
            deptField.setText(model.getValueAt(selectedRow, 3).toString());
            salaryField.setText(model.getValueAt(selectedRow, 4).toString());
            allowField.setText(model.getValueAt(selectedRow, 5).toString());
            deductField.setText(model.getValueAt(selectedRow, 6).toString());
        }
    }

    private void updateEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) return;

        int empId = (int) model.getValueAt(selectedRow, 0);
        String name = nameField.getText();
        String dept = deptField.getText();
        double salary = Double.parseDouble(salaryField.getText());
        double allow = Double.parseDouble(allowField.getText());
        double deduct = Double.parseDouble(deductField.getText());

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps1 = conn.prepareStatement("UPDATE users SET name=?, department=? WHERE id=?");
            ps1.setString(1, name);
            ps1.setString(2, dept);
            ps1.setInt(3, empId);
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement("UPDATE employee_salary SET name=?, base_salary=?, allowances=?, deductions=? WHERE emp_id=?");
            ps2.setString(1, name);
            ps2.setDouble(2, salary);
            ps2.setDouble(3, allow);
            ps2.setDouble(4, deduct);
            ps2.setInt(5, empId);
            ps2.executeUpdate();

            JOptionPane.showMessageDialog(this, "Employee updated.");
            loadEmployees();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable  to update employee details" + ex.getMessage());
        }
    }

    private void deleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) return;

        int empId = (int) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps1 = conn.prepareStatement("DELETE FROM employee_salary WHERE emp_id=?");
            ps1.setInt(1, empId);
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM users WHERE id=?");
            ps2.setInt(1, empId);
            ps2.executeUpdate();

            JOptionPane.showMessageDialog(this, "Employee deleted.");
            loadEmployees();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to delete employee details" + ex.getMessage());
        }
    }
}
