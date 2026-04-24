package com.example.project666.model;

/**
 * Represents a safe location in the safety planning application.
 * Stores information about safe locations including address and notes.
 * Includes a key field for Firebase database reference.
 */
public class SafeLocation {
    /** The physical address of the safe location */
    public String address;

    /** Additional notes about the safe location */
    public String notes;

    /** The unique key used for Firebase database reference */
    public String key;

    /**
     * Default constructor required for Firebase database operations.
     */
    public SafeLocation() {}

    /**
     * Constructs a new SafeLocation with the specified details.
     *
     * @param address The physical address of the location
     * @param notes Additional notes about the location
     */
    public SafeLocation(String address, String notes) {
        this.address = address;
        this.notes = notes;
    }

    /**
     * Sets the Firebase database key for this location.
     *
     * @param key The unique key from Firebase
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the Firebase database key for this location.
     *
     * @return The unique key from Firebase
     */
    public String getKey() {
        return key;
    }
}