package com.example.studenthub.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.studenthub.CommentsActivity;
import com.example.studenthub.Fragment.PostDetailFragment;
import com.example.studenthub.Fragment.ProfileFragment;
import com.example.studenthub.Model.Post;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPosts;
    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.ViewHolder holder,final int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPosts.get(position);

        Glide.with(mContext).load(post.getPostImage()).into(holder.post_image);
        
        if (post.getDescription().equals("")){
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
        }

        publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPublisher());

        isLiked(post.getPostId(),holder.like);
        numOfLikes(holder.likes,post.getPostId());
        getComments(post.getPostId(),holder.comments);

        holder.like.setOnClickListener(view -> {
            if(holder.like.getTag().equals("like")){
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                        .child(firebaseUser.getUid()).setValue(true);
                addNotifications(post.getPublisher(),post.getPostId());
            } else
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                        .child(firebaseUser.getUid()).removeValue();
        });

        holder.comment.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("postId",post.getPostId());
            intent.putExtra("publisherid",post.getPublisher());
            mContext.startActivity(intent);
        });

        holder.comments.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("postId",post.getPostId());
            intent.putExtra("publisherid",post.getPublisher());
            mContext.startActivity(intent);
        });


        holder.image_profile.setOnClickListener(view -> {
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("profileid",post.getPublisher());
            editor.apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        });

        holder.publisher.setOnClickListener(view -> {
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("profileid",post.getPublisher());
            editor.apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        });

        holder.username.setOnClickListener(view -> {
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("profileid",post.getPublisher());
            editor.apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        });

        holder.post_image.setOnClickListener(view -> {
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("postId",post.getPostId());
            editor.apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PostDetailFragment()).commit();
        });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile, like, post_image, comment, save;
        public TextView likes, username, publisher, comments, description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            likes = itemView.findViewById(R.id.likes);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
        }
    }

    private void getComments(String postId, final TextView comments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.setText(mContext.getString(R.string.view_all) + snapshot.getChildrenCount() + mContext.getString(R.string.comments_quote));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void isLiked(String postId, ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(Objects.requireNonNull(firebaseUser).getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    /**
     * A function that adds a notification into specific user's DB
     * @param userid ID of liking user
     * @param postId ID of post
     */
    private void addNotifications(String userid, String postId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap <String,Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", mContext.getString(R.string.liked_your_post_quote));
        hashMap.put("postId", postId);
        hashMap.put("ispost", true);

        reference.push().setValue(hashMap);
    }

    /**
     * A function that gets exact number of likes of a specific post
     * @param likes TextView to be changed
     * @param postId Post ID to fetch likes number from
     */
    private void numOfLikes(TextView likes, String postId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String numberOfLikes = mContext.getString(R.string.likes_tv);
                likes.setText(snapshot.getChildrenCount() + numberOfLikes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * A function that gets publisher info
     * @param image_profile Image to be shown
     * @param username Username to be shown
     * @param publisher Publisher of the post
     * @param userid User ID
     */
    private void publisherInfo(ImageView image_profile, TextView username, TextView publisher, String userid) {
        final Query query = FirebaseDatabase.getInstance().getReference("users").child(userid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageUrl()).into(image_profile);
                publisher.setText(user.getImageUrl());
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageUrl()).into(image_profile);
                publisher.setText(user.getImageUrl());
                username.setText(user.getUsername());
            }*/

    }
}
