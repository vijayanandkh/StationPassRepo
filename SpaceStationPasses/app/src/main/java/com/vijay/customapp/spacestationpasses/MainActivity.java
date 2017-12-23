package com.vijay.customapp.spacestationpasses;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.vijay.customapp.spacestationpasses.Constants.*;
import com.vijay.customapp.spacestationpasses.ISSPassInfo.ISSPassDetail;


import org.json.JSONArray;
import org.json.JSONObject;

import java.net.UnknownHostException;

import static com.vijay.customapp.spacestationpasses.Constants.*;


public class MainActivity extends AppCompatActivity implements PassesInfoFragment.OnListFragmentInteractionListener, DataCallback {

    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final String PASSES_INFO_FRAGMENT_TAG = "PassesInfoFragmentTag";
    private CustomLocationService mCustomLocationService = null;
    private ServiceConnection mServiceConnection = new MyServiceConnection();
    boolean mServiceConnected = false;

    PassesInfoFragment passesInfoFragment;
    Double currentLatitude;
    Double currentLongitude;
    private ResponseProcess responseProcess = new ResponseProcess();
    IntentFilter intentFilter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
        passesInfoFragment = (PassesInfoFragment) fm.findFragmentByTag(PASSES_INFO_FRAGMENT_TAG);

        if(passesInfoFragment == null) {
            passesInfoFragment = PassesInfoFragment.newInstance(1);
            fm.beginTransaction().replace(R.id.container_framelayout, passesInfoFragment, PASSES_INFO_FRAGMENT_TAG).commit();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(Constants.LOCATION_MSG));
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        this.registerReceiver(mConnectivityReceiver,intentFilter);
    }


    @Override
    public void onResume() {
        super.onResume();
        //Log.d("MainActivity", "onResume"); //check & to analyse the best option for location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //request the permission, without which can not proceed.
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.INTERNET},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            if(!isAirplaneModeOn(getApplicationContext())) {
                // start the service with location listener
                Log.d(TAG, "starting the countservice");
                if(!mServiceConnected && mServiceConnection!=null ) {
                    Intent intent = new Intent(this, CustomLocationService.class);
                    bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                }
            } else {
                Toast.makeText(this, "Airplane mode is switched on. Please switch off to get locatoin updates and fetch staelite pass info", Toast.LENGTH_SHORT).show();
                this.registerReceiver(mConnectivityReceiver,intentFilter);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d("MainActivity", "onPause");   //check & to analyse the best option for location updates
        if(mCustomLocationService!= null)
            mCustomLocationService.stopLocationUpdates();
        unregisterReceiver(mConnectivityReceiver);
        clearListenerService();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good! permissin is granted
                    Toast.makeText(this, "location permission granted", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"permission is granted now");
                } else {
                    Toast.makeText(this, "Need location permission granted!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onListFragmentInteraction(ISSPassDetail.ISSPassesItem item) {
        // getting the selected item in list
        //Log.d("MainActivity", "Item clicked is: " + item.toString());

    }

    @Override
    public void onSuccess(JSONObject resultArray) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mServiceConnected) {
                    if (!isFinishing()) {
                        PassesInfoFragment frag = (PassesInfoFragment) getSupportFragmentManager().findFragmentByTag(PASSES_INFO_FRAGMENT_TAG);
                        frag.updateAdapter(responseProcess.getIssPassesItemsList());
                    }
                }
            }
        });
    }

    @Override
    public void onFailure(final VolleyError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(error.getMessage().equals(UnknownHostException.class.getSimpleName())) {
                    Toast.makeText(getApplicationContext(), "Network not reachable: please check you have internet connection ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error in getting fetching data from server: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if(DBG) Log.d("MainActivity", "Connected to service.");
            CustomLocationService.LocalBinder mBinder = (CustomLocationService.LocalBinder) iBinder;
            mCustomLocationService = mBinder.getService();
            mCustomLocationService.intialiseLocationListener();
            mServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(DBG) Log.d("MainActivity", "Disconnected from service.");
            mCustomLocationService = null;
            mServiceConnected = false;
        }
    }

    private void clearListenerService() {
        if (mServiceConnected) {
            if(mServiceConnection != null) {
                try {
                    getApplicationContext().unbindService(mServiceConnection);
                    getApplicationContext().stopService(new Intent(getApplicationContext(), CustomLocationService.class));
                }catch (IllegalArgumentException e) {
                    Log.e(TAG, "Exception: unbind failed " + e.getMessage());
                }
            }
            mServiceConnected = false;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        clearListenerService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mServiceConnected && mServiceConnection != null) {
            unbindService(mServiceConnection);
        }
    }

    /**
     * local message receiver for the location information, to trigger the network calls post valid location info.
     * validity flag gives better control over to give error message or toast if invalid for this application use case.
     *
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean validity = intent.getBooleanExtra(Constants.VALIDITY, false);
            currentLatitude = intent.getDoubleExtra(Constants.LATITUDE, 0);
            currentLongitude = intent.getDoubleExtra(Constants.LONGITUDE, 0);

            if(validity) {
                //  valid location info. request a network to get the pass info
                //Toast.makeText(getApplicationContext(), "lat: " + currentLatitude + " long: " + currentLongitude, Toast.LENGTH_SHORT).show();
                if(!isFinishing())
                    fetchPasses.run();
            } else {
                Toast.makeText(getApplicationContext(), "Can not request as Latitude is out of range: lat: " + currentLatitude + " long: " + currentLongitude, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * local connectivity receiver to take care of enabling airplance mode inbetween
     *
     * Would notify users to disable airplane mode if user swichtes on any time.
     *
     */

    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
            if(!isAirplaneModeOn){
                // handle Airplane Mode off
                // add the location listeners by starting the services.
                Intent startServiceIntent = new Intent(getApplicationContext(), CustomLocationService.class);
                intent.setPackage(getApplicationContext().getPackageName());
                getApplicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            } else {
                // handle Airplane Mode on
                //remove location listener and notify for getting back the connectivity
                clearListenerService();
                Toast.makeText(getApplicationContext(), "Airplane mode is switched on. Please switch off to get locatoin updates and fetch staelite pass info", Toast.LENGTH_SHORT).show();
            }
        }
    };


    Runnable fetchPasses = new Runnable() {
        @Override
        public void run() {
            fetchPassesInfo(MainActivity.this);
        }
    };

    private void fetchPassesInfo(final DataCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL+getParameters(), null, new Response.Listener<JSONObject> () {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "fetchPassesInfo:onResponse");
                responseProcess.onResponse(response);
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "fetchPassesInfo:onErrorResponse: " + error.getMessage());
                responseProcess.onErrorResponse(error);
                callback.onFailure(error);
            }
        });
        NetworkController.getInstance().addToRequestQueue(getApplicationContext(), jsonObjectRequest);
    };

    private String getParameters() {
        StringBuilder builder = new StringBuilder();

        builder.append("lat").append("=").append(currentLatitude);
        builder.append("&");
        builder.append("lon").append("=").append(currentLongitude);
        return builder.toString();
    }

    /**
     * isAirplaneModeOn - to check system settings airplane mode flag
     *
     * @param context
     * @return true if airplane mode is on, else false
     */
    public static boolean isAirplaneModeOn(Context context){
        return Settings.System.getInt(
                context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON,
                0) != 0;
    }


}
