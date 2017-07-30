package com.arvis.android.upandgo.service;

import com.arvis.android.upandgo.Config;
import com.arvis.android.upandgo.event.BOMGeoHash;
import com.arvis.android.upandgo.event.BOMWeatherData;
import com.arvis.android.upandgo.event.SolutionFound;
import com.arvis.android.upandgo.event.VicRoadTrafficFound;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Jack on 29/7/17.
 */

public class JourneySelection {

    private float lat, lon;

    private SolutionFound solutionFound;


    public void start(){
       EventBus.getDefault().register(this);
    }

    public void chooseBestSolution(){


        if(Config.getFloatConfig(Config.HOME_LAT) != 0){

            lat = Config.getFloatConfig(Config.HOME_LAT);

            lon = Config.getFloatConfig(Config.HOME_LON);

        }else{

            lat = Config.getFloatConfig(Config.CUR_LOC_LAT);

            lon = Config.getFloatConfig(Config.CUR_LOC_LON);
        }

        new BOMLocationService().getLocationHash(lat, lon);
    }

    public void stop(){

        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onBOMLocation(BOMGeoHash geoHash){

        new BOMWeatherService().getTempFromLocation(geoHash.geoHash);
    }

    @Subscribe
    public void onBOMWeatherData(BOMWeatherData data){

        if(Double.parseDouble(data.temp) < 25){

            solutionFound = new SolutionFound(SolutionFound.Solution.DRIVE);

        }else{

            solutionFound = new SolutionFound(SolutionFound.Solution.TRAIN);
        }

        solutionFound.setTemperature(data.temp);

        new VicRoadService().findTrafficVolume(lat, lon);
    }

    @Subscribe
    public void onVicRoadData(VicRoadTrafficFound trafficFound){

        solutionFound.setTrafficVoulum(trafficFound.volumn);

        EventBus.getDefault().postSticky(solutionFound);
    }

}
