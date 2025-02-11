import org.example.ConflictDetector;
import org.example.FileTransaction;
import org.example.TransactionManager;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionTests {
    private static final String BASE_DIR = "src/test/TestDocument";

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(BASE_DIR));
    }

    @Test
    @Order(1)
    void testSuccessfulTransactionCommit() throws IOException, InterruptedException, NoSuchAlgorithmException {
        String snapshot = TransactionManager.startTransaction(BASE_DIR);
        FileTransaction transaction = new FileTransaction(snapshot, BASE_DIR);

        transaction.writeFile("testfile1.txt", "Testinhalt ohne Konflikt");
        assertFalse(ConflictDetector.hasConflicts(transaction, BASE_DIR));

        TransactionManager.commitTransaction(snapshot);
        Path filePath = Paths.get(BASE_DIR, "testfile1.txt");
        assertTrue(Files.exists(filePath));

        // Überprüfen, ob der Inhalt korrekt ist
        String content = Files.readString(filePath);
        assertEquals("Testinhalt ohne Konflikt", content);
    }

    @Test
    @Order(2)
    void testTransactionRollback() throws IOException, InterruptedException {
        String snapshot = TransactionManager.startTransaction(BASE_DIR);
        FileTransaction transaction = new FileTransaction(snapshot, BASE_DIR);

        transaction.writeFile("testfile2.txt", "Wird zurückgesetzt.");
        TransactionManager.rollbackTransaction(snapshot);

        Path filePath = Paths.get(BASE_DIR, "testfile2.txt");
        assertFalse(Files.exists(filePath)); // Datei darf nicht existieren nach Rollback
    }

    @Test
    @Order(3)
    void testConflictDetection() throws IOException, NoSuchAlgorithmException, InterruptedException {
        Path testFile = Paths.get(BASE_DIR, "testfile3.txt");
        Files.write(testFile, "Alter Inhalt".getBytes());

        String snapshot = TransactionManager.startTransaction(BASE_DIR);
        FileTransaction transaction = new FileTransaction(snapshot, BASE_DIR);

        // Simuliert eine parallele Änderung außerhalb der Transaktion
        Files.write(testFile, "Externe Änderung".getBytes());

        transaction.writeFile("testfile3.txt", "Neuer Inhalt");

        assertTrue(ConflictDetector.hasConflicts(transaction, BASE_DIR));

        try {
            TransactionManager.rollbackTransaction(snapshot);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(4)
    void testConcurrentTransactions() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Boolean> transaction1 = executor.submit(() -> {
            try {
                String snapshot = TransactionManager.startTransaction(BASE_DIR);
                FileTransaction transaction = new FileTransaction(snapshot, BASE_DIR);
                transaction.writeFile("sharedfile.txt", "Änderung durch Transaktion 1");
                if (!ConflictDetector.hasConflicts(transaction, BASE_DIR)) {
                    TransactionManager.commitTransaction(snapshot);
                    return true;
                } else {
                    TransactionManager.rollbackTransaction(snapshot);
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        Future<Boolean> transaction2 = executor.submit(() -> {
            try {
                String snapshot = TransactionManager.startTransaction(BASE_DIR);
                FileTransaction transaction = new FileTransaction(snapshot, BASE_DIR);
                transaction.writeFile("sharedfile.txt", "Änderung durch Transaktion 2");
                if (!ConflictDetector.hasConflicts(transaction, BASE_DIR)) {
                    TransactionManager.commitTransaction(snapshot);
                    return true;
                } else {
                    TransactionManager.rollbackTransaction(snapshot);
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        boolean result1 = transaction1.get();
        boolean result2 = transaction2.get();

        // Mindestens eine Transaktion sollte erfolgreich sein, aber nicht beide
        assertTrue(result1 ^ result2);

        executor.shutdown();
    }

    @Test
    @Order(5)
    void testTransactionAbortsOnConflict() throws IOException, NoSuchAlgorithmException, InterruptedException {
        Path testFile = Paths.get(BASE_DIR, "conflictfile.txt");
        Files.write(testFile, "Original Inhalt".getBytes());

        String snapshot1 = TransactionManager.startTransaction(BASE_DIR);
        FileTransaction transaction1 = new FileTransaction(snapshot1, BASE_DIR);
        transaction1.writeFile("conflictfile.txt", "Änderung durch Transaktion 1");

        String snapshot2 = TransactionManager.startTransaction(BASE_DIR);
        FileTransaction transaction2 = new FileTransaction(snapshot2, BASE_DIR);
        transaction2.writeFile("conflictfile.txt", "Änderung durch Transaktion 2");

        // Transaktion 1 wird committed
        TransactionManager.commitTransaction(snapshot1);

        // Transaktion 2 sollte jetzt einen Konflikt haben
        assertTrue(ConflictDetector.hasConflicts(transaction2, BASE_DIR));

        // Transaktion 2 muss abgebrochen werden
        TransactionManager.rollbackTransaction(snapshot2);

        // Sicherstellen, dass die Datei nur die Änderung von Transaktion 1 enthält
        String content = Files.readString(testFile);
        assertEquals("Änderung durch Transaktion 1", content);
    }
}
