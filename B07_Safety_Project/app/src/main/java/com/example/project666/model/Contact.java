package com.example.project666.model;

/**
 * Represents an emergency contact in the safety planning application.
 * Stores contact information including name, relationship, and phone number.
 * Includes a key field for Firebase database reference.
 */
public class Contact {
    /** The full name of the contact */
    public String name;

    /** The relationship of the contact to the user (e.g., "Friend", "Family") */
    public String relationship;

    /** The phone number of the contact */
    public String phone;

    /** The unique key used for Firebase database reference */
    public String key;

    /**
     * Default constructor required for Firebase database operations.
     */
    public Contact() {}

    /**
     * Constructs a new Contact with the specified details.
     *
     * @param name The full name of the contact
     * @param relationship The relationship to the user
     * @param phone The phone number of the contact
     */
    public Contact(String name, String relationship, String phone) {
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
    }

    /**
     * Sets the Firebase database key for this contact.
     *
     * @param key The unique key from Firebase
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the Firebase database key for this contact.
     *
     * @return The unique key from Firebase
     */
    public String getKey() {
        return key;
    }
}