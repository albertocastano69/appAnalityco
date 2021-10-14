package co.tecno.sersoluciones.analityco.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import co.tecno.sersoluciones.analityco.R;

public class AdapaterCountry extends ArrayAdapter<String> {
    Context mcontext;
    String[] names;
    int[] images;

    public AdapaterCountry(Context context,String[] names, int[] images){
        super(context,R.layout.country_car_item);
        this.mcontext = context;
        this.images = images;
        this.names = names;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            LayoutInflater layoutInflater = (LayoutInflater)mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.country_car_item,null);
            TextView nameCountry = row.findViewById(R.id.country_car);
            ImageView imageCountry = row.findViewById(R.id.flag_country);

            nameCountry.setText(names[position]);
            imageCountry.setImageResource(images[position]);

        return row;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater)mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.country_car_item,null);
        TextView nameCountry = row.findViewById(R.id.country_car);
        ImageView imageCountry = row.findViewById(R.id.flag_country);

        nameCountry.setText(names[position]);
        imageCountry.setImageResource(images[position]);

        return row;
    }
}
