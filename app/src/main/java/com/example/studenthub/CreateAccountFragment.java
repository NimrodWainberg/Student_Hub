package com.example.studenthub;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.studenthub.Fragment.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.studenthub.Model.User;

import java.util.HashMap;
import java.util.Objects;


public class CreateAccountFragment extends Fragment {

    TextInputEditText username, fullName, email, password;
    MaterialButton registerBtn;
    ProgressBar progressBar;

    // Username
    String emailString;
    String passString;
    String fullNameString;

    // Firebase
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    // Listener- listen when users logged in/ out
    FirebaseAuth.AuthStateListener mAuthListener;

    // Lottie
    LottieAnimationView lottieCreate;



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


    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*ActionBar supportActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.hide();*/

        // Firebase
        // Initialize listener
        // Use this function each time user signing in/ out/ up
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // getCurrentUser - function we can get the currently registered user as an instance of FirebaseUser class
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // TODO if want to show the user is name from login:
                    // userTv.setText(user.getDisplayName() + " is logged in!");
                    user.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullNameString).build()).addOnCompleteListener
                            (new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //profile update successful
                                    }

                                }
                            });
                } else {

                }
            }

        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account_creation, container, false);
        initViews(view);

        // animation
       // lottieCreate = view.findViewById(R.id.user_animation);

        // On registration key clicked
        // Save user in firebase
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dialog
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.create_account_dialog);
                dialog.show();
//                lottieCreate.pauseAnimation();

                emailString = email.getText().toString().trim();
                passString = password.getText().toString().trim();
                fullNameString = fullName.getText().toString().trim();

                boolean answer = validate(emailString, passString,fullNameString);

                if (answer) {
                    System.out.println(emailString + "   " + passString);
                    mAuth.createUserWithEmailAndPassword(emailString, passString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            // Complete adding new user successfully
                            if (task.isSuccessful()) {
                                User newUser = new User(emailString,fullNameString,null,null);
                                if(FirebaseAuth.getInstance().getUid()!=null)
                                    FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(FirebaseAuth.getInstance().getUid())
                                            .set(newUser);
                                Snackbar.make(view, "Sign up successful", Snackbar.LENGTH_SHORT).show();
                                getFragmentManager().popBackStack();
                            } else {
                                dialog.dismiss();
                                Snackbar.make(view, "Sign up failed", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    dialog.dismiss();
                    Snackbar.make(view,"Please make sure all credentials are correct",Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    private void initViews(View view) {
        fullName = view.findViewById(R.id.full_name_et);
        email = view.findViewById(R.id.email_et);
        password = view.findViewById(R.id.password_et);
        registerBtn = view.findViewById(R.id.create_account_btn);
        progressBar = view.findViewById(R.id.bio);
    }

    public boolean validate(String email, String pass, String fullName) {
        boolean isValid = true;
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(fullName)) {
            isValid = false;
            Toast.makeText(getContext(), "Some fields are missing!", Toast.LENGTH_SHORT).show();
        }

        if (pass.length() < 5) {
            isValid = false;
            Toast.makeText(getContext(), "Password must be longer than 5 characters!", Toast.LENGTH_SHORT).show();
        }

        if (fullName.length() < 4) {
            isValid = false;
            Toast.makeText(getContext(), "Please enter your full name!", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }
}

//getParentFragmentManager
//popBackStack