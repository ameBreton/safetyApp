package com.example.project666;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


/**
 * Activity for managing user notification preferences and scheduling reminder alarms.
 * Displays a list of reminders, allows per-item custom settings, and handles global
 * frequency/time controls. Uses Firebase to load and store reminders and
 * uses AlarmManager to schedule notifications.
 */
public class Notifications extends NavBarActivity {

    /** RecyclerView displaying the list of reminders */
    private RecyclerView rv;

    /** Adapter backing the RecyclerView with Reminder */
    private NotificationAdapter adapter;

    /** List of Reminder models loaded from Firebase */
    private List<Reminder> sampleList;

    /** Firebase reference to this user’s reminders node */
    private DatabaseReference remindersRef;

    /** Listener for Firebase real-time updates */
    private ValueEventListener listener;

    //UI elements
    private RadioGroup rgFrequency;
    private Button btnPickTime;
    private TextView tvPickedTime;

    // Time
    private int selectedHour = -1;
    private int selectedMinute = -1;

    /** SharedPreferences storing notification settings */
    private SharedPreferences prefs;

    /** Notification permission launcher */
    private ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (!isGranted) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Notifications permission is required to enable alerts",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    /**
     * Initializes UI, permissions, Firebase, and control state
     *
     * @param savedInstanceState saved state Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminders";
            String description = "Channel for reminder alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel =
                    new NotificationChannel(NotificationReceiver.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        prefs = getSharedPreferences("notifications_prefs", MODE_PRIVATE);

        initFirebase();
        initList();
        initRecyclerView();
        attachDatabaseListener();

        // Controls
        initControls();
        loadControlState();

        attachDatabaseListener();
    }

    /**
     * Callback interface for clicks on individual reminder items.
     */
    public interface OnItemClickListener {
        /**
         * Called when a reminder item is clicked
         *
         * @param r the Reminder model clicked
         * @param pos adapter position of the clicked item
         */
        void onItemClick(Reminder r, int pos);
    }

    private OnItemClickListener clickListener;


    /**
     * Registers a listener for reminder-item click events.
     *
     * @param l the OnItemClickListener to invoke
     */
    public void setOnItemClickListener(OnItemClickListener l) {
        this.clickListener = l;
    }

    /**
     * Shows the dialog for setting an individual reminder’s frequency
     *
     * @param r   the Reminder to configure
     * @param pos its position in the adapter
     */
    private void showCustomSettingsDialog(Reminder r, int pos) {
        View dlg = inflateSettingsView();

        prefillSettingsView(dlg, r);

        new AlertDialog.Builder(this)
                .setTitle(r.title)
                .setView(dlg)
                .setPositiveButton("Save", (d, w) -> saveSettingsFromView(dlg, r, pos))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Inflates and returns the settings dialog view
     *
     * @return the root View of the custom settings layout
     */
    private View inflateSettingsView() {
        View dlg = getLayoutInflater()
                .inflate(R.layout.dialog_reminder_settings, null);
        Button btnTime = dlg.findViewById(R.id.btnPickTime);
        TextView tvTime = dlg.findViewById(R.id.tvPickedTime);

        btnTime.setOnClickListener(v -> {
            // default to current dialog values
            int h = selectedHour < 0 ? Calendar.getInstance().get(Calendar.HOUR_OF_DAY) : selectedHour;
            int m = selectedMinute < 0 ? Calendar.getInstance().get(Calendar.MINUTE) : selectedMinute;

            new TimePickerDialog(Notifications.this,
                    (picker, hour, minute) -> {
                        selectedHour = hour;
                        selectedMinute = minute;
                        String fmt = String.format("%02d:%02d", hour, minute);
                        tvTime.setText(fmt);
                    },
                    h, m, true
            ).show();
        });

        return dlg;
    }


    /**
     * Pre-fills the settings dialog with the reminder’s notification settings,
     * using global defaults when no custom values are set.
     *
     * Initializes the enable switch, and selects the correct frequency radio button,
     * and displays the chosen time.
     *
     * @param dlg root view of the custom settings dialog
     * @param r the Reminder whose settings to display
     */
    private void prefillSettingsView(View dlg, Reminder r) {
        // For aurthorities for notifications

        Switch sw = dlg.findViewById(R.id.switchEnable);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        });

        RadioGroup rg = dlg.findViewById(R.id.rgFrequency);
        TextView tvTime = dlg.findViewById(R.id.tvPickedTime);

        boolean mainEnabled = prefs.getBoolean("enabled", false);
        String mainFreq = prefs.getString("frequency", "Daily");
        Calendar now = Calendar.getInstance();
        int mainHour = prefs.getInt("hour", now.get(Calendar.HOUR_OF_DAY));
        int mainMin = prefs.getInt("minute", now.get(Calendar.MINUTE));

        boolean fresh = (r.customFrequency == null);

