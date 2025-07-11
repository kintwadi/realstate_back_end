package com.imovel.api.logger.inherited;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe logging utility with context inheritance using SLF4J MDC
 */
public final class ApiLogger {
    private static final String DEFAULT_LOGGER_TYPE = "com.imovel.api.logger";
    private static final String SIMPLE_FORMAT = "[{}] {}";
    private static final String COMPLETE_FORMAT = "[{}] {}: {} - {}";

    // Thread-local storage for context inheritance
    private static final InheritableThreadLocal<ConcurrentHashMap<String, String>> threadContext =
            new InheritableThreadLocal<ConcurrentHashMap<String, String>>() {
                @Override
                protected ConcurrentHashMap<String, String> childValue(ConcurrentHashMap<String, String> parentValue) {
                    return parentValue == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(parentValue);
                }
            };

    private static Logger logger = LoggerFactory.getLogger(DEFAULT_LOGGER_TYPE);

    private ApiLogger() {}

    public static void setLoggerType(String type) {
        logger = LoggerFactory.getLogger(type);
    }

    /**
     * Sets a context value that will be inherited by child threads
     */
    public static void setContext(String key, String value) {
        ConcurrentHashMap<String, String> context = threadContext.get();
        if (context == null) {
            context = new ConcurrentHashMap<>();
            threadContext.set(context);
        }
        context.put(key, value);
    }

    public static void removeContext(String key) {
        ConcurrentHashMap<String, String> context = threadContext.get();
        if (context != null) {
            context.remove(key);
        }
    }

    public static void clearContext() {
        threadContext.remove();
    }

    private static Map<String, String> getContext() {
        ConcurrentHashMap<String, String> context = threadContext.get();
        return context == null ? Map.of() : Map.copyOf(context);
    }

    // Debug Methods
    public static void debug(String message) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.debug(message);
        } finally {
            MDC.clear();
        }
    }

    public static void debug(String location, String message) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.debug(SIMPLE_FORMAT, location, message);
        } finally {
            MDC.clear();
        }
    }

    public static void debug(String message, Object response) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.debug(SIMPLE_FORMAT, message, response);
        } finally {
            MDC.clear();
        }
    }

    public static void debug(String location, String message, Object response) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.debug(COMPLETE_FORMAT, location, message, response);
        } finally {
            MDC.clear();
        }
    }

    // Info Methods
    public static void info(String message) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.info(message);
        } finally {
            MDC.clear();
        }
    }

    public static void info(String location, String message) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.info(SIMPLE_FORMAT, location, message);
        } finally {
            MDC.clear();
        }
    }

    public static void info(String message, Object response) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.info(SIMPLE_FORMAT, message, response);
        } finally {
            MDC.clear();
        }
    }

    public static void info(String location, String message, Object response) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.info(COMPLETE_FORMAT, location, message, response);
        } finally {
            MDC.clear();
        }
    }

    // Error Methods
    public static void error(String message) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.error(message);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String location, String message) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.error(SIMPLE_FORMAT, location, message);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String message, Object response) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.error(SIMPLE_FORMAT, message, response);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String location, String message, Object response) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.error(COMPLETE_FORMAT, location, message, response);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String message, Throwable throwable) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.error(message, throwable);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String location, String message, Throwable throwable) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.error(SIMPLE_FORMAT, location, message, throwable);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String message, Throwable throwable, Object response) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.error(SIMPLE_FORMAT, message, response, throwable);
        } finally {
            MDC.clear();
        }
    }

    public static void error(String location, String message, Throwable throwable, Object response) {
        Map<String, String> context = getContext();
        try {
            context.forEach(MDC::put);
            logger.error(COMPLETE_FORMAT, location, message, response, throwable);
        } finally {
            MDC.clear();
        }
    }
}