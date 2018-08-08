package com.example.admin.routeandmap;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG= "MainActivity";

    TextView location;
    private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isServiceAvailable()) {
            isInitView();
        }
    }

    public void isInitView() {
        location = (TextView) findViewById(R.id.am_location_txt);
        location.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);
    }

    public boolean isServiceAvailable(){
        int service_available= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (service_available== ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }else if (GoogleApiAvailability.getInstance().isUserResolvableError(service_available)){

            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");

            Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,service_available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else {
            Toast.makeText(this, "You can't make a Map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
