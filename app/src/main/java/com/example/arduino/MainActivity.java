package com.example.arduino;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    double lat, lon;//위도 경도 값
    private GpsInfo gps;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    TextView temp,city,date,weather,humidity,wind;

    private Button switchBtn; // 온오프 버튼 테스트용

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temp = (TextView)findViewById(R.id.temp);
        city = (TextView)findViewById(R.id.city);
        date = (TextView)findViewById(R.id.date);
        weather = (TextView)findViewById(R.id.weather);
        humidity = (TextView)findViewById(R.id.humidity);
        wind=(TextView)findViewById(R.id.wind);

        gps = new GpsInfo(MainActivity.this);
        MyAsyncTask myAsyncTask=new MyAsyncTask();
        myAsyncTask.execute();

        switchBtn = findViewById(R.id.switchBtn);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread senderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String serverIP = "192.168.0.7"; // 추후에 변경
                            int serverPort = 8090; // 추후에 변경
                            socket = new Socket(serverIP, serverPort);
                        }
                        catch (UnknownHostException e) {
                            Log.e("SenderThread", e.getMessage());
                        }
                        catch (IOException e) {
                            Log.e("SenderThread", e.getMessage());
                        }

                        if (socket != null){
                            try {
                                PrintWriter sendSignal = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
                                sendSignal.println("A"); // 이 괄호 안에 명령어 다시 정하자
                                sendSignal.flush();
                                // 소켓 닫는 코드도 이 부분 지나서 추가하자.

                            }
                            catch (IOException e) {
                                Log.e("SenderThread", e.getMessage());
                            }
                        }
                        else {
                            Log.e("SenderThread", "Creating Socket is failed");
                        }

                        if (socket != null) {
                            try {
                                socket.close();
                            }
                            catch (IOException e) {
                                Log.e("SenderThread", e.getMessage());
                            }
                        }
                    }
                });
                senderThread.start();
                switchBtn.setBackgroundColor(Color.RED);
                switchBtn.setBackgroundResource(R.drawable.button_red);
            }
        });
    }


    private void fine_weather(String url) {
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    Log.e("대충","try");
                    JSONObject main_object=response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object=array.getJSONObject(0);
                    JSONObject wind_object=response.getJSONObject("wind");
                    String wind_speed=String.valueOf(wind_object.getDouble("speed"));
                    String mtemp = String.valueOf(main_object.getDouble("temp"));
                    String mhumi = String.valueOf(main_object.getDouble("humidity"));
                    String mdes = object.getString("description");
                    String mcity = response.getString("name");

                    city.setText(mcity);
                    WeatherHangeul weatherHangeul = new WeatherHangeul(mdes);
                    mdes=weatherHangeul.getWeather();
                    weather.setText(mdes);
                    humidity.setText(mhumi);
                    wind.setText(wind_speed);

                    SimpleDateFormat form=new SimpleDateFormat("yyyy년 MM월 dd일");
                    Date day0date=new Date();
                    String sdf=form.format(day0date);
                    date.setText(sdf);

                    double temp_int = Double.parseDouble(mtemp);
                    double centi = (temp_int-32)/1.8000;
                    centi=Math.round(centi);
                    int i=(int)centi;
                    temp.setText(i+"°C");

                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onError","Response");
            }
        });
        Log.e("queue","add");
        RequestQueue queue= Volley.newRequestQueue(this);
        queue.add(jor);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    // 전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    public class MyAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... integers) {

            lat = gps.getLatitude();
            lon = gps.getLongitude();
            callPermission();  // 권한 요청을 해야 함
            String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon +
                    "&units=metric&appid=25101ddb40fe8f611b992f17f1d60b23";
            Log.e("url=", url);
            return url;
        }

        @Override
        protected void onPostExecute(String url) {
            super.onPostExecute(url);
            fine_weather(url);
            Log.e("onresume","의 마지막부분");
        }
    }
}