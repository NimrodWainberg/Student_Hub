package com.example.studenthub.chats;

import android.app.ProgressDialog;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.studenthub.R;

public class LoadingFragment extends Fragment {
    public ProgressDialog showLoading(String content) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage(getString(R.string.loading) + content + "..");
        progressDialog.show();
        return progressDialog;
    }

    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
