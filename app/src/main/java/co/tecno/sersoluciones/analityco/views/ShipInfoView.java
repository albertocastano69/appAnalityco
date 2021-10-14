package co.tecno.sersoluciones.analityco.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenautocomplete.TokenCompleteTextView;

import java.util.regex.Pattern;

import co.tecno.sersoluciones.analityco.R;

/**
 * Created by Ser Soluciones SAS on 17/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ShipInfoView extends TokenCompleteTextView<String> {

    public ShipInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(String item) {
        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TokenTextView token = (TokenTextView) l.inflate(R.layout.ship_info, (ViewGroup) getParent(), false);
        token.setText(item);
        return token;
    }

    @Override
    protected String defaultObject(String completionText) {

        if (isDomainValid(completionText)) {
            return completionText;
        } else {
            return "";
        }
    }

    private boolean isDomainValid(String email) {
        Pattern pattern = Patterns.DOMAIN_NAME;
        return pattern.matcher(email).matches();
    }

}