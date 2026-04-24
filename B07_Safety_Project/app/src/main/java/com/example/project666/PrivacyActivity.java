package com.example.project666;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

/**
 * PrivacyActivity displays the Privacy Policy screen of the app.
 * It informs users how their data is collected, stored, and used.
 * <p>
 * This activity also supports navigation back to the previous screen via a toolbar arrow.
 */
public class PrivacyActivity extends NavBarActivity {

    /**
     * Called when the activity is first created.
     * Initializes the UI and toolbar with back navigation.
     *
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        // Set up the toolbar as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar_privacy);
        setSupportActionBar(toolbar);

        // Hide default title to use custom styling or centered title
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Move the position of the back arrow on y-axis
        toolbar.post(() -> {
            final View backIcon = toolbar.getChildAt(1);
            if (backIcon != null) {
                backIcon.setTranslationY(26);
            }
        });

        // Enable back arrow in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Handles selection of toolbar menu items.
     * Finishes the activity when the back arrow is clicked.
     *
     * @param item the selected menu item
     * @return true if handled, otherwise pass to super
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

