package com.allan.ijkplayer.views;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.allan.baselib.MyLog;
import com.allan.ijkplayer.base.IPlayCallback;
import com.allan.ijkplayer.utils.FixSize;

import java.io.IOException;

import androidx.annotation.NonNull;

public class NormalMediaPlayerView extends FrameLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    /**
     * 由ijkplayer提供，用于播放视频，需要给他传入一个surfaceView
     */
    private MediaPlayer mMediaPlayer = null;
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    private SurfaceView mSurfaceView;

    private boolean isSurfaceReady = false;
    private Uri mProduceFileUri = null; //传递进来。即将被消耗的参数，用完即焚
    private Uri mLastUri = null;

    public IPlayCallback playCallback = null;
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
                isSurfaceReady = true;
                createPlayer();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                isSurfaceReady = false;
                onCompletion(null);
            }
        });
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        addView(mSurfaceView, 0, layoutParams);
    }

    public void setUri(Uri uri) {
        mProduceFileUri = uri;
        createPlayer();
    }

    private synchronized void createPlayer() {
        if (!isSurfaceReady) {
            return;
        }

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        if (mProduceFileUri != null) {
            try {
                mMediaPlayer.setDataSource(getContext(), mProduceFileUri);
                mLastUri = mProduceFileUri;
                mProduceFileUri = null;
                mMediaPlayer.setDisplay(mSurfaceView.getHolder());
                mMediaPlayer.prepareAsync();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        MyLog.d("allan", "onCompletionnnn");
        if (playCallback != null) {
            playCallback.onVideoStopped(mLastUri, mMediaPlayer.getCurrentPosition());
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mMediaPlayer != null) {
            FixSize.changeVideoSize(mMediaPlayer, mSurfaceView, getContext());
            mMediaPlayer.start();
            if (playCallback != null) {
                int lastPosition = playCallback.onVideoStarted(mLastUri);
                mMediaPlayer.seekTo(lastPosition);
            }
        }
    }
}
