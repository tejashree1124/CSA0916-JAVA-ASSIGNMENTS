import java.sql.*;
import java.nio.file.*;
import java.util.*;

public class PassMigration {

    static final String URL =
        "jdbc:mysql://127.0.0.1:3306/csa09_b2_testdb";

    static final String USER = "javauser";
    static final String PASSWORD = "YOUR_MYSQL_PASSWORD";

    public static void main(String[] args) {

        String fileName = "bus_passes.txt";

        int successCount = 0;
        int errorCount = 0;

        try {
            // Read records from text file
            List<String> lines =
                Files.readAllLines(Paths.get(fileName));

            try (Connection con =
                 DriverManager.getConnection(URL, USER, PASSWORD)) {

                // Check database table using metadata
                DatabaseMetaData meta = con.getMetaData();

                try (ResultSet tables =
                     meta.getTables(null, null, "bus_pass",
                                    new String[]{"TABLE"})) {

                    if (!tables.next()) {
                        System.out.println("bus_pass table not found.");
                        return;
                    }
                }

                System.out.println("Database table verified.");
                System.out.println("Database: " +
                    meta.getDatabaseProductName());

                con.setAutoCommit(false);

                String sql =
                    "INSERT INTO bus_pass VALUES (?, ?, ?, ?)";

                try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

                    // Clear previous test records
                    try (Statement st = con.createStatement()) {
                        st.executeUpdate("DELETE FROM bus_pass");
                    }

                    Set<Integer> ids = new HashSet<>();

                    for (String line : lines) {

                        try {
                            String[] data = line.split(",", -1);

                            if (data.length != 4) {
                                throw new IllegalArgumentException(
                                    "Incorrect number of fields");
                            }

                            int id = Integer.parseInt(data[0].trim());
                            String name = data[1].trim();
                            String route = data[2].trim();
                            double amount =
                                Double.parseDouble(data[3].trim());

                            if (id <= 0 || name.isEmpty()
                                    || route.isEmpty() || amount <= 0) {
                                throw new IllegalArgumentException(
                                    "Invalid field value");
                            }

                            if (!ids.add(id)) {
                                throw new IllegalArgumentException(
                                    "Duplicate pass ID");
                            }

                            ps.setInt(1, id);
                            ps.setString(2, name);
                            ps.setString(3, route);
                            ps.setDouble(4, amount);

                            ps.addBatch();
                            successCount++;

                        } catch (Exception e) {
                            errorCount++;
                            System.out.println(
                                "Invalid record: " + line +
                                " | Reason: " + e.getMessage());
                        }
                    }

                    // Insert valid records in batches
                    ps.executeBatch();

                    con.commit();

                    System.out.println("\nMigration completed!");
                    System.out.println(
                        "Valid records: " + successCount);
                    System.out.println(
                        "Invalid records: " + errorCount);
                }

            } catch (Exception e) {
                System.out.println("Migration failed: " +
                    e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("File error: " + e.getMessage());
        }
    }
}