package org.jp.airports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by John on 3/19/2016.
 */
public class PlaceAdapter extends ArrayAdapter<Place> {
    private final Context context;

    public PlaceAdapter(Context context) {
        super(context, -1);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.result_list, parent, false);

        Place place = getItem(position);

        TextView idView = (TextView) rowView.findViewById(R.id.ListID);
        idView.setText(place.id);

        if (!place.isFix) {
            TextView nameView = (TextView) rowView.findViewById(R.id.ListName);
            nameView.setText((place.isNavaid ? place.freq + " " : "") + place.name);

            TextView cityStateView = (TextView) rowView.findViewById(R.id.ListCityState);
            cityStateView.setText(place.city + ", " + place.state);
        }

        String magBrng = String.format("%03.0f°", place.magBrng);
        TextView bearingView = (TextView) rowView.findViewById(R.id.ListBearing);
        bearingView.setText(magBrng);

        String dist = String.format("%.1fnm", place.dist);
        TextView distView = (TextView) rowView.findViewById(R.id.ListDistance);
        distView.setText(dist);

        return rowView;
    }
}
