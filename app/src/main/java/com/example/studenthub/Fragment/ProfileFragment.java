package com.example.studenthub.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studenthub.Adapter.PhotoAdapter;
import com.example.studenthub.Login;
import com.example.studenthub.Model.Post;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.example.studenthub.firebase.FollowingManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {
    ImageView profilePicture, exitApp;
    TextView posts, followers, following, fullname, bio, username;
    Button edit_profile;
    RecyclerView recyclerView;
    PhotoAdapter photoAdapter;
    List<Post> postList;
    FirebaseUser firebaseUser;
    String id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Here we get the exact user ID from search box, if any
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", MODE_PRIVATE);
        id = prefs.getString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        initViews(view);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), postList);
        recyclerView.setAdapter(photoAdapter);

        userInfo();
        getFollowers();
        getNumberOfPosts();
        getPhotos();

        // If a user looks at his own profile
        if (id.equals(firebaseUser.getUid())) {
            edit_profile.setText(R.string.edit_profile);
        } else {
            checkFollow();
        }

        edit_profile.setOnClickListener(edit_profile_view -> {
            String buttonString = edit_profile.getText().toString();

            if (buttonString.equals(getString(R.string.edit_profile))){
                EditProfileFragment editProfileFragment = new EditProfileFragment();
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_from_left)
                        .add(android.R.id.content, editProfileFragment, "editProfileFragment")
                        .addToBackStack("editProfileFragment").commit();
            }

            else if (buttonString.equals(getString(R.string.follow_btn))) {
                FollowingManager.follow(firebaseUser.getUid(),id);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users")
                        .child(firebaseUser.getUid());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        addNotificationToDataBase(user.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                 // If a new follow is created, update notifications

            } else if (buttonString.equals(getString(R.string.following))) {
                FollowingManager.unfollow(firebaseUser.getUid(), id);
            }
        });

        exitApp.setOnClickListener(view1 -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), Login.class));
        });

        return view;
    }

    /**
     * A function that finds all views for elements on screen
     * @param view View that contains the elements
     */
    private void initViews(View view) {
        profilePicture = view.findViewById(R.id.fragment_profile_picture);
        exitApp = view.findViewById(R.id.exit_app);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username_profile);
        edit_profile = view.findViewById(R.id.edit_profile);
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    /**
     * A function that adds the notification into the DB.
     * @param username Username to add to the notification
     */
    private void addNotificationToDataBase(String username) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(id);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", username + getString(R.string.startedfollowing));
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    /**
     * A function that fetches all of the user's information from the DB
     */
    private void userInfo() {
        final Query query = FirebaseDatabase.getInstance().getReference("users").child(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(getActivity() == null)
                    return;

                User user = dataSnapshot.getValue(User.class);
                fullname.setText(user.getFullName());
                username.setText(user.getUsername());
                bio.setText(user.getBio());
                Glide.with(getActivity()).load(user.getImageUrl()).into(profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * A function that checks if a user is being followed by current user
     */
    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(id).exists()) {
                    edit_profile.setText(R.string.following);
                } else {
                    edit_profile.setText(R.string.follow_btn);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * A function that gets specific user's Followers and Following count from DB
     */
    private void getFollowers() {
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(id).child("followers");
        followersRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(id).child("following");
        followingRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * A function that gets specific user's number of posts from DB
     */
    private void getNumberOfPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);
                    if (post.getPublisher().equals(id)) {
                        i++;
                    }
                }
                posts.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * A function that gets specific user's photos from DB
     */
    private void getPhotos() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) { // Iterating over posts IDs
                    Post post = snapshot1.getValue(Post.class);
                    if (post.getPublisher().equals(id)) {
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}