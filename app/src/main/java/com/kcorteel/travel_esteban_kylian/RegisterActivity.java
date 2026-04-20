package com.kcorteel.travel_esteban_kylian;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kcorteel.travel_esteban_kylian.auth.AuthManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = new AuthManager(this);

        usernameEditText = findViewById(R.id.etRegisterUsername);
        emailEditText = findViewById(R.id.etRegisterEmail);
        passwordEditText = findViewById(R.id.etRegisterPassword);
        confirmPasswordEditText = findViewById(R.id.etRegisterPasswordConfirm);
        Button registerButton = findViewById(R.id.btnConfirmRegister);

        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String errorMessage = authManager.register(
                usernameEditText.getText().toString(),
                emailEditText.getText().toString(),
                passwordEditText.getText().toString(),
                confirmPasswordEditText.getText().toString()
        );

        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.auth_register_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
