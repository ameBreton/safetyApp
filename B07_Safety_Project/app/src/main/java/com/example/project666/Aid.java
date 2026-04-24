package com.example.project666;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

/**
 * Aid.java manages the description on activity_aid.xml, generating the data from a json file
 * (support.java) and creating text boxes (for each source: one for the name, another for the link
 * or phone number) to express that data.
 *
 * @author Amelie Breton
 *
 * @version 0.0
 */
public class Aid extends NavBarActivity {
    private HashMap<String, List<String>> sourcesByCity;
    private HashMap<String, List<String>> descByCity;
    private String userCity = "N/A";

    /**
     * onCreate adds in activity_aid.xml into the current screen on the application, and sets up the
     * description for the sources and links (in particular, it first gets the user's city from
     * Firebase, then reads from the file, then adds the information into sourcesByCity and
     * descByCity, then finally generates the description as wanted)
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aid);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        descByCity = new HashMap<>();
        sourcesByCity = new HashMap<>();

        generateSupportMap();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            String uid = user.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("survey")
                    .child("1")
                    .child("userAnswer");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                /**
                 * On data change, finds the user's city then sets up the description to match
                 * @param snapshot The current data at the location
                 */
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        userCity = snapshot.getValue(String.class);
                        TextView userCityTitle = findViewById(R.id.userCityText);
                        userCityTitle.setText(userCity);

                        setSupportDescription();
                    }
                    else {
                        Intent i =new Intent(Aid.this, Generator.class);
                        Toast.makeText(Aid.this, "No Plans yet", Toast.LENGTH_SHORT).show();
                        startActivity(i);
                    }
                }

                /**
                 * Error management with Firebase
                 * @param error A description of the error that occurred
                 */
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Error with loading userCity", error.toException());
                }
            });
        }
    }

    /**
     * Adds in the data for the two maps (sourcesByCIty, descByCity) using CityLoader
     */
    protected void generateSupportMap(){
        CityLoader.loadSupportData(getApplicationContext(), sourcesByCity, descByCity);
    }

    /**
     * Sets up the descriptions using the data from sourcesByCity and descByCity
     */
    protected void setSupportDescription(){
        // get from the user's associated json file
        if(sourcesByCity.containsKey(userCity)){
            List<String> sources = sourcesByCity.get(userCity);
            List<String> descs = descByCity.get(userCity);

            LinearLayout container = findViewById(R.id.supportContainer);

            if((sources == null)||(descs == null)){
                TextView nothing = new TextView(this);
                String failureDesc = "We apologize, but appears that this city does not have any data";
                nothing.setText(failureDesc);
                nothing.setTextSize(25);
                nothing.setTextColor(Color.parseColor("#5e5130"));
                nothing.setPadding(20, 20, 20, 20);
                container.addView(nothing);
                return;
            }

            // Note: this is okay because order is preserved and these were added at the same time
            for(int i = 0; i < sources.size(); i++){
                // create a textview for the source name
                TextView sourceView = new TextView(this);
                sourceView.setText(sources.get(i));
                sourceView.setTextSize(25);
                sourceView.setTextColor(Color.parseColor("#f0e8da"));
                sourceView.setBackgroundColor(Color.parseColor("#6e6052"));
                sourceView.setTypeface(ResourcesCompat.getFont(this, R.font.brawler), Typeface.BOLD);
                sourceView.setPadding(20, 20, 20, 20);
                container.addView(sourceView);

                // create a textview for the links/sources themselves
                TextView linkView = new TextView(this);
                linkView.setText(descs.get(i));
                linkView.setTextSize(20);
                linkView.setTextColor(Color.parseColor("#5e5130"));
                linkView.setBackgroundColor(Color.parseColor("#e8e3d5"));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                );
                params.setMargins(0, 0, 0, 30);
                linkView.setLayoutParams(params);
                linkView.setTypeface(ResourcesCompat.getFont(this, R.font.brawler));
                linkView.setPadding(20, 20, 20, 20);
                container.addView(linkView);
            }

        }
        else{
            LinearLayout container = findViewById(R.id.supportContainer);
            TextView nothing = new TextView(this);
            String failureDesc = "As of right now, we only support the following cities: Toronto, Ottawa, Vancouver, Montreal, and Winnipeg. We apologize for any inconviniences and urge you to seek out resources from local governmental or support sites/organizations if you are not from one of these cities.";
            nothing.setText(failureDesc);
            nothing.setTextSize(25);
            nothing.setTextColor(Color.parseColor("#506634"));
            nothing.setPadding(20, 20, 20, 20);
            container.addView(nothing);
        }
    }

}
