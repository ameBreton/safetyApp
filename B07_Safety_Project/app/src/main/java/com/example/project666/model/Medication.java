package com.example.project666.model;

/**
 * Represents a medication entry in the safety planning application.
 * Stores information about medications including name and dosage.
 * Includes a key field for Firebase database reference.
 */
public class Medication {
    /** The name of the medication */
    public String name;

    /** The dosage instructions for the medication */
    public String dosage;

    /** The unique key used for Firebase database reference */
    public String key;

    /**
     * Default constructor required for Firebase database operations.
     */
    public Medication() {}

    /**
     * Constructs a new Medication with the specified details.
     *
     * @param name The name of the medication
     * @param dosage The dosage instructions
     */
    public Medication(String name, String dosage) {
        this.name = name;
        this.dosage = dosage;
    }

    /**
     * Sets the Firebase database key for this medication.
     *
     * @param key The unique key from Firebase
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the Firebase database key for this medication.
     *
     * @return The unique key from Firebase
     */
    public String getKey() {
        return key;
    }
}