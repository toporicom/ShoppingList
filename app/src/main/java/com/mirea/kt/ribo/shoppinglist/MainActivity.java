package com.mirea.kt.ribo.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    private String login;
    private String password;
    private String group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String server = "https://android-for-students.ru";
        String serverPath = "/coursework/login.php";
        login = "Student571196";
        password = "TMWS9C";

        Button enterBtn = findViewById(R.id.login_button);
        TextView tvError = findViewById(R.id.logError);
        TextInputEditText etLogin = findViewById(R.id.login);
        TextInputEditText etPassword = findViewById(R.id.password);
        enterBtn.setOnClickListener(v -> {
            try {
                login = etLogin.getText().toString();
                password = etPassword.getText().toString();
                group = "RIBO-02-23";
                Log.i("Login", login);
                Log.i("Password", password);
                Log.i("Group", group);
                HashMap<String, String> map = new HashMap<>();
                HTTPRunnable httpRunnable = new HTTPRunnable(server + serverPath, map);
                Thread th = new Thread(httpRunnable);
                map.put("lgn", login);
                map.put("pwd", password);
                map.put("g", group);
                th.start();
                try {
                    th.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        JSONObject jsonObject = new JSONObject(httpRunnable.getResponseBody());
                        Log.i("Title", "Title: " + jsonObject.getString("title"));
                        Log.i("Task", "Task: " + jsonObject.getString("task"));
                        Log.i("Variant", "Variant: " + jsonObject.getString("variant"));
                        tvError.setVisibility(View.GONE);
                        Intent loadingPageIntent = new Intent(this, StoresActivity.class);
                        startActivity(loadingPageIntent);
                    } catch (JSONException e) {
                        Log.i("MainActivityError", "Error, invalid login or pass");
                        tvError.setVisibility(View.VISIBLE);
                    }
                }
            }catch (RuntimeException e){
            }
        });
    }
}