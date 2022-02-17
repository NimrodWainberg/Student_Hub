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
import com.example.studenthub.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

public class EditProfile extends AppCompatActivity {

    ImageView close, image_profile;
    TextView saveBtn, changePictureTv;
    TextInputEditText fullName, bio;
    FirebaseUser firebaseUser;
    Uri uri;
    //StorageTask uploadTask;
    UploadTask uploadTask;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close);
        saveBtn = findViewById(R.id.save);
        image_profile = findViewById(R.id.image_profile);
        changePictureTv = findViewById(R.id.tv_change);
        fullName = findViewById(R.id.edit_profile_fullname);
        bio = findViewById(R.id.edit_profile_bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Getting the details of specific user
                User user = snapshot.getValue(User.class);
                fullName.setText(Objects.requireNonNull(user).getFullName());
                bio.setText(user.getBio());
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        close.setOnClickListener(view -> finish());

        saveBtn.setOnClickListener(view ->
                updateProfile(Objects.requireNonNull(fullName.getText()).toString(),
                Objects.requireNonNull(bio.getText()).toString()));

        changePictureTv.setOnClickListener(view -> CropImage.activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(EditProfile.this));

        image_profile.setOnClickListener(view -> CropImage.activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(EditProfile.this));
    }

    private void updateProfile(String fullName, String bio) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());

        HashMap<String, Object> map = new HashMap<>();
        map.put("fullname", fullName);
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
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(uri));

            uploadTask = fileReference.putFile(uri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String updatedUrl = Objects.requireNonNull(downloadUri).toString();

                    // Update image url on DataBase
                    DatabaseReference reference = FirebaseDatabase.getInstance()
                            .getReference("Users").child(firebaseUser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageurl", "" + updatedUrl);
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
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}
