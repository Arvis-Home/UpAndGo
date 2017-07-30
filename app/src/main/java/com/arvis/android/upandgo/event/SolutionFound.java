package com.arvis.android.upandgo.event;

/**
 * Created by Jack on 29/7/17.
 */

public class SolutionFound {

    public enum Solution{

        DRIVE, TRAM, TRAIN, BUS
    }

    public final Solution solution;

    private String temperature;

    private String trafficVoulum;

    public SolutionFound(Solution found){
        this.solution = found;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTrafficVoulum() {
        return trafficVoulum;
    }

    public void setTrafficVoulum(String trafficVoulum) {
        this.trafficVoulum = trafficVoulum;
    }
}
