package com.allan.firstlearn;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

public final class RoundRayProgressView extends RoundRayView {
    private static final String TAG = RoundRayView.class.getSimpleName();

    abstract static class Mode {
        RoundRayProgressView out;
        boolean mIsAttached = false;

        Mode(RoundRayProgressView out) {
            this.out = out;
        }

        void onSubDraw(Canvas canvas) {
            if (out.onFirstDraw != null) {
                out.onFirstDraw.onFirstDraw();
                out.onFirstDraw = null;
            }
        }

        void onAttached() { mIsAttached = true;}
        void onDetached() { mIsAttached = false;}

        void invalidate() {
            if (mIsAttached) {
                out.invalidate();
            }
        }
    }

    public interface IOnFirstDraw {
        void onFirstDraw();
    }
    private IOnFirstDraw onFirstDraw;

    /**
     * 必须在attach之前设置
     */
    public void setOneshotFirstDraw(IOnFirstDraw onFirstDraw) {
        Log.d(TAG, "set onFirst draw " + m.mIsAttached);
        this.onFirstDraw = onFirstDraw;
    }

    public static class ProgressMode extends Mode{
        private int mProgress, mTargetProgress;

        ProgressMode(RoundRayProgressView out) {
            super(out);
        }

        @Override
        void onSubDraw(Canvas canvas) {
            if (mUpdateMode == UpdateMode.Direct) {
                mProgress = mTargetProgress;
                myDraw(canvas, mProgress);
            } else {
                myDraw(canvas, mProgress);
                updateProgressCycle();
            }

            super.onSubDraw(canvas);
        }

        public enum UpdateMode {
            Direct,
            StepByStep
        }

        public enum UpdateProgress{
            P0,
            P1,
            P2,
            P3,
        }

        private int UpdateProgress2Int(UpdateProgress p) {
            switch (p) {
                case P0:
                    return 0;
                case P1:
                    return RAY_SIZE_2;
                case P2:
                    return RAY_SIZE_3_4;
                case P3:
                default:
                    return RAY_SIZE;
            }
        }

        private UpdateMode mUpdateMode = UpdateMode.Direct;

        private UpdateProgress mCurrentUp = UpdateProgress.P0;

        private static final long DELAY_STEP_TIME = 40L;
        private static final long DELAY_STEP_TIME_2 = DELAY_STEP_TIME >> 1;

        private long delayStepTime = DELAY_STEP_TIME_2;

        private Paint mGrayPaint;

        private static final int GrayColor = Color.parseColor("#C3CFF0");

        private Paint getGrayPaint() {
            if (mGrayPaint == null) {
                mGrayPaint = out.createDefaultPaint();
                mGrayPaint.setColor(GrayColor);
            }

            return mGrayPaint;
        }

        /**
         * @param progress 范围0~80
         */
        public void updateStepByStepProgress(UpdateProgress progress) {
            mCurrentUp = progress;
            mTargetProgress = UpdateProgress2Int(progress);
            mUpdateMode = UpdateMode.StepByStep;
            updateProgressCycle();
        }

        public void updateStepByStepToNextProgress() {
            if (mCurrentUp == UpdateProgress.P0) {
                delayStepTime = DELAY_STEP_TIME_2;
                updateStepByStepProgress(UpdateProgress.P1);
            } else if (mCurrentUp == UpdateProgress.P1) {
                delayStepTime = DELAY_STEP_TIME;
                updateStepByStepProgress(UpdateProgress.P2);
            } else if (mCurrentUp == UpdateProgress.P2) {
                delayStepTime = DELAY_STEP_TIME;
                updateStepByStepProgress(UpdateProgress.P3);
            }
        }

        private void updateProgressCycle() {
            if (mTargetProgress == mProgress) {
                return;
            }
            if (mTargetProgress > mProgress) {
                mProgress++;
            } else {
                mProgress--;
            }

            Handler handler = out.getHandler();
            if (handler != null) {
                handler.postDelayed(this::invalidate, delayStepTime);
            }
        }

        /**
         * 直接刷新到进度。没有过度动画
         * @param progress 范围0~3
         */
        public void directUpdateProgress(UpdateProgress progress) {
            mUpdateMode = UpdateMode.Direct;
            mCurrentUp = progress;
            mTargetProgress = UpdateProgress2Int(progress);
            invalidate();
        }

        private Paint getColorPaint() {
            if (out.mColorPaint == null) {
                out.mColorPaint = out.createDefaultPaint();
                LinearGradient lg = out.linearGradients[4];
                out.mColorPaint.setShader(lg);
            }
            return out.mColorPaint;
        }

