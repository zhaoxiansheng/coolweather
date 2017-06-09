package com.example.zy.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.example.zy.coolweather.activity.WeatherActivity;
import com.example.zy.coolweather.gson.Weather;
import com.example.zy.coolweather.utils.HttpUtils;
import com.example.zy.coolweather.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutonUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8*60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutonUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){
        SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(AutonUpdateService.this);
        String weatherString = pres.getString("weather", null);
        if (!weatherString.isEmpty()){
            //有缓存
            Weather weather = Utility.handlerWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;

            String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                    weatherId + "&key=3ece6416bb32427588224631168471a8";

            HttpUtils.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string();
                    Weather weather = Utility.handlerWeatherResponse(responseString);
                    if (weather !=null){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                                AutonUpdateService.this).edit();
                        editor.putString("weather", responseString);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtils.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                        AutonUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }
}
