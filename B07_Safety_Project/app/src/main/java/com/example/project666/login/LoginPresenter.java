package com.example.project666.login;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

/**
 * Implements the login business logic as part of the MVP pattern.
 * Handles user authentication and coordinates with the view for UI updates.
 */
public class LoginPresenter implements LoginContract.Presenter {

    private final LoginContract.View view;
    private final FirebaseAuth auth;
    private final PinManager pinManager;

    /**
     * Constructs a new LoginPresenter with dependencies.
     * @param view The view interface for UI updates
     * @param auth FirebaseAuth instance for authentication
     * @param pinManager Manages PIN-related functionality
     */
    public LoginPresenter(LoginContract.View view, FirebaseAuth auth, PinManager pinManager) {
        this.view = view;
        this.auth = auth;
        this.pinManager = pinManager;
    }

    /**
     * Handles the login process including validation and authentication.
     * @param email The user's email address
     * @param password The user's password
     */
    @Override
    public void handleLogin(String email, String password) {
        // Validate email format
        if (email.isEmpty()) {
            view.showEmailError("Email required");
            return;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            view.showEmailError("Invalid email format");
            return;
        }

        // Validate password requirements
        if (password.isEmpty()) {
            view.showPasswordError("Password required");
            return;
        } else if (password.length() < 6) {
            view.showPasswordError("Please enter a 6-digit password.");
            return;
        }

        // Attempt Firebase authentication
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Check if PIN is set for the user
                boolean isPinSet = pinManager.isPinSet();
                view.showLoginSuccess(isPinSet);
            } else {
                // Handle various authentication errors
                String msg = "Failed to sign in";
                Exception e = task.getException();
                if (e instanceof FirebaseAuthException) {
                    msg = switch (((FirebaseAuthException) e).getErrorCode()) {
                        case "ERROR_INVALID_CREDENTIAL" -> "The email or password you entered is incorrect:(";
                        case "ERROR_USER_DISABLED" -> "This email has been disabled. Please contact support:)";
                        case "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later:)";
                        default -> "Failed to sign in: " + e.getMessage();
                    };
                }
                view.showLoginError(msg);
            }
        });
    }
}