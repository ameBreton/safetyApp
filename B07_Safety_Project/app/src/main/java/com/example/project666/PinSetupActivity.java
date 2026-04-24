package com.example.project666;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * PinSetupActivity allows the user to create and save a 4-digit PIN,
 * which is then used for secure access in future sessions.
 * <p>
 * Each digit is entered into a separate EditText input, and the PIN is
 * securely stored using {@link PinManage}.
 */
public class PinSetupActivity extends NavBarActivity {

    private EditText pin1, pin2, pin3, pin4;

    /**
     * Initializes the activity and sets up the input fields for PIN creation.
     *
     * @param savedInstanceState the previously saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setup);

        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        // Setup auto-focusing behavior between digits
        setPinByPin(pin1, pin2);
        setPinByPin(pin2, pin3);
        setPinByPin(pin3, pin4);

        btnConfirm.setOnClickListener(v -> {
            String pin = pin1.getText().toString() + pin2.getText().toString() + pin3.getText().toString() + pin4.getText().toString();

            if (pin.length() != 4) {
                Toast.makeText(this, "Please enter a 4-digit PIN", Toast.LENGTH_SHORT).show();
            } else {
                PinManage.savePin(this, pin);
                Toast.makeText(this, "PIN set successfully! Welcome:)", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });
    }

    /**
     * Automatically moves the focus to the next input field when one digit is entered,
     * and back to the previous field on deletion.
     *
     * @param pin1 the current EditText
     * @param pin2 the next EditText to focus
     */
    public static void setPinByPin(EditText pin1, EditText pin2) {
        pin1.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pin1.length() == 1) {
                    pin2.requestFocus();
                }
            }
        });

        pin2.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_FORWARD_DEL) {
                if (pin2.length() == 0) {
                    pin1.setText("");
                    pin1.requestFocus();
                }
            }
            return false;
        });
    }
}
