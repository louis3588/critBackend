package com.lp.criticabackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {

    private final Logger logger;

    private AppLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static AppLogger getLogger(Class<?> clazz) {
        return new AppLogger(clazz);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    // Convenience formatting methods
    public void infof(String format, Object... args) {
        logger.info(String.format(format, args));
    }

    public void debugf(String format, Object... args) {
        logger.debug(String.format(format, args));
    }

    public void warnf(String format, Object... args) {
        logger.warn(String.format(format, args));
    }

    public void errorf(String format, Object... args) {
        logger.error(String.format(format, args));
    }
}

