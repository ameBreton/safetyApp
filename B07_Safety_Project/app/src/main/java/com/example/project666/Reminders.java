package com.example.project666;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an activity for displaying, adding, editing, and deleting user reminders
 * Extends NavBarActivity to include a shared navigation bar
 * Useses Realtime Firebase to store reminders per user
 * Excludes a hidden "plan reminder" from the main list
 */
public class Reminders extends NavBarActivity {

    private List<Reminder> sampleList;
    private ReminderAdapter adapter;
    private Button btnAdd, btnToggleDelete, btnToggleEdit;
    private DatabaseReference remindersRef;
    private ValueEventListener remindersListener;
    private Reminder editingReminder = null;

    /**
     * Called when the activity is created. Initializes Firebase, UI, and listeners
     * @param savedInstanceState Saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reminders);

        FirebaseApp.initializeApp(this);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        remindersRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(uid)
                .child("reminders");

        sampleList = new ArrayList<>();
        initRecyclerView();
        initButtons();
        applyWindowInsets();
        initFirebaseListener();

    }
    /**
     * Called when on start, and sets up Firebase listener and checks for empty state
     */
    protected void onStart(){
        super.onStart();
        NoReminders();
        remindersRef.addValueEventListener(remindersListener);
        remindersRef.addValueEventListener(remindersListener);
    }
    /**
     * Called when on stop, and removes Firebase listener to avoid leaks
     */
    protected void onStop(){
        super.onStop();
        remindersRef.removeEventListener(remindersListener);
    }

    /**
     * Initializes the Firebase ValueEventListener to update the local list and adapter on data changes
     */
    protected void initFirebaseListener() {
        remindersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                sampleList.clear();

                for (DataSnapshot child : snap.getChildren()) {
                    String key = child.getKey();
                    // Skip the Plan Reminder so it never shows here
                    if ("plan_reminder".equals(key)) {
                        continue;
                    }
                    Reminder r = child.getValue(Reminder.class);
                    r.key = key;
                    sampleList.add(r);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // handle error if needed
            }
        };

        remindersRef.addValueEventListener(remindersListener);
    }


    /**
     * If there are no reminders in the database, adds default reminders
     */
    private void NoReminders() {
        // fetch once
        remindersRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) return;
            DataSnapshot snap = task.getResult();

            // exludes the plan reminder as its hiden
            int realCount = 0;
            for (DataSnapshot child : snap.getChildren()) {
                if (!"plan_reminder".equals(child.getKey())) {
                    realCount++;
                }
            }

            if (realCount == 0) {
                List<Reminder> defaults = Arrays.asList(
                        new Reminder("Green Light","Frequency follows center control"),
                        new Reminder("Yellow Light", "Frequency follows user control"),
                        new Reminder("Grey Light", "No notification")
                );
                for (Reminder r : defaults) {
                    remindersRef.push().setValue(r);
                }
            }
        });
    }

    /**
     * Initializes the RecyclerView and sets up the adapter
     */
    private void initRecyclerView() {
        RecyclerView recycler = findViewById(R.id.recyclerReminders);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(sampleList);
        recycler.setAdapter(adapter);

        // This tell the adapter what to do when delete is tapped
        adapter.setOnDeleteClickListener((reminder, pos) -> {
            remindersRef.child(reminder.key)
                    .removeValue()
                    .addOnSuccessListener(a -> {})
                    .addOnFailureListener(e -> {});
        });

        // This tells the adapter what to do when edit is tapped
        adapter.setOnEditClickListener((reminder, pos) -> {
            editingReminder = reminder;
            showAddReminderDialog();
        });

    }

    /**
     * Binds button views and their click actions for add, delete, and edit modes
     */
    private void initButtons() {
        btnAdd = findViewById(R.id.Add);
        btnToggleDelete = findViewById(R.id.btnToggleDelete);
        btnToggleEdit = findViewById(R.id.btnToggleEdit);

        btnAdd.setOnClickListener(v -> showAddReminderDialog());
        btnToggleDelete.setOnClickListener(v -> toggleDeleteMode());
        btnToggleEdit.setOnClickListener(v -> toggleEditMode());
    }
    /**
     * Show the dialog for creating a new reminder or editing one
     */
    public void showAddReminderDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_reminder, null);
        EditText etTitle = dialogView.findViewById(R.id.etReminderTitle);
        EditText etDesc = dialogView.findViewById(R.id.etReminderDesc);

        boolean isEdit = (editingReminder != null);
        if (isEdit) {
            etTitle.setText(editingReminder.title);
            etDesc .setText(editingReminder.description);
        }
        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Reminder" : "New Reminder")
                .setView(dialogView)
                .setPositiveButton("Save", (dlg, which) -> {
                    String t = etTitle.getText().toString();
                    String d = etDesc .getText().toString();

                    if (isEdit) {
                        // overwrite existing
                        editingReminder.title = t;
                        editingReminder.description = d;
                        remindersRef
                                .child(editingReminder.key)
                                .setValue(editingReminder);
                        editingReminder = null; // reset for next time
                    } else {
                        // push new
                        Reminder r = new Reminder(t, d);
                        remindersRef.push().setValue(r);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    /**
     * Toggles the adapter's delete mode， also uptates the button states
     */
    public void toggleDeleteMode() {
        boolean isDelete = !adapter.getDeleteMode();
        adapter.setDeleteMode(isDelete);
        btnToggleDelete.setText(isDelete ? "Cancel" : "Delete");
        btnAdd.setEnabled(!isDelete);
        btnToggleEdit.setEnabled(!isDelete);
    }
    /**
     * Toggles the adapter's edit mode and uptates button states
     */
    public void toggleEditMode() {
        boolean isEdit = !adapter.isEditMode();
        adapter.setEditMode(isEdit);
        btnToggleEdit.setText(isEdit ? "Cancel" : "Edit");
        btnAdd.setEnabled(!isEdit);
        btnToggleDelete.setEnabled(!isEdit);
    }
    /**
     * Applies window insets to the main view
     */
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}