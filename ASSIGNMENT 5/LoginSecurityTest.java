import java.sql.*;

public class LoginSecurityTest {

    static final String URL =
    "jdbc:mysql://127.0.0.1:3306/csa09_b2_testdb";
    static final String USER = "javauser";
static final String PASSWORD = "YOUR_PASSWORD";
    // Unsafe login using Statement
    public static void unsafeLogin(String username, String password) {

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = con.createStatement()) {

            String query = "SELECT * FROM users WHERE username = '"
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
            System.out.println(e.getMessage());
        }
    }

    // Secure login using PreparedStatement
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
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {

        System.out.println("=== NORMAL LOGIN TEST ===");

        unsafeLogin("admin", "admin123");
        secureLogin("admin", "admin123");

        System.out.println("\n=== SQL INJECTION TEST ===");

        unsafeLogin("' OR '1'='1' -- ", "wrongpassword");
        secureLogin("' OR '1'='1' -- ", "wrongpassword");
    }
}