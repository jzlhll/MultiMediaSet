package com.allan.sound.mediaplayer;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.allan.baselib.MyLog;

/**
 * 此类中实现了UI的更新和操控mediaPlayer
 */
public class MyMediaPlayerController {
    private SeekBar mSeekBar;
    private MyMediaPlayer myMediaPlayer;
    private TextView mCurrentText;
    public MyMediaPlayerController(SeekBar s, TextView curText) {
        mSeekBar = s;
        //添加拖动监听并变更mediaPlayer的位置
        SeekBar.OnSeekBarChangeListener mSeekbarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MyLog.d("seekbar changed " + i + " " + b);
                if (b) myMediaPlayer.seekTo(i * 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        mSeekBar.setOnSeekBarChangeListener(mSeekbarChangeListener);
        mCurrentText = curText;
    }

    public MyMediaPlayer getMediaPlayer() {
        return myMediaPlayer;
    }

    public void setMediaPlayer(MyMediaPlayer mediaPlayer) {
        myMediaPlayer = mediaPlayer;
        myMediaPlayer.setPositionCallback(mPositionCallback);
    }


    public void close() {
        if(myMediaPlayer != null) myMediaPlayer.close();
    }

    private MyMediaPlayer.PositionCallback mPositionCallback = new MyMediaPlayer.PositionCallback() {
        private int mTotal = 0;

        @Override
        public void onInit(int totalMsec) {
            mTotal = totalMsec / 1000;
            mSeekBar.setMax(mTotal);
            mCurrentText.setText(String.format("%d(%d)", 0, mTotal));
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onUpdate(int curMsec) {
            int p = curMsec / 1000;
            mCurrentText.setText(String.format("%d(%d)", p, mTotal));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mSeekBar.setProgress(p, true);
            } else {
                mSeekBar.setProgress(p);
            }
        }

        @Override
        public void onProgressVisible(boolean visible) {
            MyLog.d(MyMediaPlayerController.class.getSimpleName(), "onprogrss visble " + visible);
            mSeekBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            if (!visible) {
                mSeekBar.setProgress(0);
                mCurrentText.setText("");
            }
        }
    };
}
