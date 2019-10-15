package com.example.arduino;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {
    public static final int THREAD_HANDLER_SUCCESS_INFO = 1;
    private TextView tv_WeatherInfo;

    private ForeCastManager mForeCast;
    private String lon = "128.3910799"; // 좌표 설정
    private String lat = "36.1444292";  // 좌표 설정
    private MainActivity mThis;
    private ArrayList<ContentValues> mWeatherData;
    private ArrayList<WeatherInfo> mWeatherInfomation;

    private TextView tvSolar; // 레이아웃 전환 테스트용
    private TextView tvWater; // 레이아웃 전환 테스트용

    private Button switchBtn; // 온오프 버튼 테스트용

    private Socket socket;
    private boolean isConnected = false;
    private String serverIP = "117.16.152.128";
    private int serverPort = 8080;

    private Thread receiverThread;
    private BufferedReader bufferedReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();

        new Thread(new ConnectThread(serverIP, serverPort)).start();
    }
    public void Initialize()
    {
        tvSolar = findViewById(R.id.textView1);
        tvSolar = findViewById(R.id.textView2);

        switchBtn = findViewById(R.id.switchBtn);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new SenderThread("y")).start();
                switchBtn.setBackgroundResource(R.drawable.button_red);
            }
        });

        tv_WeatherInfo = findViewById(R.id.tv_WeatherInfo);
        mWeatherInfomation = new ArrayList<>();
        mThis = this;
        mForeCast = new ForeCastManager(lon,lat,mThis);
        mForeCast.run();
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

    private class ConnectThread implements Runnable {

        private String serverIP;
        private int serverPort;

        public ConnectThread(String ip, int port) {
            serverIP = ip;
            serverPort = port;
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
                    sendSignal.println("y");
                    sendSignal.flush();

                    isConnected = true;
                }
                catch (IOException e) {
                    Log.e("ConnectThread", e.getMessage());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isConnected) {
                            /*
                            connStatusView.setText("Connected to Server");
                            ipNumberView.setText("IP Number : " + serverIP);
                            portNumberView.setText("Port Number : " + serverPort);
                            */

                            receiverThread = new Thread(new ReceiverThread());
                            receiverThread.start();
                            Log.e("ConnectThread", "ReceiverThread start");
                        }
                    }
                });

            }
            else {
                Log.e("ConnectThread","Socket is null");

                /*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connStatusView.setText("Socket is Null");
                        ipNumberView.setText("F A I L E D");
                        portNumberView.setText("F A I L E D");
                    }
                });
                */
            }

        }
    }

    private class ReceiverThread implements Runnable {

        @Override
        public void run() {
            try {
                while (isConnected) {
                    Log.e("ReceiverThread", "while");
                    if (bufferedReader == null) {
                        Log.e("ReceiverThread", "bufferedReader is null");
                        break;
                    }

                    final String recvMessage = bufferedReader.readLine();
                    // Log.e("ReceiverThread", recvMessage);
                    if (recvMessage != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // 모터가 가동중일 때와 아닐 때 구분해서
                                // 버튼 색깔 지정

                            }
                        });
                    }

                }
                /*
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("ReceiverThread", e.getMessage());
                }
                */
            }
            catch (IOException e) {
                Log.e("ReceiverThread", e.getMessage());
            }


            if (socket != null) {
                try {
                    socket.close();

                    /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connStatusView.setText("Socket is Null");
                            ipNumberView.setText("F A I L E D");
                            portNumberView.setText("F A I L E D");
                        }
                    });
                    */
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
        }
        @Override
        public void run() {
            if (isConnected && socket != null){
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
                Log.e("SenderThread", "wtf"); // 뒤로가기 버튼을 누르면 (종료코드) 여기로 온다 왜 그럴까
            }

            if (msg.equals("E")) isConnected = false; // 종료 코드
        }
    }

    public void onClickView(View v) { // 레이아웃 전환 테스트용
        switch (v.getId()) {
            case R.id.textView1:{
                Intent intent = new Intent(this, SoilActivityTest.class);
                startActivity(intent);
                break;
            }
            case R.id.textView2:{
                Intent intent = new Intent(this, WaterActivityTest.class);
                startActivity(intent);
                break;
            }
        }
    }

    public String PrintValue()
    {
        String mData = "";
        for(int i = 0; i < mWeatherInfomation.size(); i ++)
        {
            mData = mData + mWeatherInfomation.get(i).getWeather_Day() + "\r\n"
                    +  mWeatherInfomation.get(i).getWeather_Name() + "\r\n"
                    +  mWeatherInfomation.get(i).getClouds_Sort()
                    +  " /Cloud amount: " + mWeatherInfomation.get(i).getClouds_Value()
                    +  mWeatherInfomation.get(i).getClouds_Per() +"\r\n"
                    +  mWeatherInfomation.get(i).getWind_Name()
                    +  " /WindSpeed: " + mWeatherInfomation.get(i).getWind_Speed() + " mps" + "\r\n"
                    +  "Max: " + mWeatherInfomation.get(i).getTemp_Max() + "℃"
                    +  " /Min: " + mWeatherInfomation.get(i).getTemp_Min() + "℃" +"\r\n"
                    +  "Humidity: " + mWeatherInfomation.get(i).getHumidity() + "%";

            mData = mData + "\r\n" + "----------------------------------------------" + "\r\n";
        }
        return mData;
    }

    public void DataChangedToHangeul()
    {
        for(int i = 0 ; i <mWeatherInfomation.size(); i ++)
        {
            WeatherToHangeul mHangeul = new WeatherToHangeul(mWeatherInfomation.get(i));
            mWeatherInfomation.set(i,mHangeul.getHangeulWeather());
        }
    }


    public void DataToInformation()
    {
        for(int i = 0; i < mWeatherData.size(); i++)
        {
            mWeatherInfomation.add(new WeatherInfo(
                    String.valueOf(mWeatherData.get(i).get("weather_Name")),  String.valueOf(mWeatherData.get(i).get("weather_Number")), String.valueOf(mWeatherData.get(i).get("weather_Much")),
                    String.valueOf(mWeatherData.get(i).get("weather_Type")),  String.valueOf(mWeatherData.get(i).get("wind_Direction")),  String.valueOf(mWeatherData.get(i).get("wind_SortNumber")),
                    String.valueOf(mWeatherData.get(i).get("wind_SortCode")),  String.valueOf(mWeatherData.get(i).get("wind_Speed")),  String.valueOf(mWeatherData.get(i).get("wind_Name")),
                    String.valueOf(mWeatherData.get(i).get("temp_Min")),  String.valueOf(mWeatherData.get(i).get("temp_Max")),  String.valueOf(mWeatherData.get(i).get("humidity")),
                    String.valueOf(mWeatherData.get(i).get("Clouds_Value")),  String.valueOf(mWeatherData.get(i).get("Clouds_Sort")), String.valueOf(mWeatherData.get(i).get("Clouds_Per")),String.valueOf(mWeatherData.get(i).get("day"))
            ));

        }

    }
    public Handler handler = new Handler(){
        @Override
        public void publish(LogRecord record) {

        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }

        /*
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case THREAD_HANDLER_SUCCESS_INFO :
                    mForeCast.getmWeather();
                    mWeatherData = mForeCast.getmWeather();
                    if(mWeatherData.size() ==0)
                        tv_WeatherInfo.setText("데이터가 없습니다");

                    DataToInformation(); // 자료 클래스로 저장,

                    String data = "";
                    data = PrintValue();
                    DataChangedToHangeul();
                    data = data + PrintValue();

                    tv_WeatherInfo.setText(data);
                    break;
                default:
                    break;
            }
        }
        */
    };
}