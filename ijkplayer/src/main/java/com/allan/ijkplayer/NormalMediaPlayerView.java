package com.allan.ijkplayer;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.allan.ijkplayer.mediaUtil.FixSize;

import java.io.IOException;

public class NormalMediaPlayerView extends FrameLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    /**
     * 由ijkplayer提供，用于播放视频，需要给他传入一个surfaceView
     */
    private MediaPlayer mMedialPlayer = null;
    /**
     * 视频文件地址
     */
    private String mPath;

    private SurfaceView mSurfaceView;
    private boolean mEnableMediaCodec;

    public NormalMediaPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public NormalMediaPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NormalMediaPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.BLACK);

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mMedialPlayer == null) {
                    createPlayer();
                }
                if (mMedialPlayer != null) {
                    mMedialPlayer.setDisplay(holder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mMedialPlayer != null) {
                    mMedialPlayer.reset();
                    mMedialPlayer.release();
                }
            }
        });
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        addView(mSurfaceView, 0, layoutParams);
    }

    private void createPlayer() {
        mMedialPlayer = new MediaPlayer();
        mMedialPlayer.setOnPreparedListener(this);
        mMedialPlayer.setOnCompletionListener(this);

        try {
            mMedialPlayer.setDataSource(getContext(), InstanceData.fileUri);
            mMedialPlayer.setDisplay(mSurfaceView.getHolder());
            mMedialPlayer.prepareAsync();
        } catch (IOException e) {
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mMedialPlayer != null) {
            FixSize.changeVideoSize(mMedialPlayer, mSurfaceView, getContext());
            mMedialPlayer.start();
        }
    }
}
