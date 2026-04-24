package com.example.project666;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project666.login.SignInActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;


/**
 * Activity for handling email verification via Firebase.
 * Users can enter their email address to receive a password reset link.
 * A cooldown timer prevents repeated sends within 60 seconds.
 * This activity also allows users to return to the Sign In screen.
 */
public class EmailVerificationActivity extends NavBarActivity {

    /**
     * Initializes the email verification screen, binds UI components, and handles click actions.
     *
     * @param savedBundleInstance Bundle passed to the activity on recreation (e.g., rotation).
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedBundleInstance) {

        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_email_verification);

        // Firebase Authentication instance
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // UI references
        TextInputEditText editEmail = findViewById(R.id.editEmail);
        TextInputLayout emailInputLayout = findViewById(R.id.emailInputLayout);
        Button btnSendEmail = findViewById(R.id.btnSendEmail);
        Button btnBackToSignIn = findViewById(R.id.btnBackToSignIn);
        ImageView wandererImage = findViewById(R.id.happy_image);
        TextView warmText = findViewById(R.id.waitAMinute);

        // Handle "Send Email" button click
        btnSendEmail.setOnClickListener(v -> {
            emailInputLayout.setError(null);
            String email = editEmail.getText() == null ? "" : editEmail.getText().toString().trim();

            // Input validation
            if (email.isEmpty()) {
                emailInputLayout.setError("Email required");
                return;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInputLayout.setError("Invalid email format");
                return;
            }

            // Send Firebase password reset email
            auth.sendPasswordResetEmail(email).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Email sent", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Failed to send email: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }

                // Disable button and show countdown visuals
                btnSendEmail.setEnabled(false);
                btnSendEmail.setAlpha(0.5f);
                wandererImage.setVisibility(TextView.VISIBLE);
                warmText.setVisibility(TextView.VISIBLE);

                // Countdown timer for 60 seconds
                new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long seconds = millisUntilFinished / 1000;
                        btnSendEmail.setText("Resend in " + seconds + "s" );
                    }

                    @Override
                    public void onFinish() {
                        btnSendEmail.setEnabled(true);
                        btnSendEmail.setAlpha(1f);
                        btnSendEmail.setText("Send Email");
                    }
                }.start();
            });


        });

        // Handle "Back to Sign In" button click
        btnBackToSignIn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SignInActivity.class));
        });
    }
}
