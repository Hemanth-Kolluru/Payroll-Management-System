package payroll;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class AdminDashboard extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    private JTable leaveRequestsTable;
    private DefaultTableModel leaveModel;
    private JButton approveButton, rejectButton;
    ManageEmployeesPanel managePanel = new ManageEmployeesPanel();

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 123, 255));
        headerPanel.setPreferredSize(new Dimension(1000, 100));

        JLabel headerLabel = new JLabel("Admin Dashboard", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Side Menu Panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(6, 1, 10, 10));
        menuPanel.setPreferredSize(new Dimension(200, 500));
        menuPanel.setBackground(new Color(240, 240, 240));

        JButton btnAddEmp = createMenuButton("Add Employee");
        JButton btnManageEmp = createMenuButton("Manage Employees");
        JButton btnSalary = createMenuButton("Salary Management");
        JButton btnLeave = createMenuButton("Leave Requests");
        JButton btnReset = createMenuButton("Reset Password");
        JButton btnLogout = createMenuButton("Logout");

        menuPanel.add(btnAddEmp);
        menuPanel.add(btnManageEmp);
        menuPanel.add(btnSalary);
        menuPanel.add(btnLeave);
        menuPanel.add(btnReset);
        menuPanel.add(btnLogout);

        // Main Content Panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new JLabel("Welcome to Admin Dashboard", JLabel.CENTER), "home");
        mainPanel.add(new AddEmployeePanel(), "addEmp");
        mainPanel.add(managePanel, "manageEmp");
        btnManageEmp.addActionListener(e -> {
            managePanel.loadEmployees();
            cardLayout.show(mainPanel, "manageEmp");
        });
        mainPanel.add(createPlaceholder("Salary Management Panel"), "salary");
        mainPanel.add(createLeaveRequestsPanel(), "leave");
        mainPanel.add(createPlaceholder("Reset Password Panel"), "reset");

        // Add listeners
        btnAddEmp.addActionListener(e -> cardLayout.show(mainPanel, "addEmp"));
        btnManageEmp.addActionListener(e -> cardLayout.show(mainPanel, "manageEmp"));
        btnSalary.addActionListener(e -> cardLayout.show(mainPanel, "salary"));
        btnLeave.addActionListener(e -> {
            cardLayout.show(mainPanel, "leave");
            loadLeaveRequests();
        });
        btnReset.addActionListener(e -> cardLayout.show(mainPanel, "reset"));
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        // Combine panels
        add(menuPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

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

    private JPanel createPlaceholder(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel(title, JLabel.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLeaveRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table for leave requests
        leaveModel = new DefaultTableModel(new String[]{"Request ID", "Employee ID", "Start Date", "End Date", "Reason", "Status"}, 0);
        leaveRequestsTable = new JTable(leaveModel);
        JScrollPane leaveScrollPane = new JScrollPane(leaveRequestsTable);

        // Panel for action buttons (Approve/Reject)
        JPanel actionPanel = new JPanel();
        approveButton = new JButton("Approve");
        rejectButton = new JButton("Reject");

        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);

        approveButton.addActionListener(e -> updateLeaveRequest("Approved"));
        rejectButton.addActionListener(e -> updateLeaveRequest("Rejected"));

        actionPanel.add(approveButton);
        actionPanel.add(rejectButton);

        panel.add(leaveScrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        leaveRequestsTable.getSelectionModel().addListSelectionListener(e -> {
            if (leaveRequestsTable.getSelectedRow() >= 0) {
                approveButton.setEnabled(true);
                rejectButton.setEnabled(true);
            } else {
                approveButton.setEnabled(false);
                rejectButton.setEnabled(false);
            }
        });

        return panel;
    }

    private void loadLeaveRequests() {
        leaveModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM leave_requests WHERE status = 'Pending'";
            ResultSet rs = conn.createStatement().executeQuery(query);

            while (rs.next()) {
                leaveModel.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getInt("emp_id"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("reason"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching leave requests" + e.getMessage());
        }
    }

    private void updateLeaveRequest(String status) {
        int selectedRow = leaveRequestsTable.getSelectedRow();
        if (selectedRow < 0) return;

        int requestId = (int) leaveModel.getValueAt(selectedRow, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE leave_requests SET status=? WHERE request_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, requestId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Leave request " + status + " successfully.");
            loadLeaveRequests();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating leave request." + e.getMessage());
        }
    }
}
