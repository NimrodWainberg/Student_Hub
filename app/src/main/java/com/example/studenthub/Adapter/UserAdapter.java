package com.example.studenthub.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studenthub.Fragment.ProfileFragment;
import com.example.studenthub.MainActivity;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ImageViewHolder> {

    private Context context;
    private List <User> userList;
    private boolean isFragment;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context context, List<User> userList, boolean isFragment) {
        this.context = context;
        this.userList = userList;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public UserAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ImageViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        User User = userList.get(position);
        holder.followBtn.setVisibility(View.VISIBLE);
        isFollowing(User.getId(), holder.followBtn);

        holder.username.setText(User.getUsername());
        holder.fullname.setText(User.getFullName());
        Glide.with(context).load(User.getImageUrl()).into(holder.image_profile);

        // If the user looks at his own profile
        if (User.getId().equals(firebaseUser.getUid())){
            holder.followBtn.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            if (isFragment) {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",
                    Context.MODE_PRIVATE).edit();
            editor.putString("profileid", User.getId());
            editor.apply();

            // Switch display to profile fragment
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();

            } else {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("publisherid", User.getId());
                context.startActivity(intent);
            }
        });

        holder.followBtn.setOnClickListener(view -> {
            if(holder.followBtn.getText().toString().equals("follow")) {
                FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                        .child("following").child(User.getId()).setValue(true);
                FirebaseDatabase.getInstance().getReference().child("Follow").child(User.getId())
                        .child("followers").child(firebaseUser.getUid()).setValue(true);
                addNotification(User.getId());
            }
            else { // If already followed
                FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                        .child("following").child(User.getId()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Follow").child(User.getId())
                        .child("followers").child(firebaseUser.getUid()).removeValue();
            }
        });
    }

    /**
     * A function that adds a notification into specific user's DB
     * @param userid ID of following user
     */
    private void addNotification(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().
                getReference("Notifications").child(userid);

        HashMap <String,Object> hashMap = new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text", context.getString(R.string.startedfollowing));
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap); // Push data into DB
    }

    /**
     * A function that returns number of users
     * @return number of users
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView fullname;
        public CircleImageView image_profile;
        public Button followBtn;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            followBtn = itemView.findViewById(R.id.btn_follow);
        }
    }

    /**
     * A function that checks if a user is already being followed or not and changes text accordingly
     * @param userid User id checked
     * @param button Button to be changed
     */
    private void isFollowing(String userid, Button button){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()){ // If user is already followed
                    //button.setText("Following");
                    button.setText(R.string.following);
                }
                else { // If user isn't followed
                    //button.setText("Follow");
                    button.setText(R.string.follow_btn);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
