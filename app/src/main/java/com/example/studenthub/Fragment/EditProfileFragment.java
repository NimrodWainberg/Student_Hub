package com.example.studenthub.Fragment;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.studenthub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;

public class EditProfileFragment extends Fragment {
    ImageView closeIv, profilePicture;
    TextView saveBtn;
    TextInputEditText fullName, username, bio;
    FirebaseUser firebaseUser;
    FloatingActionButton camera, gallery;
    ActivityResultLauncher<Uri> cameraResultLauncher;
    ActivityResultLauncher<String> galleryResultLauncher;
    Uri uri;
    File picFile;
    UploadTask uploadTask;
    StorageReference storageRef, fileReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit_profile, container, false);

        initViews(view);
        initListeners();
        initLaunchers();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        final Query query = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Getting the details of specific user
                fullName.setText(snapshot.child("fullName").getValue(String.class));
                username.setText(snapshot.child("username").getValue(String.class));
                bio.setText(snapshot.child("bio").getValue(String.class));
                Glide.with(getContext()).load(snapshot.child("imageUrl")
                        .getValue(String.class)).into(profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        closeIv.setOnClickListener(view1 -> {
            if(requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                requireActivity().getSupportFragmentManager().popBackStack();
        });

        saveBtn.setOnClickListener(view2 -> {
            updateProfile(Objects.requireNonNull(fullName.getText()).toString(),
                    Objects.requireNonNull(username.getText()).toString(),
                    Objects.requireNonNull(bio.getText()).toString());
            if(requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    /**
     * A function that initializes the listeners
     */
    private void initListeners() {
        gallery.setOnClickListener(v -> galleryResultLauncher.launch("image/*"));

        camera.setOnClickListener(v -> {
            picFile = new File(getContext() // Creating a new file to insert the URI into
                    .getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "Photo" + System.currentTimeMillis() + ".jpg");
            uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", picFile);

            cameraResultLauncher.launch(uri);
        });
    }

    /**
     * A function that initializes the launchers
     */
    private void initLaunchers(){
        cameraResultLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> { // True if image saved into given URI
            if(result){
                Glide.with(getContext()).load(picFile.getAbsoluteFile()).into(profilePicture);

                uploadPictureToStorage();
            }
        });

        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            uri = result;
            Glide.with(getContext()).load(result).into(profilePicture);

            uploadPictureToStorage();
        });
    }

    /**
     * A function that initializes the views
     */
    private void initViews(View view) {
        closeIv = view.findViewById(R.id.close);
        saveBtn = view.findViewById(R.id.save);
        profilePicture = view.findViewById(R.id.edit_profile_profile_picture);
        camera = view.findViewById(R.id.add_picture_camera);
        gallery = view.findViewById(R.id.add_picture_gallery);
        fullName = view.findViewById(R.id.edit_profile_fullname);
        username = view.findViewById(R.id.edit_profile_username_edittext);
        bio = view.findViewById(R.id.edit_profile_bio_edittext);
    }

    /**
     * A function that updates user's profile details on DB
     * @param fullName full name to be updated
     * @param username username to be updated
     * @param bio bio to be updated
     */
    private void updateProfile(String fullName, String username, String bio) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());

        HashMap<String, Object> map = new HashMap<>();
        map.put("fullName", fullName);
        map.put("username", username);
        map.put("bio", bio);

        reference.updateChildren(map);
        Toast.makeText(getContext(), R.string.changes_saved, Toast.LENGTH_SHORT).show();
    }

    /**
     * A function that gets File type
     * @param uri uri to be checked
     * @return String of file type
     */
    private String getFileType(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * A function that uploads a new profile picture and updates the DB.
     */
    private void uploadPictureToStorage() {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage(getString(R.string.uploading));
        pd.show();
        if (uri != null) {
            // Creating file name according to System's version
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // if picture taken
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/ddHH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                fileReference = storageRef.child("Picture" + dtf.format(now) + "." + getFileType(uri));
            }
            else {
                fileReference = storageRef.child("Picture" + System.currentTimeMillis()
                        + "." + getFileType(uri));
            }

            uploadTask = fileReference.putFile(uri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadedUri = task.getResult();
                    String updatedUrl = Objects.requireNonNull(downloadedUri).toString();

                    // Getting into user's specific DB section
                    DatabaseReference reference = FirebaseDatabase.getInstance()
                            .getReference("users").child(firebaseUser.getUid());

                    // Creating an hashmap to push into DB
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageUrl", ""+updatedUrl);

                    // Pushing it into DB
                    reference.updateChildren(map);

                    // Close Dialog
                    pd.dismiss();

                } else {
                    pd.dismiss();
                    Toast.makeText(getContext(), R.string.uploading_failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            pd.dismiss();
            Toast.makeText(getContext(), R.string.no_image_selected, Toast.LENGTH_SHORT).show();
        }
    }
}