package com.example.project666;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * PinUnlockActivity verifies the user's 4-digit PIN to unlock access to the app.
 * <p>
 * If the entered PIN matches the one stored in {@link PinManage},
 * the user is granted access to {@link HomeActivity}.
 */
public class PinUnlockActivity extends NavBarActivity {

    /**
     * Initializes the unlock UI and sets up behavior for PIN validation.
     *
     * @param savedInstanceState the previously saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_unlock);

        EditText pin1 = findViewById(R.id.pin1);
        EditText pin2 = findViewById(R.id.pin2);
        EditText pin3 = findViewById(R.id.pin3);
        EditText pin4 = findViewById(R.id.pin4);
        Button btnConfirm = findViewById(R.id.btnUnlock);

        PinSetupActivity.setPinByPin(pin1, pin2);
        PinSetupActivity.setPinByPin(pin2, pin3);
        PinSetupActivity.setPinByPin(pin3, pin4);

        btnConfirm.setOnClickListener(v -> {
            String pin = pin1.getText().toString() + pin2.getText().toString() + pin3.getText().toString() + pin4.getText().toString();
            String saved_pin = PinManage.getPin(this);

            if (pin.length() != 4) {
                Toast.makeText(this, "Please enter a 4-digit PIN", Toast.LENGTH_SHORT).show();
            } else {
                if (pin.equals(saved_pin)) {
                    Toast.makeText(this, "Welcome Back:)", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "PIN incorrect:(", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Navigates the user back to the StartActivity (e.g., login or registration screen).
     *
     * @param v the clicked view
     */
    public void openStart(View v) {
        startActivity(new Intent(this, StartActivity.class));
    }
}

