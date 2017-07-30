package com.arvis.android.upandgo.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arvis.android.upandgo.Config;
import com.arvis.android.upandgo.R;
import com.arvis.android.upandgo.event.BOMWeatherData;
import com.arvis.android.upandgo.event.HomeSet;
import com.arvis.android.upandgo.event.SolutionFound;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jack on 29/7/17.
 */

public class Dashboard extends Fragment{

    Unbinder unbinder;

    @BindView(R.id.weather)
    TextView weather;

    @BindView(R.id.solution)
    TextView solution;

    @BindView(R.id.info_panel)
    View dashboardInfoPanel;

    @BindView(R.id.img_solution)
    ImageView solutionImage;

    @BindView(R.id.weather_stat)
    TextView weatherStat;

    @BindView(R.id.traffic_volume)
    TextView volume;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);

        unbinder = ButterKnife.bind(this, view);

        return view;
    }

//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//
//        super.onViewCreated(view, savedInstanceState);
//
//        if(Config.getFloatConfig(Config.HOME_LAT) == 0){
//
//            dashboardInfoPanel.setVisibility(View.INVISIBLE);
//
//            placeHolder.setVisibility(View.VISIBLE);
//
//        }else{
//
//            placeHolder.setVisibility(View.GONE);
//
//            dashboardInfoPanel.setVisibility(View.VISIBLE);
//
//            EventBus.getDefault().postSticky(new HomeSet());
//        }
//    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if(unbinder != null)
            unbinder.unbind();
    }

    @Override
    public void onStart() {

        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {

        super.onStop();

        EventBus.getDefault().unregister(this);
    }

//    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
//    public void onWeatherData(BOMWeatherData weatherData){
//
//        EventBus.getDefault().removeStickyEvent(weatherData);
//
//        placeHolder.setVisibility(View.GONE);
//
//        dashboardInfoPanel.setVisibility(View.VISIBLE);
//
//
//        weather.setText("The forecast temperature is: "+ weatherData.temp + "C");
//
//        if(Double.parseDouble(weatherData.temp) < 25){
//
//            solutionImage.setImageResource(R.drawable.drive);
//
//            solution.setText("It is too cold to stand in the wind to wait for any public transport, drive by yourself to work today");
//        }
//
//    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void solutionFound(SolutionFound solutionFound){

        EventBus.getDefault().removeStickyEvent(solutionFound);

        dashboardInfoPanel.setVisibility(View.VISIBLE);

        weather.setText(solutionFound.getTemperature() + "C");

        if(solutionFound.solution == SolutionFound.Solution.DRIVE){

            solutionImage.setImageResource(R.drawable.drive);

            int vol = Integer.parseInt(solutionFound.getTrafficVoulum());

            solution.setText("Drive");

            volume.setText("Low");

            weatherStat.setText("Shower or two");

        }else if(solutionFound.solution == SolutionFound.Solution.TRAIN){

            solutionImage.setImageResource(R.drawable.train);

            solution.setText("train");
        }
    }
}
