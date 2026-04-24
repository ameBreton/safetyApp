package com.example.project666.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project666.R;
import com.example.project666.model.Medication;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private List<Medication> medications;
    private OnMedicationClickListener listener;

    public MedicationAdapter(List<Medication> medications, OnMedicationClickListener listener) {
        this.medications = medications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        holder.bind(medications.get(position));
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, dosageText;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tv_medication_name);
            dosageText = itemView.findViewById(R.id.tv_medication_dosage);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onMedicationClick(medications.get(pos));
                }
            });
        }

        void bind(Medication medication) {
            nameText.setText(medication.name);
            dosageText.setText(medication.dosage);
        }
    }

    public interface OnMedicationClickListener {
        void onMedicationClick(Medication medication);
    }
}