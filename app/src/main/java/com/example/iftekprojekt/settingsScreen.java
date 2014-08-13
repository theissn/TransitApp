package com.example.iftekprojekt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

public class settingsScreen extends Activity {
/*
    Dette er indstillinger. Her kan brugeren slå lyd og vibration til og fra.
    Indstillingerne gemmes lokalt på telefonen, når brugeren trykker gem
 */
    private Button saveSettings;
    private Switch alarm, vibrate;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.settings_screen);


        // Tilføjer actionbaren
        ActionBar actionBar = getActionBar();

        // Laver en tilbageknap, som sender brugeren tilbage
        actionBar.setDisplayHomeAsUpEnabled(true);



        //Definerer knapper og kontakter
        saveSettings = (Button) findViewById(R.id.saveSettings);
        alarm = (Switch) findViewById(R.id.sound);
        vibrate = (Switch) findViewById(R.id.vibrate);

        //Når Der bliver trykket på gem-knappen, gemmes der data om lyd og vibration lokalt på telefonen
        //Brugeren sendes yderligere til MainActivity
        saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(settingsScreen.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("sound",alarm.isChecked());
                editor.putBoolean("vibrate",vibrate.isChecked());
                editor.commit();
                Toast.makeText(settingsScreen.this, "Gemt", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(settingsScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

        loadSavedPreferences();

    }

    private void loadSavedPreferences() { //Tjek hvilke indstillinger brugeren har sat, default er at begge kontakter er slået til
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean soundBox = sharedPreferences.getBoolean("sound", true);
        boolean vibrateEnabled = sharedPreferences.getBoolean("vibrate", true);
        if(soundBox)
            alarm.setChecked(true);
        else
            alarm.setChecked(false);
        if(vibrateEnabled)
            vibrate.setChecked(true);
        else
            vibrate.setChecked(false);
    }

}

