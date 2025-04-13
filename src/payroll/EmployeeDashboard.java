package payroll;

import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;

public class EmployeeDashboard extends JFrame {

    private final int empId;
    private String name;
    private double baseSalary;
    private double allowances;
    private double deductions;

    private JTextField startDateField, endDateField, reasonField;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private JTable leaveRequestTable;

    public EmployeeDashboard(int empId) {
        this.empId = empId;

        // Set up the frame properties
        setTitle("Employee Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 123, 255));
        headerPanel.setPreferredSize(new Dimension(1000, 100));

        JLabel headerLabel = new JLabel("Employee Dashboard", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Side Menu Panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(5, 1, 10, 10));
        menuPanel.setPreferredSize(new Dimension(200, 500));
        menuPanel.setBackground(new Color(240, 240, 240));

        JButton btnPayslip = createMenuButton("Payslip");
        JButton btnLeave = createMenuButton("Leave Requests");
        JButton btnLogout = createMenuButton("Logout");

        menuPanel.add(btnPayslip);
        menuPanel.add(btnLeave);
        menuPanel.add(btnLogout);

        // Main Content Panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createPayslipPanel(), "payslip");
        mainPanel.add(createLeaveRequestPanel(), "leave");

        // Add listeners
        btnPayslip.addActionListener(e -> cardLayout.show(mainPanel, "payslip"));
        btnLeave.addActionListener(e -> cardLayout.show(mainPanel, "leave"));
        btnLogout.addActionListener(e -> logout());

        // Combine panels
        add(menuPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Set the frame visible
        setVisible(true);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private JPanel createPayslipPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        panel.setBackground(Color.WHITE);

        // Fetch employee details from the database
        fetchEmployeeDetails();

        JLabel nameLabel = createStyledLabel("Name: " + name);
        panel.add(nameLabel);

        JLabel salaryLabel = createStyledLabel("Base Salary: ₹" + baseSalary);
        panel.add(salaryLabel);

        JLabel allowancesLabel = createStyledLabel("Allowances: ₹" + allowances);
        panel.add(allowancesLabel);

        JLabel deductionsLabel = createStyledLabel("Deductions: ₹" + deductions);
        panel.add(deductionsLabel);

        JLabel netSalaryLabel = createStyledLabel("Net Salary: ₹" + (baseSalary + allowances - deductions));
        netSalaryLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(netSalaryLabel);

        // UI components
        JButton downloadPayslipButton = createStyledButton(e -> generatePayslip());
        panel.add(downloadPayslipButton);

        return panel;
    }

    private JPanel createLeaveRequestPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        panel.setBackground(Color.WHITE);

        // Leave request form
        JPanel leavePanel = new JPanel();
        leavePanel.setLayout(new GridLayout(4, 2, 10, 10));

        leavePanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        startDateField = new JTextField();
        leavePanel.add(startDateField);

        leavePanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        endDateField = new JTextField();
        leavePanel.add(endDateField);

        leavePanel.add(new JLabel("Reason:"));
        reasonField = new JTextField();
        leavePanel.add(reasonField);

        JButton applyLeaveButton = new JButton("Apply for Leave");
        applyLeaveButton.addActionListener(e -> applyForLeave());
        leavePanel.add(new JLabel());
        leavePanel.add(applyLeaveButton);

        panel.add(leavePanel);

        // Table to show leave requests
        String[] columnNames = {"Start Date", "End Date", "Reason", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        leaveRequestTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(leaveRequestTable);
        panel.add(scrollPane);

        // Fetch and display the leave requests
        fetchLeaveRequests();

        return panel;
    }

    private void fetchEmployeeDetails() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT name, base_salary, allowances, deductions FROM employee_salary WHERE emp_id=?");
            stmt.setInt(1, empId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.name = rs.getString("name");
                this.baseSalary = rs.getDouble("base_salary");
                this.allowances = rs.getDouble("allowances");
                this.deductions = rs.getDouble("deductions");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching Employee Details" + e.getMessage());
        }
    }

    private void fetchLeaveRequests() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT start_date, end_date, reason, status FROM leave_requests WHERE emp_id=?");
            stmt.setInt(1, empId);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = (DefaultTableModel) leaveRequestTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                String startDate = rs.getString("start_date");
                String endDate = rs.getString("end_date");
                String reason = rs.getString("reason");
                String status = rs.getString("status");

                model.addRow(new Object[]{startDate, endDate, reason, status});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching leave requests" + e.getMessage());
        }
    }

    private void generatePayslip() {
        String directoryPath = "E:\\java v2\\Payroll Management System\\Payslips\\";

        String filePath = directoryPath + "Payslip_" + empId + "_" + name + ".pdf";

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(new Paragraph("Payslip for " + name));
            document.add(new Paragraph("Employee ID: " + empId));
            document.add(new Paragraph("Base Salary: ₹" + baseSalary));
            document.add(new Paragraph("Allowances: ₹" + allowances));
            document.add(new Paragraph("Deductions: ₹" + deductions));

            double netSalary = baseSalary + allowances - deductions;
            document.add(new Paragraph("Net Salary: ₹" + netSalary));

            document.close();

            JOptionPane.showMessageDialog(this, "Payslip downloaded successfully!\nLocation: " + filePath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating payslip. Please try again.");
        }
    }

    private void logout() {
        dispose();
        new LoginFrame();
    }

    private void applyForLeave() {
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();
        String reason = reasonField.getText();

        if (startDate.isEmpty() || endDate.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO leave_requests (emp_id, start_date, end_date, reason, status) VALUES (?, ?, ?, ?, 'Pending')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, empId);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.setString(4, reason);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Leave request submitted successfully.");
            fetchLeaveRequests();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error submitting leave request.");
        }
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        label.setForeground(new Color(51, 51, 51));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return label;
    }

    private JButton createStyledButton(ActionListener actionListener) {
        JButton button = new JButton("Download Payslip");
        button.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setPreferredSize(new Dimension(200, 40));
        button.addActionListener(actionListener);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }
}
