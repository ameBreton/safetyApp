package com.example.project666;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The adapter for displaying Reminder items in a RecyclerView,
 * with changes with toggleable delete and edit modes
 */

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    private List<Reminder> reminders;
    public boolean isDeleteMode = false;
    public boolean isEditMode = false;
    private OnDeleteClickListener deleteListener;
    private OnEditClickListener editListener;


    /**
     * Listener for delete button clicks within the adapter
     */
    public interface OnDeleteClickListener {
        void onDelete(Reminder reminder, int position);
    }

    /**
     * Listener for edit button clicks within the adapter
     */
    public interface OnEditClickListener {
        void onEdit(Reminder reminder, int position);
    }

    public ReminderAdapter(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    /**
     * Registers a callback to be invoked when a delete‐button is clicked
     *
     * @param l listener for delete clicks
     */
    public void setOnDeleteClickListener(OnDeleteClickListener l) {
        this.deleteListener = l;
    }

    /**
     * Registers a callback to be invoked when an edit‐button is clicked
     *
     * @param l listener for edit clicks
     */
    public void setOnEditClickListener(OnEditClickListener l) {
        this.editListener = l;
    }

    /**
     * Enables or disables delete mode and refreshes the list view
     *
     * @param enabled true to show delete buttons
     */
    public void setDeleteMode(boolean enabled) {
        isDeleteMode = enabled;
        notifyDataSetChanged(); // Refresh all items
    }

    /**
     * Enables or disables edit mode (and disables delete mode) and refreshes the list view
     *
     * @param enabled true to show edit buttons
     */
    public void setEditMode(boolean enabled) {
        isEditMode = enabled;
        isDeleteMode = false;  // Ensure only one mode is active
        notifyDataSetChanged();
    }
    /**
     * Returns whether delete mode is currently active
     *
     * @return true if in delete mode
     */
    public boolean getDeleteMode() {
        return isDeleteMode;
    }

    /**
     * Returns whether edit mode is currently active
     *
     * @return true if in edit mode
     */
    public boolean isEditMode() {
        return isEditMode;
    }

    /**
     * Inflates the layout and creates a new ViewHolder
     *
     * @param viewGroup the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new ViewHolder instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_reminder, viewGroup, false);
        return new ViewHolder(itemView);
    }

    /**
     * Binds a Reminder’s data into the given ViewHolder
     *
     * @param viewHolder the ViewHolder to bind into
     * @param position its position in the list
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Reminder r = reminders.get(position);
        viewHolder.tvTitle.setText(r.getTitle());
        viewHolder.tvDesc.setText(r.getDescription());

        //Delete button
        viewHolder.btnDelete.setVisibility(isDeleteMode ? View.VISIBLE : View.GONE); // Show/hide button
        //Edit
        viewHolder.btnEdit.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        viewHolder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(reminders.get(position), position);
            }
        });
        // Edit Button Click Handler
        viewHolder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(reminders.get(position), position);
            }
        });

    }
    /**
     * Returns the total number of reminders
     *
     * @return size of the reminders list
     */
    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public View btnDelete;
        TextView tvTitle;
        TextView tvDesc;
        public Button btnEdit;

        /**
         * Holds item’s title, description, delete and edit buttons.
         *
         * @param itemView the inflated item view
         */
        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem); //Connect to delete button
            btnEdit = itemView.findViewById(R.id.btnEditItem);//connect to edit button

        }

    }

}
