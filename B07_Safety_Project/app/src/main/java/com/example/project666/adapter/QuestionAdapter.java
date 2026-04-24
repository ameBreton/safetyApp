package com.example.project666.adapter;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project666.OnAnswerChangeListener;
import com.example.project666.Question;
import com.example.project666.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Adapter for displaying different types of questions in a RecyclerView.
 * <p>
 * This adapter supports multiple question types:
 * <ul>
 *     <li>SINGLE – Single choice (radio buttons)</li>
 *     <li>MULTI – Multiple choice (checkboxes)</li>
 *     <li>TEXT – Free text input</li>
 *     <li>TIME – Date picker input</li>
 *     <li>SELECT – Drop-down selector (spinner)</li>
 *     <li>SINTEXT – Single choice with conditional text input</li>
 * </ul>
 *
 * Each ViewHolder handles its own view type binding.
 *
 * @author Dylan Chen
 *
 * @version 1.0
 */
public class QuestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Question> questionsInAdapter;
    private OnAnswerChangeListener answerChangeListener;

    /**
     * Sets a listener to detect answer changes (e.g., for dynamic question updates).
     *
     * @param listener The listener instance to call when an answer changes.
     */
    public void setOnAnswerChangeListener(OnAnswerChangeListener listener) {
        this.answerChangeListener = listener;
    }

    /**
     * Constructs a new QuestionAdapter with the provided question list.
     *
     * @param questionsInAdapter List of Question objects to render.
     */
    public QuestionAdapter(List<Question> questionsInAdapter) {
        this.questionsInAdapter = questionsInAdapter;
    }

    /**
     * Returns the type of view for the item at position.
     *
     * @param position the position of wanted question
     */
    @Override
    public int getItemViewType(int position) {
        return questionsInAdapter.get(position).getType();
    }

    /**
     * Inflates and returns the appropriate ViewHolder for each question type.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Question.SINGLE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_single, parent, false);
            return new SingleChoiceViewHolder(view, answerChangeListener);
        } else if (viewType == Question.MULTI) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_multi, parent, false);
            return new MultiChoiceViewHolder(view);
        } else if (viewType == Question.TEXT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_text, parent, false);
            return new TEXTViewHolder(view);
        } else if (viewType == Question.TIME) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_time, parent, false);
            return new TIMEViewHolder(view);
        } else if (viewType == Question.SELECT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_select, parent, false);
            return new SelectViewHolder(view);
        } else if (viewType == Question.SINTEXT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_sintext, parent, false);
            return new SintextViewHolder(view);
        }
        throw new IllegalArgumentException("Unknown viewType: " + viewType);
    }

    /**
     * Binds the data to the ViewHolder based on question type.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Question question = questionsInAdapter.get(position);
        if (holder instanceof SingleChoiceViewHolder) {
            ((SingleChoiceViewHolder) holder).listener = answerChangeListener;
            ((SingleChoiceViewHolder) holder).bind(question);
        } else if (holder instanceof MultiChoiceViewHolder) {
            ((MultiChoiceViewHolder) holder).bind(question);
        } else if (holder instanceof TEXTViewHolder) {
            ((TEXTViewHolder) holder).bind(question);
        } else if (holder instanceof TIMEViewHolder) {
            ((TIMEViewHolder) holder).bind(question);
        }else if (holder instanceof SelectViewHolder) {
            ((SelectViewHolder) holder).bind(question);
        }else if (holder instanceof SintextViewHolder) {
            ((SintextViewHolder) holder).bind(question);
        }
    }

    /**
     * Returns the total number of questions to display.
     */
    @Override
    public int getItemCount() {
        return questionsInAdapter.size();
    }

    /**
     * ViewHolder for single choice (radio buttons) questions.
     */
    public static class SingleChoiceViewHolder extends RecyclerView.ViewHolder{
        TextView text;
        RadioGroup options;
        OnAnswerChangeListener listener;

        public SingleChoiceViewHolder(@NonNull View itemView, OnAnswerChangeListener listener) {
            super(itemView);
            text = itemView.findViewById(R.id.question_text);
            options = itemView.findViewById(R.id.radio_group);
            this.listener = listener;
        }

        public void bind(Question question) {
            text.setText(question.getQuestionText());
            options.setOnCheckedChangeListener(null);
            options.removeAllViews();

            List<String> choices = question.getOptions();
            for (int i = 0; i < choices.size(); i++) {
                String option = choices.get(i);

                RadioButton radioButton = new RadioButton(itemView.getContext());
                radioButton.setText(option);
                radioButton.setId(View.generateViewId());
                radioButton.setTag(i);
                radioButton.setTextSize(20);
                radioButton.setTextColor(ContextCompat.getColor(radioButton.getContext(), R.color.question_text_color));
                radioButton.setChecked(false);

                if (option.equals(question.getUserAnswer())) {
                    radioButton.setChecked(true);
                }

                options.addView(radioButton);
            }
            options.setOnCheckedChangeListener((group, checkedId) -> {
                View radioButton = group.findViewById(checkedId);
                if (radioButton != null && radioButton.getTag() != null) {
                    int index = (int) radioButton.getTag();

                    if (index >= 0 && index < choices.size()) {
                        String selected = choices.get(index);
                        question.setUserAnswer(selected);

                        if (question.getId() == 1 && listener != null) {
                            listener.onAnswerChanged();
                        }
                    }
                }
            });
        }
    }

    /**
     * ViewHolder for multiple choice (checkbox) questions.
     */
    public static class MultiChoiceViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        LinearLayout optionsContainer;
        public MultiChoiceViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.question_text);
            optionsContainer = itemView.findViewById(R.id.checkbox_container);
        }

        public void bind(Question question) {
            text.setText(question.getQuestionText());
            optionsContainer.removeAllViews();
            List<String> existingAnswers;

            if (question.getUserAnswers() == null) {
                existingAnswers = new ArrayList<>();
            }else {
                existingAnswers = question.getUserAnswers();
            }

            for (String option : question.getOptions()) {
                CheckBox checkBox = new CheckBox(itemView.getContext());
                checkBox.setText(option);

                checkBox.setChecked(existingAnswers.contains(option));

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!existingAnswers.contains(option)) {
                            existingAnswers.add(option);
                        }
                    } else {
                        existingAnswers.remove(option);
                    }
                });
                checkBox.setTextColor(ContextCompat.getColor(checkBox.getContext(), R.color.question_text_color));
                checkBox.setTextSize(20);

                optionsContainer.addView(checkBox);
            }

            question.setUserAnswers(existingAnswers);
        }
    }

    /**
     * ViewHolder for plain text input questions.
     */
    public static class TEXTViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        EditText answer;
        public TEXTViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.question_text);
            answer = itemView.findViewById(R.id.answer_edit);
        }

        public void bind(Question question) {
            text.setText(question.getQuestionText());

            answer.setText(question.getUserAnswer() != null ? question.getUserAnswer() : "");

            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    question.setUserAnswer(s.toString());
                }
            };
            answer.addTextChangedListener(watcher);

            answer.setTag(watcher);
        }
    }

    /**
     * ViewHolder for date input using DatePickerDialog.
     */
    public static class TIMEViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        EditText answer;
        public TIMEViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.question_text);
            answer = itemView.findViewById(R.id.answer_time);
        }

        public void bind(Question question) {
            text.setText(question.getQuestionText());

            if (answer.getTag() instanceof TextWatcher) {
                answer.removeTextChangedListener((TextWatcher) answer.getTag());
            }

            answer.setText(question.getUserAnswer() != null ? question.getUserAnswer() : "");

            answer.setOnClickListener(v -> {
                Context context = itemView.getContext();

                final Calendar calendar = Calendar.getInstance();

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        context,
                        (view, year, month, dayOfMonth) -> {
                            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            answer.setText(selectedDate);
                            question.setUserAnswer(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                datePickerDialog.show();
            });
        }
    }

    /**
     * ViewHolder for drop-down selection (spinner) questions.
     */
    public static class SelectViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        Spinner spinner;

        public SelectViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.question_text);
            spinner = itemView.findViewById(R.id.spinner);
        }

        public void bind(Question question) {
            text.setText(question.getQuestionText());

            Context context = itemView.getContext();
            List<String> options = question.getOptions();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);


            String answer = question.getUserAnswer();
            if (answer != null) {
                int index = options.indexOf(answer);
                if (index >= 0) spinner.setSelection(index);
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                boolean firstSelection = true;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (firstSelection) {
                        firstSelection = false;
                        TextView selectedText = (TextView) view;
                        selectedText.setTextColor(ContextCompat.getColor(context, R.color.question_text_color));
                        return;
                    }

                    String selectedOption = options.get(position);
                    question.setUserAnswer(selectedOption);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    /**
     * ViewHolder for single choice questions with conditional text input.
     * <p>
     * "Yes" reveals a text field; "No" does not.
     */
    public static class SintextViewHolder extends RecyclerView.ViewHolder{
        TextView questionText;
        RadioGroup radioGroup;
        RadioButton yesButton, noButton;
        TextView additionalText;
        EditText answerEdit;

        public SintextViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.question_text);
            radioGroup = itemView.findViewById(R.id.radio_group);
            additionalText = itemView.findViewById(R.id.additional_text);
            answerEdit = itemView.findViewById(R.id.answer_edit);

            yesButton = new RadioButton(itemView.getContext());
            yesButton.setText("Yes");
            yesButton.setTextSize(20);
            yesButton.setTextColor(ContextCompat.getColor(yesButton.getContext(), R.color.question_text_color));
            yesButton.setId(View.generateViewId());

            noButton = new RadioButton(itemView.getContext());
            noButton.setText("No");
            noButton.setTextSize(20);
            noButton.setId(View.generateViewId());
            noButton.setTextColor(ContextCompat.getColor(noButton.getContext(), R.color.question_text_color));

            radioGroup.removeAllViews();
            radioGroup.addView(yesButton);
            radioGroup.addView(noButton);
        }

        public void bind(Question question) {
            questionText.setText(question.getQuestionText());
            String answer = question.getUserAnswer();

            radioGroup.setOnCheckedChangeListener(null);
            answerEdit.setText("");
            additionalText.setText(question.getAdditional());
            additionalText.setVisibility(View.GONE);
            answerEdit.setVisibility(View.GONE);
            radioGroup.clearCheck();

            if (answer != null) {
                if (answer.equalsIgnoreCase("no")) {
                    noButton.setChecked(true);
                } else {
                    yesButton.setChecked(true);
                    additionalText.setVisibility(View.VISIBLE);
                    answerEdit.setVisibility(View.VISIBLE);
                    answerEdit.setText(answer);
                }
            }

            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == yesButton.getId()) {
                    additionalText.setVisibility(View.VISIBLE);
                    answerEdit.setVisibility(View.VISIBLE);
                    question.setUserAnswer(answerEdit.getText().toString());
                } else if (checkedId == noButton.getId()) {
                    additionalText.setVisibility(View.GONE);
                    answerEdit.setVisibility(View.GONE);
                    question.setUserAnswer("no");
                }
            });

            answerEdit.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (yesButton.isChecked()) {
                        question.setUserAnswer(s.toString());
                    }
                }
            });
        }
    }

}
