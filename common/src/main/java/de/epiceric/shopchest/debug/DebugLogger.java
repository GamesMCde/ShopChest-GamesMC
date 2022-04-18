package de.epiceric.shopchest.debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

public interface DebugLogger {

    static DebugLogger getLogger(File debugLogFile, Logger logger) {
        try {
            if (!debugLogFile.exists()) {
                debugLogFile.createNewFile();
            }

            new PrintWriter(debugLogFile).close();

            final FileWriter fw = new FileWriter(debugLogFile, true);

            return new FileDebugLogger(fw, logger);
        } catch (IOException e) {
            logger.info("Failed to instantiate FileWriter");
            e.printStackTrace();
        }
        return new NullDebugLogger(logger);
    }

    /**
     * Print a message to the <i>/plugins/ShopChest/debug.txt</i> file
     *
     * @param message Message to print
     */
    void debug(String message);

    /**
     * Print a {@link Throwable}'s stacktrace to the <i>/plugins/ShopChest/debug.txt</i> file
     *
     * @param throwable {@link Throwable} whose stacktrace will be printed
     */
    void debug(Throwable throwable);

    void close();

    Logger getLogger();

}
