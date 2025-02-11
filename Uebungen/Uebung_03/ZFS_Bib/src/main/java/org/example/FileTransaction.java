package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FileTransaction {
    private final String transactionId;
    private final Path transactionDir;
    private final Map<Path, byte[]> changes = new HashMap<>();
    private static final Logger logger = Logger.getLogger(FileTransaction.class.getName());

    public FileTransaction(String transactionId, String baseDir) throws IOException {
        this.transactionId = transactionId;
        this.transactionDir = Paths.get(baseDir, transactionId);
        Files.createDirectories(transactionDir);
        logger.info("Transaktion gestartet: " + transactionId);
    }

    public void writeFile(String fileName, String content) throws IOException {
        Path filePath = transactionDir.resolve(fileName);
        Files.writeString(filePath, content, StandardOpenOption.CREATE);
        changes.put(filePath, content.getBytes(StandardCharsets.UTF_8));
        logger.info("Datei geschrieben: " + filePath);
    }

    public void deleteFile(String fileName) throws IOException {
        Path filePath = transactionDir.resolve(fileName);
        Files.deleteIfExists(filePath);
        changes.put(filePath, null);
        logger.info("Datei gelöscht: " + filePath);
    }

    public Map<Path, byte[]> getChanges() {
        return changes;
    }
}
