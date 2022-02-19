package com.example.studenthub.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenthub.Adapter.interfaces.OnCreateRoomListener;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;

import java.util.List;

public class UsersRvAdapter extends RecyclerView.Adapter<UsersRvAdapter.UsersViewHolder> {

    private List<User>users;
    private OnCreateRoomListener createRoomListener;
    public UsersRvAdapter(List<User> users, OnCreateRoomListener createRoomListener) {
        this.users = users;
        this.createRoomListener = createRoomListener;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_room,parent,false);
       return new UsersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

     class UsersViewHolder extends RecyclerView.ViewHolder {
        private ImageView uImageView;
        private TextView uNameTV;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            this.uImageView = itemView.findViewById(R.id.user_image_chat_room);
            this.uNameTV = itemView.findViewById(R.id.user_name_chat_room);
        }

        @SuppressLint("SetTextI18n")
        public void bind(User user) {
            itemView.setOnClickListener(view -> {
                AlertDialog alert = showCreateRoomAlert(itemView.getContext(), user);
                alert.show();
            });
            uNameTV.setText("Start Chat With: " + user.getFullName());
        }
    }

    public AlertDialog showCreateRoomAlert(Context context, User user) {
        AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle("StudentHub")
                .setMessage("Would you like to create a chat room with " + user.getFullName())
                .setPositiveButton("Yes", (dialogInterface, i) -> createRoomListener.createRoom(user.getId()))
                .setNegativeButton("No",null).create();
        return alert;
    }
}
