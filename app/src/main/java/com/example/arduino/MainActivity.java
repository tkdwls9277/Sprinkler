package com.example.arduino;

import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {
    public static final int THREAD_HANDLER_SUCCESS_INFO = 1;
    private TextView tv_WeatherInfo;

    private MainActivity mThis;

    private TextView tvSolar; // 레이아웃 전환 테스트용
    private TextView tvWater; // 레이아웃 전환 테스트용

    double lat,lon;//위도 경도 값
    private GpsTracker gt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    private void getWeatherData(double lat, double lng){
        String url="http://api.openweathermap.org/data/2.5/forecast/daily?lat="
                +lat+"&lon="+lng+"&units=metric&appid=25101ddb40fe8f611b992f17f1d60b23";

    }

    public void onClickView(View v) { // 레이아웃 전환 테스트용
        switch (v.getId()) {
            case R.id.textView1:{
                Intent intent = new Intent(this, SoilActivityTest.class);
                startActivity(intent);
            }
            case R.id.textView2:{
                Intent intent = new Intent(this, WaterActivityTest.class);
                startActivity(intent);
            }
        }
    }
}