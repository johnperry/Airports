package org.jp.airports;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by John on 3/21/2016.
 */
public class SelectionDisplay extends AppCompatActivity implements LocationListener {
    LocationManager locationManager;
    Location location = null;
    String id;
    double lat;
    double lon;
    Station station = null;
    boolean isAirport = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Intent intent = getIntent();
        id = intent.getStringExtra(Search.SELECTIONID);

        Database db = Database.getInstance();
        station = db.getStation(id);
        if (station != null) display(station);
        else Toast.makeText(getApplicationContext(), "Cannot find "+id, Toast.LENGTH_LONG).show();
    }

    private void display(Station station) {
        lat = station.lat;
        lon = station.lon;
        String latlon = String.format("(%.3f, %.3f)", lat, lon);
        String wmmvar = String.format("%.1f", station.getWMMMagneticDeclination());

        setText(R.id.ID, station.id);
        setText(R.id.Name,
                (station.isNavaid?station.freq+" ":"") + station.name + (station.isNavaid?" "+station.type:""));
        setText(R.id.City, station.city);
        setText(R.id.State, station.state);
        setText(R.id.Elev, station.elev, " ft", R.id.ElevRow);
        setText(R.id.LatLon, latlon);
        setText(R.id.Runway, station.rwy, "", R.id.RunwaysRow);
        setText(R.id.Freq, station.freq, "", R.id.FreqRow);
        setText(R.id.Var, station.var, "°", R.id.VarRow);
        setText(R.id.WMMDec, wmmvar, "°", R.id.WMMDecRow);
        displayLocationParams(station.dist, station.trueBrng, station.magBrng);
        if (station.isNavaid) {
            View airNavButton = (View)findViewById(R.id.AirNavButton);
            airNavButton.setVisibility(View.GONE);
        }
    }

    private void displayLocationParams(double dist, double trueBrng, double magBrng) {
        String distString = (dist >= 0.0) ?
                String.format("%.1f", dist)
                : null;
        String trueBrngString = (dist >= 0.0) ?
                String.format("%03.0f", trueBrng)
                : null;
        String magBrngString = (dist >= 0.0) ?
                String.format("%03.0f", magBrng)
                : null;

        setText(R.id.Dist, distString, " nm", R.id.DistRow);
        setText(R.id.TrueBearing, trueBrngString, "°", R.id.TrueBearingRow);
        setText(R.id.MagBearing, magBrngString, "°", R.id.MagBearingRow);

        String latlon = (location != null) ?
                String.format("(%.3f, %.3f)", location.getLatitude(), location.getLongitude())
                : null;
        String wmmdec =  (location != null) ?
                String.format("%.1f", Station.getWMMMagneticDeclination(location))
                : null;
        String altitude =  (location != null) ?
                String.format("%.0f", location.getAltitude() * 39.37/12)
                : null;

        View titleRow = (View)findViewById(R.id.CurrentLocationTitleRow);
        if (location == null) {
            titleRow.setVisibility(View.GONE);
        }
        else {
            titleRow.setVisibility(View.VISIBLE);
        }
        setText(R.id.CurrentLocationLatLon, latlon, "", R.id.CurrentLocationLatLonRow);
        setText(R.id.CurrentLocationWMMDec, wmmdec, "°", R.id.CurrentLocationWMMDecRow);
        setText(R.id.CurrentLocationAltitude, altitude, " ft", R.id.CurrentLocationAltitudeRow);
    }

    private void setText(int id, String text, String units, int rowID) {
        View row = (View)findViewById(rowID);
        if ((text == null) || text.trim().equals("")) {
            row.setVisibility(View.GONE);
        }
        else {
            setText(id, text + units);
            row.setVisibility(View.VISIBLE);
        }
    }

    private void setText(int id, String text) {
        TextView view = (TextView) findViewById(id);
        view.setText(text);
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
                    3000,   // 3 sec
                    10, //10 meter minimum change
                    this);  //LocationListener
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            this.location = location;
            station.setDistanceFrom(location);
            displayLocationParams(station.dist, station.trueBrng, station.magBrng);
        }
    }

    public void onProviderDisabled(String provider) { }
    public void onProviderEnabled(String provider) { }
    public void onStatusChanged(String provider, int x, Bundle bundle) { }

    public void startMap(View view) {
        String uri = String.format("geo:%.4f,%.4f?z=14", lat, lon);
        Uri intentUri = Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW, intentUri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else Toast.makeText(getApplicationContext(),
                "The Map application is unavailable", Toast.LENGTH_LONG).show();
    }

    public void startEarth(View view) {
        String uri = String.format("geo:%.4f,%.4f?z=15", lat, lon);
        Uri intentUri = Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW, intentUri);
        intent.setPackage("com.google.earth");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else Toast.makeText(getApplicationContext(),
                "The Earth application is unavailable", Toast.LENGTH_LONG).show();
    }

    public void startBrowser(View view) {
        Uri uri = Uri.parse("http://www.airnav.com/airport/" + id);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else Toast.makeText(getApplicationContext(),
                "The browser is unavailable", Toast.LENGTH_LONG).show();
    }
}
