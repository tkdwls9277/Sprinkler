package com.example.arduino;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.IOException;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();
    }
    public void Initialize()
    {
        tvSolar = findViewById(R.id.textView1);
        tvSolar = findViewById(R.id.textView2);

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
                switchBtn.setBackgroundResource(R.drawable.button_red);
            }
        });

        tv_WeatherInfo = findViewById(R.id.tv_WeatherInfo);
        mWeatherInfomation = new ArrayList<>();
        mThis = this;
        mForeCast = new ForeCastManager(lon,lat,mThis);
        mForeCast.run();
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