        private void myDraw(Canvas canvas, int progress) {
            Log.d(TAG, "Draw22 progress " + mProgress + " " + progress);
            float halfW = out.getWidth() >> 1;
            float hh = out.getHeight() >> 1;

            Paint gp = getGrayPaint();
            Paint p = getColorPaint();

            canvas.save();
            //逆时针计算

            if (progress == 0) { //全部灰色短线
                for (int i = 0; i < RAY_SIZE; i++) {
                    canvas.drawRoundRect(out.rectFs[0], 1.9f, 1.9f, gp);
                    canvas.rotate(-RAY_DEGREES, halfW, hh);
                }
            } else if (progress <= RAY_SIZE_2) {
                canvas.rotate(45f, halfW, hh);
                //0~19  59-40
                for (int i = 0; i < RAY_SIZE; i++) {
                    if ((i <= 20 && i >= (20 - (progress + 1) / 2)) || (i < 60 && i >= (60 - (progress + 1) / 2))) {
                        canvas.drawRoundRect(out.rectFs[4], 1.9f, 1.9f, p);
                    } else {
                        canvas.drawRoundRect(out.rectFs[0], 1.9f, 1.9f, gp);
                    }
                    canvas.rotate(-RAY_DEGREES, halfW, hh);
                }
            } else if (progress <= RAY_SIZE_3_4) {
                canvas.rotate(45f, halfW, hh);

                for (int i = 0; i < RAY_SIZE; i++) {
                    if (i <= RAY_SIZE_4 ||
                            i >= RAY_SIZE_2 && i < RAY_SIZE_3_4 ||
                            i >= RAY_SIZE - progress && i < RAY_SIZE_2) {
                        canvas.drawRoundRect(out.rectFs[4], 1.9f, 1.9f, p);
                    } else {
                        canvas.drawRoundRect(out.rectFs[0], 1.9f, 1.9f, gp);
                    }
                    canvas.rotate(-RAY_DEGREES, halfW, hh);
                }
            } else {
                canvas.rotate(45f, halfW, hh);

                for (int i = 0; i < RAY_SIZE; i++) {
                    if (i < RAY_SIZE_3_4 ||
                            i >= RAY_SIZE - (progress - RAY_SIZE_3_4)) {
                        canvas.drawRoundRect(out.rectFs[4], 1.9f, 1.9f, p);
                    } else {
                        canvas.drawRoundRect(out.rectFs[0], 1.9f, 1.9f, gp);
                    }
                    canvas.rotate(-RAY_DEGREES, halfW, hh);
                }
            }

            canvas.drawCircle(halfW, hh, halfW  - out.getRayLongLength() - INNER_CYCLE_DISTANCE, out.getInnerCirclePaint());

            canvas.restore();
        }
    }

    public static class JustMode extends Mode {
        private boolean mStarted = false;
        private int mProgress = 0;

        private final boolean autoAnim;

        private boolean mGoingToAnim; //有的时候外部设置了startAnim进来，但是此时界面并没有出现；则需要延迟加载，使用这个标记一下，直到attach来了以后

        private final long delayTs, eachTs;

        JustMode(RoundRayProgressView out, boolean autoAnim, long delayTs, long eachTs) {
            super(out);
            this.autoAnim = autoAnim;
            this.delayTs = delayTs;
            this.eachTs = eachTs;
        }

        @Override
        void onSubDraw(Canvas canvas) {
            myDrawColorful(canvas, mProgress);
            if (mStarted) {
                Handler handler = out.getHandler();
                if (handler != null && mProgress <= RAY_SIZE) {
                    handler.postDelayed(this::invalidate, (mProgress++ < RAY_SIZE_2 ? eachTs >> 1 : eachTs));
                }
            }
            super.onSubDraw(canvas);
        }

        @Override
        void onAttached() {
            mIsAttached = true;
            Log.d(TAG, "on attched");
            if (autoAnim || mGoingToAnim) {
                Handler handler = out.getHandler();
                if (handler != null) {
                    handler.postDelayed(this::startAnim, delayTs);
                }
            }
            mGoingToAnim = false;
        }

        public void startAnimDelay() {
            if (mIsAttached) {
                Handler handler = out.getHandler();
                if (handler != null) {
                    handler.postDelayed(this::startAnim, delayTs);
                }
            } else {
                mGoingToAnim = true;
            }
        }

        private void startAnim() {
            Log.d(TAG, "start Anim");
            mStarted = true;
            mProgress = 0;
            invalidate();
        }

        private Paint getColorPaint(int i) {
            if (out.mColorPaint == null) {
                out.mColorPaint = out.createDefaultPaint();
            }
            LinearGradient lg = out.linearGradients[(out.linearGradients.length * i / RAY_SIZE)];
            out.mColorPaint.setShader(lg);
            return out.mColorPaint;
        }

