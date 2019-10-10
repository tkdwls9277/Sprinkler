package com.example.arduino;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class GpsTracker extends Service implements LocationListener {
    private Location location;
    private LocationManager locationManager;
    private Context context;

    private double latitude;
    private double longitude;

    private String deviceName;

    public GpsTracker () { // default constructor
    }

    public GpsTracker(Context context) {
        this.context = context;
        getLocation();
    }

    public Location getLocation() {
        Log.e("getLocation", "yes");
        try {
            // 수정 전 locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) { // 위치 정보 제공자 확인
            } else {
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
                Log.e("FINE_LOCATION", "" + hasFineLocationPermission);
                Log.e("COARSE_LOCATION", "" + hasCoarseLocationPermission);

                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) { // 권한 확인
                } else {

                    return null;
                }

                if (isNetworkEnabled) {
                    Log.e("GpsTracker", "isNetworkEnabled");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 , 0, this);

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.e("getLocation", "latitude : " + latitude + ", longitude : " + longitude);
                        }
                    }
                }

                if (isGPSEnabled) // GPS 이용
                {
                    Log.e("GpsTracker", "isGPSEnabled");
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 , 0, this);
                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 최근 위치 확인
                            if (location != null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.e("getLocation", "latitude : " + latitude + ", longitude : " + longitude);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e("Exception" , "" + e.toString());
        }

        // locationManager.removeUpdates(GpsTracker.this); // 위치 정보 갱신 종료

        return location;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // 수정 후
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID); // 단말기 고유 번호
        // 값이 -1일 경우 값 전달이 안된 것임

        getLocation();

        // Looper.myLooper() == Looper.getMainLooper() 현재 쓰레드가 메인쓰레드인지 확인하는 코드, true 일 시 메인쓰레드
        boolean thread = Looper.myLooper() == Looper.getMainLooper();
        Log.e("onStartCommand", "" + thread);

        // ServerCommunication sc = new ServerCommunication();  잠깐 보류
        // sc.sendData(dust, co, co2, latitude, longitude);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

        class LocationTask extends AsyncTask<Location, Void, Void> {
            private Context context;

            public LocationTask(Context context) {
                this.context = context;
            }

            @Override
            protected Void doInBackground(Location... params) {

                String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); // 에러 날 수도..

                latitude = params[0].getLatitude();
                longitude = params[0].getLongitude();

                Log.e("latitude", "" + latitude);
                Log.e("longitude", "" + longitude);
                return null;
            }
        }
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    public double getLatitude() {
        if(location != null)
        {
            latitude = location.getLatitude();
        }
        Log.e("latitude", "" + latitude);
        return latitude;
    }

    public double getLongitude()
    {
        if(location != null)
        {
            longitude = location.getLongitude();
        }
        Log.e("longitude", "" + longitude);
        return longitude;
    }

    public void stopUsingGPS() // 위치 정보 갱신 종료하는 메소드
    {
        if(locationManager != null)
        {
            locationManager.removeUpdates(GpsTracker.this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(GpsTracker.this);
        Toast.makeText(this, "서비스 종료", Toast.LENGTH_LONG).show();
        Log.e("GpsTracker", "onDestroy");
    }
}
