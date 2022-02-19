package com.example.studenthub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.studenthub.Fragment.GuestModeFragment;
import com.example.studenthub.Fragment.HomeFragment;
import com.example.studenthub.Fragment.NotificationFragment;
import com.example.studenthub.Fragment.ProfileFragment;
import com.example.studenthub.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
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

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        storageReference = FirebaseStorage.getInstance().getReference();

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
        };

        boolean isConnected = false;
        // Source: https://stackoverflow.com/a/45738019/15633316
        for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("password")) { // user connected through email
                isConnected = true;
            }
        }

        bottom_navigation = findViewById(R.id.bottom_navigation);
        boolean finalIsConnected = isConnected;
        bottom_navigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_search:
                    if (!finalIsConnected) {
                        selectedFragment = new GuestModeFragment();
                        //showDialogFragment();
                    }
                    else {
                        selectedFragment = new SearchFragment();
                    }
                    break;
                case R.id.nav_post:
                    if (!finalIsConnected) {
                        selectedFragment = new GuestModeFragment();
                        //showDialogFragment();
                    }
                    else {
                        selectedFragment = null;
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                    }
                    break;
                case R.id.nav_notifications:
                    if (!finalIsConnected) {
                        selectedFragment = new GuestModeFragment();
//                        showDialogFragment();
                    }
                    else {
                        selectedFragment = new NotificationFragment();
                    }
                    break;
                case R.id.nav_profile:
                    if (!finalIsConnected) {
                        selectedFragment = new GuestModeFragment();
                    }
                    else {
                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        editor.apply();
                        selectedFragment = new ProfileFragment();
                    }
                    break;
            }

            // Source: https://stackoverflow.com/questions/17210674/how-to-get-which-fragment-has-been-selected
            if (selectedFragment != null && selectedFragment instanceof GuestModeFragment){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).addToBackStack("GuestModeDialog").commit();
            }
            else if (selectedFragment != null) {
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