        private void myDrawColorful(Canvas canvas, int progress) {
            float halfW = out.getWidth() >> 1;
            float hh = out.getHeight() >> 1;

            canvas.save();
            if (progress == 0) { //全部灰色短线
                for (int i = 0; i < RAY_SIZE; i++) {
                    canvas.drawRoundRect(out.rectFs[0], 1.9f, 1.9f, getColorPaint(i));
                    canvas.rotate(-RAY_DEGREES, halfW, hh);
                }
            } else if (progress <= RAY_SIZE_2) {
                canvas.rotate(45f, halfW, hh);
                //0~19  59-40
                for (int i = 0; i < RAY_SIZE; i++) {
                    if ((i <= RAY_SIZE_4 && i >= (RAY_SIZE_4 - (progress + 1) / 2))
                            || (i < RAY_SIZE_3_4 && i >= (RAY_SIZE_3_4 - (progress + 1) / 2))) {
                        canvas.drawRoundRect(out.rectFs[4], 1.9f, 1.9f, getColorPaint(i));
                    } else {
                        canvas.drawRoundRect(out.rectFs[0], 1.9f, 1.9f, getColorPaint(i));
                    }
                    canvas.rotate(-RAY_DEGREES, halfW, hh);
                }
            } else if (progress < RAY_SIZE_3_4) {
                canvas.rotate(45f, halfW, hh);

                for (int i = 0; i < RAY_SIZE; i++) {
                    if (i <= RAY_SIZE_4 ||
                            i >= RAY_SIZE_2 && i < RAY_SIZE_3_4 ||
                            i >= RAY_SIZE - progress && i < RAY_SIZE_2) {
                        canvas.drawRoundRect(out.rectFs[4], 1.9f, 1.9f, getColorPaint(i));
                    } else {
                        canvas.drawRoundRect(out.rectFs[0], 1.9f, 1.9f, getColorPaint(i));
                    }
                    canvas.rotate(-RAY_DEGREES, halfW, hh);
                }
            } else {
                canvas.rotate(45f, halfW, hh);

                for (int i = 0; i < RAY_SIZE; i++) {
                    if (i <= RAY_SIZE_3_4 ||
                            i > RAY_SIZE - (progress - RAY_SIZE_3_4)) {
                        canvas.drawRoundRect(out.rectFs[4], 1.9f, 1.9f, getColorPaint(i));
                    } else {
                        canvas.drawRoundRect(out.rectFs[0], 1.9f, 1.9f, getColorPaint(i));
                    }
                    canvas.rotate(-RAY_DEGREES, halfW, hh);
                }
            }

            canvas.drawCircle(halfW, hh, halfW  - out.getRayLongLength() - INNER_CYCLE_DISTANCE, out.getInnerCirclePaint());

            canvas.restore();
        }
    }

    private final Mode m;
    public Mode getMode() {
        return m;
    }

    private Paint mInnerCirclePaint, mColorPaint;

    public RoundRayProgressView(Context context) {
        this(context, null);
    }

    public RoundRayProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRayProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.facerecord_roundray_style, defStyleAttr, 0);
        boolean isProgressMode = typedArray.getBoolean(R.styleable.facerecord_roundray_style_progressMode, false);
        if (isProgressMode) {
            ProgressMode pm = new ProgressMode(this);
            m = pm;

            pm.mProgress = typedArray.getInt(R.styleable.facerecord_roundray_style_progress, 0);
            pm.mTargetProgress = pm.mProgress;
        } else {
            boolean autoAnim = typedArray.getBoolean(R.styleable.facerecord_roundray_style_justModeAutoAnim, false);
            long startDelayTs = typedArray.getInt(R.styleable.facerecord_roundray_style_justModeDelayStartTs, 200);
            long eachTs = typedArray.getInt(R.styleable.facerecord_roundray_style_justModeEachTs, 80);
            m = new JustMode(this, autoAnim, startDelayTs, eachTs);
        }

        typedArray.recycle();
    }

    protected float getRayWidth() {
        return 4.2f;
    }

    protected float getRayShortLength() {
        return 14.4f;
    }

    protected float getRayLongLength() {
        return 26.8f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //no super.
        initOnceRectFs();
        m.onSubDraw(canvas);
    }

    private Paint getInnerCirclePaint() {
        if (mInnerCirclePaint == null) {
            mInnerCirclePaint = createDefaultPaint();
            mInnerCirclePaint.setColor(InnerCycleColor);
            mInnerCirclePaint.setStrokeWidth(2);
            mInnerCirclePaint.setStyle(Paint.Style.STROKE);
        }

        return mInnerCirclePaint;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        m.onDetached();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        m.onAttached();
    }
}
