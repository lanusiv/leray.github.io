package me.better.leray.rafflewheeldemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * A simple custom view to implement raffle wheel
 */
public class RaffleWheelView extends View {
    // red, green, yellow, pink, purple, blue, deep_orange
    private String[] hexColors = {"#F44336", "#4CAF50", "#FFC107", "#E91E63", "#9C27B0", "#2196F3", "#FF5722"};
    private static final String[] TEXTS = { "I'm a cat", "My name's Tom", "I'm a mouse",
            "My name's Jerry", "Come on! fool cat", "miao~ miao~" };

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private int paddingLeft, paddingTop, paddingRight, paddingBottom;
    private int contentWidth, contentHeight;
    private int centerX, centerY;
    private float mTextSize = 90;
    private int mTextColor = Color.BLACK;

    private int wheelSize = 500;
    private int mRadius = 200;

    private RectF mRect;
    private RectF mTextRange;

    private Drawable backgroud;
    private String backgroudColorHex = "#";

    private Canvas mCanvas;
    private Paint mPaint;

    private Bitmap sampleBitmap;

    public RaffleWheelView(Context context) {
        super(context);
        init(null, 0);
    }

    public RaffleWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RaffleWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.RaffleWheelView, defStyle, 0);
/*

        if (a.hasValue(R.styleable.RaffleWheelView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.RaffleWheelView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }
*/

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
//        mPaint.setColor(Color.parseColor(hexColors[0]));
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.argb(255, 23, 243, 44));

        sampleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.p6);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        wheelSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        if (wheelSize <= 0) {
            wheelSize = 500;
        }
        setMeasuredDimension(wheelSize, wheelSize);
        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();

        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = getHeight() - paddingTop - paddingBottom;

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        mRadius = Math.min(contentWidth, contentHeight) / 2;

        mTextRange = new RectF();
        mTextRange.set(getPaddingLeft(), getPaddingLeft(), mRadius * 2
                + getPaddingLeft(), mRadius * 2 + getPaddingLeft());

        mRect = new RectF(centerX - contentWidth / 2, centerY - contentHeight / 2, centerX + contentWidth / 2, centerY + contentHeight / 2);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCanvas = canvas;
        // draw bg
//        mCanvas.drawCircle(centerX, centerY, mRadius, mPaint);
        // draw color blocks
        int startAngle = 0;
        int sweepAngle = 60;

        for (int i = 0; i < 6; i++) {
            mPaint.setColor(Color.parseColor(hexColors[i]));
            mCanvas.drawArc(mRect, startAngle, sweepAngle, true, mPaint);
            drawText(startAngle, sweepAngle, TEXTS[i]);
            // save
            mCanvas.save();
            mCanvas.rotate(startAngle + sweepAngle + sweepAngle, centerX, centerY);
//            mCanvas.drawText(i + " Good Luck!", centerX, mRect.top + 360, mTextPaint);
            drawImage(sampleBitmap, 0);
            mCanvas.restore();
            // restore

            startAngle += sweepAngle;
        }
    }


    /**
     * 绘制图片
     *
     * @param startAngle
     */
    private void drawImage(Bitmap image, float startAngle) {
        // 设置图片的宽度为直径的1/8
        int imgWidth = mRadius / 3;
        int imgHeight = imgWidth * image.getHeight() / image.getWidth();

        float angle = (float) ((-90 + startAngle) * (Math.PI / 180)); // 弧度，此处是计算在圆盘正上方位置的弧度，即角度是-90度

        int position = mRadius * 2 / 3;  // 绘制图像的中心点选在半径的2/3处
        int x = (int) (centerX + position * Math.cos(angle));
        int y = (int) (centerY + position * Math.sin(angle));

        // 确定绘制图片的位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgHeight / 2, x + imgWidth
                / 2, y + imgHeight / 2);

        mCanvas.drawBitmap(image, null, rect, null);
    }

    /**
     * 绘制文本
     *
     * @param startAngle
     * @param sweepAngle
     * @param string
     */
    private void drawText(float startAngle, float sweepAngle, String string) {
        Path path = new Path();
        path.addArc(mTextRange, startAngle, sweepAngle);
        float textWidth = mTextPaint.measureText(string);
        // 利用水平偏移让文字居中
        float hOffset = 0;//(float) (mRadius * 2 * Math.PI/ 6 - textWidth / 2/* */);// 水平偏移
        Log.i("hello", "hOffset: " + hOffset + ", textWidth: " + textWidth);
//        float vOffset = radius / 2 / 6;// 垂直偏移
        float vOffset = mRadius/2;//*mRect.top + */1 * mRadius / 2;
        mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
    }

}
