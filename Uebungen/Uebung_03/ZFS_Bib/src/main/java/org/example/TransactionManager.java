package org.example;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class TransactionManager {
    private static final String ZFS_POOL = "tank"; // Name des ZFS-Pools
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Logger logger = Logger.getLogger(TransactionManager.class.getName());

    public static String startTransaction(String baseDir) throws IOException, InterruptedException {
        lock.lock();
        try {
            String snapshotName = "snapshot_" + System.currentTimeMillis();
            executeCommand("zfs snapshot " + ZFS_POOL + "@" + snapshotName);
            logger.info("Snapshot erstellt: " + snapshotName);
            return snapshotName;
        } finally {
            lock.unlock();
        }
    }

    public static void rollbackTransaction(String snapshotName) throws IOException, InterruptedException {
        lock.lock();
        try {
            executeCommand("zfs rollback " + ZFS_POOL + "@" + snapshotName);
            logger.warning("Rollback durchgeführt für Snapshot: " + snapshotName);
        } finally {
            lock.unlock();
        }
    }

    public static void commitTransaction(String snapshotName) throws IOException, InterruptedException {
        lock.lock();
        try {
            executeCommand("zfs destroy " + ZFS_POOL + "@" + snapshotName);
            logger.info("Snapshot gelöscht: " + snapshotName);
        } finally {
            lock.unlock();
        }
    }

    private static void executeCommand(String command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("bash", "-c", command).start();
        process.waitFor();
    }
}
