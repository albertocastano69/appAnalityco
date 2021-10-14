package co.tecno.sersoluciones.analityco.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;

/**
 * Created by Gustavo on 20/05/2018.
 **/
public class AutoFocusCameraRect extends View {

    private Rect focusRect;
    private float mWidth;
    private Transformation mTransformation;
    private Paint mPaint;
    private AlphaAnimation mFadeOut;
    private Paint paintText;
    private boolean isAnimation;

    public AutoFocusCameraRect(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {

        //We need a paint to efficiently modify the alpha
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2.0f);
        mPaint.setPathEffect(new CornerPathEffect(60.0f));
        //mPaint.setPathEffect(new DashPathEffect(new float[]{50, 100}, 0));
        //This is needed to house the current value during the animation
        mTransformation = new Transformation();
        //Construct an animation that will do all the timing math for you
        mFadeOut = new AlphaAnimation(1f, 0f);
        mFadeOut.setDuration(500);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(24);
        isAnimation = false;
    }

    private void startAnimation(){
        mFadeOut.start();
        mFadeOut.getTransformation(System.currentTimeMillis(), mTransformation);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        Rect dr = focusRect;
        if (dr == null) {
            return;
        }
        if (mWidth > 0) {

            float ratio = (float) this.getWidth() / mWidth;
            if (dr != null) {

                Path pathB = new Path();
                pathB.moveTo(dr.left * ratio, dr.top * ratio);
                pathB.lineTo(dr.right * ratio, dr.top * ratio);
                pathB.lineTo(dr.right * ratio, dr.bottom * ratio);
                pathB.lineTo(dr.left * ratio, dr.bottom * ratio);
                pathB.lineTo(dr.left * ratio, dr.top * ratio);
                pathB.close();
                canvas.drawPath(pathB, mPaint);

                canvas.drawText("focus area", (int) (dr.left * ratio) + 30, (int) (dr.top * ratio) + 30, paintText);
            }
        }

        if (isAnimation) {
            if (mFadeOut.hasStarted() && !mFadeOut.hasEnded()) {
                mFadeOut.getTransformation(System.currentTimeMillis(), mTransformation);
                //Keep drawing until we are done
                mPaint.setAlpha((int) (255 * mTransformation.getAlpha()));
                paintText.setAlpha((int) (255 * mTransformation.getAlpha()));
                invalidate();
            } else if (mFadeOut.hasEnded()) {
                mPaint.setAlpha(0);
                paintText.setAlpha(0);
            } else {
                //Reset the alpha if animation is canceled
                mPaint.setAlpha(255);
                paintText.setAlpha(255);
            }
        }
    }
}