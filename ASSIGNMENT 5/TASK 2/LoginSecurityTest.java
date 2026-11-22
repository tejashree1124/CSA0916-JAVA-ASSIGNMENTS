import java.sql.*;

public class LoginSecurityTest {

    static final String URL =
    "jdbc:mysql://127.0.0.1:3306/csa09_b2_testdb";

static final String USER = "javauser";
static final String PASSWORD = "YOUR_MYSQL_PASSWORD";

    // Normal Login using PreparedStatement
    public static void normalLogin(String username, String password) {

        String query =
            "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Normal Login: SUCCESS");
            } else {
                System.out.println("Normal Login: FAILED");
            }

            rs.close();

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // Unsafe Login using Statement
    public static void unsafeLogin(String username, String password) {

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = con.createStatement()) {

            String query =
                "SELECT * FROM users WHERE username = '"
                + username + "' AND password = '" + password + "'";

            System.out.println("\nUnsafe SQL Query:");
            System.out.println(query);

            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                System.out.println("Unsafe Login: SUCCESS");
            } else {
                System.out.println("Unsafe Login: FAILED");
            }

            rs.close();

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // Secure Login using PreparedStatement
    public static void secureLogin(String username, String password) {

        String query =
            "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Secure Login: SUCCESS");
            } else {
                System.out.println("Secure Login: FAILED");
            }

            rs.close();

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            System.out.println("Database Connection Successful!");

            System.out.println("\n=== NORMAL LOGIN TEST ===");

            normalLogin("admin", "admin123");

            System.out.println("\n=== SQL INJECTION TEST ===");

            unsafeLogin("' OR '1'='1' -- ", "wrongpassword");

            System.out.println("\n=== SQL INJECTION PREVENTION TEST ===");

            secureLogin("' OR '1'='1' -- ", "wrongpassword");

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver Not Found!");
        }
    }
}