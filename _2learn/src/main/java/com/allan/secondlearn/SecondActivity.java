package com.allan.secondlearn;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.allan.secondlearn.simpleAudioRecordV2.SimplePCMAudioRecord2_0;
import com.allan.secondlearn.simpleAudioRecord.SimpleWavAudioRecord;

public class SecondActivity extends Activity {
    ISimpleRecord mRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondlearn_main);
    }

    public void onClickedSecond(View view) {
        if (view.getId() == R.id.buttonSimpleStart) {
            mRecord = new SimpleWavAudioRecord();
            mRecord.init();
            mRecord.start();
        } else if (view.getId() == R.id.buttonSimpleStop) {
            mRecord.stop();
        }
    }

    public void onClickedDirectly(View view) {
        if (view.getId() == R.id.buttonDirectStart) {
            mRecord = new SimplePCMAudioRecord2_0();
            mRecord.init();
            mRecord.start();
        } else if (view.getId() == R.id.buttonDirectStop) {
            mRecord.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mRecord != null) {
            mRecord.release();
        }
    }
}
