package com.vijay.customapp.spacestationpasses;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import static com.vijay.customapp.spacestationpasses.Constants.DBG;
import static com.vijay.customapp.spacestationpasses.Constants.TAG;

/**
 * CustomLocationService : locatoin listener service using best available location provider
 * accuracy and distance are constants which can be altered to get better performances
 * interval set to 1000 milli seconds for updates
 *
 */
public class CustomLocationService extends Service {
    LocalBinder mLocalbinder = new LocalBinder();

    private static final double MAX_LATITUDE = 80.0;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 5;
    private LocationManager mLocationManager = null;
    private Location mLastLocation = null;
    private Context mContext;

    public CustomLocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mLocalbinder;
    }

    public class LocalBinder extends Binder {
        CustomLocationService getService() {
            return CustomLocationService.this;
        }
    }

    /**
     * intialiseLocationListener
     * get the system location service and get the best provider for location updates.
     *
     * register the location listener for updates.
     *
     */
    public void intialiseLocationListener() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(true);
        mLastLocation = new Location("dummyprovider");
        Log.d(TAG, "requesting criteria ");
        try {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            String provider = mLocationManager.getBestProvider(criteria, true);
            if (provider != null) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "requesting permission not granted: failed");
                } else {
                    // got a best provider for location updates, add a listener for location
                    Log.d(TAG, "requesting location updates with best provider: " + provider);
                    mLocationManager.requestLocationUpdates(provider, LOCATION_INTERVAL, LOCATION_DISTANCE, myLocationListener);
                }
            } else {
                Log.d(TAG, "requesting location with provider: null: failed");
            }
        }catch (Exception e) {
            Log.e(TAG, "Exception at location listener: " + e.getMessage());
           // e.printStackTrace();
        }
    }

    /**
     * Stop listening the location updates by removing the listener
     */
    public void stopLocationUpdates() {
        if(mLocationManager!= null)
            // remove the location listener from location manager
            mLocationManager.removeUpdates(myLocationListener);
    }

    /**
     * onDestroy - remove the listener update from location manager
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    /**
     * create a local location listener to be used with location manager
     * Implement the listener callbacks , mainly on location changed.
     *
     */
    private final LocationListener myLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if(DBG) Log.d(TAG, "onLocationChange: lat: " + location.getLatitude() + " long: " + location.getLongitude());
            mLastLocation.set(location);
            sendLocationMessageToActivity();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            //Log.d(TAG, "requesting onStatusChanged : " + s + " int i: " + i);
        }

        @Override
        public void onProviderEnabled(String s) {
            //Log.d(TAG, "requesting onProviderEnabled : " + s);
        }

        @Override
        public void onProviderDisabled(String s) {
            //Log.d(TAG, "requesting onProviderDisabled : " + s);
        }
    };

    private void sendLocationMessageToActivity() {
        Intent intent = new Intent(Constants.LOCATION_MSG);
        sendLocationBroadcast(intent);
    }

    private void sendLocationBroadcast(Intent intent){
        intent.putExtra(Constants.LATITUDE, mLastLocation.getLatitude());
        intent.putExtra(Constants.LONGITUDE, mLastLocation.getLongitude());

        if(Math.abs(mLastLocation.getLatitude()) <= MAX_LATITUDE ) {
            intent.putExtra(Constants.VALIDITY, true);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else {
            Log.d(TAG, "Latitute is out of range");
            intent.putExtra(Constants.VALIDITY, false);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }
}
