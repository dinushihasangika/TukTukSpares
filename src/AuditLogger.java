import java.io.*;
        import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogger {
    private static final String LOG_FILE = "data/audit_log.txt";

    public static void log(String action, String itemCode, int quantity) {
        try {
            FileWriter fw = new FileWriter(LOG_FILE, true);
            BufferedWriter bw = new BufferedWriter(fw);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String logEntry = timestamp + " | " + action
                    + " | " + itemCode
                    + " | qty: " + quantity;

            bw.write(logEntry);
            bw.newLine();
            bw.close();

        } catch (IOException e) {
            System.out.println("Error writing to audit log: " + e.getMessage());
        }
    }

    public static void log(String action, String itemCode) {
        log(action, itemCode, 0);
    }
}