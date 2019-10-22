package com.example.arduino;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    double lat, lon; //위도 경도 값
    private GpsInfo gps;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    TextView temp,city,date,weather,humidity,wind,sunset,sunrise;
    ImageView icon,menu_image;

    private Button amButton;
    private ImageButton switchBtn; // 온오프 버튼 테스트용
    private ImageView waterTank;

    private Socket socket;
    private boolean isConnected = false;
    private String serverIP = "192.168.43.28"; // IP 수정
    private int serverPort = 8080; // 포트번호 수정

    private Thread receiverThread;
    private BufferedReader bufferedReader;
    private int onOffStatus;
    private int autoManualStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temp = (TextView) findViewById(R.id.temp);
        city = (TextView) findViewById(R.id.city);
        date = (TextView) findViewById(R.id.date);
        weather = (TextView) findViewById(R.id.weather);
        humidity = (TextView) findViewById(R.id.humidity);
        wind = (TextView) findViewById(R.id.wind);
        sunrise=(TextView)findViewById(R.id.sunrise);
        sunset=(TextView)findViewById(R.id.sunset);
        waterTank = findViewById(R.id.waterTankStatus);
        icon=(ImageView)findViewById(R.id.icon);

        gps = new GpsInfo(MainActivity.this);
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
        Initialize();

        new Thread(new ConnectThread(serverIP, serverPort, "y")).start();
    }
    public void Initialize(){
        switchBtn = findViewById(R.id.switchBtn);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 모터를 ON, OFF하는 버튼

                new Thread(new SenderThread("m")).start();
                if (socket.isClosed()) {
                    new Thread(new ConnectThread(serverIP, serverPort, "m")).start();

                }
            }
        });
        amButton = findViewById(R.id.amButton);
        amButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 모터를 자동, 수동으로 바꾸는 버튼
                new Thread(new SenderThread("A")).start();
                if (socket.isClosed()) {
                    new Thread(new ConnectThread(serverIP, serverPort, "A")).start();

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(new SenderThread("E")).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new Thread(new SenderThread("E")).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("onResume", "yes");
    }

    private class ConnectThread implements Runnable { // 어플과 아두이노를 연결한다

        private String serverIP;
        private int serverPort;
        private String msg;
        public ConnectThread(String ip, int port, String msg) {
            serverIP = ip;
            serverPort = port;
            this.msg = msg;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(serverIP, serverPort);
            }
            catch( UnknownHostException e )
            {
                Log.e("ConnectThread",  "can't find host");
            }
            catch( SocketTimeoutException e )
            {
                Log.e("ConnectThread", "ConnectThread: timeout");
            }
            catch (Exception e) {

                Log.e("ConnectThread", e.getMessage());
            }


            if (socket != null) {
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                    PrintWriter sendSignal = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
                    sendSignal.println(msg);
                    sendSignal.flush();

                    isConnected = true;
                    Log.e("MainActivity", "ConnectThread");
                }
                catch (IOException e) {
                    Log.e("ConnectThread", e.getMessage());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isConnected) {
                            receiverThread = new Thread(new ReceiverThread());
                            receiverThread.start();
                            Log.e("ConnectThread", "ReceiverThread start");
                        }
                    }
                });

            }
            else {
                Log.e("ConnectThread","Socket is null");

            }

        }
    }

    private class ReceiverThread implements Runnable { // 아두이노에서 보낸 데이터를 받는다

        @Override
        public void run() {
            try {
                while (isConnected) {
                    Log.e("MainActivity", "ReceiverThread");
                    if (bufferedReader == null) {
                        Log.e("ReceiverThread", "bufferedReader is null");
                        break;
                    }

                    final String recvMessage = bufferedReader.readLine();
                    if (recvMessage != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String[] parsing = recvMessage.split("L"); // 데이터를 L을 기준으로 구분
                                onOffStatus = Integer.parseInt(parsing[0]); // 모터 상태에 대한 데이터가 들어오는 곳

                                switch (onOffStatus) { // ON OFF 상태에 따라 버튼 색깔 지정
                                    case 0:
                                        switchBtn.setBackgroundResource(R.drawable.button_red);
                                        break;
                                    case 1:
                                        switchBtn.setBackgroundResource(R.drawable.button_green);
                                        break;

                                }

                                int waterTankValue = Integer.parseInt(parsing[1]); // 수위량 데이터가 들어오는 곳

                                // 수위량에 따라 이미자가 바뀐다
                                if (waterTankValue < 300) waterTank.setImageResource(R.drawable.watertank_quater1);
                                else if (waterTankValue >= 300 && waterTankValue < 550) waterTank.setImageResource(R.drawable.watertank_quater2);
                                else if (waterTankValue >= 550 && waterTankValue < 750) waterTank.setImageResource(R.drawable.watertank_quater2);
                                else waterTank.setImageResource(R.drawable.watertank_full);

                                autoManualStatus = Integer.parseInt(parsing[2]); // 자동, 수동 상태에 대한 데이터가 들어오는 곳

                                switch (autoManualStatus) { // 자동, 수동 상태에 따라 텍스트 수정
                                    case 0:
                                        amButton.setText("AUTO");
                                        break;
                                    case 1:
                                        amButton.setText("MANUAL");
                                        break;
                                }

                            }
                        });
                    }
                }
            }
            catch (IOException e) {
                Log.e("ReceiverThread", e.getMessage());
            }


            if (socket != null) {
                try {
                    socket.close();

                } catch (IOException e) {
                    Log.e("ReceiverThread", e.getMessage());
                }
            }
        }

    }

    private class SenderThread implements Runnable {

        private String msg;

        public SenderThread (String msg) {
            this.msg = msg;
        } // 어플에서 아두이노로 데이터를 보내준다
        @Override
        public void run() {
            if (socket != null){
                try {
                    PrintWriter sendSignal = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
                    sendSignal.println(msg);
                    sendSignal.flush();

                }
                catch (IOException e) {
                    Log.e("SenderThread", e.getMessage());
                }
            }
            else {
                Log.e("SenderThread", "wtf");
            }

            if (msg.equals("E")) isConnected = false; // 종료 코드
        }
    }

    public void onClickView(View v) { // 레이아웃 전환 테스트용
        switch (v.getId()) {
            case R.id.soilImage:{
                Intent intent = new Intent(this, SoilActivity.class);
                isConnected = false;
                startActivity(intent);
                break;
            }
            case R.id.waterTankStatus:{
                Intent intent = new Intent(this, WaterActivity.class);
                isConnected = false;
                startActivity(intent);
                break;
            }
            case R.id.tv_WeatherInfo:{
                Intent intent = new Intent(this, Weather5days.class);
                isConnected = false;
                startActivity(intent);
                break;
            }
            case R.id.menu:{
                Log.e("click","이벤트");
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                Menu menu = popupMenu.getMenu();

                inflater.inflate(R.menu.popupmenu, menu);

                popupMenu.setOnMenuItemClickListener
                        (new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent intent;
                                switch (item.getItemId()) {
                                    case R.id.mainmenu:
                                        intent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        return true;
                                    case R.id.weathermenu:
                                        intent = new Intent(MainActivity.this, Weather5days.class);
                                        startActivity(intent);
                                        return true;
                                    case R.id.soilmenu:
                                        intent = new Intent(MainActivity.this, SoilActivity.class);
                                        startActivity(intent);
                                        return true;
                                    case R.id.watermenu:
                                        intent = new Intent(MainActivity.this, WaterActivity.class);
                                        startActivity(intent);
                                        return true;
                                }
                                return false;
                            }
                        });
                popupMenu.show();
                break;
            }
        }
    }


    private void fine_weather(String url) {
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            private Context onResponse;

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

                    String iconimage=object.getString("icon");
                    String iconurl="http://openweathermap.org/img/w/" + iconimage + ".png";
                    Log.e("iconurl",iconurl);

                    Picasso.get().load(iconurl).into(icon);
                    Log.e("image","mainweather");

                    city.setText(mcity);
                    WeatherHangeul weatherHangeul = new WeatherHangeul(mdes);
                    mdes=weatherHangeul.getWeather();
                    weather.setText(mdes);
                    humidity.setText(mhumi+"%");
                    wind.setText(wind_speed+"m/s");

                    JSONObject sys=response.getJSONObject("sys");
                    Long sunr =sys.getLong("sunrise");
                    Long suns =sys.getLong("sunset");
                    sunrise.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunr*1000)));
                    sunset.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(suns*1000)));

                    SimpleDateFormat form=new SimpleDateFormat("yyyy년 MM월 dd일 HH시");
                    Date day0date=new Date();
                    String sdf=form.format(day0date);
                    date.setText(sdf);

                    double temp_int = Double.parseDouble(mtemp);
                    int i=(int)temp_int;
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