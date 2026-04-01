package student;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/attendance_db?";
    private static final String USER = "root";   
    private static final String PASSWORD = "pass@123"; 

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //class.forname to load mysql
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            //connection-active connection btwn java and database,
            //con is literally that path your Java code uses to communicate with the database.
            //drivermanager- make connection to mysql and java using url,user,pass
            System.out.println("✅ Database connected successfully!");
            return con;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("⚠️ Database connection failed: " + e.getMessage());
            return null;
        }
    }
}
