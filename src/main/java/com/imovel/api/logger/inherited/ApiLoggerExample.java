package com.imovel.api.logger.inherited;

public class ApiLoggerExample {
    public static void main(String[] args) throws InterruptedException {
        // Set up some context in the main thread
        ApiLogger.setContext("userName", "user_from_main_thread");

        // Log from main thread
        ApiLogger.info("main", "Starting application with context inheritance");

        // Create a child thread
        Thread childThread = new Thread(() -> {
            // Add some child-specific context
            ApiLogger.setContext("userEmail", "child1@gmail.com");

            // Log from child thread - inherits parent context
            //ApiLogger.debug("child1", "Child1 thread started with inherited context");
            
            // Demonstrate nested threads
            Thread grandchildThread = new Thread(() -> {
                ApiLogger.info("grandchild", "Nested thread with doubly inherited context");
            });
            
            grandchildThread.start();
            try {
                grandchildThread.join();
            } catch (InterruptedException e) {
                ApiLogger.error("child2", "Interrupted while waiting for grandchild", e);
            }
            
            // Log after modifying context
            ApiLogger.info("child2", "Child thread finishing");
        });

        // Start the child thread
        childThread.start();
        
        // Log from main thread while child is running
        ApiLogger.debug("main", "Main thread continuing execution");
        
        // Wait for child to finish
        childThread.join();
        
        // Final log from main thread
        ApiLogger.info("main", "Application completed");
        
        // Show that contexts are isolated
        ApiLogger.debug("main", "Main thread context remains unchanged by children");
    }
}