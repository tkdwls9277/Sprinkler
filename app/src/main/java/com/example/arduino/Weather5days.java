package com.example.arduino;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Weather5days extends AppCompatActivity {

    double lat, lon;//위도 경도 값
    private GpsInfo gps;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    TextView city, wind,sunrise,sunset;
    TextView day0datetxt,day1datetxt,day2datetxt,day3datetxt;
    TextView day0weathertxt,day1weathertxt,day2weathertxt,day3weathertxt;
    TextView day0temptxt,day1temptxt,day2temptxt,day3temptxt;
    TextView day0humitxt,day1humitxt,day2humitxt,day3humitxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        gps = new GpsInfo(Weather5days.this);
        Weather5days.MyAsyncTask myAsyncTask=new Weather5days.MyAsyncTask();
        myAsyncTask.execute();

        city=(TextView)findViewById(R.id.city);
        wind=(TextView)findViewById(R.id.day0wind);
        sunrise=(TextView)findViewById(R.id.day0sunrise);
        sunset=(TextView)findViewById(R.id.day0sunset);

        day0datetxt = (TextView)findViewById(R.id.day0date);
        day1datetxt = (TextView)findViewById(R.id.day1date);
        day2datetxt= (TextView)findViewById(R.id.day2date);
        day3datetxt = (TextView)findViewById(R.id.day3date);

        day0weathertxt = (TextView)findViewById(R.id.day0weather);
        day1weathertxt = (TextView)findViewById(R.id.day1weather);
        day2weathertxt = (TextView)findViewById(R.id.day2weather);
        day3weathertxt = (TextView)findViewById(R.id.day3weather);

        day0temptxt = (TextView)findViewById(R.id.day0temp);
        day1temptxt = (TextView)findViewById(R.id.day1temp);
        day2temptxt = (TextView)findViewById(R.id.day2temp);
        day3temptxt = (TextView)findViewById(R.id.day3temp);

        day0humitxt = (TextView)findViewById(R.id.day0humi);
        day1humitxt = (TextView)findViewById(R.id.day1humi);
        day2humitxt = (TextView)findViewById(R.id.day2humi);
        day3humitxt = (TextView)findViewById(R.id.day3humi);
    }

    private void fine_weather(String url) {
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    JSONArray lists=response.getJSONArray("list");
                    JSONObject day0=lists.getJSONObject(0);
                    JSONObject day1=lists.getJSONObject(8);
                    JSONObject day2=lists.getJSONObject(16);
                    JSONObject day3=lists.getJSONObject(24);
                    JSONObject day4=lists.getJSONObject(32);

                    //도시이름 파싱
                    JSONObject getcity=response.getJSONObject("city");
                    String cityname=getcity.getString("name");
                    city.setText(cityname);

                    Long sunr =getcity.getLong("sunrise");
                    Long suns =getcity.getLong("sunset");
                    sunrise.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunr*1000)));
                    sunset.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(suns*1000)));

                    //바람세기 파싱
                    Double windtxt=day0.getJSONObject("wind").getDouble("speed");
                    wind.setText(String.valueOf(windtxt));

                    //날씨 영문
                    String day0weather=day0.getJSONArray("weather").getJSONObject(0).getString("description");
                    String day1weather=day1.getJSONArray("weather").getJSONObject(0).getString("description");
                    String day2weather=day2.getJSONArray("weather").getJSONObject(0).getString("description");
                    String day3weather=day3.getJSONArray("weather").getJSONObject(0).getString("description");
                    String day4weather=day4.getJSONArray("weather").getJSONObject(0).getString("description");

                    //날씨 한글화
                    day0weathertxt.setText(new WeatherHangeul(day0weather).getWeather());
                    day1weathertxt.setText(new WeatherHangeul(day1weather).getWeather());
                    day2weathertxt.setText(new WeatherHangeul(day2weather).getWeather());
                    day3weathertxt.setText(new WeatherHangeul(day3weather).getWeather());

                    //온도
                    JSONObject day0main=day0.getJSONObject("main");
                    Double day0temp=day0main.getDouble("temp");
                    day0temptxt.setText(((int)(day0temp-273.15))+"°C");
                    day0humitxt.setText(String.valueOf(day0main.getInt("humidity")));
                    JSONObject day1main=day1.getJSONObject("main");
                    Double day1temp=day1main.getDouble("temp");
                    day1temptxt.setText(((int)(day1temp-273.15))+"°C");
                    day1humitxt.setText(String.valueOf(day1main.getInt("humidity")));
                    JSONObject day2main=day2.getJSONObject("main");
                    Double day2temp=day2main.getDouble("temp");
                    day2temptxt.setText(((int)(day2temp-273.15))+"°C");
                    day2humitxt.setText(String.valueOf(day2main.getInt("humidity")));
                    JSONObject day3main=day3.getJSONObject("main");
                    Double day3temp=day3main.getDouble("temp");
                    day3temptxt.setText(((int)(day3temp-273.15))+"°C");
                    day3humitxt.setText(String.valueOf(day3main.getInt("humidity")));


                    //달력에서 날짜 파싱
                    Calendar mcalendar = Calendar.getInstance();
                    Date day0date=mcalendar.getTime();
                    String sdf0=new SimpleDateFormat("yyyy년 MM월 dd일 HH시").format(day0date);
                    day0datetxt.setText(sdf0);
                    mcalendar.add(Calendar.DAY_OF_WEEK,1);
                    Date day1date=mcalendar.getTime();
                    String sdf1=new SimpleDateFormat("MM월 dd일").format(day1date);
                    day1datetxt.setText(sdf1);
                    mcalendar.add(Calendar.DAY_OF_WEEK,1);
                    Date day2date=mcalendar.getTime();
                    String sdf2=new SimpleDateFormat("MM월 dd일").format(day2date);
                    day2datetxt.setText(sdf2);
                    mcalendar.add(Calendar.DAY_OF_WEEK,1);
                    Date day3date=mcalendar.getTime();
                    String sdf3=new SimpleDateFormat("MM월 dd일").format(day3date);
                    day3datetxt.setText(sdf3);

                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue queue= Volley.newRequestQueue(this);
        queue.add(jor);
    }



    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
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
            String url = "https://api.openweathermap.org/data/2.5/forecast?"+
                    "lat=" + lat +
                    "&lon="+ lon +
                    "&appid=25101ddb40fe8f611b992f17f1d60b23" +
                    "&cnt=" + 33;
            Log.e("url=",url);
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