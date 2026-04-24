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

import com.example.project666.adapter.QuestionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generator Activity – Displays a dynamic questionnaire and collects user responses.
 * <p>
 * This activity is responsible for:
 * <ul>
 *     <li>Loading questions from local JSON file and Firebase</li>
 *     <li>Displaying them in a RecyclerView using {@link QuestionAdapter}</li>
 *     <li>Handling user interactions and updating question flow based on answers</li>
 *     <li>Validating responses and saving them to Firebase Realtime Database</li>
 *     <li>Navigating to the {@link Plan} activity upon submission</li>
 * </ul>
 *
 * Layout file: activity_generator.xml
 * Data passed: None
 * Data saved: Firebase Realtime DB under "users/{uid}/survey"
 *
 * @author Dylan Chen
 *
 * @version 1.0
 */
public class Generator extends NavBarActivity {

    /** RecyclerView that displays the list of questions. */
    private RecyclerView recyclerView;

    /** List of questions displayed, updated dynamically. */
    private List<Question> questions = new ArrayList<Question>();

    /**
     * Initializes the questionnaire interface and sets up dynamic logic
     * for rendering and updating question flow based on user input.
     *
     * @param savedInstanceState Android lifecycle parameter
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generator);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recViewQues);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Load empty base questions
        List<Question> questionsList = QuestionLoader.loadQuestionsFromRaw(this);

        // Load saved answers from Firebase and merge into questionsList
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("survey")
                    .get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot qSnap : dataSnapshot.getChildren()) {
                                Question q = qSnap.getValue(Question.class);
                                if (q != null) {
                                    questions.add(q);
                                    int tempId = q.getId();
                                    for(Question i : questionsList) {
                                        if(i.getId() == q.getId()) {
                                            i.setUserAnswer(q.getUserAnswer());
                                            i.setUserAnswers(q.getUserAnswers());
                                        }
                                    }
                                }
                            }
                        } else {
                            for (Question quest : questionsList) {
                                if (quest.getSection().equals("Warm-Up")) {
                                    questions.add(quest);
                                }
                            }
                        }

                        recyclerView.getAdapter().notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show());
        }

        // Setup adapter and bind dynamic logic for answer change
        QuestionAdapter adapter = new QuestionAdapter(questions);

        adapter.setOnAnswerChangeListener(new OnAnswerChangeListener() {
            @Override
            public void onAnswerChanged() {
                String section = questions.get(0).getUserAnswer();

                recyclerView.post(() -> {
                    for (Question q : questions) {
                        for(Question i : questionsList) {
                            if(i.getId() == q.getId()) {
                                i.setUserAnswer(q.getUserAnswer());
                                i.setUserAnswers(q.getUserAnswers());
                            }
                        }
                    }

                    questions.clear();
                    for (Question quest : questionsList) {
                        if (quest.getSection().equals(section)
                                || quest.getSection().equals("Warm-Up")
                                || quest.getSection().equals("Follow")) {
                            questions.add(quest);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
            }
        });

        recyclerView.setAdapter(adapter);

        // Handle Submit button click
        findViewById(R.id.button_submit).setOnClickListener(v -> saveSurveyToFirebase());

    }

    /**
     * Validates user input and saves the current survey responses to Firebase.
     * If validation passes, navigates to the {@link Plan} activity.
     */
    private void saveSurveyToFirebase() {
        for (Question q : questions) {
            if (q.getType() == 1) {
                if (q.getUserAnswers() == null || q.getUserAnswers().isEmpty()) {
                    Toast.makeText(this, "Please answer all questions.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (q.getType() == 4) {
                if (q.getUserAnswer() == null || q.getUserAnswer().isEmpty()) {
                    Toast.makeText(this, "Please pick the city.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Objects.equals(q.getUserAnswer(), "Choose a city")) {
                    Toast.makeText(this, "Please pick the city.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (q.getUserAnswer() == null || q.getUserAnswer().trim().isEmpty()) {
                    Toast.makeText(this, "Please answer all questions.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Save to Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("survey")
                    .setValue(questions);
        }

        // Navigate to Plan activity
        Intent intent = new Intent(this, Plan.class);
        intent.putExtra("questions", new ArrayList<>(questions));
        startActivity(intent);
        finish();
    }
}