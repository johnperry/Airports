package org.jp.airports;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created by John on 3/19/2016.
 */
public class Database {
    private static Database instance = null;
    private static final int ndbs = 4;
    private Hashtable<String, Station> airportDB = new Hashtable<String, Station>();
    private Hashtable<String, Station> seaportDB = new Hashtable<String, Station>();
    private Hashtable<String, Station> vorDB = new Hashtable<String, Station>();
    private Hashtable<String, Station> ndbDB = new Hashtable<String, Station>();
    public static final int airportDBID = 0;
    public static final int seaportDBID = 1;
    public static final int vorDBID = 2;
    public static final int ndbDBID = 3;
    private int currentDBID = airportDBID;
    private Hashtable<String, Station> currentDB = null;

    public static Database getInstance(Context context) {
        if (instance == null) {
            instance = new Database(context);
        }
        return instance;
    }

    public static Database getInstance() {
        return instance;
    }

    protected Database(Context context) {
        load(context, airportDB, "Airports.txt");
        load(context, seaportDB, "Seaports.txt");
        load(context, vorDB, "VORs.txt");
        load(context, ndbDB, "NDBs.txt");
    }

    public void selectDatabase(int dbid) {
        switch (dbid) {
            case airportDBID: currentDB = airportDB; break;
            case seaportDBID: currentDB = seaportDB; break;
            case vorDBID: currentDB = vorDB; break;
            case ndbDBID: currentDB = ndbDB; break;
            default: return;
        }
        currentDBID = dbid;
    }

    private void load(Context context, Hashtable<String,Station>table, String filename) {
        AssetManager am = context.getAssets();
        BufferedReader br = null;
        try {
            try {
                br = new BufferedReader(new InputStreamReader(am.open(filename)));
                String line;
                while ((line = br.readLine()) != null) {
                    Station station = new Station(line);
                    table.put(station.getID(), station);
                }
            }
            finally {
                if (br != null) br.close();
            }
        }
        catch (Exception ex) {
            Toast toast = Toast.makeText(context, "Unable to load the "+filename+" database", Toast.LENGTH_LONG);
            toast.show();
        }
    }


    public Station getStation(String id) {
        return currentDB.get(id);
    }

    public LinkedList<Station> search(String sc) {
        sc = sc.toLowerCase();
        LinkedList<Station> list = new LinkedList<Station>();
        for (Station station : currentDB.values()) {
            if (station.matches(sc)) {
                list.add(station);
            }
        }
        return list;
    }

    public LinkedList<Station> search(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double delta = 3.0;
        LinkedList<Station> list = new LinkedList<Station>();
        for (Station station : currentDB.values()) {
            if ( (Math.abs(station.lat - lat) < delta)
                    && (Math.abs(station.lon - lon) < delta)) {
                list.add(station);
            }
        }
        return list;
    }

    public double getDistance(String fromID, String toID) {
        Station fromAP = currentDB.get(fromID);
        Station toAP = currentDB.get(toID);
        if ((fromAP != null) && (toAP != null)) {
            V3 fromV3 = new V3(fromAP.lat, fromAP.lon);
            V3 toV3 = new V3(toAP.lat, toAP.lon);
            double dotProduct = fromV3.dot(toV3);
            double angle = Math.acos(dotProduct);
            return angle * 3440.0; //scale to nm
        }
        return 0.0;
    }
}
