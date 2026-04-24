package com.example.project666;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.reflect.TypeToken;

/**
 * The class for loading question data from a JSON file in the raw resources.
 * <p>
 * This class reads the `question.json` file stored in the `res/raw` directory,
 * parses it using Gson, and returns a list of {@link Question} objects.
 * <p>
 * Usage:
 * <pre>
 *     List&lt;Question&gt; questions = QuestionLoader.loadQuestionsFromRaw(context);
 * </pre>
 *
 * This loader supports the static method {@code loadQuestionsFromRaw} and handles I/O exceptions gracefully.
 *
 * @author Dylan Chen
 *
 * @version 1.0
 */
public class QuestionLoader {

    /**
     * Loads a list of Question objects from the raw JSON resource file.
     *
     * @param context The application context used to access resources.
     * @return A list of parsed {@link Question} objects, or {@code null} if loading fails.
     */
    public static List<Question> loadQuestionsFromRaw(Context context) {
        try {
            // Open the raw resource file containing the questions
            InputStream is = context.getResources().openRawResource(R.raw.question);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the byte array to a UTF-8 encoded JSON string
            String json = new String(buffer, "UTF-8");

            // Parse the JSON string into a List<Question> using Gson
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Question>>() {}.getType();
            return gson.fromJson(json, listType);

        } catch (IOException e) {
            // Log the error and return null if an exception occurs
            e.printStackTrace();
            Log.e("QuestionLoader", "Failed to load questions JSON", e);
            return null;
        }
    }
}
