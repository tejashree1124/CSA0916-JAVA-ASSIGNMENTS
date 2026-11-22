import java.sql.*;

public class RollbackTest {

    static final String URL =
        "jdbc:mysql://127.0.0.1:3306/csa09_b2_testdb";

    static final String USER = "javauser";
    static final String PASSWORD = "YOUR_MYSQL_PASSWORD";

    public static void main(String[] args) {

        try (Connection con =
             DriverManager.getConnection(URL, USER, PASSWORD)) {

            Statement st = con.createStatement();
            st.executeUpdate("DELETE FROM bus_pass");
            con.setAutoCommit(false);

            String sql =
                "INSERT INTO bus_pass VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            try {
                for (int i = 1; i <= 10; i++) {

                    ps.setInt(1, i == 6 ? 5 : i);
                    ps.setString(2, "Passenger" + i);
                    ps.setString(3, "Route A");
                    ps.setDouble(4, 50.0);

                    ps.executeUpdate();
                }

                con.commit();
                System.out.println("Transaction committed.");

            } catch (SQLException e) {

                con.rollback();
                System.out.println("Error occurred: " + e.getMessage());
                System.out.println("Transaction rolled back.");
            }

            ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM bus_pass");

            if (rs.next()) {
                System.out.println(
                    "Records remaining after rollback: " +
                    rs.getInt(1));
            }

            con.setAutoCommit(true);
            ps.close();
            rs.close();
            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}