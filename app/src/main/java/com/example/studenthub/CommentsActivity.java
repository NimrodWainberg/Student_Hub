package com.example.studenthub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.studenthub.Adapter.CommentAdapter;
import com.example.studenthub.Model.Comment;
import com.example.studenthub.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List <Comment> commentList;

    EditText comment;
    ImageView commentProfilePicture;
    TextView postAction;
    String postid;
    String publisherid;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.comments_quote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();

        comment = findViewById(R.id.add_comment);
        commentProfilePicture = findViewById(R.id.comment_profile_picture);
        postAction = findViewById(R.id.post);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");
        commentAdapter = new CommentAdapter(this,commentList,postid);
        recyclerView.setAdapter(commentAdapter);

        postAction.setOnClickListener(view -> {
            if(comment.getText().toString().equals("")){
                Toast.makeText(CommentsActivity.this, "Comment cannot be empty",
                        Toast.LENGTH_SHORT).show();
            } else {
                addComment();
            }
        });

        getImage();
        getComments();

    }

    /**
     * A function that adds a comment to a post
     */
    private void addComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        String commentid = reference.push().getKey();

        HashMap <String,Object> hashMap= new HashMap<>();
        hashMap.put("comment", comment.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());
        hashMap.put("commentid", commentid);

        reference.child(commentid).setValue(hashMap); // Push the comment into DB
        addNotification();

        comment.setText(""); // Clear the EditText after comment has been posted
    }

    /**
     * A function that get adds the notification to DB
     */
    private void addNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", getString(R.string.commented_quote) + comment.getText().toString());
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);

        reference.push().setValue(hashMap);
    }

    /**
     * A function that gets the profile picture of a specific user
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
        final Query query = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(getApplicationContext() == null)
                    return;

                commentList.clear();
                if(snapshot.getValue() != null){
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        Comment comment = snapshot1.getValue(Comment.class);
                        commentList.add(comment);
                    }

                    commentAdapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(CommentsActivity.this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        /*query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                commentList.clear();
                if(snapshot.getValue() != null){
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        Comment comment = snapshot1.getValue(Comment.class);
                        commentList.add(comment);
                    }

                    commentAdapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(CommentsActivity.this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                }
            }*/
    }
}