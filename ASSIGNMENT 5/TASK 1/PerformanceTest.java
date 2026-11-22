import java.io.*;
import java.nio.file.*;

public class PerformanceTest {

    public static void main(String[] args) throws IOException {

        int[] sizes = {1000, 10000, 100000};

        for (int size : sizes) {

            String fileName = "test_" + size + ".txt";

            // Generate test records
            try (BufferedWriter writer =
                    Files.newBufferedWriter(Path.of(fileName))) {

                for (int i = 1; i <= size; i++) {
                    writer.write("P" + i + "|Student" + i
                            + "|Chennai|1500");
                    writer.newLine();
                }
            }

            // Measure search time
            long start = System.nanoTime();

            try (BufferedReader reader =
                    Files.newBufferedReader(Path.of(fileName))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split("\\|");

                    if (fields[0].equals("P" + size)) {
                        break;
                    }
                }
            }

            long end = System.nanoTime();

            double timeMs = (end - start) / 1_000_000.0;

            System.out.println("Records: " + size);
            System.out.println("Search time: " + timeMs + " ms");
            System.out.println("-------------------------");
        }
    }
}