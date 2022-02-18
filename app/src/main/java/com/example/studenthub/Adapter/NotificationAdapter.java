package com.example.studenthub.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studenthub.Fragment.PostDetailFragment;
import com.example.studenthub.Fragment.ProfileFragment;
import com.example.studenthub.Model.Notification;
import com.example.studenthub.Model.Post;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Context mContext;
    private final List <Notification> mNotification;

    public NotificationAdapter(Context mContext, List<Notification> mNotifications) {
        this.mContext = mContext;
        this.mNotification = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item,parent,false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Notification notification = mNotification.get(position);
        holder.notificationText.setText(notification.getText());
        if(notification.getUserId() != null)
            getUserinfo(holder.image_profile, holder.username, notification.getUserId());

        // Notification could be either a new follow or liked picture
        if (notification.isPost()){
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image, notification.getPostId());
        } else {
            holder.post_image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            if (notification.isPost()) {
                editor.putString("postid", notification.getPostId());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostDetailFragment()).commit();
            } else { // if liked picture
                editor.putString("profileid",notification.getUserId());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile, post_image;
        public TextView username, notificationText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_profile = itemView.findViewById(R.id.notification_picture);
            post_image = itemView.findViewById(R.id.post_image);
            notificationText = itemView.findViewById(R.id.comment);
            username = itemView.findViewById(R.id.username);
        }
    }

    private void getUserinfo(ImageView imageView, TextView username, String publisherid){
        final Query query = FirebaseDatabase.getInstance().getReference("users").child(publisherid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageUrl()).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostImage(ImageView imageView, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                Glide.with(mContext).load(post.getPostImage()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
