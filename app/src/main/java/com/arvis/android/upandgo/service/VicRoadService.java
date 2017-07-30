package com.arvis.android.upandgo.service;

import com.arvis.android.upandgo.event.VicRoadTrafficFound;
import com.arvis.android.upandgo.httpclient.HttpsClient;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Jack on 29/7/17.
 */

public class VicRoadService implements Callback{

    private final static String serviceURL = "https://services2.arcgis.com/18ajPSI0b3ppsmMt/arcgis/rest/services/Traffic_Volume/FeatureServer/0/query?where=1%3D1&" +
            "outFields=*&geometryType=esriGeometryPolyline&inSR=4326&spatialRel=esriSpatialRelIntersects&outSR=4326&f=json&geometry=";


    public void findTrafficVolume(double lat, double lon){

        HttpsClient client = new HttpsClient();

        client.sendGetRequestWithCustomHeader(serviceURL + lat + "," +lon, this, new HashMap<String, String>());

    }

    @Override
    public void onFailure(Call call, IOException e) {

        EventBus.getDefault().post(new VicRoadTrafficFound("0"));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        if(response.isSuccessful()){

            EventBus.getDefault().post(new VicRoadTrafficFound("1000"));


        }else{

            EventBus.getDefault().post(new VicRoadTrafficFound("0"));
        }


    }
}
