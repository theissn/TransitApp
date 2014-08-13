package com.example.iftekprojekt;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import static android.location.LocationManager.GPS_PROVIDER;

public class GPSTracker extends Service implements LocationListener {
/*
    En class, som kan bruges til at finde brugerens placering og hastighed.
 */
    private final Context mContext;

    // GPS status
    boolean isGPSEnabled = false;

    // Netværk status
    boolean isNetworkEnabled = false;

    // GPS status
    boolean canGetLocation = false;

    Location location; // Lokalitet
    double latitude; // latitude
    double longitude; // longitude

    // Minimim afstand, som brugeren skal have flyttet sig, før der opdateres
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meter

    // Minimum tid, der skal gå, før der opdateres igen
    private static final long MIN_TIME_BW_UPDATES = 0; // 0 betyder at der opdateres hver gang det er muligt

    // Location manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }



    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // Er GPS aktiveret
            isGPSEnabled = locationManager
                    .isProviderEnabled(GPS_PROVIDER);

            // Er netværk aktiveret
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {
                this.canGetLocation = true;
                // Hvis netværk er aktiveret og GPS ikke er
                if (isNetworkEnabled && !isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // Hvis GPS er aktiveret
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    //Funktion, som stopper GPS'en
    public void stopUsingGPS(){
        if(locationManager != null){

            locationManager.removeUpdates(GPSTracker.this);

            locationManager = null;
            location = null;

        }
    }

   //Funktion til at hente latitude
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        return latitude;
    }
    //Funktion til at hente hastighed
    public double getSpeed(){
        if(location != null){
            latitude = location.getSpeed();
        }
        return latitude;
    }


   //Funktion til at hente longitude
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        return longitude;
    }

   //Funktion til at tjekke om GPS er aktiveret
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    //Funktion til at vise Alert, om at GPS ikke er aktiveret
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Dialog title
        alertDialog.setTitle("GPS is settings");

        // Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Hvis der trykkes på settings, sendes man til indstillinger, hvor man kan aktivere GPS
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // Hvis der trykkes cancel
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Vis Dialog
        alertDialog.show();
    }

    //Hvis brugerplacering ændres, sættes location til de nyeste koordinater
    @Override
    public void onLocationChanged(Location gpslocation) {
        if(isGPSEnabled) {
            location = locationManager
                    .getLastKnownLocation(GPS_PROVIDER);
        }
        else {
            location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}