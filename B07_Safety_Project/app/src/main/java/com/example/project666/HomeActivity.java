package com.example.project666;

import static androidx.core.content.ContextCompat.startActivity;
import static java.lang.System.exit;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeActivity manages the user interactions with activity_home.xml, sending users to aid/support
 * page, plan generator, data management page, notification management page, reminder management
 * page, and the app-generated plan. In addition, it sets up data for the Plan page.
 *
 * @author Dylan Chen
 * @author Amelie Breton
 *
 * version 0.0
 */
public class HomeActivity extends NavBarActivity {

    /**
     * Sets up the homepage (adding activity_home.xml to the current screen)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Sends the user to the notifications page
     * @param view the current View
     */
    public void Notifications(View view) {
        Intent i =new Intent(this, Notifications.class);
        startActivity(i);
    }

    /**
     * Sends the user to the data management page
     * @param view the current View
     */
    public void Data(View view) {
        Intent i =new Intent(this, Data.class);
        startActivity(i);
    }

    /**
     * Sends the user to the reminders page
     * @param view the current View
     */
    public void Reminders(View view) {
        Intent i =new Intent(this, Reminders.class);
        startActivity(i);
    }

    /**
     * Sends the user to the aid/support page
     * @param view the current View
     */
    public void Aid(View view) {
        Intent i =new Intent(this, Aid.class);
        startActivity(i);
    }

    /**
     * Sends the user to the generated plan
     * @param view the current View
     */
    public void Plan(View view) {
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
                            List<Question> questions= new ArrayList<Question>();
                            for (DataSnapshot qSnap : dataSnapshot.getChildren()) {
                                Question q = qSnap.getValue(Question.class);
                                if (q != null) {
                                    questions.add(q);
                                }
                            }

                            Intent i =new Intent(this, Plan.class);
                            i.putExtra("questions", new ArrayList<>(questions));
                            startActivity(i);
                        } else {
                            Intent i =new Intent(this, Generator.class);
                            startActivity(i);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Sends the user to the Plan Generator
     * @param view the current View
     */
    public void Generator(View view) {
        Intent i =new Intent(this, Generator.class);
        startActivity(i);
    }



}