package com.example.gayanlakshitha.dengueanalyzis;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by Gayan Lakshitha on 9/19/2017.
 */

public class GPSTracker implements LocationListener {

    Context context;
    private LocationManager locationManager;
    private Location location;

    public GPSTracker(Context context) {
        this.context = context;
    }

    public Location getLocation() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"Permission not Granted",Toast.LENGTH_SHORT).show();
            return null;
        }

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGPSEnabled) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return location;
        }
        else
        {
            Toast.makeText(context,"Please Enable GPS",Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
