package com.example.project666;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.project666.login.SignInActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

/**
 * RegisterActivity allows users to create a new account using email and password.
 * It performs real-time input validation and ensures that users agree to the terms.
 * <p>
 * On successful registration, users are redirected to {@link PinSetupActivity}.
 */
public class RegisterActivity extends NavBarActivity {

    private FirebaseAuth auth;

    /**
     * Initializes the activity and sets up form validation for email, password,
     * and confirm password inputs. Handles account creation with FirebaseAuth.
     *
     * @param savedInstanceState the previously saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        TextInputEditText editEmail = findViewById(R.id.editEmail);
        TextInputEditText editPassword = findViewById(R.id.editPassword);
        TextInputEditText editConfirmPassword = findViewById(R.id.editConfirmPassword);
        TextInputLayout emailInputLayout = findViewById(R.id.emailInputLayout);
        TextInputLayout passwordInputLayout = findViewById(R.id.passwordInputLayout);
        TextInputLayout confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        CheckBox checkboxTerms = findViewById(R.id.checkbox);
        Button btnSignUp = findViewById(R.id.btnSignUp);


        emailInputLayout.setEndIconVisible(false);
        passwordInputLayout.setEndIconVisible(false);
        confirmPasswordInputLayout.setEndIconVisible(false);

        SpannableString ss = getSpannableString();

        TextView textView = findViewById(R.id.textTerms);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        // Email field validation on focus loss
        // Shows error message if email format is invalid
        editEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = editEmail.getText() == null ? "" : editEmail.getText().toString().trim();
                emailInputLayout.setError(null);

                if (email.isEmpty()) {
                    emailInputLayout.setEndIconVisible(false);
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInputLayout.setError("Invalid email format");
                    emailInputLayout.setEndIconVisible(false);
                } else {
                    emailInputLayout.setEndIconVisible(true);
                }
            } else {
                emailInputLayout.setError(null);
            }
        });

        // Password field listener
        // Checks password length and if it matches confirm password field
        editPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String password = editPassword.getText() == null ? "" : editPassword.getText().toString().trim();
                String confirmPassword = editConfirmPassword.getText() == null ? "" : editConfirmPassword.getText().toString().trim();

                if (password.length() < 6) {
                    passwordInputLayout.setError("Password must be at least 6 characters");
                    confirmPasswordInputLayout.setError(null);
                    passwordInputLayout.setEndIconVisible(false);
                } else {
                    if (!password.equals(confirmPassword) && confirmPassword.length() >= 6) {
                        confirmPasswordInputLayout.setError("Passwords do not match:(");
                        confirmPasswordInputLayout.setEndIconVisible(false);
                    } else if (password.equals(confirmPassword)) {
                        confirmPasswordInputLayout.setError(null);
                        confirmPasswordInputLayout.setEndIconVisible(true);
                    }

                    passwordInputLayout.setError(null);
                    passwordInputLayout.setEndIconVisible(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        // Confirm password field listener
        // Checks if confirm password matches the password field
        editConfirmPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String password = editPassword.getText() == null ? "" : editPassword.getText().toString().trim();
                String confirmPassword = editConfirmPassword.getText() == null ? "" : editConfirmPassword.getText().toString().trim();
                if (password.equals(confirmPassword)) {
                    confirmPasswordInputLayout.setError(null);
                    confirmPasswordInputLayout.setEndIconVisible(true);
                } else if (confirmPassword.length() >= password.length()){
                    confirmPasswordInputLayout.setError("Passwords do not match:(");
                    confirmPasswordInputLayout.setEndIconVisible(false);
                } else {
                    confirmPasswordInputLayout.setError(null);
                    confirmPasswordInputLayout.setEndIconVisible(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        btnSignUp.setOnClickListener(v -> {
            emailInputLayout.setError(null);
            passwordInputLayout.setError(null);
            confirmPasswordInputLayout.setError(null);

            String email = editEmail.getText() == null ? "" : editEmail.getText().toString().trim();
            String password = editPassword.getText() == null ? "" : editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText() == null ? "" : editConfirmPassword.getText().toString().trim();

            if (email.isEmpty()) {
                emailInputLayout.setError("Email required");
                emailInputLayout.setEndIconVisible(false);
                return;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInputLayout.setError("Invalid email format");
                emailInputLayout.setEndIconVisible(false);
                return;
            }

            if (password.isEmpty()) {
                passwordInputLayout.setError("Password required");
                return;
            } else if (password.length() < 6) {
                passwordInputLayout.setError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordInputLayout.setError("Passwords do not match:(");
                return;
            }

            if (!checkboxTerms.isChecked()) {
                Toast.makeText(this, "Please agree to the terms and conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt to create user with Firebase
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->  {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, PinSetupActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Failed to register: " + (task.getException() == null ? "Failed to send error message, please try again." : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    /**
     * Generates a {@link SpannableString} that displays a clickable terms and privacy text.
     * <p>
     * The returned string looks like:
     * "By signing up, you agree to our Terms & Conditions and Privacy Policy."
     * </p>
     *
     * <ul>
     *     <li>"Terms & Conditions" is clickable and opens {@link TermsActivity}</li>
     *     <li>"Privacy Policy" is clickable and opens {@link PrivacyActivity}</li>
     * </ul>
     *
     * Both clickable spans are styled with underline and a custom color defined in {@code R.color.terms_text}.
     *
     * @return a {@link SpannableString} with clickable and styled spans for "Terms & Conditions" and "Privacy Policy"
     */
    @NonNull
    private SpannableString getSpannableString() {
        String fullText = "By signing up, you agree to our Terms & Conditions and Privacy Policy.";
        int termsStart = fullText.indexOf("Terms & Conditions");
        int termsEnd = termsStart + "Terms & Conditions".length();
        int privacyStart = fullText.indexOf("Privacy Policy");
        int privacyEnd = privacyStart + "Privacy Policy".length();

        SpannableString ss = new SpannableString(fullText);

        ClickableSpan termsClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(RegisterActivity.this, TermsActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.terms_text));
            }
        };

        ClickableSpan privacyClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(RegisterActivity.this, PrivacyActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.terms_text));
            }
        };

        ss.setSpan(termsClick, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(privacyClick, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * Navigates to the sign-in screen.
     *
     * @param v the clicked view
     */
    public void openSignInFromRegister(View v) {
        startActivity(new Intent(this, SignInActivity.class));
    }
}
