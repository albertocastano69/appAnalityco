package co.tecno.sersoluciones.analityco.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.google.android.gms.vision.barcode.Barcode;

public class CustomBarcodeGraphic extends View {

    private int mId;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN
    };

    private static int mCurrentColorIndex = 0;

    private final Paint mRectPaint;
    private final Paint mTextPaint;
    private boolean success;
    private float mWidth;
    private Barcode mBarcode;

    public CustomBarcodeGraphic(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mRectPaint = new Paint();
        mRectPaint.setColor(selectedColor);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(4.0f);

        mTextPaint = new Paint();
        mTextPaint.setColor(selectedColor);
        mTextPaint.setTextSize(36.0f);
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    /**
     * Draws the barcode annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        Barcode barcode = mBarcode;
        if (barcode == null) {
            return;
        }
        if (!success) {
            mRectPaint.setColor(COLOR_CHOICES[2]);
            mRectPaint.setStrokeWidth(5.0f);
        } else {
            mRectPaint.setColor(COLOR_CHOICES[0]);
            mRectPaint.setStrokeWidth(4.0f);
        }

        if (barcode != null) {

            // Draws the bounding box around the barcode.
            RectF rectf = new RectF(barcode.getBoundingBox());
            float ratio = (float) this.getWidth() / mWidth;
            Rect rect = new Rect((int) (ratio * rectf.left), (int) (ratio * rectf.top), (int) (ratio * rectf.right),
                    (int) (ratio * rectf.bottom));
            canvas.drawRect(rect, mRectPaint);

            // Draws a label at the bottom of the barcode indicate the barcode value that was detected.
            canvas.drawText(barcode.rawValue, rect.left, rect.bottom, mTextPaint);
        }
    }

}
