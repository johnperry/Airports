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
    private Hashtable<String, Place> airportDB = new Hashtable<String, Place>();
    private Hashtable<String, Place> seaportDB = new Hashtable<String, Place>();
    private Hashtable<String, Place> vorDB = new Hashtable<String, Place>();
    private Hashtable<String, Place> ndbDB = new Hashtable<String, Place>();
    private Hashtable<String, Place> fixDB = new Hashtable<String, Place>();
    public static final int airportDBID = 0;
    public static final int seaportDBID = 1;
    public static final int vorDBID = 2;
    public static final int ndbDBID = 3;
    public static final int fixDBID = 4;
    private int currentDBID = airportDBID;
    private Hashtable<String, Place> currentDB = null;
    private String version = "";

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
        load(context, fixDB, "Fixes.txt");
        version = getText(context, "Version.txt").trim();
    }

    public String getVersion() {
        return version;
    }

    public void selectDatabase(int dbid) {
        switch (dbid) {
            case airportDBID: currentDB = airportDB; break;
            case seaportDBID: currentDB = seaportDB; break;
            case vorDBID: currentDB = vorDB; break;
            case ndbDBID: currentDB = ndbDB; break;
            case fixDBID: currentDB = fixDB; break;
            default: return;
        }
        currentDBID = dbid;
    }

    private void load(Context context, Hashtable<String, Place>table, String filename) {
        AssetManager am = context.getAssets();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(am.open(filename)));
            String line;
            while ((line = br.readLine()) != null) {
                Place place = new Place(line);
                table.put(place.getID(), place);
            }
        }
        catch (Exception ex) {
            Toast toast = Toast.makeText(context, "Unable to load the "+filename+" database", Toast.LENGTH_LONG);
            toast.show();
        }
        finally {
            try { if (br != null) br.close(); }
            catch (Exception ignore) { }
        }
    }

    private String getText(Context context, String filename) {
        AssetManager am = context.getAssets();
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        try {
            br = new BufferedReader(new InputStreamReader(am.open(filename)));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        }
        catch (Exception ex) {
            Toast toast = Toast.makeText(context, "Unable to read "+filename, Toast.LENGTH_LONG);
            toast.show();
            return null;
        }
        finally {
            try { if (br != null) br.close(); }
            catch (Exception ignore) { }
        }
    }


    public Place getStation(String id) {
        return currentDB.get(id);
    }

    public LinkedList<Place> search(String sc) {
        sc = sc.toLowerCase();
        LinkedList<Place> list = new LinkedList<Place>();
        for (Place place : currentDB.values()) {
            if (place.matches(sc)) {
                list.add(place);
            }
        }
        return list;
    }

    public LinkedList<Place> search(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double delta = 2.0;
        if (currentDB.equals(fixDB)) delta = 1.0;
        LinkedList<Place> list = new LinkedList<Place>();
        for (Place place : currentDB.values()) {
            if ( (Math.abs(place.lat - lat) < delta)
                    && (Math.abs(place.lon - lon) < delta)) {
                list.add(place);
            }
        }
        return list;
    }

    public double getDistance(String fromID, String toID) {
        Place fromAP = currentDB.get(fromID);
        Place toAP = currentDB.get(toID);
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
