package com.example.studenthub.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studenthub.Adapter.PhotoAdapter;
import com.example.studenthub.EditProfile;
import com.example.studenthub.Login;
import com.example.studenthub.Model.Post;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    ImageView image_profile, exitApp;
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

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("PREFS", MODE_PRIVATE);
        id = prefs.getString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        image_profile = view.findViewById(R.id.image_profile);
        exitApp = view.findViewById(R.id.exit_app);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        edit_profile = view.findViewById(R.id.edit_profile);

        recyclerView = view.findViewById(R.id.recycler_view);
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

//        // Image animation
//        image_profile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });

        // If a user looks at his own profile
        if (id.equals(firebaseUser.getUid())) {
            edit_profile.setText(R.string.edit_profile);
        } else {
            checkFollow();
        }

        edit_profile.setOnClickListener(edit_profile_view -> {
            String buttonString = edit_profile.getText().toString();

            if (buttonString.equals(getString(R.string.edit_profile)))
                startActivity(new Intent(getContext(), EditProfile.class));
            else if (buttonString.equals(getString(R.string.follow_btn))) {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("following").child(id).setValue(true);
                FirebaseDatabase.getInstance().getReference().child("Follow").child(id)
                        .child("followers").child(firebaseUser.getUid()).setValue(true);

                getNotifications(); // If a new follow is created, update notifications

            } else if (buttonString.equals(getString(R.string.following))) {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("following").child(id).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Follow").child(id)
                        .child("followers").child(firebaseUser.getUid()).removeValue();
            }
        });

        exitApp.setOnClickListener(view1 -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), Login.class));
        });

        return view;
    }

    private void getNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(id);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", getString(R.string.startedfollowing));
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) {
                    return;
                }

                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                fullname.setText(user.getFullName());
                bio.setText(user.getBio());
                Glide.with(getContext()).load(user.getImageUrl()).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
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
            public void onCancelled(@NonNull DatabaseError error) {

            }
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
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
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