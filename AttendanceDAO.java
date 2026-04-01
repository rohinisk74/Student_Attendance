package student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

	public void markAttendance(int studentId, String date, String status) {
	    try (Connection con = DBConnection.getConnection()) {
	        // Check if already marked for the date
	        String check = "SELECT * FROM attendance WHERE student_id = ? AND date = ?";
	        PreparedStatement checkStmt = con.prepareStatement(check);
	        checkStmt.setInt(1, studentId);
	        checkStmt.setString(2, date);
	        ResultSet rs = checkStmt.executeQuery();

	        if (rs.next()) return; // Skip if already marked

	        String query = "INSERT INTO attendance (student_id, date, status) VALUES (?, ?, ?)";
	        PreparedStatement ps = con.prepareStatement(query);
	        ps.setInt(1, studentId);
	        ps.setString(2, date);
	        ps.setString(3, status);
	        ps.executeUpdate();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
    public List<String> getAllAttendance() {
        List<String> records = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT s.name, s.roll_no, a.date, a.status FROM attendance a JOIN students s ON a.student_id = s.id";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                records.add(rs.getString("name") + " (" + rs.getString("roll_no") + ") - " +
                            rs.getDate("date") + " - " + rs.getString("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }
}
