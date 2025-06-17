package com.imovel.api.logger.inherited;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe logging utility class that provides structured logging with
 * context information that is inherited by child threads.
 * Uses Log4j2 with CloseableThreadContext for automatic resource management.
 */
public final class ApiLogger {
    // Default logger name
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
    
    private static Logger logger = LogManager.getLogger(DEFAULT_LOGGER_TYPE);

    private ApiLogger() {
    }

    /**
     * Sets the logger type/name to be used for logging.
     * 
     * @param type The logger name to use
     */
    public static void setLoggerType(String type) {
        logger = LogManager.getLogger(type);
    }

    /**
     * Sets a context value that will be inherited by child threads.
     * 
     * @param key The context key
     * @param value The context value
     */
    public static void setContext(String key, String value) {
        ConcurrentHashMap<String, String> context = threadContext.get();

        //parent
        if (context == null) {
            context = new ConcurrentHashMap<>();
            threadContext.set(context);
        }
        //child
        context.put(key, value);
    }

    /**
     * Removes a context value.
     * 
     * @param key The context key to remove
     */
    public static void removeContext(String key) {
        ConcurrentHashMap<String, String> context = threadContext.get();
        if (context != null) {
            context.remove(key);
        }
    }

    /**
     * Clears all context values for the current thread.
     */
    public static void clearContext() {
        threadContext.remove();
    }

    /**
     * Gets a copy of the current thread's context.
     * 
     * @return An immutable map containing the context information
     */
    private static Map<String, String> getContext() {
        ConcurrentHashMap<String, String> context = threadContext.get();
        return context == null ? Map.of() : Map.copyOf(context);
    }

    public static void debug(final String message) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.debug(message);
        }
    }

    public static void debug(final String location, final String message) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.debug(SIMPLE_FORMAT, location, message);
        }
    }

    public static void debug(final String message, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.debug(SIMPLE_FORMAT, message, new ObjectMessage(response));
        }
    }

    public static void debug(final String location, final String message, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.debug(COMPLETE_FORMAT, location, message, new ObjectMessage(response));
        }
    }

    public static void info(final String message) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.info(message);
        }
    }

    public static void info(final String location, final String message) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.info(SIMPLE_FORMAT, location, message);
        }
    }

    public static void info(final String message, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.info(SIMPLE_FORMAT, message, new ObjectMessage(response));
        }
    }

    public static void info(final String location, final String message, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.info(COMPLETE_FORMAT, location, message, new ObjectMessage(response));
        }
    }

    public static void error(final String message) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.error(message);
        }
    }

    public static void error(final String location, final String message) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.error(SIMPLE_FORMAT, location, message);
        }
    }

    public static void error(final String message, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.error(SIMPLE_FORMAT, message, new ObjectMessage(response));
        }
    }

    public static void error(final String location, final String message, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.error(COMPLETE_FORMAT, location, message, new ObjectMessage(response));
        }
    }

    public static void error(final String message, final Throwable throwable) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.error(SIMPLE_FORMAT, message, throwable);
        }
    }

    public static void error(final String location, final String message, final Throwable throwable) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.error(SIMPLE_FORMAT, location, message, throwable);
        }
    }

    public static void error(final String message, final Throwable throwable, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.error(SIMPLE_FORMAT, message, new ObjectMessage(response), throwable);
        }
    }

    public static void error(final String location, final String message, final Throwable throwable, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext())) {
            logger.error(COMPLETE_FORMAT, location, message, new ObjectMessage(response), throwable);
        }
    }
}