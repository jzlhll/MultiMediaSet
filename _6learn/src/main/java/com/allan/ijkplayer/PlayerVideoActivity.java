package com.allan.ijkplayer;

import android.app.ActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.allan.ijkplayer.present.VideoPlayingPresent;
import com.allan.ijkplayer.views.NormalMediaPlayerView;
import com.tencent.mmkv.MMKV;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerVideoActivity extends AppCompatActivity {
    private VideoPlayingPresent mViewPlayingPresent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_layout);

        MMKV.initialize(this);

        NormalMediaPlayerView mVideoView = findViewById(R.id.videoFrameLayout);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((v.getId() == R.id.pauseBtn)) {
                    mViewPlayingPresent.onPauseBtnClick();
                } else if (v.getId() == R.id.videoFrameLayout) {
                    mViewPlayingPresent.onFullClick();
                }
            }
        };

        mVideoView.setOnClickListener(onClickListener);
        findViewById(R.id.pauseBtn).setOnClickListener(onClickListener);

        RelativeLayout controlsLayout = findViewById(R.id.fullCtrlBtnsLayout);

        mViewPlayingPresent = new VideoPlayingPresent(mVideoView, controlsLayout);
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            Uri uri = getIntent().getData();
            if (uri != null) {
                mViewPlayingPresent.setVideoFileUri(uri);
                //String str = Uri.decode(uri.getEncodedPath());
                //Toast.makeText(getApplicationContext(), "url:" + uri, Toast.LENGTH_LONG).show();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewPlayingPresent.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
