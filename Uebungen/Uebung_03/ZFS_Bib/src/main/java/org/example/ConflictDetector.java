package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class ConflictDetector {
    public static boolean hasConflicts(FileTransaction transaction, String baseDir) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        for (Map.Entry<Path, byte[]> entry : transaction.getChanges().entrySet()) {
            Path originalPath = Paths.get(baseDir).resolve(entry.getKey().getFileName());
            if (Files.exists(originalPath)) {
                byte[] existingHash = md.digest(Files.readAllBytes(originalPath));
                byte[] newHash = md.digest(entry.getValue());
                if (!MessageDigest.isEqual(existingHash, newHash)) {
                    return true;
                }
            }
        }
        return false;
    }
}
