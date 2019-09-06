package org.jp.airports;

import android.Manifest;
import android.support.v7.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Collections;
import java.util.LinkedList;

public class Search extends AppCompatActivity implements LocationListener {

    public final static String SELECTIONID = "org.jp.airports.SELECTIONID";
    private static final int LOCATION_REQUEST_CODE = 400;

    LocationManager locationManager = null;
    Location location = null;
    ListView listView;
    StationAdapter stationAdapter;
    Toast searchToast = null;
    String lastSearch = "";
    boolean nearestSearch = false;
    LinkedList<Station> list = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Database db = Database.getInstance(getApplicationContext());
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupPermissions();

        listView = (ListView) findViewById(R.id.ResultList);
        stationAdapter = new StationAdapter(this);

        listView.setAdapter(stationAdapter);
        listView.setOnItemClickListener(new SearchSelectionListener(this));

        EditText editText = (EditText) findViewById(R.id.SearchText);
        editText.addTextChangedListener(new SearchTextWatcher());

        db.selectDatabase(Database.airportDBID);
        search(location, true);
    }

    private void setupPermissions() {
        int locPerm = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        if (locPerm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
        else initLocService();
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if ((grantResults.length == 0) ||
                        (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(),
                            "Cannot obtain permission for LocationManager", Toast.LENGTH_LONG).show();
                }
                else initLocService();
            }
        }
    }

    private void initLocService() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                3000,   // 3 sec
                100, //100 meter minimum change
                this);   //LocationListener
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new NearestListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
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
            if (nearestSearch) {
                search(location, false);
            }
            else if (list != null) {
                showResults(list, false);
            }
        }
    }
    public void onProviderDisabled(String provider) {
        Toast.makeText(getBaseContext(), "GPS disabled", Toast.LENGTH_LONG).show();
    }
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "GPS enabled", Toast.LENGTH_LONG).show();
    }
    public void onStatusChanged(String provider, int x, Bundle bundle) { }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        ActionBar ab = getSupportActionBar();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_airports) {
            ab.setTitle(R.string.action_airports);
            Database.getInstance().selectDatabase(Database.airportDBID);
        } else if (id == R.id.action_seaports) {
            ab.setTitle(R.string.action_seaports);
            Database.getInstance().selectDatabase(Database.seaportDBID);
        } else if (id == R.id.action_vors) {
            ab.setTitle(R.string.action_vors);
            Database.getInstance().selectDatabase(Database.vorDBID);
        } else if (id == R.id.action_ndbs) {
            ab.setTitle(R.string.action_ndbs);
            Database.getInstance().selectDatabase(Database.ndbDBID);
        } else return super.onOptionsItemSelected(item);

        listView.setAdapter(stationAdapter);
        search(location, true);
        return true;
    }

    class SearchSelectionListener implements AdapterView.OnItemClickListener {
        AppCompatActivity creator;
        public SearchSelectionListener(AppCompatActivity creator) {
            this.creator = creator;
        }
        public void onItemClick(AdapterView<?> av, View view, int position, long id) {
            Intent intent = new Intent(creator, SelectionDisplay.class);
            Station station = stationAdapter.getItem(position);
            intent.putExtra(SELECTIONID, station.getID());
            startActivity(intent);
        }
    }

    class NearestListener implements View.OnClickListener {
        public NearestListener() { }
        public void onClick(View view) {
            if (location != null) {
                search(location, true);
                nearestSearch = true;
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "Location is unavailable", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void clear(View view) {
        EditText searchText = (EditText) findViewById(R.id.SearchText);
        searchText.setText("");
        search(view);
    }

    class SearchTextWatcher implements TextWatcher {
        public SearchTextWatcher() { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void afterTextChanged(Editable s) {
            if (s.length() > 1) {
                search();
                nearestSearch = false;
            }
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }

    public void search(View view) {
        lastSearch += "x";
        search();
    }

    public void search(Location loc, boolean showToast) {
        if (searchToast != null) searchToast.cancel();
        Database db = Database.getInstance();
        list = db.search(loc);
        showResults(list, showToast);
    }

    public void search() {
        if (searchToast != null) searchToast.cancel();
        EditText searchText = (EditText) findViewById(R.id.SearchText);
        String text = searchText.getText().toString();
        if (!text.equals(lastSearch)) {
            lastSearch = text;
            Database db = Database.getInstance();
            list = db.search(text);
            showResults(list, true);
        }
    }

    private void showResults(LinkedList<Station> list, boolean showToast) {
        if (location != null) {
            for (Station ap : list) ap.setDistanceFrom(location);
        }
        Collections.sort(list);
        stationAdapter.clear();
        stationAdapter.addAll(list);
        if (showToast) {
            int n = list.size();
            String result = (n > 0) ?
                    n + " station" + ((n > 1) ? "s" : "") + " found" :
                    "No matches found";
            Context context = getApplicationContext();
            searchToast = Toast.makeText(context, result, Toast.LENGTH_SHORT);
            searchToast.show();
        }
    }

}
