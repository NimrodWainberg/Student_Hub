package com.example.studenthub.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenthub.Adapter.PostAdapter;
import com.example.studenthub.Model.Post;
import com.example.studenthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostDetailFragment extends Fragment {

    private String userId;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> posts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail,container,false);

        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        userId = preferences.getString("postid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        setViewsAndInitializeComponents(view);
        readPosts();

        return view;
    }

    /**
     * A function that sets the corresponding views and initializes components
     * @param view View to fetch items from
     */
    private void setViewsAndInitializeComponents(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts);
        recyclerView.setAdapter(postAdapter);
    }

    /**
     * A function that reads all of the posts of a specific user
     */
    private void readPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                Post post = snapshot.getValue(Post.class);
                posts.add(post);

                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}