package com.example.project666;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * NavBarActivity manages the interactions with the user and the navigation bar (desgined in
 * item_navigation_bar.xml).
 * In particular, NavBarActivity allows users to go back to the home page, log out of their account,
 * or do an emergency exit (clearing all tasks and logging out, then being redirected to a new tab)
 *
 * @author Amelie Breton
 *
 * version 0.0
 */
public class NavBarActivity extends AppCompatActivity {

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_navigation_bar);
    }

    /**
     * Eject acts as our emergency exit button, clearing all tasks before logging the user out,
     * sending them to a new google page, and closing the application
     * @param view is the current View
     */
    public void Eject(View view) {
        String url = "https://google.com";
        FirebaseAuth.getInstance().signOut();
        PinManage.clearPin(this);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    /**
     * Logout logs the user out of their account and sends them to the login page
     * @param view is the current View
     */
    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        PinManage.clearPin(this);
        Intent i = new Intent(this, StartActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * toHome sends the user to the home page
     * @param view is the current View
     */
    public void toHome(View view) {
        Intent i =new Intent(this, HomeActivity.class);
        startActivity(i);
    }
  
}