package com.example.studenthub;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.studenthub.Fragment.HomeFragment;
import com.example.studenthub.Fragment.NotificationFragment;
import com.example.studenthub.Fragment.ProfileFragment;
import com.example.studenthub.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser;
    // Listener- listen when users logged in / out
    FirebaseAuth.AuthStateListener mAuthListener;
    StorageReference storageReference;
    DrawerLayout drawerLayout;
    BottomNavigationView bottom_navigation;
    Fragment selectedFragment = null;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        storageReference = FirebaseStorage.getInstance().getReference();
        final View view = getLayoutInflater().inflate(R.layout.guest_dailog, null);
        Button login = view.findViewById(R.id.dialog_login);
        Button got_it = view.findViewById(R.id.dialog_got_it);

        // Set toolbar and menu icon
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        mAuthListener = firebaseAuth -> {
            // getCurrentUser - function we can get the currently registered user as an
            // instance of FirebaseUser class
            firebaseUser = firebaseAuth.getCurrentUser();
            //Log.d("Language111", ""+firebaseUser.getEmail());
        };
        // Dialog Animation
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.guest_dailog);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_search:
                    if (firebaseUser.getEmail() == null) {
                        dialog.show();
                    }
                    else {
                        selectedFragment = new SearchFragment();
                    }
                    break;
                case R.id.nav_post:
                    if (firebaseUser.getEmail() == null) {
                        dialog.show();
                    }
                    else {
                        selectedFragment = null;
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                    }
                    break;
                case R.id.nav_notifications:
                    if (firebaseUser.getEmail() == null) {
                        dialog.show();
                    }
                    else {
                        if (firebaseUser.getEmail() == null) {
                            dialog.show();
                        }
                        else {
                            selectedFragment = new NotificationFragment();
                        }
                    }
                    break;
                case R.id.nav_profile:

                    // Check if user is logged in
                   // Toast.makeText(MainActivity.this, ""+firebaseUser,Toast.LENGTH_LONG).show();
                    if (firebaseUser.getEmail() == null) {
                        dialog.show();
                    }
                    else {

                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        editor.apply();
                        selectedFragment = new ProfileFragment();
                    }
                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
            }

            return true;
        });

        Bundle intent = getIntent().getExtras();
        if (intent != null){
            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
            editor.putString("profileid",publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }
    }


}