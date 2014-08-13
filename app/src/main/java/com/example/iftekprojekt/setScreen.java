package com.example.iftekprojekt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class setScreen extends Activity{
/*
    Her indstiller brugeren alarmen. Destinationens navn vises, og der vises hvor lang
    afstanden fra brugern til destinationen er. Brugeren skal skrive hvor stor afstand fra destinationen
    han vil have at app'en vækker ham på.
 */

    private TextView destination, distance; //Destinationens navn
    private Button start,addFavorite; //Start-knap
    private EditText alertDistance; //Tekst-felt
    String[] names, lats, lngs;
    Location userLocation = new Location("");
    ArrayList<String> namesList, latsList, lngsList;
    Double lat,lng;
    GPSTracker gps;
    boolean isFavorite = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.set_screen);

        namesList = new ArrayList<String>();
        latsList = new ArrayList<String>();
        lngsList = new ArrayList<String>();

        gps = new GPSTracker(this);
        if(gps.canGetLocation()){
            Double gpsLat = gps.getLatitude();
            Double gpsLng = gps.getLongitude();

            userLocation.setLatitude(gpsLat);
            userLocation.setLongitude(gpsLng);
        }
        else
            gps.showSettingsAlert();






        // Laver actionbar
        ActionBar actionBar = getActionBar();

        // Tilbage-knap
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Textviews og knapper defineres
        destination = (TextView) findViewById(R.id.destination);
        distance = (TextView) findViewById(R.id.distance);
        start = (Button) findViewById(R.id.startTracking);
        alertDistance = (EditText) findViewById(R.id.editText);
        addFavorite = (Button) findViewById(R.id.addFavorite);

        // Afstand

        distance.setText("Afstand: ");


        // Hent data, som blev sendt fra anden activity
        final Intent setScreen = getIntent();
        final Bundle dataDestination = setScreen.getExtras();

        if (dataDestination != null) {
            String item = (String) dataDestination.get("item");
            lat = (Double) dataDestination.get("lat");
            lng = (Double) dataDestination.get("lng");
            destination.setText("Destination: " + item);
            Location destinationLocation = new Location("");
            destinationLocation.setLatitude(lat);
            destinationLocation.setLongitude(lng);
            distance.setText("Afstand: " + round(userLocation.distanceTo(destinationLocation)/1000,2) + " km");
        }

        final String data = (String) dataDestination.get("item");

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRunning();

            }

        });
        //Hvis der trykkes på tilføj til foretrukne
        addFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(setScreen.this);
                String name = sharedPreferences.getString("Names", null);
                String latitude = sharedPreferences.getString("Lats", null);
                String longitude = sharedPreferences.getString("Lngs", null);

                StringBuilder nameList = new StringBuilder();
                StringBuilder latList = new StringBuilder();
                StringBuilder lngList = new StringBuilder();



                    if (name == null) { //Er der ingen foretrukne i forvejen

                        nameList.append(dataDestination.get("item"));
                        latList.append(lat);
                        lngList.append(lng);

                    } else {
                        if (name.contains(",")) { //Er der flere end en
                            names = name.split(",");
                            lats = latitude.split(",");
                            lngs = longitude.split(",");

                            if(isFavorite){ //Er den allerede en foretrukken, bliver den fjernet fra foretrukne
                                for(int i = 0; i < names.length; i++){
                                    namesList.add(names[i]);
                                    latsList.add(lats[i]);
                                    lngsList.add(lngs[i]);
                                }

                                int i = namesList.indexOf(dataDestination.get("item"));

                                namesList.remove(i);
                                latsList.remove(i);
                                lngsList.remove(i);

                                names = new String[namesList.size()];
                                lats = new String[latsList.size()];
                                lngs = new String[lngsList.size()];

                                names = namesList.toArray(names);
                                lats = latsList.toArray(lats);
                                lngs = lngsList.toArray(lngs);

                                addFavorite.setText("Tilføj til foretrukne");
                                Toast.makeText(setScreen.this, dataDestination.get("item") + " er blevet fjernet fra foretrukne!", Toast.LENGTH_SHORT).show();

                            }

                            for (int i = 0; i < names.length; i++) {
                                nameList.append(names[i]).append(",");
                            }
                            if(!isFavorite) {
                                nameList.append(dataDestination.get("item"));
                            }

                            for (int i = 0; i < lats.length; i++) {
                                latList.append(lats[i]).append(",");
                            }
                            if(!isFavorite) {
                                latList.append(lat);
                            }


                            for (int i = 0; i < lngs.length; i++) {
                                lngList.append(lngs[i]).append(",");
                            }

                            if(!isFavorite) {
                                lngList.append(lng);
                            }

                        } else { //Hvis der kun er én foretrukken
                            nameList.append(name).append(",");
                            if(!isFavorite) {
                                nameList.append(dataDestination.get("item"));
                            }
                            latList.append(latitude).append(",");
                            if(!isFavorite) {
                                latList.append(lat);
                            }
                            lngList.append(longitude).append(",");
                            if(!isFavorite) {
                                lngList.append(lng);
                            }

                        }
                    }


                    //Gem foretrukne lokalt på telefon
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("Names", nameList.toString());
                    editor.putString("Lats", latList.toString());
                    editor.putString("Lngs", lngList.toString());

                    editor.commit();

                    Toast.makeText(setScreen.this, dataDestination.get("item") + " er blevet tilføjet til foretrukne!", Toast.LENGTH_SHORT).show();
                    addFavorite.setText("Fjern fra foretrukne");


                }


        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(setScreen.this);
        String name = sharedPreferences.getString("Names", null);
        StringBuilder nameList = new StringBuilder();

        if(name!=null){ //Der tjekkes om den destination, man er inde på, allerede er foretrukken
            if(!name.contains(",")) {
                if(name.equals(dataDestination.get("item"))){ //Hvis navnet findes i listen over foretrukne
                    isFavorite = true;
                    addFavorite.setText("Fjern fra foretrukne");//Ændrer teksten på knappen

                }
            }
            else {
                String[] names = name.split(",");

                for(int i = 0; i<names.length; i++){
                    if(names[i].equals(dataDestination.get("item"))){
                        isFavorite = true;
                        addFavorite.setText("Fjern fra foretrukne");
                        break;
                    }

                }

            }
        }

    }

    private void goToRunning() { //Gå videre til næste activity
        if (alertDistance.getText().toString().matches("")) {//Hvis der ikke er sat en afstand
            AlertDialog noText = new AlertDialog.Builder(this)//Vis meddelse
                    .setTitle("Ingen afstand sat")
                    .setMessage("Husk at skrive en afstand, som du vil vækkes på")
                    .setNeutralButton("Ok", null)
                    .show();
        } else {
            //Bekræftelse, sender information videre om navn og koordinater, samt brugerdefineret afstand
            AlertDialog show = new AlertDialog.Builder(this)
                    .setTitle("Start Tracking?")
                    .setMessage("Vil du starte tracking?")
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent setScreen = getIntent();
                            Bundle dataDestination = setScreen.getExtras();
                            final String data = (String) dataDestination.get("item");
                            Intent runningNow = new Intent(com.example.iftekprojekt.setScreen.this, running.class);
                            runningNow.putExtra("data", data);
                            runningNow.putExtra("lat", lat);
                            runningNow.putExtra("lng", lng);
                            runningNow.putExtra("alert", String.valueOf(alertDistance.getText()));
                            gps.stopUsingGPS();
                            startActivity(runningNow);
                        }
                    })
                    .setNegativeButton("Nej", null)
                    .show();
    }
    }
    //Hvis der trykkes tilbage
    public void onBackPressed() {
        gps.stopUsingGPS();//Stop gps'en
        startActivity(new Intent(setScreen.this, MainActivity.class));
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }


}

