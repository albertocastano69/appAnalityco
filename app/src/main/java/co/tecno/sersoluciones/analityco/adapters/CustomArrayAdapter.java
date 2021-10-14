package co.tecno.sersoluciones.analityco.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import co.tecno.sersoluciones.analityco.R;

/**
 * Created by Ser Soluciones SAS on 17/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */

public class CustomArrayAdapter extends ArrayAdapter{

    public CustomArrayAdapter(@NonNull Context context, @NonNull String[] objects) {
        super(context, R.layout.simple_spinner_item, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView tv = (TextView) view;
        if (position == 0) tv.setTextColor(Color.GRAY);
        else tv.setTextColor(Color.BLACK);
        return view;
    }

}
