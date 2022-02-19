package com.example.studenthub;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

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

        email = view.findViewById(R.id.forgot_password_email_edit_text);
        restore_btn = view.findViewById(R.id.restore_password_material_btn);

        restore_btn.setOnClickListener(v -> {
            // Dialog Animation
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.reset_password_animation);
            dialog.show();

            if(!Objects.requireNonNull(email.getText()).toString().isEmpty())
            {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    if(getActivity() != null)
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(),
                                getString(R.string.reset_link_message) + email.getText().toString(),
                                Toast.LENGTH_SHORT).show());
                      }).addOnFailureListener(e -> {
                          if(getActivity() != null)
                              getActivity().runOnUiThread(() -> Toast.makeText(getContext(),
                                      R.string.reset_link_problem,
                                      Toast.LENGTH_SHORT).show());
                      });
            } else {
                dialog.dismiss();
                Toast.makeText(getContext(), R.string.restore_password_error,
                        Toast.LENGTH_SHORT).show();
            }

            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                requireActivity().getSupportFragmentManager().popBackStack();
        });
        return view;
    }
}