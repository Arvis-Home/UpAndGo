package com.arvis.android.upandgo.service;



import com.arvis.android.upandgo.event.BOMDataServiceError;
import com.arvis.android.upandgo.event.BOMGeoHash;
import com.arvis.android.upandgo.httpclient.HttpsClient;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Jack on 29/7/17.
 */

public class BOMLocationService implements Callback{

    private static final String BASE_URL = "https://api.cloud.bom.gov.au/locations/v1";


    public void getLocationHash(float lat, float lon){

        Map<String, String> header = new HashMap<>();

        header.put("x-api-key", BOMConstants.API_KEY);

        HttpsClient httpsClient = new HttpsClient();

        httpsClient.sendGetRequestWithCustomHeader(BASE_URL + "/geohashes?latitude="+lat+"&longitude="+lon, this, header);
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

                JSONObject resJson = new JSONObject(responseInString);

                JSONArray data = resJson.optJSONArray("data");

                JSONObject geoHash = data.optJSONObject(0);

                String geoHashInString = geoHash.optString("id");

                EventBus.getDefault().post(new BOMGeoHash(geoHashInString));

            } catch (JSONException e) {

                EventBus.getDefault().post(new BOMDataServiceError(e.getMessage()));
            }


        }else{

            EventBus.getDefault().post(new BOMDataServiceError(response.code() + " " + response.message()));
        }
    }
}
