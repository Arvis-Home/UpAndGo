package com.arvis.android.upandgo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.arvis.android.upandgo.event.BOMDataServiceError;
import com.arvis.android.upandgo.event.BOMGeoHash;
import com.arvis.android.upandgo.event.BOMWeatherData;
import com.arvis.android.upandgo.event.HomeSet;
import com.arvis.android.upandgo.event.SolutionFound;
import com.arvis.android.upandgo.event.WorkSet;
import com.arvis.android.upandgo.fragment.Dashboard;
import com.arvis.android.upandgo.service.BOMLocationService;
import com.arvis.android.upandgo.service.BOMWeatherService;
import com.arvis.android.upandgo.service.JourneySelection;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMapLongClickListener{

    private static final int REQ_SETUP_HOME_ADDRESS = 110;

    private static final int REQ_SETUP_WORK_ADDRESS = 111;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private ActionBarDrawerToggle drawerToggle;

    private ProgressDialog progressDialog;

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;

    private boolean mLocationPermissionGranted = false;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000;

    private JourneySelection journeySelection;

    @BindView(R.id.panel)
     View panel;

    @BindView(R.id.from)
     TextView from;

    @BindView(R.id.to)
     TextView to;

    Dashboard dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("Let's go");

        ButterKnife.bind(this);

        journeySelection = new JourneySelection();

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawerLayout.addDrawerListener(drawerToggle);

        ActionBar aActionBar = getSupportActionBar();
        if (aActionBar != null) {
            aActionBar.setHomeButtonEnabled(true);
            aActionBar.setDisplayHomeAsUpEnabled(true);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        if(Config.getFloatConfig(Config.WORK_LAT) != 0 && Config.getFloatConfig(Config.HOME_LAT) != 0){

            startJourneySelection();
        }


    }

    @OnClick(R.id.setup_home_address)
    public void setupHomeAddress(View view){

        drawerLayout.closeDrawers();

        Intent setHome = new Intent(this, MapsActivity.class);

        setHome.putExtra(MapsActivity.EXTRA_SELECTION_TYPE, MapsActivity.TYPE_HOME);

        startActivityForResult(setHome, REQ_SETUP_HOME_ADDRESS);
    }

    @OnClick(R.id.setup_work_address)
    public void setupWorkAddress(View view){

        drawerLayout.closeDrawers();

        Intent setWork = new Intent(this, MapsActivity.class);

        setWork.putExtra(MapsActivity.EXTRA_SELECTION_TYPE, MapsActivity.TYPE_WORK);

        startActivityForResult(setWork, REQ_SETUP_WORK_ADDRESS);
    }

    @OnClick(R.id.plan_journey)
    public void planYourJonurney(View view){

        drawerLayout.closeDrawers();

        startJourneyPlanner();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case REQ_SETUP_HOME_ADDRESS:

                if(resultCode == RESULT_OK){

                    Config.addFloatConfig(Config.HOME_LAT, (float)data.getDoubleExtra(MapsActivity.EXTRA_LAT, 0));

                    Config.addFloatConfig(Config.HOME_LON, (float)data.getDoubleExtra(MapsActivity.EXTRA_LON, 0));

                    EventBus.getDefault().postSticky(new HomeSet());
                }

                break;

            case REQ_SETUP_WORK_ADDRESS:

                if(resultCode == RESULT_OK){

                    Config.addFloatConfig(Config.WORK_LAT, (float)data.getDoubleExtra(MapsActivity.EXTRA_LAT, 0));

                    Config.addFloatConfig(Config.WORK_LON, (float)data.getDoubleExtra(MapsActivity.EXTRA_LON, 0));

                    EventBus.getDefault().postSticky(new WorkSet());
                }

                break;
        }
    }

    @Override
    protected void onStart() {

        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {

        super.onStop();

        EventBus.getDefault().unregister(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBOMServiceError(BOMDataServiceError error){

        removeProgressDialog();

        journeySelection.stop();

        new AlertDialog.Builder(this).setTitle("Error").setMessage(error.error).setPositiveButton("OK", null).show();
    }

    private void removeProgressDialog(){

        if(progressDialog != null && progressDialog.isShowing()){

            progressDialog.dismiss();
        }
    }

    @Subscribe(sticky = true)
    public void onHomeSet(HomeSet homeSet){

        EventBus.getDefault().removeStickyEvent(homeSet);

        if(Config.getFloatConfig(Config.WORK_LAT) != 0){

//            progressDialog = ProgressDialog.show(this, null, "Updating...");
//
//            progressDialog.setCancelable(false);

            //new BOMLocationService().getLocationHash(Config.getFloatConfig(Config.HOME_LAT), Config.getFloatConfig(Config.HOME_LON));

            startJourneySelection();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        removeProgressDialog();
    }

//    @Subscribe
//    public void onBOMLocation(BOMGeoHash geoHash){
//
//        new BOMWeatherService().getTempFromLocation(geoHash.geoHash);
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onBOMWeatherData(BOMWeatherData data){
//
//        removeProgressDialog();
//    }

    @Subscribe(sticky = true)
    public void onWorkSet(WorkSet workSet){

        EventBus.getDefault().removeStickyEvent(workSet);

        if(Config.getFloatConfig(Config.HOME_LAT) != 0){

           startJourneySelection();

            //new BOMLocationService().getLocationHash(Config.getFloatConfig(Config.HOME_LAT), Config.getFloatConfig(Config.HOME_LON));
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateLocationUI();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        if(Config.getFloatConfig(Config.CUR_LOC_LAT) == 0){

            mMap.addMarker(new MarkerOptions().position(latLng).title("From"));

            from.setText(""+latLng.latitude + " "+ latLng.longitude);

            Config.addFloatConfig(Config.CUR_LOC_LAT, (float) latLng.latitude);
            Config.addFloatConfig(Config.CUR_LOC_LON, (float) latLng.longitude);

        }else if(Config.getFloatConfig(Config.DES_LOC_LAT) == 0){

            mMap.addMarker(new MarkerOptions().position(latLng).title("To"));

            to.setText(""+latLng.latitude + " "+ latLng.longitude);

            Config.addFloatConfig(Config.DES_LOC_LAT, (float) latLng.latitude);
            Config.addFloatConfig(Config.DES_LOC_LON, (float) latLng.longitude);
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);
    }

    @OnClick(R.id.find_way)
    public void findWay(View view){

        if(Config.getFloatConfig(Config.DES_LOC_LAT) != 0 && Config.getFloatConfig(Config.CUR_LOC_LAT) != 0){

            startJourneySelection();

        }else{

            new AlertDialog.Builder(this).setTitle("Error").setMessage("Please set the from and to location").setPositiveButton("OK", null).create().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }


    private void startJourneyPlanner(){

        getFragmentManager().beginTransaction().remove(dashboard).commit();

        panel.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        mGoogleApiClient.disconnect();

        Config.addFloatConfig(Config.CUR_LOC_LAT, 0);

        Config.addFloatConfig(Config.DES_LOC_LAT, 0);

    }

    private void showDashboard(){


        panel.setVisibility(View.GONE);

        dashboard = new Dashboard();

        getFragmentManager().beginTransaction().replace(R.id.main_menu_container, dashboard).commit();
    }

    private void startJourneySelection(){

        progressDialog = ProgressDialog.show(this, null, "Updating...");

        progressDialog.setCancelable(false);

        journeySelection.start();

        journeySelection.chooseBestSolution();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJourneyFound(SolutionFound solutionFound){

        journeySelection.stop();

        removeProgressDialog();

        showDashboard();
    }

    @OnClick(R.id.clear)
    public void clear(View view){

       Config.addFloatConfig(Config.CUR_LOC_LAT, 0);

       Config.addFloatConfig(Config.DES_LOC_LAT, 0);

       from.setText("Not set");

       to.setText("Not Set");

        mMap.clear();

    }

    @OnClick(R.id.plan_journey)
    public void planJourney(View view){

        startJourneyPlanner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }
}
