package com.example.studenthub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPasswordFragment extends Fragment {

    TextInputEditText email;
    MaterialButton restore_btn;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        email = view.findViewById(R.id.username_layout);
        restore_btn = view.findViewById(R.id.restore_password_material_btn);

        restore_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!Objects.requireNonNull(email.getText()).toString().isEmpty())
                {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                    .addOnSuccessListener(unused -> {
                        if(getActivity() != null)
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(),
                                    "Reset link has been sent to " + email.getText().toString(),
                                    Toast.LENGTH_SHORT).show());
                          }).addOnFailureListener(e -> {
                              if(getActivity() != null)
                                  getActivity().runOnUiThread(() -> Toast.makeText(getContext(),
                                          "Problem sending a reset link!",
                                          Toast.LENGTH_SHORT).show());
                          });
                } else {
                    Toast.makeText(getContext(),"Email address cannot be empty!",
                            Toast.LENGTH_SHORT).show();
                }

                if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                    requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return view;
    }
}