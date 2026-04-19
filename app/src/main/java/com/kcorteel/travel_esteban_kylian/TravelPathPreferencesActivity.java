package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;

public class TravelPathPreferencesActivity extends AppCompatActivity {

    private EditText budgetEditText, durationEditText;
    private CheckBox cultureCheckBox, leisureCheckBox, foodCheckBox;
    private RadioGroup effortRadioGroup;
    private Button nextButton;

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

        // Action du bouton "Suivant"
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupération des préférences
                String budget = budgetEditText.getText().toString();
                String duration = durationEditText.getText().toString();
                boolean culture = cultureCheckBox.isChecked();
                boolean leisure = leisureCheckBox.isChecked();
                boolean food = foodCheckBox.isChecked();
                int selectedEffortId = effortRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedEffort = findViewById(selectedEffortId);
                String effort = selectedEffort != null ? selectedEffort.getText().toString() : "Non spécifié";

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
    }
}