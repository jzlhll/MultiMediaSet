package com.allan.audiotrack;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.allan.audiotrack.mediaplayer.MyMediaPlayer;
import com.allan.audiotrack.mediaplayer.MyMediaPlayerController;
import com.allan.audiotrack.soundpool.MySoundPool;

import java.io.File;

public class ThirdMainActivity extends Activity {
    private MyMediaPlayerController myMediaPlayerController;
    MySoundPool mySoundPool;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third_main);
        myMediaPlayerController = new MyMediaPlayerController(
                (SeekBar) findViewById(R.id.mediaPlayerSeekBar),
                (TextView) findViewById(R.id.mediaPlayerCurrentText));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mySoundPool != null) {
            mySoundPool.release();
        }

        if (myMediaPlayerController != null) {
            myMediaPlayerController.close();
        }
    }

    public void onClickedMediaPlayer(View v) {
        if (v.getId() == R.id.mediaPlayerStartBtn) {
            if (myMediaPlayerController.getMediaPlayer() == null) {
                myMediaPlayerController.setMediaPlayer(new MyMediaPlayer());
            }
            myMediaPlayerController.getMediaPlayer().start(getApplicationContext(),
                    Environment.getExternalStorageDirectory() + File.separator + "yanhua.mp3");
        } else if (v.getId() == R.id.mediaPlayerPauseBtn) {
            if (myMediaPlayerController.getMediaPlayer() != null) {
                myMediaPlayerController.getMediaPlayer().pause();
            }
        } else if (v.getId() == R.id.mediaPlayerStopBtn) {
            if (myMediaPlayerController.getMediaPlayer() != null) {
                myMediaPlayerController.getMediaPlayer().stop();
            }
        } else if (v.getId() == R.id.mediaPlayerResumeBtn) {
            if (myMediaPlayerController.getMediaPlayer() != null) {
                myMediaPlayerController.getMediaPlayer().resume();
            }
        }
    }

    public void onClickedSoundPool(View v) {
         if (v.getId() == R.id.soundPoolBtn) {
            int randomId = (int) (Math.random() * 3);
            if (mySoundPool == null) {
                mySoundPool = new MySoundPool(getApplicationContext());
            }
            switch (randomId) {
                case 0:
                    mySoundPool.play(mySoundPool.mSoundEffectPaopaoId);
                    break;
                case 1:
                    mySoundPool.play(mySoundPool.mSoundEffectQiuId);
                    break;
                case 2:
                    MySoundPool.play(getApplicationContext());
                    break;
            }

        }
    }
}
