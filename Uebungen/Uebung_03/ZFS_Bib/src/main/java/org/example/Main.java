package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class Main {
    private static final String BASE_DIR = "src/test/TestDocument"; // Passe den Pfad an
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            logger.info("Starte Test-Transaktion...");

            // 1️⃣ Transaktion starten
            String snapshotName = TransactionManager.startTransaction(BASE_DIR);

            // 2️⃣ Transaktionsobjekt erstellen
            FileTransaction transaction = new FileTransaction(snapshotName, BASE_DIR);

            // 3️⃣ Datei schreiben & löschen
            transaction.writeFile("testfile.txt", "Hallo, das ist eine Transaktion!");
            transaction.deleteFile("oldfile.txt");

            // 4️⃣ Konflikt prüfen
            if (ConflictDetector.hasConflicts(transaction, BASE_DIR)) {
                logger.warning("Konflikt erkannt! Rollback wird durchgeführt.");
                TransactionManager.rollbackTransaction(snapshotName);
            } else {
                logger.info("Kein Konflikt – Änderungen werden übernommen.");
                TransactionManager.commitTransaction(snapshotName);
            }
        } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
