package com.example.project666.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project666.Question;
import com.example.project666.R;
import com.example.project666.TipProducer;

import java.util.List;

/**
 * Adapter class for displaying a list of personalized safety plan tips
 * based on the user's answers to safety questions.
 * <p>
 * This adapter binds {@link Question} objects to RecyclerView items
 * and uses {@link TipProducer} to generate a custom tip for each question.
 * <p>
 * Layout used: R.layout.item_plan
 *
 * @author Dylan Chen
 *
 * @version 1.0
 */
public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    /** The list of questions used to generate tips. */
    private List<Question> questions;

    /**
     * Constructs a PlanAdapter with a list of questions.
     *
     * @param questions List of {@link Question} objects containing user responses and tip logic
     */
    public PlanAdapter(List<Question> questions) {
        this.questions = questions;
    }

    /**
     * Inflates the item layout and creates a ViewHolder for it.
     *
     * @param parent The parent view group
     * @param viewType The view type (unused here)
     * @return A new {@link PlanViewHolder} instance
     */
    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    /**
     * Binds the Question data to the ViewHolder at the given position.
     *
     * @param holder The holder to bind data to
     * @param position The position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.bind(question, questions);
    }

    /**
     * Returns the number of items in the question list.
     *
     * @return Number of items to display
     */
    @Override
    public int getItemCount() {
        return questions.size();
    }

    /**
     * ViewHolder class for each tip item in the RecyclerView.
     */
    static class PlanViewHolder extends RecyclerView.ViewHolder {

        /** TextView that displays the generated tip. */
        private TextView tipText;

        /**
         * Constructor that initializes the tip TextView.
         *
         * @param itemView The item view inflated from layout
         */
        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tipText = itemView.findViewById(R.id.tip_text);
        }

        /**
         * Binds a {@link Question} object to this view by generating and displaying the tip.
         *
         * @param question The current question item
         * @param questionstot The full list of questions (used by TipProducer for placeholder replacement)
         */
        public void bind(Question question, List<Question> questionstot) {
            tipText.setText(TipProducer.generateTip(question, questionstot));
        }
    }

}
