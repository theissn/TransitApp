package com.example.iftekprojekt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

public class running extends Activity{
/*
    I denne activity kører trackeren, som holder øje med hvor langt der er til destinationen,
    og udregner hvor lang tid der er tilbage, samt aktiverer alarmen, når man kommer tæt på
    destinationen
 */

    private TextView destination, time, distance; //Tekst
    Button updateButton; //Knap til opdatering af brugerdefineret afstand
    EditText editDistance;//Tekstfelt
    Timer timer; //Timer
    GPSTracker gps;//GPS
    Location userLocation = new Location("");
    Location destinationLocation = new Location("");
    Float afstand;
    int alertDistance;
    int counter;
    Double totalSpeed;
    Double avgSpeed;
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.running);

        totalSpeed = 0.0;
        avgSpeed = 0.0;

        // Actionbar
        ActionBar actionBar = getActionBar();

        //GPS defineres
        gps = new GPSTracker(this);

        // Textviews og knapper og tekstfelt defineres
        destination = (TextView) findViewById(R.id.destination);
        time = (TextView) findViewById(R.id.time);
        distance = (TextView) findViewById(R.id.distance);
        updateButton = (Button) findViewById(R.id.update);
        editDistance = (EditText) findViewById(R.id.editText);



        // Skriver tid
        time.setText("Tid:");

        //Sætter timer igang, som kører noget bestemt kode hvert 10. sekund
        timer = new Timer();
        timer.schedule(new updateGPS(), 10000, 10000);

        // Skriver Afstand
        distance.setText("Afstand:");

        // Hent data om destnationen
        Intent setScreen = getIntent();
        Bundle dataDestination = setScreen.getExtras();

        if(dataDestination!=null) {
            String data = (String) dataDestination.get("data");
            Double lat = (Double) dataDestination.get("lat");
            Double lng = (Double) dataDestination.get("lng");
            String alert = (String) dataDestination.get("alert");
            alertDistance = Integer.parseInt(alert);

            destination.setText("Destination: " + data);

            destinationLocation.setLatitude(lat);
            destinationLocation.setLongitude(lng);
        }
        //Tjekker om GPS er aktiveret
        if(gps.canGetLocation()) {
            Double gpsLat = gps.getLatitude();
            Double gpsLng = gps.getLongitude();
            //Udregn afstand og skriv den
            userLocation.setLatitude(gpsLat);
            userLocation.setLongitude(gpsLng);
            afstand = destinationLocation.distanceTo(userLocation);
            distance.setText("Afstand: " + round(afstand/1000,2) + " km" );
        }
        else { //Vis besked, som fortæller at GPS er slået fra
            gps.showSettingsAlert();
        }
        //Når der trykkes opdater, hentes tekst fra editDistance og alertDistance sættes til den nye afstand
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editDistance.getText().toString().matches("")) {//Hvis der ikke er sat en afstand
                    AlertDialog noText = new AlertDialog.Builder(running.this)//Vis meddelse
                            .setTitle("Ingen afstand sat")
                            .setMessage("Husk at skrive en afstand!")
                            .setNeutralButton("Ok", null)
                            .show();
                }
                else {
                    alertDistance = Integer.parseInt(String.valueOf(editDistance.getText()));
                    Toast.makeText(running.this, "Afstand opdateret til: " + alertDistance + " meter", Toast.LENGTH_SHORT).show();
                    editDistance.setText("");
                }
            }

        });

    }
    //Hvis der trykkes tilbage
    @Override
    public void onBackPressed() {
        gotoMain();
    }

    //Hvis der trykkes tilbage, stoppes timeren og GPS'en, og brugeren sendes tilbage til startsiden
    private void gotoMain() {
        new AlertDialog.Builder(this)
                .setTitle("Stop Tracking?")
                .setMessage("Vil du stoppe traking?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        timer.cancel();
                        timer.purge();
                        gps.stopUsingGPS();
                        startActivity(new Intent(running.this, MainActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("Nej", null)
                .show();
    }

    //En TimerTask, som opdaterer brugerens placering, i et bestemt interval
    class updateGPS extends TimerTask {

        @Override
        public void run() {
            //Hvis GPS er aktiveret
            if(gps.canGetLocation()) {
                counter++;

                Double gpsLat = gps.getLatitude();
                Double gpsLng = gps.getLongitude();
                final Double speed = gps.getSpeed();
                if (speed != 0.0) {
                    totalSpeed += speed;
                    avgSpeed = totalSpeed / counter;
                }

                userLocation.setLatitude(gpsLat);
                userLocation.setLongitude(gpsLng);
                afstand = destinationLocation.distanceTo(userLocation);

                int timeLeft = (int) (afstand/avgSpeed);
                int hours = timeLeft/3600;
                int remainder = timeLeft%3600;
                int minutes = remainder / 60;
                int seconds = remainder % 60;
                //Sætter sekunder til tid i timer, minutter og sekunder
                final String newTime = (hours < 10 ? "0" : "") + hours
                        + ":" + (minutes < 10 ? "0" : "") + minutes
                        + ":" + (seconds< 10 ? "0" : "") + seconds;
                //Hvis afstanden er mindre end den brugerdefinerede afstand
                //stoppes timer og GPS, og alarm sættes igang
                if (afstand<alertDistance) {
                    timer.cancel();
                    timer.purge();
                    gps.stopUsingGPS();
                    Intent intent = new Intent(running.this, AlarmActivity.class);
                    startActivity(intent);
                }
                //En timertask kører ikke på mainThread, men kun ting, som kører på mainthread
                // må opdatere UI, så det, som opdaterer UI, er sat til at køre på mainThread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Hvis den gennemsnitlige tid er 0
                        if(avgSpeed != 0.0)
                            time.setText("Tid: " + newTime); //Sæt tiden til den udregnede tid
                        else //Ellers skrives der beregner, indtil gennemsnitstiden kommer over 0, da der ellers ville stå infinity
                            time.setText("Tid: Beregner..");
                        distance.setText("Afstand: " + round(afstand/1000,2) + " km");

                    }
                });

            }
            else {
                gps.showSettingsAlert();
            }




        }
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

}
