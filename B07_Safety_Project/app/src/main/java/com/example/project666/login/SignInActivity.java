package com.example.project666.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.project666.EmailVerificationActivity;
import com.example.project666.HomeActivity;
import com.example.project666.NavBarActivity;
import com.example.project666.PinSetupActivity;
import com.example.project666.R;
import com.example.project666.RegisterActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

/**
 * The login activity that implements the View interface from MVP.
 * Handles UI interactions and delegates business logic to the presenter.
 */
public class SignInActivity extends NavBarActivity implements LoginContract.View{

    private LoginContract.Presenter presenter;

    /**
     * Initializes the activity, sets up UI components and presenter.
     * @param savedInstanceState Saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        TextInputEditText editEmail = findViewById(R.id.editEmail);
        TextInputEditText editPassword = findViewById(R.id.editPassword);
        TextInputLayout emailInputLayout = findViewById(R.id.emailInputLayout);
        TextInputLayout passwordInputLayout = findViewById(R.id.passwordInputLayout);
        Button btnSignIn = findViewById(R.id.btnSignIn);

        presenter = new LoginPresenter(this, FirebaseAuth.getInstance(), () -> getSharedPreferences("prefs", MODE_PRIVATE)
                .getBoolean("PIN_SET", false));

        btnSignIn.setOnClickListener(v -> {

            emailInputLayout.setError(null);
            passwordInputLayout.setError(null);

            String email = editEmail.getText() == null ? "" : editEmail.getText().toString().trim();
            String password = editPassword.getText() == null ? "" : editPassword.getText().toString().trim();

            presenter.handleLogin(email, password);
        });
    }

    /**
     * Opens the registration activity.
     * @param v The view that triggered this action
     */
    public void openRegister(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    /**
     * Opens the email verification activity.
     * @param v The view that triggered this action
     */
    public void openEmailVerification(View v) {
        startActivity(new Intent(this, EmailVerificationActivity.class));
    }

    // MVP View Implementation
    @Override
    public void showEmailError(String message) {
        TextInputLayout emailInputLayout = findViewById(R.id.emailInputLayout);
        emailInputLayout.setError(message);
    }

    @Override
    public void showPasswordError(String message) {
        TextInputLayout passwordInputLayout = findViewById(R.id.passwordInputLayout);
        passwordInputLayout.setError(message);
    }

    @Override
    public void showLoginSuccess(boolean isPinSet) {
        if (isPinSet) {
            navigateToHome();
        } else {
            navigateToPinSetup();
        }
        showToast("Signed in successfully!");
    }

    @Override
    public void showLoginError(String message) {
        showToast(message);
    }

    @Override
    public void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    public void navigateToPinSetup() {
        startActivity(new Intent(this, PinSetupActivity.class));
        finish();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}