package co.tecno.sersoluciones.analityco.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Gustavo on 20/05/2018.
 **/
public class CustomCameraRect extends View {

    private Paint paint = new Paint();
    private Rect focusRect;
    private float mWidth;

    private Paint mCharacterPaint;
    private Transformation mTransformation;
    private AlphaAnimation mFadeOut;

    public CustomCameraRect(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(9);
        init();
    }

    //    public void setBlurringRect(Rect mBlurringRect) {
//        this.mBlurringRect = mBlurringRect;
//    }


    /*private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touchStart(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        path.lineTo(mX, mY);
        // commit the path to our offscreen
        canvas.drawPath(path, paint);
        // kill this so we don't double draw
        path.reset();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }*/

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect dr = focusRect;
        if (dr == null) {
            return;
        }

        Paint outFillPaint = new Paint();
        outFillPaint.setAntiAlias(true);
        outFillPaint.setColor(0x70000000);

        float ratio = (float) this.getWidth() / mWidth;
        canvas.drawRect(0, 0, this.getWidth(), dr.top * ratio, outFillPaint);
        canvas.drawRect(0, 0 + dr.top * ratio, dr.left * ratio, this.getHeight(), outFillPaint);
        canvas.drawRect(this.getWidth() - dr.left * ratio, 0 + dr.top * ratio, this.getWidth(), this.getHeight(), outFillPaint);
        canvas.drawRect(0 + dr.left * ratio, this.getHeight() - dr.top * ratio, this.getWidth() - dr.left * ratio, this.getHeight(), outFillPaint);

        canvas.drawLine(dr.left * ratio, this.getHeight()/2, this.getWidth() - dr.left * ratio,
                this.getHeight()/2, mCharacterPaint);
        canvas.drawLine(this.getWidth()/2, 0 + dr.top * ratio, this.getWidth()/2,
                this.getHeight() - dr.top * ratio, mCharacterPaint);

        if (mWidth > 0) {

            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
            if (dr != null) {
                canvas.drawRect(new Rect((int) (ratio * dr.left), (int) (ratio * dr.top), (int) (ratio * dr.right),
                        (int) (this.getHeight() - dr.top * ratio)), paint);
            }
        }
    }

    private void init() {
        //We need a paint to efficiently modify the alpha
        mCharacterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCharacterPaint.setStrokeWidth(2);
        mCharacterPaint.setColor(Color.RED);
        //This is needed to house the current value during the animation
        mTransformation = new Transformation();
        //Construct an animation that will do all the timing math for you
        mFadeOut = new AlphaAnimation(1f, 0f);
        mFadeOut.setDuration(500);
        //mFadeOut.setRepeatMode(ObjectAnimator.RESTART);
        //Use a listener to trigger the end action
        mFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Trigger your action to change screens here.


            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });


    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    private void startAnimation() {
        mFadeOut.start();
        mFadeOut.getTransformation(System.currentTimeMillis(), mTransformation);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mFadeOut.hasStarted() && !mFadeOut.hasEnded()) {
            mFadeOut.getTransformation(System.currentTimeMillis(), mTransformation);
            //Keep drawing until we are done
            mCharacterPaint.setAlpha((int) (255 * mTransformation.getAlpha()));
            invalidate();
        } else {
            //Reset the alpha if animation is canceled
            mCharacterPaint.setAlpha(255);
        }
    }
}