package com.example.project666.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project666.R;
import com.example.project666.model.Contact;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contacts;
    private OnContactClickListener listener; // For click handling

    // Constructor
    public ContactAdapter(List<Contact> contacts, OnContactClickListener listener) {
        this.contacts = contacts;
        this.listener = listener;
    }

    // ViewHolder creation
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    // Bind data to views
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    // ViewHolder class
    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, relationshipTextView, phoneTextView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_contact_name);
            relationshipTextView = itemView.findViewById(R.id.tv_contact_relationship);
            phoneTextView = itemView.findViewById(R.id.tv_contact_phone);

            // Click listener for the entire item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onContactClick(contacts.get(position));
                }
            });
        }

        public void bind(Contact contact) {
            nameTextView.setText(contact.name);
            relationshipTextView.setText(contact.relationship);
            phoneTextView.setText(contact.phone);
        }
    }

    // Click interface
    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }
}