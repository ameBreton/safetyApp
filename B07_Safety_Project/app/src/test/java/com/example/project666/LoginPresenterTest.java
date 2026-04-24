package com.example.project666;

import static org.mockito.Mockito.*;

import com.example.project666.login.LoginContract;
import com.example.project666.login.LoginPresenter;
import com.example.project666.login.PinManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link LoginPresenter} verifying authentication logic and error handling.
 *
 * <p><b>Test Strategy:</b>
 * <ul>
 *   <li>Uses Mockito to isolate dependencies (View, FirebaseAuth, PinManager)</li>
 *   <li>Covers all specified validation rules from requirements</li>
 *   <li>Tests all FirebaseAuthException error code scenarios</li>
 *   <li>Verifies correct View interactions for each case</li>
 * </ul>
 *
 * <p><b>Test Coverage Includes:</b>
 * <table border="1">
 *   <tr><th>Category</th><th>Coverage</th></tr>
 *   <tr><td>Input Validation</td><td>100%</td></tr>
 *   <tr><td>Firebase Success Path</td><td>Verified</td></tr>
 *   <tr><td>Firebase Error Cases</td><td>All specified codes</td></tr>
 *   <tr><td>PIN Flow</td><td>Post-auth check</td></tr>
 * </table>
 *
 * @see LoginPresenter
 * @author Deepika Chandrashekar
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {
    private LoginContract.View mockView;
    private FirebaseAuth mockAuth;
    private PinManager mockPinManager;
    private LoginPresenter presenter;

    /**
     * Initializes test environment before each test case.
     * <p>Configures:
     * <ul>
     *   <li>Mock View using Mockito</li>
     *   <li>Mock FirebaseAuth instance</li>
     *   <li>PinManager with default PIN set (true)</li>
     *   <li>Fresh LoginPresenter instance for each test</li>
     * </ul>
     */
    @Before
    public void setUp() {
        mockView = mock(LoginContract.View.class);
        mockAuth = mock(FirebaseAuth.class);
        mockPinManager = mock(PinManager.class);
        when(mockPinManager.isPinSet()).thenReturn(true);
        presenter = new LoginPresenter(mockView, mockAuth, mockPinManager);
    }

    /**
     * Creates a configured mock Task for Firebase Auth operations.
     *
     * @param isSuccessful Whether the task should simulate success
     * @param exception Exception to return if task fails (nullable)
     * @return Configured Task<AuthResult> mock
     */
    private Task<AuthResult> createMockTask(boolean isSuccessful, Exception exception) {
        Task<AuthResult> mockTask = mock(Task.class);
        when(mockTask.isSuccessful()).thenReturn(isSuccessful);
        when(mockTask.getException()).thenReturn(exception);

        // Trigger the listener immediately when addOnCompleteListener is called
        when(mockTask.addOnCompleteListener(any()))
                .thenAnswer(invocation -> {
                    OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
                    listener.onComplete(mockTask);
                    return mockTask;
                });

        return mockTask;
    }

    /**
     * Verifies empty email validation shows correct error.
     * <p><b>Given:</b> Empty email
     * <p><b>When:</b> handleLogin() called
     * <p><b>Then:</b> View.showEmailError("Email required")
     */
    @Test
    public void testEmptyEmail_ShowsError() {
        presenter.handleLogin("", "password123");
        verify(mockView).showEmailError("Email required");
        verifyNoMoreInteractions(mockAuth); // Ensure no Firebase call
    }

    /**
     * Verifies invalid email format validation.
     * <p><b>Given:</b> Malformed email (no @)
     * <p><b>When:</b> handleLogin() called
     * <p><b>Then:</b> View.showEmailError("Invalid email format")
     */
    @Test
    public void testInvalidEmailFormat_ShowsError() {
        presenter.handleLogin("not-an-email", "password123");
        verify(mockView).showEmailError("Invalid email format");
        verifyNoMoreInteractions(mockAuth);
    }

    /**
     * Verifies empty password validation.
     * <p><b>Given:</b> Valid email + empty password
     * <p><b>When:</b> handleLogin() called
     * <p><b>Then:</b> View.showPasswordError("Password required")
     */
    @Test
    public void testEmptyPassword_ShowsError() {
        presenter.handleLogin("test@example.com", "");
        verify(mockView).showPasswordError("Password required");
        verifyNoMoreInteractions(mockAuth);
    }

    /**
     * Verifies minimum password length validation.
     * <p><b>Given:</b> Password < 6 characters
     * <p><b>When:</b> handleLogin() called
     * <p><b>Then:</b> View.showPasswordError("Please enter a 6-digit password.")
     */
    @Test
    public void testShortPassword_ShowsError() {
        presenter.handleLogin("test@example.com", "123");
        verify(mockView).showPasswordError("Please enter a 6-digit password.");
        verifyNoMoreInteractions(mockAuth);
    }

    /**
     * Verifies successful authentication flow.
     * <p><b>Given:</b> Valid credentials
     * <p><b>When:</b> Firebase auth succeeds
     * <p><b>Then:</b>
     * <ul>
     *   <li>PinManager.isPinSet() called</li>
     *   <li>View.showLoginSuccess(true) called</li>
     * </ul>
     */
    @Test
    public void testSuccessfulLogin_ShowsSuccess() {
        Task<AuthResult> mockTask = createMockTask(true, null);
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask);

        presenter.handleLogin("test@example.com", "password123");

        verify(mockPinManager).isPinSet();
        verify(mockView).showLoginSuccess(true);
    }

    /**
     * Verifies Firebase invalid credential error handling.
     * <p><b>Given:</b> ERROR_INVALID_CREDENTIAL error code
     * <p><b>When:</b> Auth fails
     * <p><b>Then:</b> View shows user-friendly error message
     */
    @Test
    public void testFirebaseAuthException_ShowsSpecificError() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_INVALID_CREDENTIAL");

        Task<AuthResult> mockTask = createMockTask(false, exception);
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask);

        presenter.handleLogin("test@example.com", "wrongpassword");

        verify(mockView).showLoginError("The email or password you entered is incorrect");
    }

    /**
     * Verifies generic exception handling.
     * <p><b>Given:</b> Non-Firebase exception
     * <p><b>When:</b> Auth fails
     * <p><b>Then:</b> View shows generic error message
     */
    @Test
    public void testGenericException_ShowsDefaultError() {
        Exception genericException = new Exception("Something went wrong");

        Task<AuthResult> mockTask = createMockTask(false, genericException);
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask);

        presenter.handleLogin("test@example.com", "password123");

        verify(mockView).showLoginError("Failed to sign in");
    }

    /**
     * Verifies disabled account error handling.
     * <p><b>Given:</b> ERROR_USER_DISABLED error code
     * <p><b>When:</b> Auth fails
     * <p><b>Then:</b> View shows account disabled message
     */
    @Test
    public void testFirebaseAuthException_UserDisabled_ShowsSpecificError() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_USER_DISABLED");

        Task<AuthResult> mockTask = createMockTask(false, exception);
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask);

        presenter.handleLogin("test@example.com", "password123");

        verify(mockView).showLoginError("This email has been disabled. Please contact support");
    }

    /**
     * Verifies rate limiting error handling.
     * <p><b>Given:</b> ERROR_TOO_MANY_REQUESTS error code
     * <p><b>When:</b> Auth fails
     * <p><b>Then:</b> View shows rate limit message
     */
    @Test
    public void testFirebaseAuthException_TooManyRequests_ShowsSpecificError() {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getErrorCode()).thenReturn("ERROR_TOO_MANY_REQUESTS");

        Task<AuthResult> mockTask = createMockTask(false, exception);
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask);

        presenter.handleLogin("test@example.com", "password123");

        verify(mockView).showLoginError("Too many attempts. Please try again later");
    }
}
