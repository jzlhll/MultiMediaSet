package com.allan.secondlearn;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.allan.secondlearn.audioRecordV3.ResumeWavAudioRecord3_1;
import com.allan.secondlearn.mediaRecord.MediaRecordAudio;
import com.allan.secondlearn.simpleAudioRecordV1.SimpleWavAudioRecord;
import com.allan.secondlearn.simpleAudioRecordV2.SimpleWavAudioRecord2_0;

import java.io.File;

import androidx.annotation.Nullable;

public class SecondActivity extends Activity {
    ISimpleRecord mRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondlearn_main);
    }

    public void onClickedAudioRecordV1(View view) {
        if (view.getId() == R.id.buttonSimpleStart) {
            mRecord = new SimpleWavAudioRecord();
            mRecord.start();
        } else if (view.getId() == R.id.buttonSimpleStop) {
            mRecord.stop();
        }
    }

    public void onClickedAudioRecordV2(View view) {
        if (view.getId() == R.id.buttonDirectStart) {
            mRecord = new SimpleWavAudioRecord2_0();
            mRecord.start();
        } else if (view.getId() == R.id.buttonDirectStop) {
            mRecord.stop();
        }
    }

    public void onClickedMediaRecord(View view) {
        if (view.getId() == R.id.buttonMediaStart) {
            mRecord = new MediaRecordAudio();
            mRecord.start();
        } else if (view.getId() == R.id.buttonMediaStop) {
            mRecord.stop();
        } else if (view.getId() == R.id.buttonMediaPause) {
            ((IRecord)mRecord).pause();
        } else if (view.getId() == R.id.buttonMediaResume) {
            ((IRecord)mRecord).resume(null);
        }
    }

    public void onClickedAudioRecordV3(View view) {
        if (view.getId() == R.id.buttonResumeStart) {
            //TODO mRecord = new ResumeWavAudioRecord3_0(); 简单的wait
            mRecord = new ResumeWavAudioRecord3_1(); //3.1 文件拼接
            mRecord.start();
        } else if (view.getId() == R.id.buttonResumeStop) {
            mRecord.stop();
        } else if (view.getId() == R.id.buttonResumeResume) {
            ((IRecord)mRecord).resume(Environment.getExternalStorageDirectory() + File.separator + "testV3Resume.wav");
        } else if (view.getId() == R.id.buttonResumePause) {
            ((IRecord)mRecord).pause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mRecord != null) {
            mRecord.stop();
        }
    }
}
