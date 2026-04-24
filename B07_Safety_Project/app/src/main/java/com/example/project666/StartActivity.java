package com.example.project666;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialCustomException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.Credential;
import androidx.credentials.CustomCredential;
import androidx.credentials.exceptions.GetCredentialUnknownException;
import androidx.credentials.exceptions.NoCredentialException;

import com.example.project666.login.SignInActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

/**
 * StartActivity is the entry point of the application.
 * It provides options for users to sign in using Google or PIN (if previously set),
 * or navigate to email/password login screen.
 * <p>
 * This activity uses AndroidX CredentialManager to perform a one-tap Google Sign-In.
 */
@SuppressWarnings("deprecation")
public class StartActivity extends NavBarActivity {
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;

    /**
     * Initializes UI components and configures CredentialManager to support Google login.
     * Also handles fallback login options like PIN and email/password.
     *
     * @param savedInstanceState the previous state if reinitialized, otherwise null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();
        CredentialManager manager = CredentialManager.create(this);
        CancellationSignal signal = new CancellationSignal();
        Button btnContinueWithGoogle = findViewById(R.id.btnContinueWithGoogle);
        Button btnBackToPin = findViewById(R.id.btnBackToPin);


        // Show "Back to PIN" only if a PIN has been set before
        if (PinManage.isPinSet(this)) {
            btnBackToPin.setVisibility(View.VISIBLE);
        }

        // CredentialManager option setup
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest credentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Handle Google login via CredentialManager
        btnContinueWithGoogle.setOnClickListener(v ->
            manager.getCredentialAsync(this, credentialRequest, signal, Executors.newSingleThreadExecutor(), new CredentialManagerCallback<>() {

                /**
                 * Called when the credential is successfully retrieved.
                 * <p>
                 * This method checks if the credential is a {@link GoogleIdTokenCredential}, extracts the
                 * ID token, and proceeds to sign in using the extracted token.
                 *
                 * @param result the credential response containing the credential
                 */
                @Override
                public void onResult(@NonNull GetCredentialResponse result) {
                    Credential cred = result.getCredential();

                    if (!(cred instanceof CustomCredential custom)) {
                        printToast("Unexpected credential type:(");
                        return;
                    }

                    if (!(custom.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL))) {
                        printToast("Credential is not a Google ID token:(");
                        return;
                    }

                    String idToken = GoogleIdTokenCredential.createFrom(custom.getData()).getIdToken();
                    runOnUiThread(() -> signInWithGoogle(idToken));
                }

                /**
                 * Called when there is an error while retrieving the credential.
                 * <p>
                 * Handles different error cases like cancellation, missing credentials, unknown errors,
                 * or custom format errors. If no credential is found, initiates Google Sign-In manually.
                 *
                 * @param e the exception thrown during credential retrieval
                 */
                @Override
                public void onError(@NonNull GetCredentialException e) {
                    if (e instanceof GetCredentialCancellationException) return;

                    String msg;
                    if (e instanceof NoCredentialException) {
                        msg = "Let's add your Google account first:)";
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                                GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build();

                        GoogleSignInClient client = GoogleSignIn.getClient(StartActivity.this, gso);
                        startActivityForResult(client.getSignInIntent(), RC_SIGN_IN);
                    } else if (e instanceof GetCredentialUnknownException) {
                        msg = "Something went wrong. Please try again later:)";
                    } else if (e instanceof GetCredentialCustomException) {
                        msg = "The credential format is invalid or incompatible:(";
                    } else {
                        msg = "Failed to sign in: " + e.getMessage();
                    }

                    printToast(msg);
                }
            })
        );
        btnBackToPin.setPaintFlags(btnBackToPin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    /**
     * Displays a toast message on the UI thread.
     *
     * @param msg the message to display
     */
    private void printToast(String msg) {
        runOnUiThread(() -> Toast.makeText(StartActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    /**
     * Handles Firebase sign-in using the provided Google ID token.
     * After authentication, redirects user to PIN setup or home depending on state.
     *
     * @param idToken the Google ID token retrieved from CredentialManager or GoogleSignIn API
     */
    private void signInWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                if (!PinManage.isPinSet(this)) {
                    Toast.makeText(this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, PinSetupActivity.class));
                } else {
                    Toast.makeText(this, "Welcome back:)", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                }
            } else {
                Toast.makeText(this, "Firebase authentication failed, please try again:)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles the result from fallback Google Sign-In intent.
     *
     * @param requestCode request code identifying the Google Sign-In
     * @param resultCode  the result code returned by the child activity
     * @param data        intent containing Google account data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                signInWithGoogle(idToken);
            } catch (ApiException e) {
                int code = e.getStatusCode();

                if (code == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) return;

                String msg;
                if (code == GoogleSignInStatusCodes.NETWORK_ERROR) {
                    msg = "Network error. Check your connection:)";
                } else {
                    msg = "Failed to sign in with Google. Please try again:)";
                }

                printToast(msg);
            }
        }
    }

    /**
     * Opens the email/password sign-in screen.
     *
     * @param v the clicked view
     */
    public void openSignInFromStart(View v) {
        startActivity(new Intent(this, SignInActivity.class));
    }

    /**
     * Opens the PIN unlock screen.
     *
     * @param v the clicked view
     */
    public void openPinUnlock(View v) {
        startActivity(new Intent(this, PinUnlockActivity.class));
    }
}



