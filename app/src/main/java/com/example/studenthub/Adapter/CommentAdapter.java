package com.example.studenthub.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studenthub.MainActivity;
import com.example.studenthub.Model.Comment;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ImageViewHolder> {

    private final Context mContext;
    private final List<Comment> mComment;
    private final String postId;
    private FirebaseUser firebaseUser;

    public CommentAdapter(Context context, List<Comment> comments, String postId) {
        this.mContext = context;
        this.mComment = comments;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommentAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ImageViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Comment comment = mComment.get(position);
        holder.comment.setText(comment.getComment());
        getUserInfo(holder.image_profile, holder.username, comment.getPublisher());

        holder.username.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("publisherid", comment.getPublisher());
            mContext.startActivity(intent);
        });

        holder.image_profile.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("publisherid", comment.getPublisher());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView image_profile;
        public TextView username, comment;

        public ImageViewHolder(View itemView) {
            super(itemView);
            image_profile = itemView.findViewById(R.id.comment_item_picture);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    /**
     * A function that gets user information from DB.
     * @param imageView ImageView to be changed
     * @param publisherid ID to get details from
     */
    private void getUserInfo(final ImageView imageView, TextView username, String publisherid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(publisherid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                if(mContext == null)
                    return;

                User user = Snapshot.getValue(User.class);
                username.setText(user.getUsername());
                Glide.with(mContext).load(user.getImageUrl()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}

