package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class TravelPathTypeActivity extends AppCompatActivity {

    private Button economicButton, balancedButton, comfortButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travelpath_type);

        // Initialisation des vues
        economicButton = findViewById(R.id.economicButton);
        balancedButton = findViewById(R.id.balancedButton);
        comfortButton = findViewById(R.id.comfortButton);

        // Récupération des préférences de l'écran précédent
        Bundle extras = getIntent().getExtras();
        String budget = extras != null ? extras.getString("budget") : "";
        String duration = extras != null ? extras.getString("duration") : "";
        boolean culture = extras != null && extras.getBoolean("culture");
        boolean leisure = extras != null && extras.getBoolean("leisure");
        boolean food = extras != null && extras.getBoolean("food");
        String effort = extras != null ? extras.getString("effort") : "";

        // Action des boutons de choix de type de parcours
        View.OnClickListener typeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pathType = "";
                if (v.getId() == R.id.economicButton) {
                    pathType = "Économique";
                } else if (v.getId() == R.id.balancedButton) {
                    pathType = "Équilibré";
                } else if (v.getId() == R.id.comfortButton) {
                    pathType = "Confort";
                }

                // Passage à l'écran de récapitulatif du parcours
                Intent intent = new Intent(TravelPathTypeActivity.this, TravelPathSummaryActivity.class);
                intent.putExtra("budget", budget);
                intent.putExtra("duration", duration);
                intent.putExtra("culture", culture);
                intent.putExtra("leisure", leisure);
                intent.putExtra("food", food);
                intent.putExtra("effort", effort);
                intent.putExtra("pathType", pathType);
                startActivity(intent);
            }
        };

        economicButton.setOnClickListener(typeListener);
        balancedButton.setOnClickListener(typeListener);
        comfortButton.setOnClickListener(typeListener);
    }
}