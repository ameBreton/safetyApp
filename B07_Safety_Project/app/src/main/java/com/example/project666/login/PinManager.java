package com.example.project666.login;

/**
 * Interface for managing PIN-related functionality.
 */
public interface PinManager {
    /**
     * Checks if the user has set up a PIN.
     * @return true if PIN is set, false otherwise
     */
    boolean isPinSet();
}