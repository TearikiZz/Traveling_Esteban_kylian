package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnTravelShare;
    private Button btnTravelPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTravelShare = findViewById(R.id.btnTravelShare);
        btnTravelPath = findViewById(R.id.btnTravelPath);

        btnTravelShare.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TravelShareActivity.class);
            startActivity(intent);
        });

        btnTravelPath.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TravelPathActivity.class);
            startActivity(intent);
        });
    }
}