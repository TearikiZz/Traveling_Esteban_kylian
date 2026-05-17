package com.kcorteel.travel_esteban_kylian;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.kcorteel.travel_esteban_kylian.auth.AuthManager;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private Button btnLogin;
    private Button btnRegister;
    private Button btnAnonymous;
    private Button btnLogout;
    private TextView tvSessionStatus;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TravelShareRepository.getInstance(this).applyCurrentUserThemePreference();
        setContentView(R.layout.activity_main);

        authManager = new AuthManager(this);

        Button btnTravelShare = findViewById(R.id.btnTravelShare);
        Button btnTravelPath = findViewById(R.id.btnTravelPath);
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
            Intent intent = new Intent(MainActivity.this, TravelPathPreferencesActivity.class);
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

        ensureLocationPermission();
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

    private void ensureLocationPermission() {
        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        boolean granted = false;
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }

        if (granted) {
            Toast.makeText(this, R.string.location_permission_granted, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show();
        }
    }
}
