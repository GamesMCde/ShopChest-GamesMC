package de.epiceric.shopchest.debug;

import java.util.logging.Logger;

public class NullDebugLogger extends LogDebugLogger {

    public NullDebugLogger(Logger logger) {
        super(logger);
    }

    @Override
    public void debug(String message) {
    }

    @Override
    public void debug(Throwable throwable) {
    }

    @Override
    public void close() {
    }
}
