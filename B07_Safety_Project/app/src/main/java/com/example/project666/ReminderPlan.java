package com.example.project666;

/**
 * Constructs a special new plan reminder with specific title and description for the plans
 * and assigns the fixed key "plan_reminder".
 */
public class ReminderPlan extends Reminder {
    boolean exist =false;


    public ReminderPlan() {
        super("Plan Reminder",
                "Click to set up a reminder for your plan");
        this.key = "plan_reminder";
    }

}