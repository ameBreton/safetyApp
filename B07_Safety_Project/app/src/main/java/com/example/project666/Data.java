package com.example.project666;

import android.os.Bundle;
import android.view.View;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;

import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project666.adapter.*;
import com.example.project666.model.*;
import androidx.activity.OnBackPressedCallback;

//firebase classes
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.os.Environment;
import androidx.core.content.FileProvider;
import android.content.ActivityNotFoundException;
import android.webkit.MimeTypeMap;


import java.util.ArrayList;
import java.util.List;

/**
 * Main data management activity that handles displaying and managing user's safety plan components.
 * This includes contacts, documents, safe locations, and medications. Implements click listeners
 * for RecyclerView items and handles all Firebase database operations for CRUD functionality.
 *
 * <p>Key features:
 * <ul>
 *   <li>Displays all safety plan items in categorized RecyclerViews</li>
 *   <li>Provides add/edit/delete functionality for all item types</li>
 *   <li>Handles document uploads and viewing</li>
 *   <li>Manages Firebase Realtime Database and Firestore operations</li>
 * </ul>
 *
 * @extends NavBarActivity to inherit navigation functionality
 * @implements ContactAdapter.OnContactClickListener for contact item clicks
 * @implements DocumentAdapter.OnDocumentClickListener for document item clicks
 * @implements LocationAdapter.OnLocationClickListener for location item clicks
 * @implements MedicationAdapter.OnMedicationClickListener for medication item clicks
 */
