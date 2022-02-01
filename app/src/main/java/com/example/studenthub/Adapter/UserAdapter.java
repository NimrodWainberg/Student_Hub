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

    private Context mcontext;
    private List <User> musers;
    private boolean isFragment;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mcontext, List<User> musers, boolean isFragment) {
        this.mcontext = mcontext;
        this.musers = musers;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public UserAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ImageViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User User = musers.get(position);
        holder.btn_follow.setVisibility(View.VISIBLE);
        isFollowing(User.getId(), holder.btn_follow);

        holder.username.setText(User.getUsername());
        holder.fullname.setText(User.getFullName());
        Glide.with(mcontext).load(User.getImageurl()).into(holder.image_profile);

        if (User.getId().equals(firebaseUser.getUid())){
            holder.btn_follow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFragment) {
                SharedPreferences.Editor editor = mcontext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("profileid",User.getId());
                editor.apply();

                ((FragmentActivity)mcontext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            } else {
                    Intent intent = new Intent(mcontext, MainActivity.class);
                    intent.putExtra("publisherid", User.getId());
                    mcontext.startActivity(intent);
                }
            }
        });


        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.btn_follow.getText().toString().equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                            .child("following").child(User.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(User.getId())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                    addNotifications(User.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                            .child("following").child(User.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(User.getId())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });
    }

    private void addNotifications(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().
                getReference("Notifications").child(userid);

        HashMap <String,Object> hashMap = new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","started following you");
        hashMap.put("postid","");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }
    @Override
    public int getItemCount() {
        return musers.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView fullname;
        public CircleImageView image_profile;
        public Button btn_follow;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }

    private void isFollowing(final String userid, final Button button){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()){
                    button.setText("Following");
                } else{
                    button.setText("Follow");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
