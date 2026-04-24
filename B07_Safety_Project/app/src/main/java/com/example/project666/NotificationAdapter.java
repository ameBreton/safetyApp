package com.example.project666;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying Reminder notifications in a RecyclerView,
 * showing title, description, and a status indicator, with item click support
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{
    private OnItemClickListener clickListener;
    private List<Reminder> items;

    public interface OnItemClickListener {
        /**
         * Listener for click events on a notification item
         * @param reminder the Reminder model that was clicked
         * @param position the adapter position of the clicked item
         */
        void onItemClick(Reminder reminder, int position);
    }

    /**
     * Registers a callback to be invoked when a notification item is clicked
     * @param listener the click listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Constructs the adapter with the Reminder items
     * @param items the list of Reminder models to display
     */
    public NotificationAdapter(List<Reminder> items) {
        this.items = items;
    }

    /**
     * Inflates the notification item layout and creates a new ViewHolder
     * @param parent the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new ViewHolder instance
     */
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a Reminder’s data into the ViewHolder and updates the status indicator
     * and wires up the click listener
     * @param holder the ViewHolder to bind into
     * @param position its position in the adapter
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Reminder r = items.get(position);
        holder.title.setText(r.title);
        holder.desc.setText(r.description);

        // choose the correct circle drawable
        int circleRes;
        if (!r.notificationsEnabled) {
            circleRes = R.drawable.circle_grey;
        } else if (r.useDefaultSettings) {
            circleRes = R.drawable.circle_green;
        } else {
            circleRes = R.drawable.circle_yellow;
        }

        holder.indicator.setVisibility(View.VISIBLE);
        holder.indicator.setBackgroundResource(circleRes);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(items.get(position), position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Holds references to the notification item’s title, description, and status indicator views
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView desc;
        View indicator;

        /**
         * Creates a ViewHolder holding references to the key subviews
         * @param itemView the inflated notification item view
         */
        ViewHolder(View itemView) {
            super(itemView);
            title= itemView.findViewById(R.id.tvItemTitle);
            desc= itemView.findViewById(R.id.tvItemDesc);
            indicator = itemView.findViewById(R.id.viewSelectedIndicator);
        }
    }
}
