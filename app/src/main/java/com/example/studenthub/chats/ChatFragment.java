package com.example.studenthub.chats;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenthub.Adapter.ChatMessagesRvAdapter;
import com.example.studenthub.Model.ChatMessage;
import com.example.studenthub.Model.ChatRoom;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.example.studenthub.firebase.MessagingManager;
import com.example.studenthub.firebase.interfaces.FirebaseCallBack;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatFragment extends LoadingFragment {
    RecyclerView messagesRv;
    ChatMessagesRvAdapter messagesRvAdapter;
    Button sendButton;
    EditText messageEt;
    String roomId;
    ChatRoom room;
    private final MessagingManager messagingManager = MessagingManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendButton = view.findViewById(R.id.send_message_btn);
        messageEt = view.findViewById(R.id.message_et);
        messagesRv = view.findViewById(R.id.chat_rv);
        messagesRv.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle i = getArguments();
        ProgressDialog progressDialog = showLoading(getString(R.string.messages));
        if(i != null) {
            roomId = i.getString("roomId");

            messagingManager.addChatRoomMessagesValueEventListener(roomId, new FirebaseCallBack<Pair<List<ChatMessage>,ChatRoom>>() {
                @Override
                public void onComplete(Pair<List<ChatMessage>,ChatRoom> response) {
                    if(room ==null) {
                        progressDialog.dismiss();
                    }
                    room = response.second;
                    User owner = messagingManager.getUserById(response.second.getOwnerId());
                    User second = messagingManager.getUserById(response.second.getSecondUserId());
                    messagesRvAdapter = new ChatMessagesRvAdapter(response.first,owner ,second);
                    messagesRv.setAdapter(messagesRvAdapter);
                }

                @Override
                public void onFailure(Exception e) {
                    if(ChatFragment.this.room ==null)
                        progressDialog.dismiss();
                    showToast(getString(R.string.problem_loading_messages));
                }
            });

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(room ==null)
                        return;
                    String content = messageEt.getText().toString();
                    if(content.isEmpty())
                        return;
                    String uid = FirebaseAuth.getInstance().getUid();
                    String recipientId = room.getOwnerId().equals(uid) ? room.getSecondUserId() : room.getOwnerId();
                    messagingManager.sendNewMessage(roomId,recipientId , content, new FirebaseCallBack<String>() {
                        @Override
                        public void onComplete(String successMessage) {
                            messageEt.getText().clear();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            showToast(getString(R.string.problem_sending_message));
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(roomId!=null)
            messagingManager.removeChatRoomMessagesValueEventListener(roomId);
    }
}
