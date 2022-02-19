package com.example.studenthub.chats;

import android.app.ProgressDialog;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {


    public ProgressDialog showLoading(String content) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("StudentsHub");
        progressDialog.setMessage("Loading " + content + " ..");
        progressDialog.show();
        return progressDialog;
    }


    public void showToast(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

    }
}
