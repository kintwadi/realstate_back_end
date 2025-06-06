
package com.imovel.api.logger;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.ObjectMessage;



/**
 * 
 * A thread-safe logging utility class that provides structured logging with
 * context information.
 * Uses Log4j2 with CloseableThreadContext for automatic resource management.
 */
public final class ApiLogger
{
    // Default logger name
    private static final String  DEFAULT_LOGGER_TYPE = "imovel.api,logger.";
    private static final String  SIMPLE_FORMAT       = "[{}] {}";
    private static final String  COMPLETE_FORMAT     = "[{}] {}: {} - {}";

    // Immutable snapshot for global headers
    private static volatile ConcurrentHashMap< String, String > globalHeaders = new ConcurrentHashMap<>();
    private static Logger logger = LogManager.getLogger( DEFAULT_LOGGER_TYPE );

    /**
     * Private constructor to prevent instantiation of this BaseLogger class.
     */
    private ApiLogger()
    {
    }
    /**
     * Sets the logger type/name to be used for logging.
     * 
     * @param type The logger name to use
     */
    public static void setLoggerType( String type )
    {
        logger = LogManager.getLogger( type );
    }

    /**
     * Sets a global header that will be included in all log messages.
     * 
     * @param key The header key
     * @param value The header value
     */
    public static synchronized void setContext(String key,
                                               String value )
    {
        if(ThreadContext.isEmpty()){
            ThreadContext.put(key,value);
        }else{
            ConcurrentHashMap< String, String > newHeaders = new ConcurrentHashMap<>( globalHeaders );
            newHeaders.put( key,value );
            newHeaders.forEach((k,v)->globalHeaders.putIfAbsent(k,v));
        }
    }
    /**
     * Combines global and thread-local headers into a single context map.
     * 
     * @return An immutable map containing all context information
     */
    private static Map< String, String > getContext()
    {
        ThreadContext.clearMap();

        // Safe publication via volatile read - no synchronization needed
        Map< String, String > globalSnapshot = globalHeaders;
        globalSnapshot.forEach( ThreadContext::put );
        return Map.copyOf( ThreadContext.getImmutableContext() );
    }

    /**
     * ThreadContext (Hash Map) is not a Thread.  This is  needed by log4j to map the key value in the JSON
     * layout.
     * Logs a debug level message with context information.
     * Uses CloseableThreadContext for automatic resource cleanup.
     * CloseableThreadContext is log4j recommended when using ThreadContext hash maps
     * 
     * @param message The message to log
     */
    public static void debug(final String message) {
         try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext()))
        {
            logger.debug(message);
        }
    }

    /**
     * ThreadContext (Hash Map) is not a Thread.  This is  needed by log4j to map the key value in the JSON
     * layout.
     * Logs a debug level message with location context.
     * 
     * @param location The code location
     * @param message The message to log
     */
    public static void debug(final String location, final String message) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext()))
        {
            logger.debug( SIMPLE_FORMAT,
                          location,
                          message );
        }
    }

    /**
     * Logs a debug level message with response object.
     * 
     * @param message The message to log
     * @param response The response object to include
     */
    public static void debug(final String message, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext()))
        {
            logger.debug( SIMPLE_FORMAT,
                          message,
                    new ObjectMessage( response )  );
        }
    }

    /**
     * Logs a debug level message with location and response object.
     * 
     * @param location The code location
     * @param message The message to log
     * @param response The response object to include
     */
    public static void debug(final String location, final String message, final Object response) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext()))
        {
            logger.debug( COMPLETE_FORMAT,
                          location,
                          message,
                          new ObjectMessage( response )  );
        }
    }
    /**
     * Logs an info level message with context information.
     * 
     * @param message The message to log
     */
    public static void info(final String message) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext()))
        {
            logger.info(message);
        }
    }

    /**
     * Logs an info level message with location context.
     * 
     * @param location The code location
     * @param message The message to log
     */
    public static void info(final String location, final String message) {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext()))
        {
            logger.info( SIMPLE_FORMAT,
                         location,
                         message );
        }
    }

    /**
     * Logs an info level message with response object.
     * 
     * @param message The message to log
     * @param response The response object to include
     */
    public static void info( final String message,
                             final Object response )
    {
         try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll( getContext() ))
        {
            logger.info( SIMPLE_FORMAT,
                         message,new ObjectMessage( response ) );
        }
    }
    /**
     * Logs an info level message with location and response object.
     * 
     * @param location The code location
     * @param message The message to log
     * @param response The response object to include
     */
    public static void info(final String location, final String message, final Object response) {
         try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(getContext()))
        {
            logger.info( COMPLETE_FORMAT,
                         location,
                         message,
                         new ObjectMessage( response ) );
        }
    }
    /**
     * Logs an error level message with context information.
     * 
     * @param message The message to log
     */
    public static void error( final String message )
    {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll( getContext() ))
        {
            logger.error( message );
        }
    }

    /**
     * Logs an error level message with location context.
     * 
     * @param location The code location
     * @param message The message to log
     */
    public static void error( final String location,
                              final String message )
    {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll( getContext() ))
        {
            logger.error( SIMPLE_FORMAT,
                          location,
                          message );
        }
    }

    /**
     * Logs an error level message with response object.
     * 
     * @param message The message to log
     * @param response The response object to include
     */
    public static void error( final String message,
                              final Object response )
    {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll( getContext() ))
        {
            logger.error( SIMPLE_FORMAT,
                          message,
                          new ObjectMessage( response )  );
        }
    }

    /**
     * Logs an error level message with location and response object.
     * 
     * @param location The code location
     * @param message The message to log
     * @param response The response object to include
     */
    public static void error( final String location,
                              final String message,
                              final Object response )
    {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll( getContext() ))
        {
            logger.error( COMPLETE_FORMAT,
                          location,
                          message,
                          new ObjectMessage( response )  );
        }
    }

    /**
     * Logs an error level message with throwable.
     * 
     * @param message The message to log
     * @param throwable The exception/throwable to include
     */
    public static void error( final String message,
                              final Throwable throwable )
    {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll( getContext() ))
        {
            logger.error( SIMPLE_FORMAT,
                          message,
                          throwable );
        }
    }

    /**
     * Logs an error level message with location and throwable.
     * 
     * @param location The code location
     * @param message The message to log
     * @param throwable The exception/throwable to include
     */
    public static void error( final String location,
                              final String message,
                              final Throwable throwable )
    {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll( getContext() ))
        {
            logger.error( SIMPLE_FORMAT,
                          location,
                          message,
                          throwable );
        }
    }

    /**
     * Logs an error level message with throwable and response object.
     * 
     * @param message The message to log
     * @param throwable The exception/throwable to include
     * @param response The response object to include
     */
    public static void error( final String message,
                              final Throwable throwable,
                              final Object response )
    {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll( getContext() ))
        {
            logger.error( SIMPLE_FORMAT,
                          message,
                          new ObjectMessage( response ) ,
                          throwable );

        }
    }

    /**
     * Logs an error level message with location, throwable and response
     * object.
     * 
     * @param location The code location
     * @param message The message to log
     * @param throwable The exception/throwable to include
     * @param response The response object to include
     */
    public static void error( final String location,
                              final String message,
                              final Throwable throwable,
                              final Object response )
    {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll( getContext() ))
        {
            logger.error( COMPLETE_FORMAT,
                          location,
                          message,
                          new ObjectMessage( response ) ,
                          throwable );

        }
    }

}