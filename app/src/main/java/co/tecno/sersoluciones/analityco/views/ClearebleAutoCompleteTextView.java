package co.tecno.sersoluciones.analityco.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;

/**
 * Created by Ser SOluciones SAS on 25/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ClearebleAutoCompleteTextView extends androidx.appcompat.widget.AppCompatAutoCompleteTextView
        implements View.OnTouchListener, View.OnFocusChangeListener, TextWatcherAdapter.TextWatcherListener{

    @Override
    public void onTextChanged(EditText view, String text) {
        if (isFocused()) {
            setClearIconVisible(false);
        }
    }

    public enum Location {
        LEFT(0), RIGHT(2);

        final int idx;

        Location(int idx) {
            this.idx = idx;
        }
    }

    public interface Listener {
        void didClearText(View view);
    }

    public ClearebleAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    public ClearebleAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearebleAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(ClearebleAutoCompleteTextView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.l = l;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener f) {
        this.f = f;
    }

    private ClearebleAutoCompleteTextView.Location loc = ClearebleAutoCompleteTextView.Location.RIGHT;

    private Drawable xD;
    private ClearebleAutoCompleteTextView.Listener listener;

    private OnTouchListener l;
    private OnFocusChangeListener f;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getDisplayedDrawable() != null) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int left = (loc == ClearebleAutoCompleteTextView.Location.LEFT) ? 0 : getWidth() - getPaddingRight() - xD.getIntrinsicWidth();
            int right = (loc == ClearebleAutoCompleteTextView.Location.LEFT) ? getPaddingLeft() + xD.getIntrinsicWidth() : getWidth();
            boolean tappedX = x >= left && x <= right && y >= 0 && y <= (getBottom() - getTop());
            if (tappedX) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setText("");
                    if (listener != null) {
                        listener.didClearText(this);

                    }
                }
                return true;
            }
        }
        if (l != null) {
            return l.onTouch(v, event);
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(isNotEmpty(getText()));
        } else {
            setClearIconVisible(false);
        }
        if (f != null) {
            f.onFocusChange(v, hasFocus);
        }
    }

    private boolean isNotEmpty(Editable text) {
        return !text.toString().isEmpty();
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        initIcon();
    }

    private void init() {
        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
        addTextChangedListener(new TextWatcherAdapter(this, this));
        initIcon();
        setClearIconVisible(false);
    }

    private void initIcon() {
        xD = null;
        if (loc != null) {
            xD = getCompoundDrawables()[loc.idx];
        }
        if (xD == null) {
            xD = ContextCompat.getDrawable(getContext(), R.drawable.ic_close_black_24dp);
        }
        xD.setBounds(0, 0, xD.getIntrinsicWidth(), xD.getIntrinsicHeight());
        int min = getPaddingTop() + xD.getIntrinsicHeight() + getPaddingBottom();
        if (getSuggestedMinimumHeight() < min) {
            setMinimumHeight(min);
        }
    }

    private Drawable getDisplayedDrawable() {
        return (loc != null) ? getCompoundDrawables()[loc.idx] : null;
    }

    public void setClearIconVisible(boolean visible) {
        Drawable[] cd = getCompoundDrawables();
        Drawable displayed = getDisplayedDrawable();
        boolean wasVisible = (displayed != null);
        if (visible != wasVisible) {
            Drawable x = visible ? xD : null;
            super.setCompoundDrawables((loc == ClearebleAutoCompleteTextView.Location.LEFT) ? x : cd[0], cd[1], (loc == ClearebleAutoCompleteTextView.Location.RIGHT) ? x : cd[2],
                    cd[3]);
        }
    }
}
