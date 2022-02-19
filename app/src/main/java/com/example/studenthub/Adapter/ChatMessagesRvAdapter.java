package com.example.studenthub.Adapter;


import static com.example.studenthub.Model.ChatRoom.DUMMY;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenthub.Model.ChatMessage;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatMessagesRvAdapter extends RecyclerView.Adapter<ChatMessagesRvAdapter.ChatMessagesViewHolder> {

    private List<ChatMessage> chatMessages;
    private User owner,second;
    public ChatMessagesRvAdapter(List<ChatMessage> chatMessages,User owner,User second) {
        this.chatMessages = chatMessages;
        this.owner = owner;
        this.second = second;
    }

    @NonNull
    @Override
    public ChatMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message,parent,false);
        return new ChatMessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessagesViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

     class ChatMessagesViewHolder extends RecyclerView.ViewHolder {
        private ImageView senderImageView;
        private TextView senderNameTv;
        private TextView messageContent;
        public ChatMessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            this.senderImageView = itemView.findViewById(R.id.user_image_chat);
            this.senderNameTv = itemView.findViewById(R.id.user_name_chat);
            this.messageContent = itemView.findViewById(R.id.user_message_chat);
        }

        public void bind(ChatMessage message) {
            if(message.getId().equals(DUMMY)) {
                itemView.setVisibility(View.GONE);
                return;
            }
            if (message.getAuthorId().equals(owner.getId())) {
                senderNameTv.setText(owner.getFullName());
                Picasso.get().load(owner.getImageUrl()).into(senderImageView);
            }else {
                senderNameTv.setText(second.getFullName());
                Picasso.get().load(second.getImageUrl()).into(senderImageView);
            }

            messageContent.setText(message.getMessage());
        }
    }
}

