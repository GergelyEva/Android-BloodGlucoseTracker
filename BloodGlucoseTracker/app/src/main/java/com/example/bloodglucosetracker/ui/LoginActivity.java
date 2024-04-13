package com.example.bloodglucosetracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodglucosetracker.MainActivity;
import com.example.bloodglucosetracker.R;
import com.example.bloodglucosetracker.ui.bloodSugarToday.NotificationForegroundService;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText username = findViewById(R.id.Username);
        EditText password = findViewById(R.id.Password);
        EditText email = findViewById(R.id.Email);

        Button logInBtn = findViewById(R.id.btn_Login);

        logInBtn.setOnClickListener(view -> {
            String Username = username.getText().toString();
            String Password = password.getText().toString();
            String Email = email.getText().toString();


            goToMain(Username, Password, Email);
        });
    }

    private void goToMain(String username, String password, String email) {
        // Concatenates username, password and email
        String loginInfo = username + "-" + password + "-" + email;

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("loginInfo", loginInfo);
        startActivity(intent);
        startForegroundService(new Intent(this, NotificationForegroundService.class));
        finish();
    }
}
