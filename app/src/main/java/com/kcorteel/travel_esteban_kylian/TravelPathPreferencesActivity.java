package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TravelPathPreferencesActivity extends AppCompatActivity {

    private EditText budgetEditText, durationEditText;
    private CheckBox cultureCheckBox, leisureCheckBox, foodCheckBox;
    private RadioGroup effortRadioGroup;
    private Button nextButton, loadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travelpath_preferences);

        // Initialisation des vues
        budgetEditText = findViewById(R.id.budgetEditText);
        durationEditText = findViewById(R.id.durationEditText);
        cultureCheckBox = findViewById(R.id.cultureCheckBox);
        leisureCheckBox = findViewById(R.id.leisureCheckBox);
        foodCheckBox = findViewById(R.id.foodCheckBox);
        effortRadioGroup = findViewById(R.id.effortRadioGroup);
        nextButton = findViewById(R.id.nextButton);
        loadButton = findViewById(R.id.loadButton);

        // Action du bouton "Suivant"
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String budget = budgetEditText.getText().toString().trim();
                String duration = durationEditText.getText().toString().trim();
                boolean culture = cultureCheckBox.isChecked();
                boolean leisure = leisureCheckBox.isChecked();
                boolean food = foodCheckBox.isChecked();
                int selectedEffortId = effortRadioGroup.getCheckedRadioButtonId();
                if (selectedEffortId == -1) {
                    Toast.makeText(TravelPathPreferencesActivity.this, "Veuillez sélectionner un niveau d'effort.", Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton selectedEffort = findViewById(selectedEffortId);
                String effort = selectedEffort != null ? selectedEffort.getText().toString() : "Non spécifié";

                if (budget.isEmpty() || duration.isEmpty()) {
                    Toast.makeText(TravelPathPreferencesActivity.this, "Veuillez renseigner budget et durée.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!culture && !leisure && !food) {
                    Toast.makeText(TravelPathPreferencesActivity.this, "Sélectionnez au moins une activité.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Passage à l'écran de choix du type de parcours
                Intent intent = new Intent(TravelPathPreferencesActivity.this, TravelPathTypeActivity.class);
                intent.putExtra("budget", budget);
                intent.putExtra("duration", duration);
                intent.putExtra("culture", culture);
                intent.putExtra("leisure", leisure);
                intent.putExtra("food", food);
                intent.putExtra("effort", effort);
                startActivity(intent);
            }
        });

        loadButton.setOnClickListener(v -> {
            Intent intent = new Intent(TravelPathPreferencesActivity.this, TravelPathLoadActivity.class);
            startActivity(intent);
        });
    }
}
