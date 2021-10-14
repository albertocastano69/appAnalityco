package co.tecno.sersoluciones.analityco.views;

import android.content.Context;
import android.util.AttributeSet;

import co.tecno.sersoluciones.analityco.R;

/**
 * Created by Ser Soluciones SAS on 17/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class TokenTextView extends androidx.appcompat.widget.AppCompatTextView {

    public TokenTextView(Context context) {
        super(context);
    }

    public TokenTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setCompoundDrawablesWithIntrinsicBounds(0, 0, selected ? R.drawable.ic_close_circle : 0, 0);
    }
}