package com.kcorteel.travel_esteban_kylian;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kcorteel.travel_esteban_kylian.auth.AuthManager;

public class LoginActivity extends AppCompatActivity {

    private EditText identifierEditText;
    private EditText passwordEditText;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = new AuthManager(this);

        identifierEditText = findViewById(R.id.etLoginIdentifier);
        passwordEditText = findViewById(R.id.etLoginPassword);
        Button loginButton = findViewById(R.id.btnConfirmLogin);

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String errorMessage = authManager.login(
                identifierEditText.getText().toString(),
                passwordEditText.getText().toString()
        );

        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.auth_login_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
