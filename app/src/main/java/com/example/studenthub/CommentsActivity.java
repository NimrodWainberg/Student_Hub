package com.example.studenthub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studenthub.Adapter.CommentAdapter;
import com.example.studenthub.Model.Comment;
import com.example.studenthub.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    CommentAdapter commentAdapter;
    List <Comment> commentList;
    EditText commentEditText;
    ImageView commentProfilePicture;
    TextView postCommentBtn;
    String postid;
    String publisherid;
    FirebaseUser firebaseUser;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        initViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.comments_quote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Getting information about the publisher
        Intent intent = getIntent();
        postid = intent.getStringExtra("postId");
        publisherid = intent.getStringExtra("publisherid");
        commentAdapter = new CommentAdapter(CommentsActivity.this, commentList, postid);
        recyclerView.setAdapter(commentAdapter);

        postCommentBtn.setOnClickListener(view -> {
            if(commentEditText.getText().toString().equals("")){
                Toast.makeText(CommentsActivity.this,
                        getString(R.string.comment_cannot_be_empty), Toast.LENGTH_SHORT).show();
            } else {
                addComment();
                commentEditText.getText().toString();
            }
        });

        getImage();
        getComments();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        commentEditText = findViewById(R.id.add_comment);
        commentProfilePicture = findViewById(R.id.comment_profile_picture);
        postCommentBtn = findViewById(R.id.post);
    }

    /**
     * A function that adds a comment to a post
     */
    private void addComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        // Getting random comment ID to push into DB
        String commentid = reference.push().getKey();
        String comment = commentEditText.getText().toString();

        HashMap <String,Object> hashMap = new HashMap<>();
        hashMap.put("comment", comment);
        hashMap.put("publisher", firebaseUser.getUid());
        hashMap.put("commentid", commentid);

        reference.child(commentid).setValue(hashMap); // Push the comment into DB

        // After pushing to Database, send notification
        addCommentNotification();
    }

    /**
     * A function that get adds the notification to DB
     */
    private void addCommentNotification() {
        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(firebaseUser.getUid());
        usernameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                DatabaseReference reference = FirebaseDatabase.getInstance()
                        .getReference("Notifications").child(publisherid);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("userid", firebaseUser.getUid());
                hashMap.put("text", user.getUsername() + getString(R.string.commented_quote) + commentEditText.getText().toString());
                hashMap.put("postid", postid);
                hashMap.put("ispost", true);

                reference.push().setValue(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * A function that gets the profile picture
     */
    private void getImage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(commentProfilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * A function that gets all of the comments of a specific post
     */
    private void getComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getApplicationContext() == null)
                    return;

                commentList.clear();
                if(snapshot.getValue() != null){
                    for(DataSnapshot data : snapshot.getChildren()) { // Iterating over comments
                            Comment comment = data.getValue(Comment.class);
                            commentList.add(comment);
                    }
                        commentAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        }
    }