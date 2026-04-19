package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kcorteel.travel_esteban_kylian.auth.AuthManager;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;

public class MainActivity extends AppCompatActivity {

    private Button btnTravelShare;
    private Button btnTravelPath;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnAnonymous;
    private Button btnLogout;
    private TextView tvSessionStatus;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager(this);

        btnTravelShare = findViewById(R.id.btnTravelShare);
        btnTravelPath = findViewById(R.id.btnTravelPath);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnAnonymous = findViewById(R.id.btnAnonymous);
        btnLogout = findViewById(R.id.btnLogout);
        tvSessionStatus = findViewById(R.id.tvSessionStatus);

        btnTravelShare.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TravelShareActivity.class);
            startActivity(intent);
        });

        btnTravelPath.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TravelPathActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
        btnRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
        btnAnonymous.setOnClickListener(v -> {
            authManager.continueAsAnonymous();
            updateSessionUi();
        });
        btnLogout.setOnClickListener(v -> {
            authManager.logout();
            updateSessionUi();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSessionUi();
    }

    private void updateSessionUi() {
        User currentUser = authManager.getCurrentUser();
        boolean anonymous = currentUser == null || currentUser.isAnonymous();

        if (anonymous) {
            tvSessionStatus.setText(R.string.auth_status_anonymous);
        } else {
            tvSessionStatus.setText(getString(R.string.auth_status_connected_format, currentUser.getUsername()));
        }

        btnLogin.setEnabled(anonymous);
        btnRegister.setEnabled(anonymous);
        btnAnonymous.setEnabled(!anonymous);
        btnLogout.setEnabled(!anonymous);
    }
}
