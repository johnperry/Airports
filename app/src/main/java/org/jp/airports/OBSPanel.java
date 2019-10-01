package org.jp.airports;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

public class OBSPanel extends AppCompatActivity implements LocationListener {

    OBS obs1;
    OBS obs2;

    static Place obs1Place = null;
    static Place obs2Place = null;

    LocationManager locationManager = null;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panel_display);
        obs1 = findViewById(R.id.OBS1);
        obs2 = findViewById(R.id.OBS2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    200,   // msec
                    10, // meters minimum change
                    this);  //LocationListener
            onLocationChanged(location);
        }

    }

    protected void onStop() {
        super.onStop();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    protected void onResume() {
        super.onResume();
        PanelDB db = PanelDB.getInstance();
        obs1.setPlace(db.getObs1Place());
        obs2.setPlace(db.getObs2Place());
        obs1.setRadial(db.getObs1Radial());
        obs2.setRadial(db.getObs2Radial());

    }

    protected void onPause() {
        super.onPause();
        PanelDB db = PanelDB.getInstance();
        db.setObs1Radial(obs1.getRadial());
        db.setObs2Radial(obs2.getRadial());
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            obs1.setLocation(location);
            obs2.setLocation(location);
        }
    }
    public void onProviderDisabled(String provider) { }
    public void onProviderEnabled(String provider) { }
    public void onStatusChanged(String provider, int x, Bundle bundle) { }

}
