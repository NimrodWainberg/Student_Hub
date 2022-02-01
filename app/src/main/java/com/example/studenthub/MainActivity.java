package com.example.studenthub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int GALLERY_INTENT_CODE = 1023;
    TextView fullName, email, verifyMsg;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    Button resendCode;
    Button resetPassLocal, changeProfileImage;
    FirebaseUser user;
    ImageView profileImage;
    StorageReference storageReference;
    DrawerLayout drawerLayout;
    BottomNavigationView bottom_navigation;
    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawerLayout = findViewById(R.id.drawer_layout);
        setContentView(R.layout.activity_main);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

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

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()){
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_search:
                    selectedFragment = new SearchFragment();
                    break;
                case R.id.nav_add:
                    selectedFragment = null;
                    startActivity(new Intent(MainActivity.this,PostActivity.class));
                    break;
                case R.id.nav_heart:
                    selectedFragment = new NotificationFragment();
                    break;
                case R.id.nav_profile:
                    SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                    editor.putString("profileid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                    editor.apply();
                    selectedFragment = new ProfileFragment();
                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
            }

            return true;
        }
    };
}