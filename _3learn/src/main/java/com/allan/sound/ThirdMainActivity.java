package com.allan.sound;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.allan.baselib.MyLog;
import com.allan.pcm.PcmInfo;
import com.allan.secondlearn.PCMAndWavUtil;
import com.allan.sound.audiotracker.MyAudioTrackerStatic;
import com.allan.sound.mediaplayer.MyMediaPlayer;
import com.allan.sound.mediaplayer.MyMediaPlayerController;
import com.allan.sound.soundpool.MySoundPool;

import java.io.File;

public class ThirdMainActivity extends Activity {
    private MyMediaPlayerController myMediaPlayerController;
    MySoundPool mySoundPool;
    private MyAudioTrackerStatic mStaticAudioTrack;

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

        if (mStaticAudioTrack != null) {
            mStaticAudioTrack.release();
        }
    }

    public void onClickedAudioTrackStatic(View view) {
        if (view.getId() == R.id.audioTrackStaticBtn) {
            if (mStaticAudioTrack == null) {
                mStaticAudioTrack = new MyAudioTrackerStatic();
            }
            mStaticAudioTrack.play(getApplicationContext());
        }
    }

    public void onClickedAudioTrackStream(View view) {
        if (view.getId() == R.id.audioTrackTestGetPcmInfoBtn) {
            File[] files = new File(String.valueOf(Environment.getExternalStorageDirectory())).listFiles();
            for(File f : files) {
                if (f.getName().startsWith("v2_") && f.getName().endsWith(".wav")) {
                   PcmInfo pcmInfo = PCMAndWavUtil.getInfo(f);
                   if(pcmInfo != null) MyLog.d(f + ": " + pcmInfo.toString());
                }
            }
        } else if (view.getId() == R.id.audioTrackStreamBtn) {
            if (mStaticAudioTrack == null) {
                mStaticAudioTrack = new MyAudioTrackerStatic();
            }
            mStaticAudioTrack.play(getApplicationContext());
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
