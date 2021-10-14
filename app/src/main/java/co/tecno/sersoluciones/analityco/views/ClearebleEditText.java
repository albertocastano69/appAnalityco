package co.tecno.sersoluciones.analityco.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;


public class ClearebleEditText extends androidx.appcompat.widget.AppCompatEditText
        implements View.OnTouchListener, View.OnFocusChangeListener, TextWatcherAdapter.TextWatcherListener{

    @Override
    public void onTextChanged(EditText view, String text) {
        if (isFocused()) {
            setClearIconVisible(!text.isEmpty());
        }
    }

    public enum Location {
        LEFT(0), RIGHT(2);

        final int idx;

        Location(int idx) {
            this.idx = idx;
        }
    }

    interface Listener {
        void didClearText();
    }

    public ClearebleEditText(Context context) {
        super(context);
        init();
    }

    public ClearebleEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearebleEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(ClearebleEditText.Listener listener) {
        this.listener = listener;
    }

    /**
     * null disables the icon
     */
    public void setIconLocation(ClearebleEditText.Location loc) {
        this.loc = loc;
        initIcon();
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.l = l;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener f) {
        this.f = f;
    }

    private ClearebleEditText.Location loc = ClearebleEditText.Location.RIGHT;

    private Drawable xD;
    private Drawable backIcon;
    private ClearebleEditText.Listener listener;

    private OnTouchListener l;
    private OnFocusChangeListener f;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getDisplayedDrawable() != null) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int left = (loc == ClearebleEditText.Location.LEFT) ? 0 : getWidth() - getPaddingRight() - xD.getIntrinsicWidth();
            int right = (loc == ClearebleEditText.Location.LEFT) ? getPaddingLeft() + xD.getIntrinsicWidth() : getWidth();
            boolean tappedX = x >= left && x <= right && y >= 0 && y <= (getBottom() - getTop());
            if (tappedX) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setText("");
                    if (listener != null) {
                        listener.didClearText();
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
        setClearIconVisible(!getText().toString().isEmpty());
        /*if (hasFocus) {
            setClearIconVisible(isNotEmpty(getText()));
        } else {
            setClearIconVisible(false);
        }*/
        if (f != null) {
            f.onFocusChange(v, hasFocus);
        }
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

    private void setClearIconVisible(boolean visible) {
        Drawable[] cd = getCompoundDrawables();
        Drawable displayed = getDisplayedDrawable();
        boolean wasVisible = (displayed != null);
        if (visible != wasVisible) {
            Drawable x = visible ? xD : null;
            super.setCompoundDrawables((loc == Location.LEFT) ? x : cd[0], cd[1], (loc == Location.RIGHT) ? x : cd[2],
                    cd[3]);
        }
    }
}
