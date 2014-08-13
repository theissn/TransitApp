package com.example.iftekprojekt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;
/*
    Dette er alarmen. Den spiller en standard lyd og/eller vibrerer, alt efter hvad brugeren
    har sat i indstillingerne. Den er også sat til at vække telefoner fra Sleepmode.
 */

public class AlarmActivity extends Activity {
    private MediaPlayer mMediaPlayer;
    private PowerManager.WakeLock mWakeLock;
    private Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean sound = sharedPreferences.getBoolean("sound", true);
        final boolean vibrate = sharedPreferences.getBoolean("vibrate", true);

        //PowerManager, som bruger wakelock til at vække telefonen
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | //Fuld skærm
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | //Vis selvom telefonen er låst
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, //Tænd for skærmen
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        setContentView(R.layout.activity_alarm);

        //Når der trykkes på stop knappen, stoppes lyd og vibrering, og brugeren sendes
        //tilbage til hovedskærmen, medmindre telefonen er låst. Hvis telefonen er låst,
        //sendes brugeren to hvovedskærmen, når telefonen låses op.
        Button stopAlarm = (Button) findViewById(R.id.stopAlarm);
        stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sound) {
                    mMediaPlayer.stop();
                }

                if(vibrate){
                    vib.cancel();
                }

                Intent intent = new Intent(AlarmActivity.this, MainActivity.class);
                startActivity(intent);
                finish();


            }
        });



        if(sound) {
            playSound(this, getAlarmUri());
        }

        if(vibrate){
            vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 500, 1000};
            vib.vibrate(pattern, 0);
        }



    }
    //Funktion, som spiller den alarm, som er sat til standard
    private void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private Uri getAlarmUri() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);//Hvis der er en standard alarm
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);//Hvis der ikke er en standard alarm bruges standard notifications-lud
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);//Hvis ingen af de to andre findes, bruges ringetonen
            }
        }
        return alert;
    }

    protected void onStop() {
        super.onStop();
        try {
            mWakeLock.release();
        } catch (Throwable th) {

        }
    }

}


