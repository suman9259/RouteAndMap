package com.example.admin.routeandmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MapActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;

    private boolean isLocation_permission_granted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private EditText searchText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getLocationPermission();
        isInitView();

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is Ready");
        mMap = googleMap;

        if (isLocation_permission_granted){

            getDeviceLocation();

            if(ActivityCompat.checkSelfPermission(this,FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,COURSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            isInitView();
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }









    private void isInitView(){
        searchText=(EditText)findViewById(R.id.am_search_edit_txt);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId== EditorInfo.IME_ACTION_SEARCH || actionId==EditorInfo.IME_ACTION_DONE ||
                        keyEvent.getAction()==KeyEvent.ACTION_DOWN || keyEvent.getAction()==KeyEvent.KEYCODE_ENTER){
                    //execute our method for searching
                    geoLocate();
                }
                return false;
            }
        });
    }

    // method for searching
    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchLocation= searchText.getText().toString();
        Geocoder geocoder=new Geocoder(MapActivity.this);
        List<Address> addresses= new ArrayList<>();
        try {
            addresses=geocoder.getFromLocationName(searchLocation,1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }
        if (addresses.size()>0){
            Address address=addresses.get(0);
            Log.d(TAG, "geoLocate: found a location: " + address.toString());
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: map Initializing");
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {

        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                isLocation_permission_granted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }

        } else {
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        isLocation_permission_granted=false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if (grantResults.length > 0){
                    for (int i=0; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            isLocation_permission_granted=false;
                            return;
                        }
                    }
                    isLocation_permission_granted=true;
                    //initialize our map here
                    initMap();
                }
            }
        }
    }

    private void getDeviceLocation(){

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

         try {
             if (isLocation_permission_granted){
                 final Task location = fusedLocationProviderClient.getLastLocation();

                 location.addOnCompleteListener(new OnCompleteListener() {
                     @Override
                     public void onComplete(@NonNull Task task) {
                         if (task.isSuccessful()){

                             Location currentLocation=(Location) task.getResult();
                             moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM);

                         }else {
                             Log.d(TAG, "onComplete: current location is null");
                             Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                         }
                     }
                 });
             }

         }catch (SecurityException e){
             Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
         }

    }

    private void moveCamera(LatLng latLng, Float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }


}
