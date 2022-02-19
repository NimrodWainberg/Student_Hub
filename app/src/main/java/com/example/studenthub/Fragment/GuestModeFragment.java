package com.example.studenthub.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.studenthub.Login;
import com.example.studenthub.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class GuestModeFragment extends Fragment {

    MaterialButton login, got_it;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.guest_dailog, container, false);
        login = view.findViewById(R.id.dialog_login);
        got_it = view.findViewById(R.id.dialog_got_it);

        login.setOnClickListener(view1 -> {
            FirebaseAuth.getInstance().signOut();

            if(requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                requireActivity().getSupportFragmentManager().popBackStack();
            startActivity(new Intent(getActivity(), Login.class));
            requireActivity().finish();
        });

        got_it.setOnClickListener(view1 -> {
            if(requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
}
