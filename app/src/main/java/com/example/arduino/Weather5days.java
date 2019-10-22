package com.example.arduino;

import android.Manifest;
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

    ImageView icon;
    TextView city, wind,sunrise,sunset;
    TextView day0datetxt,day1datetxt,day2datetxt,day3datetxt,day4datetxt;
    TextView day0weathertxt,day1weathertxt,day2weathertxt,day3weathertxt,day4weathertxt;
    TextView day0temptxt,day1temptxt,day2temptxt,day3temptxt,day4temptxt;
    TextView day0humitxt,day1humitxt,day2humitxt,day3humitxt,day4humitxt;
    TextView day0time0,day0time1,day0time2,day0time3,day0temp0,day0temp1,day0temp2,day0temp3;
    ImageView day0icon0,day0icon1,day0icon2,day0icon3;


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
        icon=(ImageView)findViewById(R.id.icon);

        day0datetxt = (TextView)findViewById(R.id.day0date);
        day1datetxt = (TextView)findViewById(R.id.day1date);
        day2datetxt= (TextView)findViewById(R.id.day2date);
        day3datetxt = (TextView)findViewById(R.id.day3date);
        day4datetxt = (TextView)findViewById(R.id.day4date);

        day0weathertxt = (TextView)findViewById(R.id.day0weather);
        day1weathertxt = (TextView)findViewById(R.id.day1weather);
        day2weathertxt = (TextView)findViewById(R.id.day2weather);
        day3weathertxt = (TextView)findViewById(R.id.day3weather);
        day4weathertxt = (TextView)findViewById(R.id.day4weather);

        day0temptxt = (TextView)findViewById(R.id.day0temp);
        day1temptxt = (TextView)findViewById(R.id.day1temp);
        day2temptxt = (TextView)findViewById(R.id.day2temp);
        day3temptxt = (TextView)findViewById(R.id.day3temp);
        day4temptxt = (TextView)findViewById(R.id.day4temp);

        day0humitxt = (TextView)findViewById(R.id.day0humi);
        day1humitxt = (TextView)findViewById(R.id.day1humi);
        day2humitxt = (TextView)findViewById(R.id.day2humi);
        day3humitxt = (TextView)findViewById(R.id.day3humi);
        day4humitxt = (TextView)findViewById(R.id.day4humi);

        day0time0 = (TextView)findViewById(R.id.day0time0);
        day0time1 = (TextView)findViewById(R.id.day0time1);
        day0time2 = (TextView)findViewById(R.id.day0time2);
        day0time3 = (TextView)findViewById(R.id.day0time3);

        day0temp0 = (TextView)findViewById(R.id.day0temp0);
        day0temp1 = (TextView)findViewById(R.id.day0temp1);
        day0temp2 = (TextView)findViewById(R.id.day0temp2);
        day0temp3 = (TextView)findViewById(R.id.day0temp3);

        day0icon0=(ImageView)findViewById(R.id.day0icon0);
        day0icon1=(ImageView)findViewById(R.id.day0icon1);
        day0icon2=(ImageView)findViewById(R.id.day0icon2);
        day0icon3=(ImageView)findViewById(R.id.day0icon3);
    }
    public void onClickView(View v) {
        if(v.getId()==R.id.menu){
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
                                    intent = new Intent(Weather5days.this, MainActivity.class);
                                    startActivity(intent);
                                    return true;
                                case R.id.weathermenu:
                                    intent = new Intent(Weather5days.this, Weather5days.class);
                                    startActivity(intent);
                                    return true;
                                case R.id.soilmenu:
                                    intent = new Intent(Weather5days.this, SoilActivity.class);
                                    startActivity(intent);
                                    return true;
                                case R.id.watermenu:
                                    intent = new Intent(Weather5days.this, WaterActivity.class);
                                    startActivity(intent);
                                    return true;
                            }
                            return false;
                        }
                    });
            popupMenu.show();

        }
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

                    Calendar hcalendar = Calendar.getInstance();
                    hcalendar.add(Calendar.HOUR,3);
                    Date day0time=hcalendar.getTime();
                    day0time0.setText(new SimpleDateFormat("HH시").format(day0time));
                    hcalendar.add(Calendar.HOUR,3);
                    day0time=hcalendar.getTime();
                    day0time1.setText(new SimpleDateFormat("HH시").format(day0time));
                    hcalendar.add(Calendar.HOUR,3);
                    day0time=hcalendar.getTime();
                    day0time2.setText(new SimpleDateFormat("HH시").format(day0time));
                    hcalendar.add(Calendar.HOUR,3);
                    day0time=hcalendar.getTime();
                    day0time3.setText(new SimpleDateFormat("HH시").format(day0time));

                    JSONObject day00=lists.getJSONObject(1);
                    JSONObject day01=lists.getJSONObject(2);
                    JSONObject day02=lists.getJSONObject(3);
                    JSONObject day03=lists.getJSONObject(4);

                    String icontxt=day00.getJSONArray("weather").getJSONObject(0).getString("icon");
                    String iconurl="http://openweathermap.org/img/w/" + icontxt + ".png";
                    Picasso.get().load(iconurl).into(day0icon0);
                    icontxt=day01.getJSONArray("weather").getJSONObject(0).getString("icon");
                    iconurl="http://openweathermap.org/img/w/" + icontxt + ".png";
                    Picasso.get().load(iconurl).into(day0icon1);
                    icontxt=day02.getJSONArray("weather").getJSONObject(0).getString("icon");
                    iconurl="http://openweathermap.org/img/w/" + icontxt + ".png";
                    Picasso.get().load(iconurl).into(day0icon2);
                    icontxt=day03.getJSONArray("weather").getJSONObject(0).getString("icon");
                    iconurl="http://openweathermap.org/img/w/" + icontxt + ".png";
                    Picasso.get().load(iconurl).into(day0icon3);

                    JSONObject day0main0=day00.getJSONObject("main");
                    Double day0temp00=day0main0.getDouble("temp");
                    day0temp0.setText(((int)(day0temp00-273.15))+"°C");
                    JSONObject day0main1=day01.getJSONObject("main");
                    Double day0temp01=day0main1.getDouble("temp");
                    day0temp1.setText(((int)(day0temp01-273.15))+"°C");
                    JSONObject day0main2=day02.getJSONObject("main");
                    Double day0temp02=day0main2.getDouble("temp");
                    day0temp2.setText(((int)(day0temp02-273.15))+"°C");
                    JSONObject day0main3=day03.getJSONObject("main");
                    Double day0temp03=day0main3.getDouble("temp");
                    day0temp3.setText(((int)(day0temp03-273.15))+"°C");

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
                    wind.setText(windtxt+"m/s");

                    //날씨 영문
                    String day0weather=day0.getJSONArray("weather").getJSONObject(0).getString("description");
                    String day1weather=day1.getJSONArray("weather").getJSONObject(0).getString("description");
                    String day2weather=day2.getJSONArray("weather").getJSONObject(0).getString("description");
                    String day3weather=day3.getJSONArray("weather").getJSONObject(0).getString("description");
                    String day4weather=day4.getJSONArray("weather").getJSONObject(0).getString("description");

                    icontxt=day0.getJSONArray("weather").getJSONObject(0).getString("icon");
                    iconurl="http://openweathermap.org/img/w/" + icontxt + ".png";
                    Picasso.get().load(iconurl).into(icon);


                    //날씨 한글화
                    day0weathertxt.setText(new WeatherHangeul(day0weather).getWeather());
                    day1weathertxt.setText(new WeatherHangeul(day1weather).getWeather());
                    day2weathertxt.setText(new WeatherHangeul(day2weather).getWeather());
                    day3weathertxt.setText(new WeatherHangeul(day3weather).getWeather());
                    day4weathertxt.setText(new WeatherHangeul(day4weather).getWeather());

                    //온도
                    JSONObject day0main=day0.getJSONObject("main");
                    Double day0temp=day0main.getDouble("temp");
                    day0temptxt.setText(((int)(day0temp-273.15))+"°C");
                    day0humitxt.setText(day0main.getInt("humidity")+"%");
                    JSONObject day1main=day1.getJSONObject("main");
                    Double day1temp=day1main.getDouble("temp");
                    day1temptxt.setText(((int)(day1temp-273.15))+"°C");
                    day1humitxt.setText(day1main.getInt("humidity")+"%");
                    JSONObject day2main=day2.getJSONObject("main");
                    Double day2temp=day2main.getDouble("temp");
                    day2temptxt.setText(((int)(day2temp-273.15))+"°C");
                    day2humitxt.setText(day2main.getInt("humidity")+"%");
                    JSONObject day3main=day3.getJSONObject("main");
                    Double day3temp=day3main.getDouble("temp");
                    day3temptxt.setText(((int)(day3temp-273.15))+"°C");
                    day3humitxt.setText(day3main.getInt("humidity")+"%");
                    JSONObject day4main=day4.getJSONObject("main");
                    Double day4temp=day4main.getDouble("temp");
                    day4temptxt.setText(((int)(day4temp-273.15))+"°C");
                    day4humitxt.setText(day4main.getInt("humidity")+"%");


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
                    mcalendar.add(Calendar.DAY_OF_WEEK,1);
                    Date day4date=mcalendar.getTime();
                    String sdf4=new SimpleDateFormat("MM월 dd일").format(day4date);
                    day4datetxt.setText(sdf4);

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