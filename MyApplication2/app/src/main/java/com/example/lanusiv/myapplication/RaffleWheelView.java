package com.example.lanusiv.myapplication;

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

import java.util.ArrayList;
import java.util.List;


/**
 * 抽奖算法：
 * 1. 第一次点击开始旋转，在旋转时点击停止，停止后指针所指断位置就时中奖位置
 * 2. 每次点击开始抽奖时，传一个参数，此参数代表结束时将要停到的位置
 * 3. 为了能够控制每次转盘停止时指针所指的位置就是后台设定的，需要在点击停止转动的时候，先保证转够原始位置的整数圈，然后再做减速运动
 * 4. 为了保证每次停止的位置都在item的正中间，需要在开始转动前设好当前速度在减速时所产生的偏移量，在开始时加上偏移量
 */
public class RaffleWheelView extends View implements Runnable{
    private static final String TAG = RaffleWheelView.class.getSimpleName();
    private String mViewString;
    private int mViewColor = Color.BLACK; // TODO: use a default from R.color...
    private float mTextSize = 0; // TODO: use a default from R.dimen...
    private Drawable mViewDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private Canvas mCanvas;
    private int centerX, centerY;
    private int radius;
    private static final int SPEED = 60;

    private RectF rect;
    private List<Award> list = new ArrayList<Award>();
    private static int[] images = {R.drawable.p01, R.drawable.p02,
            R.drawable.p03, R.drawable.p04, R.drawable.p6,
            R.drawable.p05};
    private int startAngle = 0;
    private int sweepAngle = 60;
    private int[] colors = {Color.GREEN, Color.RED};
    // red, green, yellow, pink, purple, blue, deep_orange
    private String[] hexColors = {"#F44336", "#4CAF50", "#FFC107", "#E91E63", "#9C27B0", "#2196F3", "#FF5722"};
    private boolean shouldStart = false;
    private boolean stop = false;
    private long curTime;
    private float speed = SPEED;
    private int vInterval = 90;  // 每刷新一次的时间间隔， 每次转60度，转六次完成一圈，即转一圈需要540（6×90）毫秒
    private RectF mTextRange;

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

        mViewString = a.getString(
                R.styleable.RaffleWheelView_exampleString);
        mViewColor = a.getColor(
                R.styleable.RaffleWheelView_exampleColor,
                mViewColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mTextSize = a.getDimension(
                R.styleable.RaffleWheelView_exampleDimension,
                mTextSize);

        if (a.hasValue(R.styleable.RaffleWheelView_exampleDrawable)) {
            mViewDrawable = a.getDrawable(
                    R.styleable.RaffleWheelView_exampleDrawable);
            mViewDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mViewColor);

        initData();
        new Thread(this).start();

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void initData() {
        for (int i = 0; i < 6; i++) {
            Award item = new Award();
            item.name = "Good Luck!";
            item.id = i;
            item.image = BitmapFactory.decodeResource(getResources(), images[i]);

            list.add(item);
        }
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mViewColor);
//        mTextWidth = mTextPaint.measureText(mViewString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    /**
     * 设置控件为正方形
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        radius = Math.min(contentWidth, contentHeight) / 2;
        rect = new RectF();
        rect.set(centerX - contentWidth / 2, centerY - contentHeight / 2, centerX + contentWidth / 2, centerY + contentHeight / 2);

        mTextRange = new RectF(getPaddingLeft(), getPaddingLeft(), radius * 2
                + getPaddingLeft(), radius * 2 + getPaddingLeft());

        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mCanvas = canvas;
        // draw the wheel
        drawWheel();
    }

    private void drawWheel() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(10);
        paint.setColor(Color.argb(255, 23, 243, 44));

        int i = 0;
        for (Award award : list) {
            int color = Color.parseColor(hexColors[i]);
            paint.setColor(color);
            mCanvas.drawArc(rect, startAngle, sweepAngle, true, paint);
            drawText(startAngle, sweepAngle, "Num " + i);

            // draw text and bitmap that to the center point
            // save
            mCanvas.save();
            mCanvas.rotate(startAngle + sweepAngle + sweepAngle, centerX, centerY);
//            mCanvas.drawText(i + " Good Luck!", centerX, rect.top + 360, mTextPaint);
            drawImage(award.image, 0);
            mCanvas.restore();
            // restore
            i++;
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
        int imgWidth = radius / 3;
        int imgHeight = imgWidth * image.getHeight() / image.getWidth();

        float angle = (float) ((-90 + startAngle) * (Math.PI / 180)); // 弧度，此处是计算在圆盘正上方位置的弧度，即角度是-90度

        int position = radius * 2 / 3;  // 绘制图像的中心点选在半径的2/3处
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
        path.addArc(rect, startAngle, sweepAngle);
        float textWidth = mTextPaint.measureText(string);
        // 利用水平偏移让文字居中
        int size = list.size();
        float hOffset = (float) (radius * Math.PI / size / 2 - textWidth / 2);// 水平偏移
        Log.d("hello", "hOffset: " + hOffset + ", textWidth: " + textWidth);
//        float vOffset = radius / 2 / 6;// 垂直偏移
        float vOffset = rect.top + 1 * radius / 2;
        mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
    }

    /**
     * 开始转动转盘
     * @param position  指定的抽奖结果的位置，按照计算机坐标开始的0度为第一个位置，顺时针递推
     */
    public void startRun(int position) {
        shouldStart = true;
        stop = false;
        speed = SPEED;
        int offset = (int) ((speed + 1) * speed / 2); // 减速开始直到速度为0时停止，因为减速所少转的角度
        offset %= 360;
        // distance, 每个位置到达正上方所需转过的角度
        int distance = 240 - position * 60; // position * 60 + x = 240; x = 240 - position * 60;
        startAngle = 0;  // 重置startAngle，保证每次点击开始转动的时候每个位置的起点是固定的
        startAngle += offset + distance;  // offset + distance 计算的是为保证每次抽中奖品的位置在正上方在开始转动前圆盘所要设定的偏移
        Log.d(TAG, "distance is " + distance + ", offset is " + offset);
    }

    public void stopRun() {
        stop = true;
    }

    @Override
    public void run() {
        curTime = System.currentTimeMillis();
        int i = 0;
        while (true) {
            while(shouldStart && System.currentTimeMillis() - curTime > vInterval) {
                curTime = System.currentTimeMillis();
                if (stop && i % 6 == 0) { // 如果开始停止（即减速）并且转够n个整圈（i%6==0）
                    speed--;
                } else { // 不然继续转够整数圈
                    i++;
                }
                if (speed == 0) {
                    shouldStart = false;
                }
                startAngle += speed; // 60
                postInvalidate();
            }
        }

    }

}
