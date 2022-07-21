package com.allan.sound;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.allan.baselib.MainUIManager;
import com.allan.baselib.MyLog;
import com.allan.pcm.PcmInfo;
import com.allan.secondlearn.PCMAndWavUtil;
import com.allan.sound.audiotracker.MyAudioTracker;
import com.allan.sound.mediaplayer.MyMediaPlayer;
import com.allan.sound.mediaplayer.MyMediaPlayerController;
import com.allan.sound.soundpool.MySoundPool;

import java.io.File;

import androidx.annotation.Nullable;

public class ThirdMainActivity extends Activity {
    private MyMediaPlayerController myMediaPlayerController;
    MySoundPool mySoundPool;
    private MyAudioTracker mAudioTracker;

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

        if (mAudioTracker != null) {
            mAudioTracker.release();
        }
    }

    public void onClickedAudioTrack(View view) {
        if (view.getId() == R.id.audioTrackTestGetPcmInfoBtn) {
            File[] files = new File(String.valueOf(Environment.getExternalStorageDirectory())).listFiles();
            for(File f : files) {
                if (f.getName().startsWith("v2_") && f.getName().endsWith(".wav")) {
                   PcmInfo pcmInfo = PCMAndWavUtil.getInfo(f);
                   if(pcmInfo != null) MyLog.d(f + ": " + pcmInfo.toString());
                }
            }
        } else if (view.getId() == R.id.audioTrackPlayBtn) {
            if (mAudioTracker == null) {
                mAudioTracker = new MyAudioTracker();
            }
            int r = mAudioTracker.play(getApplicationContext(),
                    Environment.getExternalStorageDirectory() + File.separator + "test.wav");
            switch (r) { //TODO 自行修改全局变量
                case -1:
                    MainUIManager.get().toastSnackbar(view, "当前状态不应该点击");
                    break;
                case 0:
                    findViewById(R.id.audioTrackPauseBtn).setVisibility(View.VISIBLE);
                    findViewById(R.id.audioTrackResumeBtn).setVisibility(View.VISIBLE);
                    findViewById(R.id.audioTrackStopBtn).setVisibility(View.VISIBLE);
                    break;
                case 1:
                    findViewById(R.id.audioTrackPauseBtn).setVisibility(View.INVISIBLE);
                    findViewById(R.id.audioTrackResumeBtn).setVisibility(View.INVISIBLE);
                    findViewById(R.id.audioTrackStopBtn).setVisibility(View.INVISIBLE);
                    break;
            }
        } else if (view.getId() == R.id.audioTrackStopBtn) {
            if (mAudioTracker != null) {
                mAudioTracker.stop();
            }
        } else if (view.getId() == R.id.audioTrackResumeBtn) {
            if (mAudioTracker != null) {
                mAudioTracker.resume();
            }
        } else if (view.getId() == R.id.audioTrackPauseBtn) {
            if (mAudioTracker != null) {
                mAudioTracker.pause();
            }
        }
    }

    public void onClickedMediaPlayer(View v) {
        if (v.getId() == R.id.mediaPlayerStartBtn) {
            if (myMediaPlayerController.getMediaPlayer() == null) {
                myMediaPlayerController.setMediaPlayer(new MyMediaPlayer());
            }
            try {
                myMediaPlayerController.getMediaPlayer().start(getApplicationContext(),
                        Environment.getExternalStorageDirectory() + File.separator + "test.wav"); //"yanhua.mp3"
            } catch (Exception e) {
                MainUIManager.get().toastSnackbar(v, e.toString());
            }
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
