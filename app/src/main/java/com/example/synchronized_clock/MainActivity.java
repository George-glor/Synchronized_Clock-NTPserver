package com.example.synchronized_clock;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Handler handler;

    private SimpleDateFormat timeFormat;
    private TextView timeTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeTextView = findViewById(R.id.timeTextView);

        handler = new Handler();

        //skapa tid
        Updatethetime();

        // Update every sec
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getNetWorktime();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void Updatethetime() {
        if (isThenetworkavelable()) {
            getNetWorktime();
        } else {
            getSystemTime();
        }
    }

    private void getSystemTime() {  // funktion för att få tid information från mobile system
        Date date = new Date(System.currentTimeMillis());
        String time = timeFormat.format(date);
        updateTimeText("System Tid: " + time, Color.parseColor("#0000FF"));
    }

    private void getNetWorktime() {  //NTP server some ta information från google time
        NTPUDPClient ntp = new NTPUDPClient();
        Thread networkTimeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                InetAddress inetAddress = InetAddress.getByName ("1.se.pool.ntp.org");
                    TimeInfo timeInfo  = ntp.getTime(inetAddress);
            Long NTPTime = timeInfo.getMessage(). getTransmitTimeStamp() .getTime();
            Date date = new Date (NTPTime);
            System.out.println("getTime) returning NTServer time: " + date);
                        runOnUiThread(() -> {
                        updateTimeText("Nätvärk Tid: " + date, Color.BLACK);
                    });
                } catch (Exception e) {
                    e.printStackTrace();


                    runOnUiThread(() -> {
                        getSystemTime();
                    });
                }
            }
        });
        networkTimeThread.start();
    }


        private void updateTimeText(String text, int color) { // text storlek
        runOnUiThread(() -> {
            timeTextView.setText(text);
            if (text.startsWith("System tid")) {
                timeTextView.setTextColor(Color.BLUE);
                timeTextView.setTextSize(60);
            } else {
                timeTextView.setTextColor(color);
            }
        });
    }

    private boolean isThenetworkavelable() {  // is the network avelable
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        } else {
            return false;
        }
    }
}
