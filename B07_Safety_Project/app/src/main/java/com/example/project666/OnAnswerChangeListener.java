package com.example.project666;

/**
 * A listener interface for receiving callbacks when a user's answer is changed.
 * <p>
 * This interface is typically implemented by UI components or controllers that need to react
 * to user input changes in a {@link Question} (e.g., to update UI, enable a button, or save state).
 * <p>
 * Example usage:
 * <pre>
 *       questionView.setOnAnswerChangeListener(() -> updateNextButtonState());
 * </pre>
 *
 * @author Dylan Chen
 *
 * @version 1.0
 */
public interface OnAnswerChangeListener {
    void onAnswerChanged();
}