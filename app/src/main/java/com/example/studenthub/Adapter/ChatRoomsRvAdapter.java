package com.example.studenthub.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studenthub.Adapter.interfaces.OnEnterChatRoomListener;
import com.example.studenthub.Model.ChatRoom;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.example.studenthub.firebase.MessagingManager;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatRoomsRvAdapter extends RecyclerView.Adapter<ChatRoomsRvAdapter.ChatRoomsViewHolder> {

    private List<ChatRoom> chatRooms;
    private OnEnterChatRoomListener onEnterChatRoomListener;
    public ChatRoomsRvAdapter(List<ChatRoom> chatRooms, OnEnterChatRoomListener onEnterChatRoomListener) {
        this.chatRooms = chatRooms;
        this.onEnterChatRoomListener = onEnterChatRoomListener;
    }

    @NonNull
    @Override
    public ChatRoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_room,parent,false);
        return new ChatRoomsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomsViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

     class ChatRoomsViewHolder extends RecyclerView.ViewHolder {
        private ImageView uImageView;
        private TextView uNameTV;
        public ChatRoomsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.uImageView = itemView.findViewById(R.id.user_image_chat_room);
            this.uNameTV = itemView.findViewById(R.id.user_name_chat_room);
        }

        public void bind(ChatRoom room) {
            // @TODO : set Image
            User u;
            String uid = FirebaseAuth.getInstance().getUid();
            if(room.getOwnerId().equals(uid))
                u = MessagingManager.getInstance().getUserById(room.getSecondUserId());
            else u = MessagingManager.getInstance().getUserById(room.getOwnerId());

            uNameTV.setText("Chat" + u.getFullName());
            Picasso.get().load(u.getImageUrl()).into(uImageView);
            itemView.setOnClickListener(view -> onEnterChatRoomListener.enterChatRoom(room.getId()));
        }
    }
}
