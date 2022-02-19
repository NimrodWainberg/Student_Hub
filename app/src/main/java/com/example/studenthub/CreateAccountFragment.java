package com.example.studenthub;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.studenthub.Model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CreateAccountFragment extends Fragment {

    TextInputEditText username, fullName, email, password, bio;
    MaterialButton registerBtn;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    String usernameString;
    String emailString;
    String passString;
    String fullNameString;
    String bioString;
    String userID;

    // Firebase
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    // Listener- listen when users logged in / out
    FirebaseAuth.AuthStateListener mAuthListener;

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    // When the app is visible to the user
    @Override
    public void onStart() {
        super.onStart();
        // Add the listener
        mAuth.addAuthStateListener(mAuthListener);
    }

    // When the app is no more visible to the user
    @Override
    public void onStop() {
        super.onStop();

        // Remove the listener
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuthListener = firebaseAuth -> {
            // getCurrentUser - function we can get the currently registered user as an
            // instance of FirebaseUser class
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                user.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullNameString).build());
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account_creation, container, false);
        initViews(view);

        // On registration key clicked, save user in FireBase
        registerBtn.setOnClickListener(v -> {

            // Dialog Animation
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.create_account_animation);
            dialog.show();

            usernameString = Objects.requireNonNull(username.getText().toString().trim());
            emailString = Objects.requireNonNull(email.getText()).toString().trim();
            passString = Objects.requireNonNull(password.getText()).toString().trim();
            fullNameString = Objects.requireNonNull(fullName.getText()).toString().trim();
            bioString = Objects.requireNonNull(bio.getText()).toString().trim();

            boolean answer = validate(usernameString, emailString, passString, fullNameString, bioString);

            if (answer) {
                mAuth.createUserWithEmailAndPassword(emailString, passString)
                        .addOnCompleteListener(task -> {
                    // Added a new user successfully
                    if (task.isSuccessful()) {
                        firebaseUser = mAuth.getCurrentUser();
                        userID = Objects.requireNonNull(firebaseUser).getUid();

                        User newUser = new User(usernameString, emailString, fullNameString,
                                getString(R.string.image_url_link),
                                bioString, userID);

                        firebaseDatabase = FirebaseDatabase.getInstance();
                        reference = firebaseDatabase.getReference().child("users");

                        reference.child(userID).setValue(newUser).addOnCompleteListener(addUserTask -> {
                            if(addUserTask.isSuccessful()){
                                Snackbar.make(view, "Sign up successful", Snackbar.LENGTH_SHORT).show();
                                dialog.dismiss();
                                if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                                    requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
                    } else {
                        dialog.dismiss();
                        Snackbar.make(view, "Sign up failed", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
            else { // Registration wasn't successful
                dialog.dismiss();
                Snackbar.make(view,"Please make sure all credentials are correct",
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void initViews(View view) {
        username = view.findViewById(R.id.username);
        fullName = view.findViewById(R.id.full_name_et);
        email = view.findViewById(R.id.email_et);
        password = view.findViewById(R.id.password_et);
        bio = view.findViewById(R.id.edit_profile_bio_edittext);
        registerBtn = view.findViewById(R.id.create_account_btn);
    }

    public boolean validate(String username, String email, String pass, String fullName, String bio) {
        boolean isValid = true;

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) ||
                TextUtils.isEmpty(fullName) || TextUtils.isEmpty(bio)) {
            isValid = false;
            Toast.makeText(getContext(), "Some fields are missing!", Toast.LENGTH_SHORT).show();
        }

        if (username.length() < 5) {
            isValid = false;
            Toast.makeText(getContext(), "username must be longer than 5 characters!",
                    Toast.LENGTH_SHORT).show();
        }

        if (pass.length() < 5) {
            isValid = false;
            Toast.makeText(getContext(), "Password must be longer than 5 characters!",
                    Toast.LENGTH_SHORT).show();
        }

        if (fullName.length() < 4) {
            isValid = false;
            Toast.makeText(getContext(), "Please enter your full name!", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }
}