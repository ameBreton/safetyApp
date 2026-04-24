package com.example.project666.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project666.R;
import com.example.project666.model.SafeLocation;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<SafeLocation> locations;
    private OnLocationClickListener listener;

    public LocationAdapter(List<SafeLocation> locations, OnLocationClickListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        holder.bind(locations.get(position));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView addressText, notesText;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            addressText = itemView.findViewById(R.id.tv_location_address);
            notesText = itemView.findViewById(R.id.tv_location_notes);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onLocationClick(locations.get(pos));
                }
            });
        }

        void bind(SafeLocation location) {
            addressText.setText(location.address);
            notesText.setText(location.notes);
        }
    }

    public interface OnLocationClickListener {
        void onLocationClick(SafeLocation location);
    }
}