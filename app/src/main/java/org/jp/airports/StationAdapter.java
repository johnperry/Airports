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
public class StationAdapter extends ArrayAdapter<Station> {
    private final Context context;

    public StationAdapter(Context context) {
        super(context, -1);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.result_list, parent, false);

        Station station = getItem(position);

        TextView idView = (TextView) rowView.findViewById(R.id.ListID);
        idView.setText(station.id);

        TextView nameView = (TextView) rowView.findViewById(R.id.ListName);
        nameView.setText( (station.isNavaid?station.freq+" ":"") + station.name );

        TextView cityStateView = (TextView) rowView.findViewById(R.id.ListCityState);
        cityStateView.setText(station.city + ", " + station.state);

        String magBrng = String.format("%03.0f°", station.magBrng);
        TextView bearingView = (TextView) rowView.findViewById(R.id.ListBearing);
        bearingView.setText(magBrng);

        String dist = String.format("%.1fnm", station.dist);
        TextView distView = (TextView) rowView.findViewById(R.id.ListDistance);
        distView.setText(dist);

        return rowView;
    }
}
