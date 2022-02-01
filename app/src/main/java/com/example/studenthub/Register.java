package com.example.studenthub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Objects;

public class Register extends AppCompatActivity {
    TextInputEditText mFullName, mEmail, mPassword;
    TextView mLoginBtn;
    Button mRegisterBtn;
    FirebaseAuth fAuth;
    DatabaseReference reference;
    ProgressBar progressBar;
    ProgressDialog pd;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.createText);
        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.createAccountProgressBar);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = new ProgressDialog(Register.this);
                pd.setMessage("Please wait...");
                pd.show();

                final String email = Objects.requireNonNull(mEmail.getText()).toString().trim();
                String password = Objects.requireNonNull(mPassword.getText()).toString().trim();
                final String fullName = Objects.requireNonNull(mFullName.getText()).toString();

                if (validate(email, password, fullName)) {
                    createAccount(fullName, email, password);
                }
            }
        });
    }

    public void createAccount(final String fullName, String email, String password) {

        progressBar.setVisibility(View.VISIBLE);

        // Register our user in the FireBase's DB.
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Send verification link
                    FirebaseUser firebaseUser = fAuth.getCurrentUser();
                    Objects.requireNonNull(firebaseUser).sendEmailVerification().
                            addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Register.this, "Verification email has been Sent.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Register.this, "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    userID = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                    HashMap<String, Object> map = new HashMap<>();

                    // Setting the new user's details + default profile picture and bio
                    map.put("id", userID);
                    map.put("fullName", fullName);
                    map.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/instagramtest-fcbef.appspot.com/o/placeholder.png?alt=media&token=b09b809d-a5f8-499b-9563-5252262e9a49");
                    map.put("bio", "");

                    // Adding the user and switching into Home Screen
                    reference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                Intent intent = new Intent(Register.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    });
                } else { // In case Registration wasn't successful.
                    pd.dismiss();
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Register.this, "There was a problem creating an account.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * A function that checks if the input from the user was OK.
     * @param email User's inputted email address
     * @param pass User's inputted password
     * @param fullName User's inputted full name
     * @return If the input was valid or not.
     */
    public boolean validate(String email, String pass, String fullName) {
        boolean isValid = true;
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(fullName)) {
            isValid = false;
            Toast.makeText(this, "Some fields are missing!", Toast.LENGTH_SHORT).show();
        }

        if (pass.length() < 6) {
            isValid = false;
            Toast.makeText(this, "Password must be longer than 6 characters!", Toast.LENGTH_SHORT).show();
        }

        if (fullName.length() < 4) {
            isValid = false;
            Toast.makeText(this, "Please enter your full name!", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }
}