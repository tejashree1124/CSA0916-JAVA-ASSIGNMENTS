import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class JDBCPerformanceTest {

    static final String URL =
        "jdbc:mysql://127.0.0.1:3306/csa09_b2_testdb";

    static final String USER = "javauser";
    static final String PASSWORD = "YOUR_MYSQL_PASSWORD";

    static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static void clearTable(Connection con) throws Exception {
        Statement st = con.createStatement();
        st.executeUpdate("DELETE FROM bus_pass");
        st.close();
    }

    // 1. Insert using Statement
    static void testStatement(int records) throws Exception {

        Connection con = getConnection();
        clearTable(con);
        con.setAutoCommit(false);

        long start = System.nanoTime();

        Statement st = con.createStatement();

        for (int i = 1; i <= records; i++) {
            String sql = "INSERT INTO bus_pass VALUES (" +
                i + ", 'Passenger" + i + "', 'Route A', 50.0)";

            st.executeUpdate(sql);
        }

        con.commit();

        long end = System.nanoTime();

        System.out.printf("Statement: %.2f ms%n",
                (end - start) / 1_000_000.0);

        st.close();
        con.close();
    }

    // 2. Insert using PreparedStatement
    static void testPreparedStatement(int records,
                                      boolean autoCommit) throws Exception {

        Connection con = getConnection();
        clearTable(con);
        con.setAutoCommit(autoCommit);

        long start = System.nanoTime();

        String sql = "INSERT INTO bus_pass VALUES (?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);

        for (int i = 1; i <= records; i++) {
            ps.setInt(1, i);
            ps.setString(2, "Passenger" + i);
            ps.setString(3, "Route A");
            ps.setDouble(4, 50.0);

            ps.executeUpdate();
        }

        if (!autoCommit) {
            con.commit();
        }

        long end = System.nanoTime();

        System.out.printf("PreparedStatement (%s): %.2f ms%n",
                autoCommit ? "Auto-commit" : "Transaction",
                (end - start) / 1_000_000.0);

        ps.close();
        con.close();
    }

    // 3. Insert using PreparedStatement Batch
    static void testBatch(int records) throws Exception {

        Connection con = getConnection();
        clearTable(con);
        con.setAutoCommit(false);

        long start = System.nanoTime();

        String sql = "INSERT INTO bus_pass VALUES (?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);

        for (int i = 1; i <= records; i++) {
            ps.setInt(1, i);
            ps.setString(2, "Passenger" + i);
            ps.setString(3, "Route A");
            ps.setDouble(4, 50.0);

            ps.addBatch();

            if (i % 1000 == 0) {
                ps.executeBatch();
            }
        }

        ps.executeBatch();
        con.commit();

        long end = System.nanoTime();

        System.out.printf("PreparedStatement Batch: %.2f ms%n",
                (end - start) / 1_000_000.0);

        ps.close();
        con.close();
    }

    public static void main(String[] args) throws Exception {

        int[] sizes = {1000, 10000, 100000};

        for (int records : sizes) {

            System.out.println("\n================================");
            System.out.println("Records: " + records);
            System.out.println("================================");

            testStatement(records);

            testPreparedStatement(records, true);

            testPreparedStatement(records, false);

            testBatch(records);
        }
    }
}