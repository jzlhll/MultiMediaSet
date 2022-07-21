package com.allan.firstlearn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class FirstCustomView extends View {
    public FirstCustomView(Context context) {
        super(context);
    }

    public FirstCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FirstCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
    }

    private boolean mIsAttached = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawByRender(canvas);
    }

    private void drawByRender(Canvas canvas) {
        if (mRender == null) {
            return;
        }
        SoftReference<Bitmap> srBitmap;
        synchronized (mRender.Bitmaps) {
            srBitmap = mRender.Bitmaps.size() > 0 ? mRender.Bitmaps.get(0) : null;
        }

        if (srBitmap != null) {
            Bitmap bitmap = srBitmap.get();
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0, 0, mRender.paint);
                if(!bitmap.isRecycled()) bitmap.recycle();
            }

            synchronized (mRender.Bitmaps) {
                mRender.Bitmaps.remove(0);
            }
        }
    }

    private BitmapRender mRender;

    public void drawByMine() {
        if (mRender == null) {
            mRender = new BitmapRender(FirstCustomView.this);
        }
    }

    static class BitmapRender implements Runnable{
        Paint paint = new Paint();
        final List<SoftReference<Bitmap>> Bitmaps = new ArrayList<>();
        FirstCustomView out;

        BitmapRender(FirstCustomView o) {
            out = o;
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            Thread thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {
            while (out.mIsAttached) {
                //预制2张图在sdcard根目录中。test0.png, test1.png
                int id = Math.random() > 0.5 ? 1 : 0;
                Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()
                        + File.separator + "test" + id + ".png");
                synchronized (Bitmaps) {
                    Bitmaps.add(new SoftReference<Bitmap>(bitmap));
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                out.postInvalidate();
            }
        }
    }
}
