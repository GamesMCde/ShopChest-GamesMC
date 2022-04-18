package de.epiceric.shopchest.debug;

import java.util.logging.Logger;

public abstract class LogDebugLogger implements DebugLogger {

    protected final Logger logger;

    protected LogDebugLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
