package com.example.studenthub;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Login extends AppCompatActivity {
    TextInputEditText mEmail,mPassword;
    MaterialButton mLoginBtn;
    MaterialButton mLoginAnonymousBtn;
    FirebaseAuth fAuth;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBar actionBar;
    CreateAccountFragment createAccountFragment;
    ForgotPasswordFragment forgotPasswordFragment;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.text_input_email);
        mPassword = findViewById(R.id.text_input_password);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.login_button);
        mLoginAnonymousBtn = findViewById(R.id.login_anonymous_button);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        // Login Button
        mLoginBtn.setOnClickListener(v -> {
            Dialog dialog = new Dialog(Login.this);
            dialog.setContentView(R.layout.progress_dialog);
            dialog.show();

            String email = Objects.requireNonNull(mEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(mPassword.getText()).toString().trim();

            // Validate input
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                dialog.dismiss();
                Toast.makeText(Login.this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
            } else {
                // Authenticate details in DB
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Login.this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        dialog.dismiss();
                        startActivity(intent);
                        finish();
                    } else {
                        dialog.dismiss();
                        Snackbar.make(v, getString(R.string.sign_in_failed), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Anonymous login
        mLoginAnonymousBtn.setOnClickListener(v -> {
            Dialog dialog = new Dialog(Login.this);
            dialog.setContentView(R.layout.progress_dialog);
            dialog.show();

            fAuth.signInAnonymously()
                    .addOnCompleteListener(Login.this, task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            dialog.dismiss();
                            startActivity(intent);
                            finish();
                        } else {
                            dialog.dismiss();
                            Snackbar.make(v, getString(R.string.sign_in_failed), Snackbar.LENGTH_SHORT).show();
                        }
                    });
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            item.setChecked(true);
            drawerLayout.closeDrawers();

            if (item.getItemId() == R.id.drawer_create_account) {
                createAccountFragment = new CreateAccountFragment();

                // If there isn't any other instance of this fragment in stack
                if (getSupportFragmentManager().findFragmentByTag("CreateAccount") == null) {
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_from_left)
                            .add(android.R.id.content, createAccountFragment, "CreateAccount")
                            .addToBackStack("CreateAccount").commit();
                }
            } else { // Forgot your password?
                forgotPasswordFragment = new ForgotPasswordFragment();

                // If there isn't any other instance of this fragment in stack
                if (getSupportFragmentManager().findFragmentByTag("ForgotPassword") == null) {
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_from_left)
                            .add(android.R.id.content, forgotPasswordFragment, "ForgotPassword")
                            .addToBackStack("ForgotPassword").commit();
                }
            }

            return true;
        });
    }
}
