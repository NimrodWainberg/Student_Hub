package com.example.studenthub;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {
    FloatingActionButton camera, gallery;
    ActivityResultLauncher<Uri> cameraResultLauncher;
    ActivityResultLauncher<String> galleryResultLauncher;
    StorageReference storageRef, fileReference;
    DatabaseReference photosFolderReference;
    UploadTask task;
    File picFile;
    Uri uri;
    ImageView close, image;
    TextView post;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initViews();
        initListeners();
        initLaunchers();

        storageRef = FirebaseStorage.getInstance().getReference("Posts");

        close.setOnClickListener(view -> {
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        });

        post.setOnClickListener(view -> {
            if(!description.getText().toString().isEmpty())
                PostActivity.this.uploadImage();
            else{
                Toast.makeText(PostActivity.this, getString(R.string.must_enter_description)
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * A function that initializes the views
     */
    private void initViews() {
        close = findViewById(R.id.close);
        image = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        camera = findViewById(R.id.upload_pic_camera);
        gallery = findViewById(R.id.upload_pic_gallery);
    }

    /**
     * A function that initializes the listeners
     */
    private void initListeners() {
        gallery.setOnClickListener(v -> galleryResultLauncher.launch("image/*"));

        camera.setOnClickListener(v -> {
            picFile = new File(getApplicationContext() // Creating a new file to insert the URI into
                    .getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "Photo" + System.currentTimeMillis() + ".jpg");
            uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", picFile);

            cameraResultLauncher.launch(uri);
        });
    }

    /**
     * A function that initializes the launchers
     */
    private void initLaunchers(){
        cameraResultLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            // True if image saved into given URI
            if(result){
                Glide.with(getApplicationContext()).load(picFile.getAbsoluteFile()).into(image);
            }
        });

        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            uri = result;
            Glide.with(getApplicationContext()).load(result).into(image);
        });
    }

    /**
     * A function that gets File type
     * @param uri uri to be checked
     * @return String of file type
     */
    private String getFileType(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * A function that uploads the user's picture into the DB
     */
    private void uploadImage(){
        // Dialog Animation
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.post_wall);
        dialog.show();

        if (uri != null){
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

            task = fileReference.putFile(uri);
            task.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String imageUri = Objects.requireNonNull(downloadUri).toString();

                    photosFolderReference = FirebaseDatabase.getInstance().getReference("Posts");

                    String postId = photosFolderReference.push().getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("postId", postId);
                    hashMap.put("postImage", imageUri);
                    hashMap.put("description", description.getText().toString());
                    hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    photosFolderReference.child(postId).setValue(hashMap);

                    dialog.dismiss();

                    startActivity(new Intent(PostActivity.this, MainActivity.class));
                    finish();

                } else {
                    Toast.makeText(PostActivity.this, getString(R.string.failed_to_post), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(PostActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show());

        } else {
            Toast.makeText(PostActivity.this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }
}