        if (fresh) {
            sw.setChecked(mainEnabled);
        } else {
            sw.setChecked(r.notificationsEnabled);
        }
        String freqToShow;
        if (fresh || (r.useDefaultSettings && r.notificationsEnabled)) {
            freqToShow = mainFreq;
        } else {
            freqToShow = r.customFrequency;
        }

        int freqId = freqToShow.equals("Weekly") ? R.id.rbWeekly
                : freqToShow.equals("Monthly") ? R.id.rbMonthly
                : R.id.rbDaily;
        rg.check(freqId);

        int hourToShow = fresh || (r.useDefaultSettings && r.notificationsEnabled) ? mainHour : r.customHour;
        int minuteToShow = fresh || (r.useDefaultSettings && r.notificationsEnabled) ? mainMin : r.customMinute;
        tvTime.setText(String.format("%02d:%02d", hourToShow, minuteToShow));
    }


    /**
     * Reads the enable switch, frequency radios, and time field from the dialog,
     * applies them to the Reminder such as deciding default v.s. custom settings, and
     * updates the record in Firebase, and refreshes the adapter at the given position.
     *
     * @param dlg root view of the custom settings dialog
     * @param r the Reminder to update
     * @param pos adapter position of the reminder
     */
    private void saveSettingsFromView(View dlg, Reminder r, int pos) {
        Switch sw = dlg.findViewById(R.id.switchEnable);
        RadioGroup rg = dlg.findViewById(R.id.rgFrequency);
        TextView tvTime = dlg.findViewById(R.id.tvPickedTime);

        boolean enabled = sw.isChecked();
        String freq = (rg.getCheckedRadioButtonId() == R.id.rbWeekly) ? "Weekly"
                : (rg.getCheckedRadioButtonId() == R.id.rbMonthly) ? "Monthly"
                : "Daily";
        String timeStr= tvTime.getText().toString();
        int[] parts = Arrays.stream(timeStr.split(":"))
                .mapToInt(Integer::parseInt)
                .toArray();
        int ch = parts[0];
        int cm = parts[1];

        r.notificationsEnabled = enabled;
        r.customFrequency = freq;
        r.customHour = ch;
        r.customMinute = cm;

        // decide default vs custom
        if (!enabled) {
            // grey: off at all
            r.useDefaultSettings = false;
        } else {
            // compare to global prefs
            String mainFreq = prefs.getString("frequency", "Daily");
            int mainHour = prefs.getInt("hour", selectedHour);
            int mainMin = prefs.getInt("minute", selectedMinute);

            boolean freqSame = freq.equals(mainFreq);
            boolean timeSame = (ch == mainHour && cm == mainMin);

            r.useDefaultSettings = (freqSame && timeSame);
        }

        remindersRef.child(r.key).setValue(r);
        adapter.notifyItemChanged(pos);
    }

    /** Sets up the global frequency and time picker controls */
    private void initControls() {
        initFrequencySelector();
        initTimePicker();
    }

    /** Initializes the global frequency RadioGroup listener */
    private void initFrequencySelector() {
        rgFrequency = findViewById(R.id.rgFrequency);
        rgFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            String freq = (checkedId == R.id.rbWeekly) ? "Weekly"
                    : (checkedId == R.id.rbMonthly)? "Monthly"
                    : "Daily";
            prefs.edit()
                    .putString("frequency", freq)
                    .apply();

            scheduleAllReminders();
            Log.d("Notifications", "Global frequency = " + freq);
        });
    }

    /** Initializes the global time-picker button and listener */
    private void initTimePicker() {
        btnPickTime  = findViewById(R.id.btnPickTime);
        tvPickedTime = findViewById(R.id.tvPickedTime);

        // default to now or last selection
        btnPickTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            int h = selectedHour < 0 ? now.get(Calendar.HOUR_OF_DAY) : selectedHour;
            int m = selectedMinute < 0 ? now.get(Calendar.MINUTE) : selectedMinute;

            new TimePickerDialog(this, (tp, hour, minute) -> {
                selectedHour = hour;
                selectedMinute = minute;
                String fmt = String.format("%02d:%02d", hour, minute);
                tvPickedTime.setText(fmt);

                prefs.edit()
                        .putInt("hour", hour)
                        .putInt("minute", minute)
                        .apply();

                scheduleAllReminders();
                Log.d("Notifications", "Global time = " + fmt);
            }, h, m, true).show();
        });
    }

    /** Loads saved global frequency and time into the controls */
    private void loadControlState() {
        //Frequency
        String freq = prefs.getString("frequency", "Daily");
        int rbId = freq.equals("Weekly") ? R.id.rbWeekly
                : freq.equals("Monthly") ? R.id.rbMonthly
                : R.id.rbDaily;
        rgFrequency.check(rbId);

        //Time
        Calendar now = Calendar.getInstance();
        int defaultHour = now.get(Calendar.HOUR_OF_DAY);
        int defaultMinute = now.get(Calendar.MINUTE);

        selectedHour = prefs.getInt("hour", defaultHour);
        selectedMinute = prefs.getInt("minute", defaultMinute);

        String fmt = String.format("%02d:%02d", selectedHour, selectedMinute);
        tvPickedTime.setText(fmt);
    }

    /**
     * Generates a unique request code for a Reminder’s PendingIntent
     *
     * @param r the Reminder
     * @return integer requestCode for AlarmManager
     */
    private int requestCodeFor(Reminder r) {
        return r.key.hashCode();
    }

    /** Cancels all currently scheduled alarms for every loaded reminder */
    private void cancelAllAlarms() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        for (Reminder r : sampleList) {
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("reminderKey", r.key);
            PendingIntent pi = PendingIntent.getBroadcast(
                    this,
                    requestCodeFor(r),
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pi != null) {
                am.cancel(pi);
                pi.cancel();
            }
        }
    }

    /** Cancels existing alarms then schedules new ones per current settings */
    private void scheduleAllReminders() {
        cancelAllAlarms();

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        long defaultInterval = computeInterval(prefs.getString("frequency", "Daily"));

        int defaultHour = prefs.getInt("hour", Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        int defaultMinute = prefs.getInt("minute", Calendar.getInstance().get(Calendar.MINUTE));

        for (Reminder r : sampleList) {
            if (!r.notificationsEnabled) continue;

            boolean useDef = r.useDefaultSettings;
            long interval = useDef ? defaultInterval : computeInterval(r.customFrequency);

            long triggerAt = computeTriggerTime(
                    useDef ? defaultHour : r.customHour,
                    useDef ? defaultMinute : r.customMinute,
                    interval
            );

            scheduleSingleReminder(am, r.key, triggerAt, interval);
        }
    }

    /**
     * Computes the interval in milliseconds for a named frequency.
     *
     * @param freq one of Daily, Weekly, Monthly
     * @return interval in ms
     */
    private long computeInterval(String freq) {
        switch (freq) {
            case "Weekly": return AlarmManager.INTERVAL_DAY * 7;
            case "Monthly": return AlarmManager.INTERVAL_DAY * 30;
            default: return AlarmManager.INTERVAL_DAY;
        }
    }
    private long computeTriggerTime(int hour, int minute, long interval) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        long when = cal.getTimeInMillis();
        return (when < System.currentTimeMillis())
                ? when + interval
                : when;
    }
    private void scheduleSingleReminder(AlarmManager am, String reminderKey, long triggerAt, long interval) {

        Intent intent = new Intent(this, NotificationReceiver.class)
                .putExtra("reminderKey", reminderKey);

        int requestCode = reminderKey.hashCode();
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                interval,
                pi
        );
    }

    /**
     * Called when the activity is destroyed
     * Detaches the Firebase listener to avoid leaks
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachDatabaseListener();
    }

    /** Initializes the Firebase reference for this user’s reminders */
    private void initFirebase() {
        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();
        remindersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("reminders");
    }

    /** Initializes the in-memory list and adapter for notifications */
    private void initList() {
        sampleList = new ArrayList<>();
        adapter= new NotificationAdapter(sampleList);
    }

    private void initRecyclerView() {
        rv = findViewById(R.id.rvNotificationsReminders);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener((reminder, pos) -> {
            showCustomSettingsDialog(reminder, pos);
        });
    }

    /**
     * Attaches a Firebase listener to load reminders, ensure the plan reminder
     * is present at index 0, updates the adapter, and re-schedules all alarms
     * whenever the data changes.
     */
    private void attachDatabaseListener() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                sampleList.clear();
                boolean hasPlan = false;

                // Load all reminders, marking if "plan_reminder" exists
                for (DataSnapshot child : snap.getChildren()) {
                    String key = child.getKey();
                    Reminder r = child.getValue(Reminder.class);
                    r.key = key;
                    if ("plan_reminder".equals(key)) {
                        hasPlan = true;
                    }
                    sampleList.add(r);
                }

                // If no Plan Reminder in the DB yet, create it and add at top
                if (!hasPlan) {
                    ReminderPlan plan = new ReminderPlan();
                    remindersRef.child(plan.key).setValue(plan);
                    sampleList.add(0, plan);
                } else {
                    // Otherwise find the existing Plan Reminder and move it to index 0
                    for (int i = 0; i < sampleList.size(); i++) {
                        if ("plan_reminder".equals(sampleList.get(i).key)) {
                            Reminder plan = sampleList.remove(i);
                            sampleList.add(0, plan);
                            break;
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                scheduleAllReminders();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // handle error if needed
            }
        };

        remindersRef.addValueEventListener(listener);
    }

    /** Removes the Firebase listener if attached */
    private void detachDatabaseListener() {
        if (listener != null) {
            remindersRef.removeEventListener(listener);
            listener = null;
        }
    }
}
