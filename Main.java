package student;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main extends JFrame { //jframe-Window in swing
    private StudentDAO studentDAO = new StudentDAO();//student db actions
    private AttendanceDAO attendanceDAO = new AttendanceDAO();//attendence db actions

    public Main() {
        setTitle("🎓 Student Attendance Management");//sets window title(top bar)
        setSize(600, 450);//window size in pixels
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ensure program closes when you click ❌.
        setLocationRelativeTo(null);//centers the window on screen
        setLayout(new BorderLayout());

        JLabel heading = new JLabel("🎓 Student Attendance Management", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(heading, BorderLayout.NORTH);//place it at top of the window
        //jpanel-create panel
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 10, 10));//4 rows, 1 column, 10px gaps between buttons.
        JButton addStudentBtn = new JButton("Add Student");
        JButton viewStudentsBtn = new JButton("View Students");
        JButton markAttendanceBtn = new JButton("Mark Attendance");
        JButton viewAttendanceBtn = new JButton("View Attendance");
        JButton attendanceReportBtn = new JButton("View Attendance Report");
        JButton addMultipleBtn = new JButton("Add Multiple Students");
        //add buttons to the panel
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        buttonsPanel.add(addStudentBtn);
        buttonsPanel.add(viewStudentsBtn);
        buttonsPanel.add(markAttendanceBtn);
        buttonsPanel.add(viewAttendanceBtn);
        buttonsPanel.add(attendanceReportBtn);
        buttonsPanel.add(addMultipleBtn);

        add(buttonsPanel, BorderLayout.CENTER);

        // 🎯 Button Actions
        addStudentBtn.addActionListener(e -> addStudentDialog());
        viewStudentsBtn.addActionListener(e -> showStudents());
        markAttendanceBtn.addActionListener(e -> markAttendanceDialog());
        viewAttendanceBtn.addActionListener(e -> showAttendance());
        attendanceReportBtn.addActionListener(e -> showAttendanceReport());
        addMultipleBtn.addActionListener(e -> addMultipleStudentsDialog());

    }

    private void addStudentDialog() {
        JTextField nameField = new JTextField();
        JTextField rollField = new JTextField();
        JTextField deptField = new JTextField();

        Object[] fields = {
                "Name:", nameField,
                "Roll No:", rollField,
                "Department:", deptField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add Student", JOptionPane.OK_CANCEL_OPTION);
        //optionpane create a dialog box
        if (option == JOptionPane.OK_OPTION) {
            studentDAO.addStudent(nameField.getText(), rollField.getText(), deptField.getText());
            JOptionPane.showMessageDialog(this, "✅ Student added successfully!");
        }
    }
    
    private void addMultipleStudentsDialog() {
        JTextArea inputArea = new JTextArea(10, 40);
        inputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        inputArea.setText("Example:\nRohini, 21IT045, IT\nRahul, 21IT046, CSE");

        JScrollPane scrollPane = new JScrollPane(inputArea);
        int option = JOptionPane.showConfirmDialog(this, scrollPane, "Add Multiple Students", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String[] lines = inputArea.getText().split("\\n");
            int addedCount = 0;

            for (String line : lines) {
                // Skip example line or empty lines
                if (line.trim().isEmpty() || line.toLowerCase().contains("example")) continue;

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    JOptionPane.showMessageDialog(this, "⚠️ Invalid format for: " + line);
                    continue;
                }

                String name = parts[0].trim();
                String roll = parts[1].trim();
                String dept = parts[2].trim();

                studentDAO.addStudent(name, roll, dept);
                addedCount++;
            }

            JOptionPane.showMessageDialog(this, "✅ " + addedCount + " students added successfully!");
        }
    }

    private void showStudents() {
        try (java.sql.Connection con = DBConnection.getConnection()) {
            String query = "SELECT id, name, roll_no, department FROM students";
            java.sql.Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery(query);

            String[] columns = {"ID", "Name", "Roll No", "Department"};
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String roll = rs.getString("roll_no");
                String dept = rs.getString("department");
                model.addRow(new Object[]{id, name, roll, dept});
            }

            javax.swing.JTable table = new javax.swing.JTable(model);
            table.setEnabled(false);
            table.setRowHeight(25);
            javax.swing.table.JTableHeader header = table.getTableHeader();
            header.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 13));
            header.setBackground(new java.awt.Color(220, 220, 220));

            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(table);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 250));

            JOptionPane.showMessageDialog(this, scrollPane, "🎓 Student List", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "⚠️ Error loading students: " + e.getMessage());
        }
    }


    private void markAttendanceDialog() {
        List<String> students = studentDAO.getAllStudents();
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students available!");
            return;
        }

     // Ask for date first
        JTextField dateField = new JTextField(java.time.LocalDate.now().toString());
        Object[] dateInput = { "Enter Attendance Date (YYYY-MM-DD):", dateField };
        int dateOption = JOptionPane.showConfirmDialog(this, dateInput, "Select Date", JOptionPane.OK_CANCEL_OPTION);
        if (dateOption != JOptionPane.OK_OPTION) return;

        String date = dateField.getText().trim();

        // Validate date format
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "⚠️ Please enter a valid date in format YYYY-MM-DD (e.g., 2025-11-06)");
            return;
        }

        // 🔹 Main panel with vertical layout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Select Present Students (" + date + ")");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 🔹 Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));        
        JButton selectAllBtn = new JButton("Select All");
        JButton deselectAllBtn = new JButton("Deselect All");
        buttonPanel.add(selectAllBtn);
        buttonPanel.add(deselectAllBtn);
        panel.add(buttonPanel);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 🔹 Checkboxes for students
        JCheckBox[] boxes = new JCheckBox[students.size()];
        JPanel studentPanel = new JPanel(new GridLayout(0, 1));
        for (int i = 0; i < students.size(); i++) {
            boxes[i] = new JCheckBox(students.get(i));
            studentPanel.add(boxes[i]);
        }

        JScrollPane scrollPane = new JScrollPane(studentPanel);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        panel.add(scrollPane);

        // 🎯 Action for Select All
        selectAllBtn.addActionListener(e -> {
            for (JCheckBox box : boxes) box.setSelected(true);
        });

        // 🎯 Action for Deselect All
        deselectAllBtn.addActionListener(e -> {
            for (JCheckBox box : boxes) box.setSelected(false);
        });

        // 🔹 Show dialog
        int option = JOptionPane.showConfirmDialog(this, panel, "Mark Attendance", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            int count = 0;
            for (int i = 0; i < students.size(); i++) {
                int studentId = Integer.parseInt(students.get(i).split(" ")[0]);
                String status = boxes[i].isSelected() ? "Present" : "Absent";
                attendanceDAO.markAttendance(studentId, date, status);
                if (boxes[i].isSelected()) count++;
            }
            JOptionPane.showMessageDialog(this, "✅ Attendance marked (" + count + " Present, " + (students.size() - count) + " Absent)");
        }
    }

    private void showAttendance() {
        try (java.sql.Connection con = DBConnection.getConnection()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "⚠️ Database connection failed! Please check DBConnection.java settings.");
                return;
            }

            String query = """
                SELECT s.name, s.roll_no, a.date, a.status
                FROM attendance a
                JOIN students s ON a.student_id = s.id
                ORDER BY a.date ASC, s.name;
            """;

            java.sql.Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery(query);

            // --- 1) Gather rows into an ordered structure: YearMonth -> LocalDate -> List<row>
            // Use LinkedHashMap to preserve insertion (date) order
            java.util.LinkedHashMap<java.time.YearMonth, java.util.LinkedHashMap<java.time.LocalDate, java.util.List<String[]>>> monthsMap = new java.util.LinkedHashMap<>();

            while (rs.next()) {
                String dateStr = rs.getString("date");
                if (dateStr == null || dateStr.trim().isEmpty()) continue;
                java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr.trim());

                java.time.YearMonth ym = java.time.YearMonth.from(localDate);
                monthsMap.computeIfAbsent(ym, k -> new java.util.LinkedHashMap<>())
                         .computeIfAbsent(localDate, d -> new java.util.ArrayList<>())
                         .add(new String[]{ rs.getString("name"), rs.getString("roll_no"), rs.getString("status") });
            }

            // --- 2) Render UI from monthsMap
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

            for (java.time.YearMonth ym : monthsMap.keySet()) {
                // Month header
                String monthLabelText = ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                        + " " + ym.getYear();
                JLabel monthLabel = new JLabel("📅 " + monthLabelText);
                monthLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 17));
                monthLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 5, 5));
                mainPanel.add(Box.createVerticalStrut(10));
                mainPanel.add(monthLabel);
                mainPanel.add(Box.createVerticalStrut(5));

                // monthPanel that will contain rows of two date-sections
                JPanel monthPanel = new JPanel();
                monthPanel.setLayout(new BoxLayout(monthPanel, BoxLayout.Y_AXIS));

                JPanel rowPanel = new JPanel(new java.awt.GridLayout(0, 2, 20, 20));
                int colCount = 0;

                java.util.LinkedHashMap<java.time.LocalDate, java.util.List<String[]>> datesMap = monthsMap.get(ym);
                for (java.time.LocalDate localDate : datesMap.keySet()) {
                    // Build a dateSection JPanel for this date
                    JPanel dateSection = new JPanel();
                    dateSection.setLayout(new BoxLayout(dateSection, BoxLayout.Y_AXIS));

                    JLabel dateLabel = new JLabel(dateFormatter.format(localDate));
                    dateLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
                    dateLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
                    dateSection.add(dateLabel);
                    dateSection.add(Box.createRigidArea(new java.awt.Dimension(0, 5)));

                    String[] cols = {"Student Name", "Roll No", "Status"};
                    javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0);
                    javax.swing.JTable table = new javax.swing.JTable(model);
                    table.setEnabled(false);
                    table.setRowHeight(25);

                    // Add rows for this date
                    java.util.List<String[]> rows = datesMap.get(localDate);
                    for (String[] row : rows) {
                        model.addRow(new Object[]{ row[0], row[1], row[2] });
                    }

                    // Status color renderer
                    table.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                        @Override
                        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                                                                                boolean isSelected, boolean hasFocus, int row, int column) {
                            java.awt.Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            String status = value == null ? "" : value.toString();
                            if (status.equalsIgnoreCase("Present")) cell.setForeground(new java.awt.Color(0, 153, 0));
                            else cell.setForeground(java.awt.Color.RED);
                            setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                            return cell;
                        }
                    });

                    javax.swing.table.JTableHeader header = table.getTableHeader();
                    header.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 13));
                    header.setBackground(new java.awt.Color(220, 220, 220));

                    javax.swing.JScrollPane sp = new javax.swing.JScrollPane(table);
                    sp.setPreferredSize(new java.awt.Dimension(300, 180));
                    dateSection.add(sp);

                    // add dateSection to current rowPanel
                    rowPanel.add(dateSection);
                    colCount++;

                    // if two columns filled → add rowPanel to monthPanel and start new rowPanel
                    if (colCount == 2) {
                        monthPanel.add(rowPanel);
                        rowPanel = new JPanel(new java.awt.GridLayout(0, 2, 20, 20));
                        colCount = 0;
                    }
                }

                // add remaining rowPanel if it has components
                if (rowPanel.getComponentCount() > 0) monthPanel.add(rowPanel);

                mainPanel.add(monthPanel);
            }

            // final scroll pane and show
            JScrollPane scrollPane = new JScrollPane(mainPanel,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(new java.awt.Dimension(750, 450));
            JOptionPane.showMessageDialog(this, scrollPane, "🗓️ Attendance Records (Month View)", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "⚠️ Error loading attendance: " + e.getMessage());
        }
    }


    private void showAttendanceReport() {
        try (java.sql.Connection con = DBConnection.getConnection()) {
            String query = """
                SELECT s.name, s.roll_no,
                       SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present_count,
                       SUM(CASE WHEN a.status = 'Absent' THEN 1 ELSE 0 END) AS absent_count,
                       ROUND((SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) * 100.0) / COUNT(*), 2) AS percentage
                FROM students s
                LEFT JOIN attendance a ON s.id = a.student_id
                GROUP BY s.id, s.name, s.roll_no;
            """;

            java.sql.Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery(query);

            // Table columns
            String[] columns = {"Student Name", "Roll No", "Present", "Absent", "Attendance %"};
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0);

            while (rs.next()) {
                String name = rs.getString("name");
                String roll = rs.getString("roll_no");
                int present = rs.getInt("present_count");
                int absent = rs.getInt("absent_count");
                double percent = rs.getDouble("percentage");
                model.addRow(new Object[]{name, roll, present, absent, percent});
            }

            javax.swing.JTable table = new javax.swing.JTable(model);
            table.setEnabled(false);
            table.setRowHeight(25);

            // 🎨 Custom cell color renderer for attendance %
            table.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {

                    java.awt.Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    double percent = (double) value;

                    if (percent >= 90) {
                        cell.setForeground(new java.awt.Color(0, 153, 0)); // Green
                    } else if (percent >= 75) {
                        cell.setForeground(new java.awt.Color(255, 140, 0)); // Orange
                    } else {
                        cell.setForeground(java.awt.Color.RED); // Red
                    }
                    setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    return cell;
                }
            });

            // 🎨 Style header
            javax.swing.table.JTableHeader header = table.getTableHeader();
            header.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 13));
            header.setBackground(new java.awt.Color(220, 220, 220));

            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(table);
            scrollPane.setPreferredSize(new java.awt.Dimension(550, 250));

            JOptionPane.showMessageDialog(this, scrollPane, "📊 Attendance Report", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading report: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}

