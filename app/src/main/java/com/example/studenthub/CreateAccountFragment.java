package com.example.studenthub;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Objects;

public class CreateAccountFragment extends Fragment {
    TextInputEditText fullName, email, password;
    MaterialButton registerBtn;
    ProgressDialog pd;

    // String representation of details
    String emailString;
    String passString;
    String fullNameString;
    String userID;

    // Firebase
    FirebaseAuth mAuth;
    DatabaseReference reference;

    // When the app is visible to the user
    @Override
    public void onStart() {
        super.onStart();
    }

    // When the app is no more visible to the user
    @Override
    public void onStop() {
        super.onStop();
    }


    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account_creation, container, false);
        initViews(view);

        // Save user in firebase
        registerBtn.setOnClickListener(v -> {
            pd = new ProgressDialog(getContext());
            pd.setMessage("Please wait...");
            pd.show();

            emailString = Objects.requireNonNull(email.getText()).toString().trim();
            passString = Objects.requireNonNull(password.getText()).toString().trim();
            fullNameString = Objects.requireNonNull(fullName.getText()).toString().trim();

            boolean answer = validate(emailString, passString,fullNameString);

            if (answer) {
                createAccount(fullNameString, emailString, passString);
            }
        });

        return view;
    }

    /**
     * A function that registers a user into the FireBase's DB.
     * @param fullName Full name of user
     * @param email user's Email
     * @param password User's password
     */
    public void createAccount(String fullName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            pd.dismiss();
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                userID = Objects.requireNonNull(firebaseUser).getUid();
                reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                HashMap<String, Object> map = new HashMap<>();

                // Setting the new user's details + default profile picture and bio
                map.put("id", userID);
                map.put("fullName", fullName);
                map.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/instagramtest-fcbef.appspot.com/o/placeholder.png?alt=media&token=b09b809d-a5f8-499b-9563-5252262e9a49");
                map.put("bio", "");

                // Adding the user and switching into Login Screen
                reference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_SHORT).show();
                            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                                requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    }
                });
            } else { // In case Registration wasn't successful.
                Toast.makeText(getContext(), "Problem creating an account.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * A function that initializes all views by specific IDs.
     * @param view View shown on the screen
     */
    private void initViews(View view) {
        fullName = view.findViewById(R.id.full_name_et);
        email = view.findViewById(R.id.email_et);
        password = view.findViewById(R.id.password_et);
        registerBtn = view.findViewById(R.id.create_account_btn);
        mAuth = FirebaseAuth.getInstance();
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
            pd.dismiss();
            Toast.makeText(getContext(), "Some fields are missing!", Toast.LENGTH_SHORT).show();
        }

        if (pass.length() < 6) {
            isValid = false;
            pd.dismiss();
            Toast.makeText(getContext(), "Password must be longer than 6 characters!", Toast.LENGTH_SHORT).show();
        }

        if (fullName.length() < 4) {
            isValid = false;
            pd.dismiss();
            Toast.makeText(getContext(), "Please enter your full name!", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }
}