package org.jp.airports;

public class PanelDB {

    static PanelDB db = null;

    Place obs1Place = null;
    Place obs2Place = null;
    int obs1Radial = 0;
    int obs2Radial = 0;

    public static PanelDB getInstance() {
        if (db == null) db = new PanelDB();
        return db;
    }

    protected PanelDB() { }

    public void setObs1Place(Place place) {
        obs1Place = place;
    }

    public void setObs2Place(Place place) {
        obs2Place = place;
    }

    public void setObs1Radial(int radial) {
        obs1Radial = radial;
    }

    public void setObs2Radial(int radial) {
        obs2Radial = radial;
    }

    public Place getObs1Place() {
        return obs1Place;
    }

    public Place getObs2Place() {
        return obs2Place;
    }

    public int getObs1Radial() {
        return obs1Radial;
    }

    public int getObs2Radial() {
        return obs2Radial;
    }
}
