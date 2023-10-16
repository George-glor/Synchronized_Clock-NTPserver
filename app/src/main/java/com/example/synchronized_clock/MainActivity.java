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
        updateraklockan();

        // Update every sec
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getnätvarktid();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updateraklockan() {
        if (ärNätvarktillgänglig()) {
            getnätvarktid();
        } else {
            getSystemTid();
        }
    }

    private void getSystemTid() {
        Date date = new Date(System.currentTimeMillis());
        String time = timeFormat.format(date);
        updateTimeText("System Time: " + time, Color.parseColor("#FF00FF"));
    }

    private void getnätvarktid() {  //NTP server some ta information från google time
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
                    // InetAddress addr = InetAddress.getByName("time.google.com");
                    // ntp.open();
                    //TimeInfo info = ntp.getTime(addr);
                    // ntp.close();

                    //Date networkTime = new Date(timeInfo.getReturnTime());
                    // String timen = timeFormat.format(networkTime);

                    runOnUiThread(() -> {
                        updateTimeText("Network Tid: " + date, Color.BLACK);
                    });
                } catch (Exception e) {
                    e.printStackTrace();


                    runOnUiThread(() -> {
                        getSystemTid();
                    });
                }
            }
        });
        networkTimeThread.start();
    }
    /*
private Date getnätvarktid()
    {
        NTPUDPClient timeClient = new NTPUDPClient();
        timeClient.setDefaultTimeout (2000) ;
        TimeInfo timeInfo;
        try {
            InetAddress inetAddress = InetAddress.getByName ("1.se.pool.ntp.org");
            timeInfo = timeClient.getTime(inetAddress);
            Long NTPTime = timeInfo.getMessage(). getTransmitTimeStamp() .getTime();
            Date date = new Date (NTPTime);
            System.out.println("getTime) returning NTServer time: " + date);
            return date;


        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
*/
        private void updateTimeText(String text, int color) {
        runOnUiThread(() -> {
            timeTextView.setText(text);
            if (text.startsWith("System Time")) {
                timeTextView.setTextColor(Color.BLUE);
                timeTextView.setTextSize(60);
            } else {
                timeTextView.setTextColor(color);
            }
        });
    }

    private boolean ärNätvarktillgänglig() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        } else {
            return false;
        }
    }
}
