package de.epiceric.shopchest.debug;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

public class FileDebugLogger extends LogDebugLogger {

    private final FileWriter fw;
    private final SimpleDateFormat dateFormat;
    private final long offset;

    public FileDebugLogger(FileWriter fw, Logger logger) {
        super(logger);
        this.fw = fw;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        final long offset = System.currentTimeMillis() - Calendar.getInstance().getTimeInMillis(); // Get a long offset
        this.offset = offset - offset % 100; // Remove the difference made by the Calendar creation time
    }

    @Override
    public void debug(String message) {
        try {
            final String timestamp = dateFormat.format(new Date(System.currentTimeMillis() - offset));
            fw.write(String.format("[%s] %s\r\n", timestamp, message));
            fw.flush();
        } catch (IOException e) {
            logger.severe("Failed to print debug message.");
            e.printStackTrace();
        }
    }

    @Override
    public void debug(Throwable throwable) {
        final PrintWriter pw = new PrintWriter(fw);
        throwable.printStackTrace(pw);
        pw.flush();
    }

    @Override
    public void close() {
        try {
            fw.close();
        } catch (IOException e) {
            logger.severe("Failed to close FileWriter");
            e.printStackTrace();
        }
    }

}
