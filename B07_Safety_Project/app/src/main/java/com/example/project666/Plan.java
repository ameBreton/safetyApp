package com.example.project666;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project666.adapter.PlanAdapter;
import com.example.project666.adapter.QuestionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Plan Activity – displays a list of safety plans based on user responses.
 * <p>
 * This screen shows each question and the user's answer in a scrollable RecyclerView.
 * Users can review their plan and choose to edit it by clicking the "Edit" button,
 * which redirects them to the Generator activity.
 * <p>
 * This activity extends {@link NavBarActivity} to provide a consistent bottom navigation experience.
 * <p>
 * Data is passed into this activity via Intent extras as a List of {@link Question} objects.
 * <p>
 * Layout: activity_plan.xml
 *
 * @author Dylan Chen
 *
 * @version 1.0
 */
public class Plan extends NavBarActivity {

    /** RecyclerView which display the user's safety plan questions and answers. */
    private RecyclerView recyclerView;

    /** List of questions passed from previous activity via Intent. */
    private List<Question> questions = new ArrayList<Question>();

    /**
     * Initializes the UI and populates the RecyclerView with user safety plan data.
     *
     * @param savedInstanceState Saved instance state from Android lifecycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView and apply layout manager and divider
        recyclerView = findViewById(R.id.recViewPlans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Retrieve questions from intent
        Intent intent = getIntent();
        questions = (List<Question>) intent.getSerializableExtra("questions");

        // Bind questions to adapter
        PlanAdapter adapter = new PlanAdapter(questions);
        recyclerView.setAdapter(adapter);

        // Set up edit button to open Generator activity
        findViewById(R.id.edit).setOnClickListener(v -> editGenerator());
    }

    /**
     * Opens the Generator activity so the user can modify their answers.
     * Finishes the current activity to prevent stacking.
     */
    private void editGenerator() {
        startActivity(new Intent(this, Generator.class));
        finish();
    }
}