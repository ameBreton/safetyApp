package com.example.project666;


/**
 * This is the reminder, stored in Firebase for users and in the recycler view
 * Each reminder has a title, description, and optional notification settings.
 */
public class Reminder {
    public String title;
    public String description;

    /** Firebase key for this reminder*/
    public String key;
    /** Whether notifications are enabled for this reminder */
    public boolean notificationsEnabled;
    /** Whether this reminder uses the default frequency settings */
    public boolean useDefaultSettings;

    //For notifications
    public String customFrequency;
    public int customHour;
    public int customMinute;

    public Reminder() {
    }
    public Reminder(String title, String description) {
        this.title = title;
        this.description = description;

        this.notificationsEnabled = false;
        this.useDefaultSettings = true;
        this.customFrequency = null;
        this.customHour = 0;
        this.customMinute = 0;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
}
