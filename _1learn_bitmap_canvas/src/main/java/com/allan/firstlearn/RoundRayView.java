package com.allan.firstlearn;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class RoundRayView extends View {
    private static final String TAG = RoundRayView.class.getSimpleName();
    private static final int StartColor1 = Color.parseColor("#87a0fa");
    private static final int EndColor1 = Color.parseColor("#8ca0fa");

    private static final int StartColor2 = Color.parseColor("#7893ff");
    private static final int EndColor2 = Color.parseColor("#7394ff");

    private static final int StartColor3 = Color.parseColor("#489aff");
    private static final int EndColor3 = Color.parseColor("#479dff");

    private static final int StartColor4 = Color.parseColor("#47CAFF");
    private static final int EndColor4 = Color.parseColor("#2BC2FF");

    private static final int StartColor5 = Color.parseColor("#47CAFF");
    private static final int EndColor5 = Color.parseColor("#2BC2FF");

    static final int InnerCycleColor = Color.parseColor("#c5dcff");

    final LinearGradient[] linearGradients = new LinearGradient[5];

    final RectF[] rectFs = new RectF[5];

    boolean mInit = false;

    //射线根数
    static final int RAY_SIZE = 80;

    static final int RAY_SIZE_2 = RAY_SIZE >> 1;
    static final int RAY_SIZE_4 = RAY_SIZE_2 >> 1;
    static final int RAY_SIZE_3_4 = RAY_SIZE_4 * 3;

    //射线单个角度
    static final float RAY_DEGREES = 4.5f;

    static final int INNER_CYCLE_DISTANCE = 13;

    public RoundRayView(Context context) {
        this(context, null);
    }

    public RoundRayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        initOnceRectFs();
        myDraw(canvas);
    }

    void initOnceRectFs() {
        if (mInit) {
            return;
        }

        float halfW = getWidth() >> 1;
        int rectSize = 5;
        float rayShortLen = getRayShortLength();
        float rayLongLen = getRayLongLength();
        float halfRayWidth = getRayWidth() / 2;

        float delta = (rayLongLen - rayShortLen);
        //最终rectFs中，从0~4一次，越来越长。
        //长长度
        rectFs[--rectSize] = new RectF(halfW - halfRayWidth, 0, halfW + halfRayWidth, rayLongLen);
        linearGradients[rectSize] = new LinearGradient(halfW, 0, halfW, rayLongLen,
                StartColor5, EndColor5, Shader.TileMode.CLAMP);
        //中长度
        rectFs[--rectSize] = new RectF(halfW - halfRayWidth, delta / 4, halfW + halfRayWidth, rayLongLen);
        linearGradients[rectSize] = new LinearGradient(halfW, 0, halfW, rayLongLen,
                StartColor4, EndColor4, Shader.TileMode.CLAMP);

        rectFs[--rectSize] = new RectF(halfW - halfRayWidth, delta / 2, halfW + halfRayWidth, rayLongLen);
        linearGradients[rectSize] = new LinearGradient(halfW, 0, halfW, rayLongLen,
                StartColor3, EndColor3, Shader.TileMode.CLAMP);

        rectFs[--rectSize] = new RectF(halfW - halfRayWidth, delta * 3 / 4, halfW + halfRayWidth, rayLongLen);
        linearGradients[rectSize] = new LinearGradient(halfW, 0, halfW, rayLongLen,
                StartColor2, EndColor2, Shader.TileMode.CLAMP);

        //短长度
        rectFs[--rectSize] = new RectF(halfW - halfRayWidth, delta, halfW + halfRayWidth, rayLongLen);
        linearGradients[rectSize] = new LinearGradient(halfW, 0, halfW, rayLongLen,
                StartColor1, EndColor1, Shader.TileMode.CLAMP);
        mInit = true;
    }

    private RectF getDefaultRecordNextRectF(int i) {
        int _3_4size = RAY_SIZE * 3 / 4;
        //前3/4的部分，一长一短交替出现
        if (i <= _3_4size) {
            return rectFs[i % 2];
        }
        //后1/4的部分，逐渐变长，再逐渐变短
        if (i == _3_4size + 1 || i == RAY_SIZE - 1) {
            return rectFs[1];
        }
        if (i == _3_4size + 2 || i == RAY_SIZE - 2) {
            return rectFs[2];
        }
        if (i == _3_4size + 3 || i == RAY_SIZE - 3) {
            return rectFs[3];
        }

        return rectFs[4];
    }

    protected float getRayWidth() {
        return 3.6f;
    }

    protected float getRayShortLength() {
        return 12.8f;
    }

    protected float getRayLongLength() {
        return 22.8f;
    }

    protected boolean hasInnerCycle() {
        return true;
    }

    protected Paint createDefaultPaint() {
        final Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);//充满
        p.setAntiAlias(true);// 设置画笔的锯齿效果
        p.setStrokeWidth(1f);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        return p;
    }

    protected void resetDefaultPaint(Paint p) {
        p.reset();
        p.setStyle(Paint.Style.FILL);//充满
        p.setAntiAlias(true);// 设置画笔的锯齿效果
        p.setStrokeWidth(1f);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
    }

    private void myDraw(Canvas canvas) {
        float halfW = getWidth() >> 1;
        float hh = getHeight() >> 1;
        canvas.save();
        canvas.rotate(45f, halfW, hh);
        Paint p = createDefaultPaint();

        int sz = linearGradients.length;
        for (int i = 0; i < RAY_SIZE; i++) {
            LinearGradient lg = linearGradients[(sz * i / RAY_SIZE)];
            p.setShader(lg);
            canvas.rotate(-RAY_DEGREES, halfW, hh);
            canvas.drawRoundRect(getDefaultRecordNextRectF(i), 1.9f, 1.9f, p);
        }

        resetDefaultPaint(p);
        p.setColor(InnerCycleColor);
        p.setStrokeWidth(2);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(halfW, hh, halfW  - getRayLongLength() - INNER_CYCLE_DISTANCE, p);

        canvas.restore();
    }

}