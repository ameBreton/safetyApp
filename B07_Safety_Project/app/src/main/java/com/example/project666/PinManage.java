package com.example.project666;

import android.content.Context;
import android.widget.Toast;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * PinManage is a utility class responsible for securely storing,
 * retrieving, and clearing the user's 4-digit PIN using EncryptedSharedPreferences.
 * <p>
 * It uses Android Jetpack Security's AES256 encryption to protect sensitive data.
 */
public class PinManage {
    private static final String ESP_NAME = "encrypted_prefs";
    private static final String KEY = "user_pin";

    /**
     * Returns the encrypted SharedPreferences instance for secure PIN storage.
     *
     * @param context the application context
     * @return an EncryptedSharedPreferences instance
     * @throws GeneralSecurityException if encryption fails
     * @throws IOException              if reading/writing fails
     */
    private static EncryptedSharedPreferences getESP(Context context) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

        return (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                context,
                ESP_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /**
     * Saves the given 4-digit PIN securely into EncryptedSharedPreferences.
     *
     * @param context the context to access storage
     * @param pin     the 4-digit PIN to save
     */
    public static void savePin(Context context, String pin) {
        try {
            getESP(context).edit().putString(KEY, pin).apply();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to save PIN, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Retrieves the stored PIN from EncryptedSharedPreferences.
     *
     * @param context the context to access storage
     * @return the stored 4-digit PIN or null if not found
     */
    public static String getPin(Context context) {
        try {
            return getESP(context).getString(KEY, null);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to get PIN, please try again.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Checks whether a PIN has been previously stored.
     *
     * @param context the context to access storage
     * @return true if a PIN exists, false otherwise
     */
    public static boolean isPinSet(Context context){
        return getPin(context) != null;
    }

    /**
     * Clears the stored PIN from EncryptedSharedPreferences.
     *
     * @param context the context to access storage
     */
    public static void clearPin(Context context) {
        try {
            getESP(context).edit().remove(KEY).apply();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to clear PIN, please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}

