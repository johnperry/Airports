package org.jp.airports;

import android.hardware.GeomagneticField;
import android.location.Location;

import java.util.Calendar;

/**
 * Created by John on 3/19/2016.
 */
public class Place implements Comparable<Place> {
    private static final String TAG = "Place";
    public String id;
    public String type = "";
    public String name = "";
    public String city = "";
    public String state = "";
    public double lat = 0.0;
    public double lon = 0.0;
    public String elev = "";
    public String rwy = "";
    public String freq = "";
    public String var = "";
    public double dvar = 0.0;
    public double dist = 0.0;
    public double trueBrng = 0.0;
    public double magBrng = 0.0;

    public boolean isAirport = false;
    public boolean isSeaport = false;
    public boolean isVOR = false;
    public boolean isNDB = false;
    public boolean isPort = false;
    public boolean isNavaid = false;
    public boolean isFix = false;

    private String text;

    public Place(String stationText) {
        String[] txt = stationText.split("\\|");
        for (int i=0; i<txt.length; i++) {
            switch (i) {
                case 0: this.id = txt[0]; break;
                case 1: this.type = txt[1]; break;
                case 2: this.name = txt[2]; break;
                case 3: this.city = txt[3]; break;
                case 4: this.state = txt[4]; break;
                case 5:
                    String[] latlon = txt[5].split(",");
                    this.lat = Double.parseDouble(latlon[0]);
                    this.lon = Double.parseDouble(latlon[1]);
                    break;
                case 6: this.elev = txt[6]; break;
                case 7: this.rwy = txt[7].replaceAll(";","\n"); break;
                case 8:
                    this.var = txt[8];
                    try {
                        String varx = txt[8].substring(txt[8].length() - 1);
                        dvar = Double.parseDouble(varx);
                        if (txt[8].endsWith("W")) dvar = -dvar;
                    }
                    catch (Exception ex) { dvar = 0.0; }
                    break;
                case 9: this.freq = txt[9].replaceAll(";","\n"); break;
            }
        }
        isAirport = type.equals("AP");
        isSeaport = type.equals("SP");
        isVOR = type.startsWith("VO"); //VOR, VORTAC, VOT, VOR/DME
        isNDB = type.equals("NDB");
        isPort = isAirport || isSeaport;
        isNavaid = isVOR || isNDB;
        isFix = type.equals("FIX") || type.equals("MIL");

        if (!isFix) text = (id+"|"+name+"|"+city+"|"+state).toLowerCase();
        else text = (id+"|"+type+"|"+state).toLowerCase();
    }

    public String getID() {
        return id;
    }

    public boolean matches(String sc) {
        return text.contains(sc);
    }

    public int compareTo(Place place) {
        if ((dist >= 0.0) && (place.dist >= 0.0)) {
            if (dist < place.dist) return -1;
            if (dist > place.dist) return 1;
        }
        return id.compareTo(place.id);
    }

    public void setDistanceFrom(Location location) {
        dist = getDistanceFrom(location);
        trueBrng = getTrueBearingFrom(location);
        magBrng = trueBrng - getWMMMagneticDeclination(location);
    }

    public double getDistanceFrom(Location location) {
        if (location != null) {
            V3 fromV3 = new V3(location.getLatitude(), location.getLongitude());
            V3 toV3 = new V3(lat, lon);
            double dotProduct = fromV3.dot(toV3);
            double angle = Math.acos(dotProduct);
            return angle * 3440.0; //scale to nm
        }
        return -1.0;
    }

    public double getTrueBearingFrom(Location location) {
        if (location != null) {
            V3 loc = new V3(location);
            V3 here = new V3(lat, lon);
            return loc.bearingTo(here);
        }
        return -1.0;
    }

    public static double getWMMMagneticDeclination(Location location) {
        GeomagneticField gmf = new GeomagneticField(
                (float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                Calendar.getInstance().getTimeInMillis());
        return gmf.getDeclination();
    }

    public double getWMMMagneticDeclination() {
        try {
            double elevInMeters = Double.parseDouble(elev) * 12/39.37;
            GeomagneticField gmf = new GeomagneticField(
                    (float) lat,
                    (float) lon,
                    (float) elevInMeters,
                    Calendar.getInstance().getTimeInMillis());
            return gmf.getDeclination();
        }
        catch (Exception ex) {
            return 0.0;
        }
    }
}
