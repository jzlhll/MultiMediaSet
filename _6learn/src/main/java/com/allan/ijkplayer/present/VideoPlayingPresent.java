package com.allan.ijkplayer.present;

import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;

import com.allan.baselib.WeakHandler;
import com.allan.ijkplayer.base.IButtonClick;
import com.allan.ijkplayer.base.IFullClick;
import com.allan.ijkplayer.base.IPlayCallback;
import com.allan.ijkplayer.viewmodel.PlayingSaveData;
import com.allan.ijkplayer.views.NormalMediaPlayerView;

import java.lang.ref.WeakReference;

public class VideoPlayingPresent implements IPlayCallback, IButtonClick, IFullClick {
    private WeakReference<NormalMediaPlayerView> mView;
    private WeakReference<RelativeLayout> mControlsLayout;

    private WeakHandler mHandler = new WeakHandler(new WeakHandler.Callback() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT_DATA: {

                }
                case MSG_HIDE_ALL_BTNS: {
                    if (mControlsLayout.get() != null) {
                        mControlsLayout.get().setVisibility(View.GONE);
                    }
                }
                break;
            }
        }
    });
    private PlayingSaveData mData;

    private boolean mIsPaused = false;

    private static final int MSG_INIT_DATA = 0;
    private static final int MSG_HIDE_ALL_BTNS = 1;

    public VideoPlayingPresent(NormalMediaPlayerView normalMediaPlayerView, RelativeLayout controlsLayout) {
        mView = new WeakReference<>(normalMediaPlayerView);
        mControlsLayout = new WeakReference<>(controlsLayout);
        normalMediaPlayerView.playCallback = this;
        mData = new PlayingSaveData();
    }

    public void setVideoFileUri(Uri uri) {
        if (mView.get() != null) {
            mView.get().setUri(uri);
        }
    }

    public void release() {
        if (mView.get() != null) {
            mView.get().playCallback = null;
        }

        mData.save();
    }

    @Override
    public void onVideoStopped(Uri uri, int currentPosition) {
        mData.update(uri, currentPosition);
    }

    @Override
    public int onVideoStarted(Uri uri) {
        return mData.read(uri);
    }

    @Override
    public void onPauseBtnClick() {
        mIsPaused = !mIsPaused;
        if (mView.get() != null) {
            if (mIsPaused) {
                mView.get().getMediaPlayer().pause();
            } else {
                mView.get().getMediaPlayer().start();
            }
        }

        mHandler.removeMessages(MSG_HIDE_ALL_BTNS);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_ALL_BTNS, 4000);
    }

    @Override
    public void onFullClick() {
        assert mControlsLayout.get() != null;
        mControlsLayout.get().setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_ALL_BTNS, 4000);
    }
}
