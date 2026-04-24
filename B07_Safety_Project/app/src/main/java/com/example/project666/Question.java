package com.example.project666;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single question in the safety planning app.
 * <p>
 * This class holds all the necessary data for rendering and processing a question,
 * including its type, options, section label, user responses, and tips.
 * <p>
 * Used for: Displaying, saving question data and generate further plan.
 * Serializable is implemented to allow passing Question objects between activities using intents.
 *
 * @author Dylan Chen
 *
 * @version 1.0
 */

public class Question implements Serializable {

    public static final int SINGLE = 0; // Single choice
    public static final int MULTI = 1; // Multiple choice
    public static final int TEXT = 2; // Free text input
    public static final int TIME = 3; // Time input
    public static final int SELECT = 4; // Dropdown selector
    public static final int SINTEXT = 5; // Single choice with text input

    private int id; // Unique identifier for the question
    private String section;  // The section of this question
    private String questionText; // The displayed question text
    private int type; // The answering type of question
    private List<String> options; // List of selectable options
    private String userAnswer; // The user's single answer
    private List<String> userAnswers; // Only for multiple choice questions
    private List<String> tips; // A list of tips or suggestions associated with the answer order
    private String additional; // additional text for sintext question

    public Question() {

    }

    /**
     * Constructs a Question object with full attributes.
     *
     * @param id            Unique identifier
     * @param section       Section label
     * @param questionText  The question text
     * @param type          Type of question
     * @param options       List of options for selection
     * @param tips          List of safety tips related to the answer
     * @param additional    additional text for sintext question
     */

    public Question(int id, String section, String questionText, int type, List<String> options, List<String> tips, String additional) {
         this.id = id;
         this.section = section;
         this.questionText = questionText;
         this.type = type;
         this.options = options;
         this.tips = tips;
         this.additional = additional;
        userAnswers = new ArrayList<>();
    }

    // ----------- Getters -----------

    public int getId() { return id; }
    public String getSection() { return section; }
    public String getQuestionText() { return questionText; }
    public int getType() { return type; }
    public List<String> getOptions() { return options; }
    public List<String> getTips() { return tips; }
    public String getUserAnswer() { return userAnswer; }
    public List<String> getUserAnswers() { return userAnswers; }
    public String getAdditional() { return additional; }

    // ----------- Setters -----------

    public void setId(int id) { this.id = id; }
    public void setSection(String section) { this.section = section; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setType(int type) { this.type = type; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setTips(List<String> tips) { this.tips = tips; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
    public void setUserAnswers(List<String> userAnswers) { this.userAnswers = userAnswers; }
    public void setAdditional(String additional) { this.additional = additional; }
}
