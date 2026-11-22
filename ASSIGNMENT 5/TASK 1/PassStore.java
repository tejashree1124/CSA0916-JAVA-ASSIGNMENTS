import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PassStore {

    private static final String FILE_NAME = "bus_passes.txt";

    // Bus pass record
    static class Pass {
        String passId;
        String name;
        String route;
        double amount;

        Pass(String passId, String name, String route, double amount) {
            this.passId = passId;
            this.name = name;
            this.route = route;
            this.amount = amount;
        }

        public String toString() {
            return "Pass ID: " + passId +
                   ", Name: " + name +
                   ", Route: " + route +
                   ", Amount: Rs." + amount;
        }
    }

    // ADD a new bus pass
    public static void add(Pass pass) throws IOException {

        String line = pass.passId + "|" +
                      pass.name + "|" +
                      pass.route + "|" +
                      pass.amount;

        Files.write(
            Path.of(FILE_NAME),
            (line + System.lineSeparator()).getBytes(),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );

        System.out.println("Bus pass added successfully.");
    }

    // FIND a bus pass by ID
    public static Pass find(String searchId) throws IOException {

        Path path = Path.of(FILE_NAME);

        if (!Files.exists(path)) {
            System.out.println("File not found.");
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;

            while ((line = reader.readLine()) != null) {

                try {
                    String[] fields = line.split("\\|", -1);

                    if (fields.length != 4) {
                        continue;
                    }

                    double amount = Double.parseDouble(fields[3]);

                    Pass pass = new Pass(
                        fields[0], fields[1],
                        fields[2], amount
                    );

                    if (pass.passId.equals(searchId)) {
                        return pass;
                    }

                } catch (NumberFormatException e) {
                    // Skip damaged records
                    continue;
                }
            }
        }

        return null;
    }

    // LIST all valid bus passes
    public static void list() throws IOException {

        Path path = Path.of(FILE_NAME);

        if (!Files.exists(path)) {
            System.out.println("File not found.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;

            System.out.println("\n--- BUS PASS RECORDS ---");

            while ((line = reader.readLine()) != null) {

                try {
                    String[] fields = line.split("\\|", -1);

                    if (fields.length != 4) {
                        continue;
                    }

                    double amount = Double.parseDouble(fields[3]);

                    Pass pass = new Pass(
                        fields[0], fields[1],
                        fields[2], amount
                    );

                    System.out.println(pass);

                } catch (NumberFormatException e) {
                    // Ignore damaged records
                    continue;
                }
            }
        }
    }

    // Main method
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try {
            while (true) {

                System.out.println("\n===== BUS PASS MANAGEMENT =====");
                System.out.println("1. Add Bus Pass");
                System.out.println("2. Find Bus Pass");
                System.out.println("3. List Bus Passes");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");

                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {

                    case 1:
                        System.out.print("Enter Pass ID: ");
                        String id = sc.nextLine();

                        System.out.print("Enter Name: ");
                        String name = sc.nextLine();

                        System.out.print("Enter Route: ");
                        String route = sc.nextLine();

                        System.out.print("Enter Amount: ");
                        double amount = Double.parseDouble(sc.nextLine());

                        add(new Pass(id, name, route, amount));
                        break;

                    case 2:
                        System.out.print("Enter Pass ID to search: ");
                        String searchId = sc.nextLine();

                        Pass result = find(searchId);

                        if (result != null) {
                            System.out.println(result);
                        } else {
                            System.out.println("Pass not found.");
                        }
                        break;

                    case 3:
                        list();
                        break;

                    case 4:
                        System.out.println("Program closed.");
                        sc.close();
                        return;

                    default:
                        System.out.println("Invalid choice.");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}