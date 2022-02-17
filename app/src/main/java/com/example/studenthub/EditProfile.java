package com.example.studenthub;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

public class EditProfile extends AppCompatActivity {
    ImageView closeIv, profilePicture;
    TextView saveBtn, changePictureTv;
    TextInputEditText fullName, username, bio;
    FirebaseUser firebaseUser;
    Uri uri;
    UploadTask uploadTask;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();

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
                Glide.with(getApplicationContext()).load(snapshot.child("imageUrl")
                        .getValue(String.class)).into(profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        closeIv.setOnClickListener(view -> finish());

        saveBtn.setOnClickListener(view -> updateProfile(Objects.requireNonNull(fullName.getText()).toString(),
                        Objects.requireNonNull(username.getText()).toString(),
                        Objects.requireNonNull(bio.getText()).toString()));

        saveBtn.setOnClickListener(view -> {
            updateProfile(Objects.requireNonNull(fullName.getText()).toString(),
                    Objects.requireNonNull(username.getText()).toString(),
                    Objects.requireNonNull(bio.getText()).toString());
            finish();
        });

        changePictureTv.setOnClickListener(view -> CropImage.activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(EditProfile.this));
    }

    private void initViews() {
        closeIv = findViewById(R.id.close);
        saveBtn = findViewById(R.id.save);
        profilePicture = findViewById(R.id.edit_profile_profile_picture);
        changePictureTv = findViewById(R.id.tv_change);
        fullName = findViewById(R.id.edit_profile_fullname);
        username = findViewById(R.id.edit_profile_username_edittext);
        bio = findViewById(R.id.edit_profile_bio_edittext);
    }

    private void updateProfile(String fullName, String username, String bio) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());

        HashMap<String, Object> map = new HashMap<>();
        map.put("fullName", fullName);
        map.put("username", username);
        map.put("bio", bio);

        reference.updateChildren(map);
        Toast.makeText(EditProfile.this, R.string.changes_saved, Toast.LENGTH_SHORT).show();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * A function that uploads a new profile picture and updates the DB.
     */
    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        if (uri != null) {
            // Randomizing file name
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(uri));

            uploadTask = fileReference.putFile(uri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String updatedUrl = Objects.requireNonNull(downloadUri).toString();

                    // Update image url on DataBase
                    DatabaseReference reference = FirebaseDatabase.getInstance()
                            .getReference("users").child(firebaseUser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageurl", ""+updatedUrl);
                    reference.updateChildren(map);

                    pd.dismiss();

                } else {
                    pd.dismiss();
                    Toast.makeText(EditProfile.this, R.string.uploading_failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            pd.dismiss();
            Toast.makeText(EditProfile.this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO change, onActivityResult is deprecated
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            uri = result.getUri();

            uploadImage();

        } else {
            Toast.makeText(this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
        }
    }
}
