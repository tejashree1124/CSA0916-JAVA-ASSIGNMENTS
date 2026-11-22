import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

class StudentResult {
    String fileName;
    double average;
    boolean successful;
    String message;

    StudentResult(String fileName, double average,
                  boolean successful, String message) {
        this.fileName = fileName;
        this.average = average;
        this.successful = successful;
        this.message = message;
    }
}

public class ResultProcessor {

    static final String DATA_FOLDER = "data";
    static final int TOTAL_FILES = 10000;

    // Generate sample student score files
    static void generateFiles() throws IOException {

        Files.createDirectories(Paths.get(DATA_FOLDER));

        for (int i = 1; i <= TOTAL_FILES; i++) {

            Path file = Paths.get(
                DATA_FOLDER, "student" + i + ".txt"
            );

            // Simulate missing files
            if (i % 200 == 0) {
                Files.deleteIfExists(file);
                continue;
            }

            // Simulate corrupted files
            if (i % 199 == 0) {
                Files.writeString(file, "80\nINVALID\n70\n");
                continue;
            }

            int mark1 = 50 + (i % 51);
            int mark2 = 55 + (i % 46);
            int mark3 = 60 + (i % 41);

            String content = mark1 + "\n" +
                             mark2 + "\n" +
                             mark3 + "\n";

            Files.writeString(file, content);
        }

        System.out.println("Student files generated.");
    }

    // Process one student file
    static Callable<StudentResult> createTask(Path file) {

        return () -> {

            String fileName = file.getFileName().toString();

            try {

                List<String> lines = Files.readAllLines(file);

                if (lines.size() != 3) {
                    throw new IllegalArgumentException(
                        "Invalid number of marks"
                    );
                }

                int total = 0;

                for (String line : lines) {

                    int mark = Integer.parseInt(line.trim());

                    if (mark < 0 || mark > 100) {
                        throw new IllegalArgumentException(
                            "Invalid mark"
                        );
                    }

                    total += mark;
                }

                double average = total / 3.0;

                return new StudentResult(
                    fileName, average, true, "Processed"
                );

            } catch (Exception e) {

                return new StudentResult(
                    fileName, 0, false,
                    e.getMessage()
                );
            }
        };
    }

    // Process files using a fixed thread pool
    static void processFiles(int threadCount)
            throws Exception {

        ExecutorService executor =
            Executors.newFixedThreadPool(threadCount);

        ConcurrentHashMap<String, StudentResult> results =
            new ConcurrentHashMap<>();

        List<Future<StudentResult>> futures =
            new ArrayList<>();

        long startTime = System.nanoTime();

        try {

            for (int i = 1; i <= TOTAL_FILES; i++) {

                Path file = Paths.get(
                    DATA_FOLDER,
                    "student" + i + ".txt"
                );

                futures.add(
                    executor.submit(createTask(file))
                );
            }

            // Collect every task result
            for (Future<StudentResult> future : futures) {

                StudentResult result = future.get();

                results.put(result.fileName, result);
            }

        } finally {

            executor.shutdown();
            executor.awaitTermination(
                1, TimeUnit.HOURS
            );
        }

        long endTime = System.nanoTime();

        int success = 0;
        int failure = 0;

        for (StudentResult result : results.values()) {

            if (result.successful) {
                success++;
            } else {
                failure++;
            }
        }

        double timeMs =
            (endTime - startTime) / 1_000_000.0;

        System.out.println("\nThread Pool Size: " + threadCount);
        System.out.println("Total Files: " + TOTAL_FILES);
        System.out.println("Successful Files: " + success);
        System.out.println("Failed Files: " + failure);

        System.out.printf(
            "Execution Time: %.2f ms%n", timeMs
        );

        System.out.println(
            "Results Stored: " + results.size()
        );

        System.out.println("--------------------------------");
    }

    public static void main(String[] args)
            throws Exception {

        generateFiles();

        int[] threadCounts = {1, 2, 4, 8};

        for (int count : threadCounts) {
            processFiles(count);
        }

        System.out.println(
            "All Student Results Processed Successfully."
        );
    }
}