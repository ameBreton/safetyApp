package com.example.project666;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

/**
 * SplashActivity is the first screen launched when the app starts.
 * <p>
 * It determines whether a PIN has been previously set:
 * <ul>
 *   <li>If PIN exists, it launches {@link PinUnlockActivity}</li>
 *   <li>If not, it launches {@link StartActivity}</li>
 * </ul>
 * This activity is transient and immediately finishes itself after redirection.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends NavBarActivity {

    /**
     * Checks the presence of a stored PIN and navigates accordingly.
     *
     * @param savedInstanceState the saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (PinManage.isPinSet(this)) {
            startActivity(new Intent(this, PinUnlockActivity.class));
        } else {
            startActivity(new Intent(this, StartActivity.class));
        }
        finish();
    }
}