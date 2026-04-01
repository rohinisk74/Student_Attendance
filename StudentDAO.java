package student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public void addStudent(String name, String rollNo, String dept) {
        try (Connection con = DBConnection.getConnection()) {
            String query = "INSERT INTO students (name, roll_no, department) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);
            ps.setString(2, rollNo);
            ps.setString(3, dept);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllStudents() {
        List<String> students = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM students");
            while (rs.next()) {
                students.add(rs.getInt("id") + " - " + rs.getString("name") + " (" + rs.getString("roll_no") + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return students;
    }
}
