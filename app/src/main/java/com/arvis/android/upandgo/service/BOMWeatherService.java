package com.arvis.android.upandgo.service;

import com.arvis.android.upandgo.event.BOMDataServiceError;
import com.arvis.android.upandgo.event.BOMWeatherData;
import com.arvis.android.upandgo.httpclient.HttpsClient;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Jack on 29/7/17.
 */

public class BOMWeatherService implements Callback{

    private static final String BASE_URL = "https://api.cloud.bom.gov.au/forecasts/v1/grid";

    public void getTempFromLocation(String geoHash){

        Map<String, String> header = new HashMap<>();

        header.put("x-api-key", BOMConstants.API_KEY);

        HttpsClient httpsClient = new HttpsClient();

        httpsClient.sendGetRequestWithCustomHeader(BASE_URL + "/three-hourly/"+geoHash+"/temperatures", this, header);

    }

    public void getWeatherPrediction(String geoHash){

        Map<String, String> header = new HashMap<>();

        header.put("x-api-key", BOMConstants.API_KEY);

        HttpsClient httpsClient = new HttpsClient();

        httpsClient.sendGetRequestWithCustomHeader(BASE_URL + "/three-hourly/"+geoHash+"/precipitation", this, header);
    }


    @Override
    public void onFailure(Call call, IOException e) {

        EventBus.getDefault().post(new BOMDataServiceError(e.getMessage()));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        if(response.isSuccessful()){

            String responseInString = response.body().string();

            try {

                JSONObject respJson = new JSONObject(responseInString);

                String type = respJson.optJSONObject("data").optString("type");

                if(type.equals("temperature")){

                    JSONObject airTemperature = respJson.optJSONObject("data").optJSONObject("attributes")
                            .optJSONObject("air_temperature");

                    JSONArray forecastData = airTemperature.optJSONArray("forecast_data");


                    JSONObject temperature = forecastData.getJSONObject(forecastData.length() - 1);

                    EventBus.getDefault().post(new BOMWeatherData(temperature.optString("value")));



                }

            } catch (JSONException e) {

                EventBus.getDefault().post(new BOMDataServiceError(e.getMessage()));
            }


        }else{

            EventBus.getDefault().post(new BOMDataServiceError(response.code() + " "+ response.message()));
        }

    }
}