public class Data extends NavBarActivity implements
        ContactAdapter.OnContactClickListener,
        DocumentAdapter.OnDocumentClickListener,
        LocationAdapter.OnLocationClickListener,
        MedicationAdapter.OnMedicationClickListener {


    // RecyclerViews
    private RecyclerView contactsRecycler, documentsRecycler, locationsRecycler, medsRecycler;

    // Adapters
    private ContactAdapter contactAdapter;
    private DocumentAdapter documentAdapter;
    private LocationAdapter locationAdapter;
    private MedicationAdapter medicationAdapter;

    // Data Lists
    private List<Contact> contactList = new ArrayList<>();
    private List<DocumentItem> documentList = new ArrayList<>();
    private List<SafeLocation> locationList = new ArrayList<>();
    private List<Medication> medicationList = new ArrayList<>();

    //firebase
    private DatabaseReference userDataRef;

    // for blobs
    private static final int PICK_DOCUMENT_REQUEST = 101;
    private Uri selectedDocumentUri;
    private EditText titleInput;

    // Firebase instances
    private FirebaseFirestore db;
    private CollectionReference documentsRef;

    /**
     * Initializes the activity, sets up UI components, and configures Firebase connections.
     * Performs the following key operations:
     * <ol>
     *   <li>Sets up edge-to-edge display</li>
     *   <li>Verifies user authentication</li>
     *   <li>Initializes Firebase Database and Firestore references</li>
     *   <li>Configures all RecyclerViews and adapters</li>
     *   <li>Loads initial data from Firebase</li>
     *   <li>Sets up button click listeners</li>
     * </ol>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            finish(); // or redirect to login
            return;
        }
        db = FirebaseFirestore.getInstance();
        userDataRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        if (currentUser != null) {
            documentsRef = db.collection("users").document(currentUser.getUid())
                    .collection("documents");
        }

        // Edge-to-edge insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup all RecyclerViews
        setupContactsRecycler();
        setupDocumentsRecycler();
        setupLocationsRecycler();
        setupMedicationsRecycler();

        initializeTestData();

        // New back handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        //for the add buttons
        setupButtonListeners();

    }

    /**
     * Loads initial test data for all categories by calling respective load methods.
     * This serves as the entry point for populating all RecyclerViews with data.
     */
    private void initializeTestData() {
        loadContacts();
        loadDocuments();
        loadLocations();
        loadMedications();
    }

    /**
     * Fetches and loads contact data from Firebase Realtime Database.
     * Updates the contact list and notifies the adapter when data changes.
     * Displays a toast message if the operation fails.
     */
    private void loadContacts() {
        userDataRef.child("contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot contactSnap : snapshot.getChildren()) {
                    Contact contact = contactSnap.getValue(Contact.class);
                    contact.setKey(contactSnap.getKey());
                    contactList.add(contact);
                }
                contactAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Data.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches and loads documents from Firestore database.
     * Handles both successful and failed loading scenarios.
     * Requires initialized documentsRef to work properly.
     */
    private void loadDocuments() {
        if (documentsRef == null) {
            Toast.makeText(this, "Firestore not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        documentsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    documentList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DocumentItem doc = document.toObject(DocumentItem.class);
                        doc.setKey(document.getId());
                        documentList.add(doc);
                    }
                    documentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Data.this, "Failed to load documents", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads safe locations from Firebase Realtime Database.
     * Clears existing data and repopulates the list from the database.
     * Notifies the adapter when loading is complete.
     */
    private void loadLocations() {
        userDataRef.child("locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locationList.clear();
                for (DataSnapshot locSnap : snapshot.getChildren()) {
                    SafeLocation location = locSnap.getValue(SafeLocation.class);
                    location.setKey(locSnap.getKey());
                    locationList.add(location);
                }
                locationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Data.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads medications from Firebase Realtime Database.
     * Updates the medication list and refreshes the RecyclerView.
     * Shows error message if the operation fails.
     */
    private void loadMedications() {
        userDataRef.child("medications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicationList.clear();
                for (DataSnapshot medSnap : snapshot.getChildren()) {
                    Medication medication = medSnap.getValue(Medication.class);
                    medication.setKey(medSnap.getKey());
                    medicationList.add(medication);
                }
                medicationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Data.this, "Failed to load medications", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // ====================== RecyclerView Setup ======================
    /**
     * Initializes the Contacts RecyclerView with a linear layout manager and adapter.
     * Binds the adapter to the contact list and sets it on the RecyclerView.
     */
    private void setupContactsRecycler() {
        contactsRecycler = findViewById(R.id.contactsRecycler);
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactList, this);
        contactsRecycler.setAdapter(contactAdapter);
    }

    /**
     * Configures the Documents RecyclerView with necessary components.
     * Sets up layout manager and adapter for displaying document items.
     */
    private void setupDocumentsRecycler() {
        documentsRecycler = findViewById(R.id.documentsRecycler);
        documentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        documentAdapter = new DocumentAdapter(documentList, this);
        documentsRecycler.setAdapter(documentAdapter);
    }

    /**
     * Prepares the Locations RecyclerView for displaying safe locations.
     * Initializes adapter with location data and click listener.
     */
    private void setupLocationsRecycler() {
        locationsRecycler = findViewById(R.id.locationsRecycler);
        locationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        locationAdapter = new LocationAdapter(locationList, this);
        locationsRecycler.setAdapter(locationAdapter);
    }

    /**
     * Sets up the Medications RecyclerView with adapter and layout manager.
     * Connects the medication list data to the UI component.
     */
    private void setupMedicationsRecycler() {
        medsRecycler = findViewById(R.id.medsRecycler);
        medsRecycler.setLayoutManager(new LinearLayoutManager(this));
        medicationAdapter = new MedicationAdapter(medicationList, this);
        medsRecycler.setAdapter(medicationAdapter);
    }


    // ====================== Click Handlers ======================
    /**
     * Called when a contact item is clicked. Allows the user to edit or delete the contact.
     *
     * @param contact The Contact object clicked by the user.
     */
    @Override
    public void onContactClick(Contact contact) {
        showEditDialog(contact, "contact");
    }

    /**
     * Handles click events on document items. Presents options to view or edit the document.
     *
     * @param document The DocumentItem that was clicked
     */
    @Override
    public void onDocumentClick(DocumentItem document) {
        // Show options to view/edit
        new AlertDialog.Builder(this)
                .setTitle(document.title)
                .setItems(new String[]{"View", "Edit"}, (dialog, which) -> {
                    if (which == 0) {
                        viewDocument(document);
                    } else {
                        showEditDialog(document, "document");
                    }
                })
                .show();
    }

    /**
     * Handles long click events on document items. Currently shows a simple toast notification.
     *
     * @param document The DocumentItem that was long-clicked
     */
    @Override
    public void onDocumentLongClick(DocumentItem document) {
        Toast.makeText(this, "Long clicked: " + document.title, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles click events on location items. Shows edit dialog for the selected location.
     *
     * @param location The SafeLocation that was clicked
     */
    @Override
    public void onLocationClick(SafeLocation location) {
        showEditDialog(location, "location");
    }

    /**
     * Handles click events on medication items. Shows edit dialog for the selected medication.
     *
     * @param medication The Medication that was clicked
     */
    @Override
    public void onMedicationClick(Medication medication) {
        showEditDialog(medication, "medication");
    }

    /**
     * Displays an appropriate edit dialog based on the item type.
     * Supports editing contacts, documents, locations, and medications.
     *
     * @param item The item to be edited (Contact, DocumentItem, SafeLocation, or Medication)
     * @param type The type of item ("contact", "document", "location", or "medication")
     */
    private void showEditDialog(Object item, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView;

        switch (type) {
            case "contact":
                Contact contact = (Contact) item;
                dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);

                EditText etName = dialogView.findViewById(R.id.et_contact_name);
                EditText etRelationship = dialogView.findViewById(R.id.et_relationship);
                EditText etPhone = dialogView.findViewById(R.id.et_phone);

                etName.setText(contact.name);
                etRelationship.setText(contact.relationship);
                etPhone.setText(contact.phone);

                builder.setTitle("Edit Contact")
                        .setView(dialogView)
                        .setPositiveButton("Save", (d, which) -> {
                            contact.name = etName.getText().toString();
                            contact.relationship = etRelationship.getText().toString();
                            contact.phone = etPhone.getText().toString();
                            userDataRef.child("contacts").child(contact.getKey()).setValue(contact);
                            loadContacts();
                        })
                        .setNegativeButton("Delete", (d, which) -> {
                            userDataRef.child("contacts").child(contact.getKey()).removeValue();
                            contactList.remove(contact);
                            loadContacts();
                        });
                break;

            case "document":
                DocumentItem document = (DocumentItem) item;
                dialogView = getLayoutInflater().inflate(R.layout.dialog_add_document, null);

                EditText docTitle = dialogView.findViewById(R.id.et_document_title);
                docTitle.setText(document.title);

                builder.setTitle("Edit Document")
                        .setView(dialogView)
                        .setPositiveButton("Save", (d, which) -> {
                            document.title = docTitle.getText().toString();
                            documentsRef.document(document.getKey())
                                    .update("title", document.title)
                                    .addOnSuccessListener(aVoid -> loadDocuments());
                        })
                        .setNegativeButton("Delete", (d, which) -> {
                            // Delete local file first
                            File file = new File(document.getLocalFilePath());
                            if (file.exists()) {
                                file.delete();
                            }

                            // Then delete from Firestore
                            documentsRef.document(document.getKey())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> loadDocuments());
                        });
                break;

            case "location":
                SafeLocation location = (SafeLocation) item;
                dialogView = getLayoutInflater().inflate(R.layout.dialog_add_location, null);

                EditText locAddress = dialogView.findViewById(R.id.et_address);
                EditText locNotes = dialogView.findViewById(R.id.et_notes);

                locAddress.setText(location.address);
                locNotes.setText(location.notes);

                builder.setTitle("Edit Location")
                        .setView(dialogView)
                        .setPositiveButton("Save", (d, which) -> {
                            location.address = locAddress.getText().toString();
                            location.notes = locNotes.getText().toString();
                            userDataRef.child("locations").child(location.getKey()).setValue(location);
                            loadLocations();
                        })
                        .setNegativeButton("Delete", (d, which) -> {
                            userDataRef.child("locations").child(location.getKey()).removeValue();
                            locationList.remove(location);
                            loadLocations();
                        });
                break;

            case "medication":
                Medication medication = (Medication) item;
                dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medication, null);

                EditText medName = dialogView.findViewById(R.id.et_med_name);
                EditText medDosage = dialogView.findViewById(R.id.et_dosage);

                medName.setText(medication.name);
                medDosage.setText(medication.dosage);

                builder.setTitle("Edit Medication")
                        .setView(dialogView)
                        .setPositiveButton("Save", (d, which) -> {
                            medication.name = medName.getText().toString();
                            medication.dosage = medDosage.getText().toString();
                            userDataRef.child("medications").child(medication.getKey()).setValue(medication);
                            loadMedications();
                        })
                        .setNegativeButton("Delete", (d, which) -> {
                            userDataRef.child("medications").child(medication.getKey()).removeValue();
                            medicationList.remove(medication);
                            loadMedications();
                        });
                break;

            default:
                return;
        }

        builder.setNeutralButton("Cancel", null).show();
    }


    // ====================== Helper Methods ======================

    /**
     * Sets up click listeners for all add buttons in the activity.
     * Configures handlers for adding contacts, documents, locations, and medications.
     */
    private void setupButtonListeners() {
        // Add Document Button
        findViewById(R.id.addDocument).setOnClickListener(v -> showAddDocumentDialog());

        // Add Contact Button
        findViewById(R.id.addContact).setOnClickListener(v -> showAddContactDialog());

        // Add Location Button
        findViewById(R.id.addLocations).setOnClickListener(v -> showAddLocationDialog());

        // Add Medication Button
        findViewById(R.id.addMed).setOnClickListener(v -> showAddMedicationDialog());
    }

    // ====================== Dialog Methods ======================

    /**
     * Displays a dialog for adding a new document.
     * Allows user to input a title and select a file to upload.
     */
    private void showAddDocumentDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_document, null);
        titleInput = dialogView.findViewById(R.id.et_document_title);

        Button uploadBtn = dialogView.findViewById(R.id.btn_upload_file);
        uploadBtn.setOnClickListener(v -> openFilePicker());

        new AlertDialog.Builder(this)
                .setTitle("Add Document")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    String title = titleInput.getText().toString().trim();
                    if (!title.isEmpty() && selectedDocumentUri != null) {
                        saveDocumentLocally(title);
                    }
                })
                .show();
    }

    /**
     * Saves a selected document to local storage and uploads metadata to Firestore.
     * Handles file operations and Firestore document creation.
     *
     * @param title The title of the document as entered by the user
     */
    private void saveDocumentLocally(String title) {
        try {
            // Get the file from URI
            InputStream inputStream = getContentResolver().openInputStream(selectedDocumentUri);
            File localDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            String fileName = "doc_" + System.currentTimeMillis() + "_" + title;
            File localFile = new File(localDir, fileName);

            // Copy file to app storage
            FileOutputStream outputStream = new FileOutputStream(localFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Save metadata to Firestore
            DocumentItem document = new DocumentItem(title, localFile.getAbsolutePath());
            documentsRef.add(document)
                    .addOnSuccessListener(documentReference -> {
                        document.setKey(documentReference.getId());
                        documentList.add(document);
                        documentAdapter.notifyDataSetChanged();
                    });

        } catch (Exception e) {
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Opens a selected document using an appropriate viewer application.
     * Handles file provider permissions and MIME type detection.
     *
     * @param document The DocumentItem to be viewed
     */
    private void viewDocument(DocumentItem document) {
        File file = new File(document.getLocalFilePath());
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this,
                    "com.example.project666.fileprovider",
                    file);

            intent.setDataAndType(uri, getMimeType(file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No app to view this document", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Determines the MIME type of a file based on its extension.
     *
     * @param file The file whose MIME type needs to be determined
     * @return The MIME type as String, or null if type cannot be determined
     */
    private String getMimeType(File file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    /**
     * Opens the system file picker to allow document selection.
     * Uses PICK_DOCUMENT_REQUEST as the request code.
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select Document"), PICK_DOCUMENT_REQUEST);
    }

    /**
     * Handles activity results from intents like file picker.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult()
     * @param resultCode The integer result code returned by the child activity
     * @param data An Intent that carries the result data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedDocumentUri = data.getData();
            Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Displays a dialog for adding a new emergency contact.
     * Collects name, relationship, and phone number information.
     */
    private void showAddContactDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        EditText nameInput = dialogView.findViewById(R.id.et_contact_name);
        EditText relationshipInput = dialogView.findViewById(R.id.et_relationship);
        EditText phoneInput = dialogView.findViewById(R.id.et_phone);

        new AlertDialog.Builder(this)
                .setTitle("Add Emergency Contact")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    Contact contact = new Contact(
                            nameInput.getText().toString(),
                            relationshipInput.getText().toString(),
                            phoneInput.getText().toString()
                    );
                    String key = userDataRef.child("contacts").push().getKey();
                    userDataRef.child("contacts").child(key).setValue(contact);
                    loadContacts(); // reloads contacts list and updates adapter
                    Toast.makeText(this, "Contact saved to Firebase", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Shows a dialog for adding a new safe location.
     * Collects address and optional notes information.
     */
    private void showAddLocationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_location, null);
        EditText addressInput = dialogView.findViewById(R.id.et_address);
        EditText notesInput = dialogView.findViewById(R.id.et_notes);

        new AlertDialog.Builder(this)
                .setTitle("Add Safe Location")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    String address = addressInput.getText().toString().trim();
                    String notes = notesInput.getText().toString().trim();

                    if (!address.isEmpty()) {
                        String key = userDataRef.child("locations").push().getKey();
                        SafeLocation location = new SafeLocation(address, notes);
                        userDataRef.child("locations").child(key).setValue(location);
                        loadLocations();
                        Toast.makeText(this, "Location saved to Firebase", Toast.LENGTH_SHORT).show();
                    } else {
                        addressInput.setError("Address required");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Displays a dialog for adding a new medication.
     * Collects medication name and dosage information.
     */
    private void showAddMedicationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medication, null);
        EditText nameInput = dialogView.findViewById(R.id.et_med_name);
        EditText dosageInput = dialogView.findViewById(R.id.et_dosage);

        new AlertDialog.Builder(this)
                .setTitle("Add Medication")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String dosage = dosageInput.getText().toString().trim();

                    if (!name.isEmpty()) {
                        String key = userDataRef.child("medications").push().getKey();
                        userDataRef.child("medications").child(key).setValue(new Medication(name, dosage));
                        loadMedications();
                        Toast.makeText(this, "Medication saved to Firebase", Toast.LENGTH_SHORT).show();
                    } else {
                        nameInput.setError("Name required");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}