package com.example.studenthub.chats;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenthub.Adapter.ChatRoomsRvAdapter;
import com.example.studenthub.Adapter.interfaces.OnEnterChatRoomListener;
import com.example.studenthub.Model.ChatRoom;
import com.example.studenthub.R;
import com.example.studenthub.firebase.MessagingManager;
import com.example.studenthub.firebase.interfaces.FirebaseCallBack;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ChatRoomsFragment extends LoadingFragment implements OnEnterChatRoomListener {


    private RecyclerView chatRoomsRv;
    private ChatRoomsRvAdapter chatRoomsRvAdapter;
    private FloatingActionButton addNewChatRoomBtn;
    private final MessagingManager messagingManager = MessagingManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_rooms,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatRoomsRv = view.findViewById(R.id.chat_rooms_rv);
        chatRoomsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        addNewChatRoomBtn = view.findViewById(R.id.newChatRoom_btn);

        addNewChatRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentFragmentManager().findFragmentByTag("UsersFragment") == null) {

                    UsersFragment usersFragment = new UsersFragment();
                    getParentFragmentManager().beginTransaction()

                            .setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_from_left)
                            .add(android.R.id.content, usersFragment, "UsersFragment")
                            .addToBackStack("UsersFragment").commit();
                }
            }
        });
        ProgressDialog progressDialog =  showLoading("Chat Rooms");

        messagingManager.addChatRoomsValueEventListener(new FirebaseCallBack<List<ChatRoom>>() {
            @Override
            public void onComplete(List<ChatRoom> chatRooms) {
                progressDialog.dismiss();
                chatRoomsRvAdapter = new ChatRoomsRvAdapter(chatRooms, ChatRoomsFragment.this);
                chatRoomsRv.setAdapter(chatRoomsRvAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                progressDialog.dismiss();
                showToast("There was an problem loading chat rooms");
                System.out.println(e.getLocalizedMessage());
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messagingManager.removeChatRoomsValueEventListener();
    }

    @Override
    public void enterChatRoom(String roomId) {

        if (getParentFragmentManager().findFragmentByTag("ChatFragment") == null) {
            Bundle b  = new Bundle();
            b.putString("roomId",roomId);
            ChatFragment chatFragment = new ChatFragment();
            chatFragment.setArguments(b);
            getParentFragmentManager().beginTransaction()

                    .setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_from_left)
                    .add(android.R.id.content, chatFragment, "ChatFragment")
                    .addToBackStack("ChatFragment").commit();
        }
    }

}