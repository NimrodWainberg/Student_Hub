package com.example.studenthub.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenthub.Adapter.NotificationAdapter;
import com.example.studenthub.Model.Notification;
import com.example.studenthub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List <Notification> notificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_notification, container,false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(),notificationList);
        recyclerView.setAdapter(notificationAdapter);

        readAllNotifications();

        return view;
    }

    /**
     * A function that reads all notifications of specific used from DB
     */
    private void readAllNotifications() {
        // Get specific user's data
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Read its notifications
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").
                child(Objects.requireNonNull(firebaseUser).getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Notification notification = snapshot1.getValue(Notification.class);
                    notificationList.add(notification);
                }

                // Notifications were shown in reversed order, therefore reverse them.
                Collections.reverse(notificationList);

                // Update data set
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}