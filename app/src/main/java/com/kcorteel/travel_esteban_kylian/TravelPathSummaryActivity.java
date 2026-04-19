package com.kcorteel.travel_esteban_kylian;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TravelPathSummaryActivity extends AppCompatActivity {

    private TextView summaryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travelpath_summary);

        // Initialisation des vues
        summaryTextView = findViewById(R.id.summaryTextView);

        // Récupération des données des écrans précédents
        Bundle extras = getIntent().getExtras();
        String budget = extras != null ? extras.getString("budget") : "";
        String duration = extras != null ? extras.getString("duration") : "";
        boolean culture = extras != null && extras.getBoolean("culture");
        boolean leisure = extras != null && extras.getBoolean("leisure");
        boolean food = extras != null && extras.getBoolean("food");
        String effort = extras != null ? extras.getString("effort") : "";
        String pathType = extras != null ? extras.getString("pathType") : "";

        // Affichage du récapitulatif
        String summary = String.format(
                "Type de parcours: %s\n\nBudget: %s €\nDurée: %s heures\nActivités: %s%s%s\nNiveau d'effort: %s",
                pathType, budget, duration,
                culture ? "Culture, " : "",
                leisure ? "Loisirs, " : "",
                food ? "Nourriture" : "",
                effort
        );

        summaryTextView.setText(summary);
    }
}