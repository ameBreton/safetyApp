package com.example.project666.login;

/**
 * Defines the contract between the Login View and Presenter in the MVP architecture.
 * This interface separates the concerns of view display and business logic.
 */
public interface LoginContract {
    /**
     * Defines the view methods that the presenter can call to update the UI.
     */
    interface View {
        /**
         * Displays an error message for the email field.
         * @param message The error message to display
         */
        void showEmailError(String message);

        /**
         * Displays an error message for the password field.
         * @param message The error message to display
         */
        void showPasswordError(String message);

        /**
         * Handles successful login attempt.
         * @param isPinSet True if user has set up a PIN, false otherwise
         */
        void showLoginSuccess(boolean isPinSet);

        /**
         * Displays a login error message.
         * @param message The error message to display
         */
        void showLoginError(String message);

        /**
         * Navigates to the home activity after successful login.
         */
        void navigateToHome();

        /**
         * Navigates to the PIN setup activity for new users.
         */
        void navigateToPinSetup();

        /**
         * Displays a toast message to the user.
         * @param message The message to display
         */
        void showToast(String message);
    }

    /**
     * Defines the presenter methods that handle business logic.
     */
    interface Presenter {
        /**
         * Handles the login process with email and password.
         * @param email The user's email address
         * @param password The user's password
         */
        void handleLogin(String email, String password);
    }
}