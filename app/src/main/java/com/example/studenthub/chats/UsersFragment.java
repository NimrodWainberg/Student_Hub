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

import com.example.studenthub.Adapter.UsersRvAdapter;
import com.example.studenthub.Adapter.interfaces.OnCreateRoomListener;
import com.example.studenthub.Model.User;
import com.example.studenthub.R;
import com.example.studenthub.firebase.FollowingManager;
import com.example.studenthub.firebase.MessagingManager;
import com.example.studenthub.firebase.interfaces.FirebaseCallBack;

import java.util.List;

public class UsersFragment extends LoadingFragment implements OnCreateRoomListener {
    private RecyclerView rvUsers;
    private UsersRvAdapter usersRvAdapter;
    private final MessagingManager messagingManager = MessagingManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvUsers = view.findViewById(R.id.users_rv);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        ProgressDialog progressDialog = showLoading(getString(R.string.followers));
        FollowingManager.getFollowing(new FirebaseCallBack<List<User>>() {
            @Override
            public void onComplete(List<User> users) {
                usersRvAdapter = new UsersRvAdapter(users, UsersFragment.this);
                rvUsers.setAdapter(usersRvAdapter);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Exception e) {
                progressDialog.dismiss();
                showToast(getString(R.string.problem_loading));
            }
        });
    }

    @Override
    public void createRoom(String recipientId) {
        messagingManager.createNewChatRoom(recipientId, new FirebaseCallBack<String>() {
            @Override
            public void onComplete(String successMessage) {
                System.out.println(successMessage);
                showToast(successMessage);
                getParentFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(Exception e) {
                showToast(e.getMessage());
                System.out.println(e.getLocalizedMessage());
            }
        });
    }
}
