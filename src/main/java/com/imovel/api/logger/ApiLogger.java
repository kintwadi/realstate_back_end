package com.imovel.api.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe logging utility using SLF4J with MDC for context
 */
public final class ApiLogger {
    private static final String DEFAULT_LOGGER_TYPE = "com.imovel.api.logger";
    private static final String SIMPLE_FORMAT = "[{}] {}";
    private static final String COMPLETE_FORMAT = "[{}] {}: {} - {}";

    private static volatile ConcurrentHashMap<String, String> globalHeaders = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(DEFAULT_LOGGER_TYPE);

    private ApiLogger() {}

    public static void setLoggerType(String type) {
        logger = LoggerFactory.getLogger(type);
    }

    public static synchronized void setContext(String key, String value) {
        if (MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty()) {
            MDC.put(key, value);
        } else {
            ConcurrentHashMap<String, String> newHeaders = new ConcurrentHashMap<>(globalHeaders);
            newHeaders.put(key, value);
            newHeaders.forEach((k, v) -> globalHeaders.putIfAbsent(k, v));
        }
    }

    private static Map<String, String> getContext() {
        Map<String, String> globalSnapshot = globalHeaders;
        globalSnapshot.forEach(MDC::put);
        return MDC.getCopyOfContextMap();
    }

    // Debug Methods
    public static void debug(String message) {
        try {
            getContext().forEach(MDC::put);
            logger.debug(message);
        } finally {
            MDC.clear();
        }
    }

    public static void debug(String location, String message) {
        try {
            getContext().forEach(MDC::put);
            logger.debug(SIMPLE_FORMAT, location, message);
        } finally {
            MDC.clear();
        }
    }

    public static void debug(String message, Object response) {
        try {
            getContext().forEach(MDC::put);
            logger.debug(SIMPLE_FORMAT, message, response);
        } finally {
            MDC.clear();
        }
    }

    public static void debug(String location, String message, Object response) {
        try {
            getContext().forEach(MDC::put);
            logger.debug(COMPLETE_FORMAT, location, message, response);
        } finally {
            MDC.clear();
        }
    }

    // Info Methods
    public static void info(String message) {
        try {
            getContext().forEach(MDC::put);
            logger.info(message);
        } finally {
            MDC.clear();
        }
    }

    public static void info(String location, String message) {
        try {
            getContext().forEach(MDC::put);
            logger.info(SIMPLE_FORMAT, location, message);
        } finally {
            MDC.clear();
        }
    }

    public static void info(String message, Object response) {
        try {
            getContext().forEach(MDC::put);
            logger.info(SIMPLE_FORMAT, message, response);
        } finally {
            MDC.clear();
        }
    }

    public static void info(String location, String message, Object response) {
        try {
            getContext().forEach(MDC::put);
            logger.info(COMPLETE_FORMAT, location, message, response);
        } finally {
            MDC.clear();
        }
    }

    // Error Methods
    public static void error(String message) {
        try {
            getContext().forEach(MDC::put);
            logger.error(message);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String location, String message) {
        try {
            getContext().forEach(MDC::put);
            logger.error(SIMPLE_FORMAT, location, message);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String message, Object response) {
        try {
            getContext().forEach(MDC::put);
            logger.error(SIMPLE_FORMAT, message, response);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String location, String message, Object response) {
        try {
            getContext().forEach(MDC::put);
            logger.error(COMPLETE_FORMAT, location, message, response);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String message, Throwable throwable) {
        try {
            getContext().forEach(MDC::put);
            logger.error(message, throwable);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String location, String message, Throwable throwable) {
        try {
            getContext().forEach(MDC::put);
            logger.error(SIMPLE_FORMAT, location, message, throwable);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String message, Throwable throwable, Object response) {
        try {
            getContext().forEach(MDC::put);
            logger.error(SIMPLE_FORMAT, message, response, throwable);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String location, String message, Throwable throwable, Object response) {
        try {
            getContext().forEach(MDC::put);
            logger.error(COMPLETE_FORMAT, location, message, response, throwable);
        } finally {
            MDC.clear();
        }
    }
}