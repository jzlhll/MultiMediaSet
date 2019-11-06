package com.allan.firstlearn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.allan.baselib.MyLog;

import java.io.File;

public class MyCustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private boolean mIsCreated = false;

    public MyCustomSurfaceView(Context context) {
        super(context);
        init();
    }

    public MyCustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyCustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mIsCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mIsCreated = false;
    }

    public void drawByMine() {
        if (!mIsCreated) {
            MyLog.w("draw not init!");
            return;
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + File.separator + "test.png");  // 获取bitmap
        Canvas canvas = getHolder().lockCanvas();  // 先锁定当前surfaceView的画布
        canvas.drawBitmap(bitmap, 0, 0, paint); //执行绘制操作
        getHolder().unlockCanvasAndPost(canvas); // 解除锁定并显示在界面上
    }
}
