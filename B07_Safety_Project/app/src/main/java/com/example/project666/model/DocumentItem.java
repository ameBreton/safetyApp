package com.example.project666.model;

/**
 * Represents a document stored in the safety planning application.
 * Contains metadata about documents including title, local file path,
 * timestamp, and Firebase key.
 */
public class DocumentItem {
    /** The title/name of the document */
    public String title;

    /** The local file path where the document is stored */
    String localFilePath;

    /** The timestamp when the document was added (in milliseconds) */
    String timestamp;

    /** The unique key used for Firebase database reference */
    String key;

    /**
     * Default constructor required for Firestore operations.
     */
    public DocumentItem() {}

    /**
     * Constructs a new DocumentItem with the specified title and file path.
     * Automatically sets the timestamp to current time.
     *
     * @param title The title/name of the document
     * @param localFilePath The local file system path to the document
     */
    public DocumentItem(String title, String localFilePath) {
        this.title = title;
        this.localFilePath = localFilePath;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    /**
     * Gets the title of the document.
     *
     * @return The document title
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of the document.
     *
     * @param title The new title for the document
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Gets the local file path of the document.
     *
     * @return The path to the local document file
     */
    public String getLocalFilePath() { return localFilePath; }

    /**
     * Sets the local file path of the document.
     *
     * @param localFilePath The new path to the document file
     */
    public void setLocalFilePath(String localFilePath) { this.localFilePath = localFilePath; }

    /**
     * Gets the timestamp when the document was added.
     *
     * @return The timestamp in string format
     */
    public String getTimestamp() { return timestamp; }

    /**
     * Sets the timestamp for the document.
     *
     * @param timestamp The new timestamp in string format
     */
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    /**
     * Gets the Firebase database key for this document.
     *
     * @return The unique key from Firebase
     */
    public String getKey() { return key; }

    /**
     * Sets the Firebase database key for this document.
     *
     * @param key The unique key from Firebase
     */
    public void setKey(String key) { this.key = key; }
}