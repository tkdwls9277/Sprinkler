package com.example.arduino;

import android.Manifest;
import android.content.pm.PackageManager;
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

public class MainActivity extends AppCompatActivity {

    double lat, lon;//위도 경도 값
    private GpsInfo gps;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    TextView temp,city,date,humi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temp = (TextView)findViewById(R.id.temp);
        city = (TextView)findViewById(R.id.city);
        date = (TextView)findViewById(R.id.date);
        humi = (TextView)findViewById(R.id.humi);
    }

    @Override
    protected void onResume() {
        super.onResume();

        gps = new GpsInfo(MainActivity.this);
        lat = gps.getLatitude();
        lon = gps.getLongitude();
        callPermission();  // 권한 요청을 해야 함
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon="+lon+
                "&units=metric&appid=25101ddb40fe8f611b992f17f1d60b23";
        Log.e("url=",url);

        fine_weather(url);
    }

    public void fine_weather(String url) {
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    JSONObject main_object=response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object=array.getJSONObject(0);
                    String mtemp = String.valueOf(main_object.getDouble("temp"));
                    String mdes = object.getString("description");
                    String mcity = response.getString("name");

                    temp.setText(mtemp);
                    city.setText(mcity);
                    humi.setText(mdes);

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat sdf=new SimpleDateFormat("EEEE-MM-DD");
                    String formatted_date=sdf.format(calendar.getTime());

                    date.setText(formatted_date);

                    double temp_int = Double.parseDouble(mtemp);
                    double centi = (temp_int-32)/1.8000;
                    centi=Math.round(centi);
                    int i=(int)centi;
                    temp.setText(String.valueOf(i));

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

  /*  class WeatherTask extends AsyncTask<String, Void, String> {
        private String url;
        public WeatherTask(String url){
            this.url=url;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("onPreExecute","in");
        }

        protected String doInBackground(String... args) {
            String result = null;
            Log.e("doInBackground","in");
            RequestHttpURLConnection requestHttpURLConnection=new RequestHttpURLConnection();
            try {
                Log.e("doin result","전");
                result=requestHttpURLConnection.request(url);
                Log.e("doin result","후");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                Log.e("try","in");
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                Long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                String temp = main.getString("temp") + "°C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");

                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");

                String address = jsonObj.getString("name") + ", " + sys.getString("country");

                Weather.setText("main="+main+"wind="+wind+"weather="+weather);

            } catch (JSONException e) {
                Log.e("JSONException","e");
                e.printStackTrace();
            }
        }
    }*/

}