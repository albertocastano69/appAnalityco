package co.tecno.sersoluciones.analityco.adapters.contracts;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import java.util.regex.Pattern;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.utilities.Constantes;


public class SpinnerAdapterImage extends ArrayAdapter<String> {

    // Your sent context
    private final Context context;
    // Your custom values for the spinner (User)
    private final String[] values;

    public SpinnerAdapterImage(Context context, int textViewResourceId,
                               String[] values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    public int getCount() {
        return values.length;
    }

    public String getItem(int position) {
        return values[position];
    }

    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ImageView label = new ImageView(context);
        String url = Constantes.URL_IMAGES + values[position];
        chargeImage(url, label);
        //label.setImageResource(values[position]);

        return label;
    }

    private void chargeImage(String url, ImageView label) {
        String[] format = url.split(Pattern.quote("."));
        if (format[format.length - 1].equals("svg")) {
            Glide.with(getContext())
                    .load(url)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.loading_animation)
                            .error(R.drawable.image_not_available))
                    .into(label);
        } else {
            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(label);
        }
    }
    
    @Override
    public View getDropDownView(int position, View convertView,
                                @NonNull ViewGroup parent) {
        ImageView label = new ImageView(context);
        String url = Constantes.URL_IMAGES + values[position];

        chargeImage(url, label);
        /**/

        return label;
    }
}
