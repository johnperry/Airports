package org.jp.airports;

import android.location.Location;

/**
 * Created by John on 3/22/2016.
 */
public class V3 {
    double x, y, z;
    static V3 pole = new V3(0.0, 0.0, 1.0);
    public V3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public V3(Location location) {
        this(location.getLatitude(), location.getLongitude());
    }
    public V3(double lat, double lon) {
        lat = lat * Math.PI / 180.0;
        lon = lon * Math.PI / 180.0;
        z = Math.sin(lat);
        x = Math.cos(lat) * Math.cos(lon);
        y = Math.cos(lat) * Math.sin(lon);
    }
    public V3 normalize() {
        double len = length();
        if (len > 0.0) return scale(1.0/length());
        else return new V3(0.0, 0.0, 0.0);
    }
    public V3 scale(double r) {
        return new V3(x * r, y * r, z * r);
    }
    public double length() {
        return Math.sqrt(dot(this));
    }
    public double dot(V3 v) {
        return x * v.x + y * v.y + z * v.z;
    }
    public V3 cross(V3 v) {
        return new V3(
                y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x );
    }
    public double bearingTo(V3 dest) {
        V3 course = this.cross(dest).cross(this).normalize();
        V3 north = this.cross(pole).cross(this).normalize();
        V3 east = north.cross(this);
        double theta = Math.acos(course.dot(north));
        if (course.dot(east) < 0) theta = 2 * Math.PI - theta;
        return theta * 180.0 / Math.PI;
    }
}
