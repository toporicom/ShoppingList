package com.mirea.kt.ribo.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private String login;
    private String password;
    private String group;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //Bundle savedInstanceState - параметр, содержащий сохраненное состояние активности
        super.onCreate(savedInstanceState); //Родительский класс выполняет внутреннюю работу: Восстанавливает предыдущее состояние
        setContentView(R.layout.activity_main); //Устанавливает макет из ресурсов activity_main.xml
        String server = "https://android-for-students.ru";
        String serverPath = "/coursework/login.php";
        login = "Student571196";
        password = "TMWS9C";

        Button enterBtn = findViewById(R.id.login_button);
        TextView tvError = findViewById(R.id.logError);
        TextInputEditText etLogin = findViewById(R.id.login);
        TextInputEditText etPassword = findViewById(R.id.password);

        enterBtn.setOnClickListener(v -> { //Устанавливаем обработчик клика на кнопку входа
            try {
                login = etLogin.getText().toString();
                password = etPassword.getText().toString();
                group = "RIBO-02-23";

                //Выводим в лог введенные данные для отладки.
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
                    th.join(); //приостанавливает выполнение текущего потока до тех пор, пока поток th не завершит свою работу
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        JSONObject jsonObject = new JSONObject(httpRunnable.getResponseBody()); // Переводит данные сервера из \u0421 в символы типа цуацуа
                        Log.i("Title", "Title: " + jsonObject.getString("title")); //наше задание написано в Logcat
                        Log.i("Task", "Task: " + jsonObject.getString("task"));
                        Log.i("Variant", "Variant: " + jsonObject.getString("variant"));
                        tvError.setVisibility(View.GONE); //ошибку не будет видно при заходе в приложение или при успешной авториз. (место не будет занимать в отличие от invisible)
                        Intent loadingPageIntent = new Intent(this, StoresActivity.class); //Intent - это "намерение" выполнить действие (в данном случае - открыть новый экран)
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