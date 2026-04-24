package com.example.project666;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

/**
 * TermsActivity displays the Terms and Conditions screen of the app.
 * This screen provides legal usage terms to users and includes a back navigation arrow in the toolbar.
 */
public class TermsActivity extends NavBarActivity {

    /**
     * Called when the activity is first created.
     * Sets up the layout, toolbar, and back navigation icon.
     *
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        // Set up the toolbar as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar_terms);
        setSupportActionBar(toolbar);

        // Remove default title display (title is likely set via XML or manually)
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Move the position of the back arrow on y-axis
        toolbar.post(() -> {
            final View backIcon = toolbar.getChildAt(1);
            if (backIcon != null) {
                backIcon.setTranslationY(26);
            }
        });

        // Enable back arrow for navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Handles toolbar item selection.
     * Specifically listens for the back arrow click and finishes the activity.
     *
     * @param item the selected menu item
     * @return true if handled, otherwise false
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

