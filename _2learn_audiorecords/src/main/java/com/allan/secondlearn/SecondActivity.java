package com.allan.secondlearn;

import android.app.Activity;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import com.allan.baselib.MainUIManager;
import com.allan.baselib.MyLog;
import com.allan.pcm.PcmInfo;
import com.allan.secondlearn.audioRecordV3.ResumeWavAudioRecord3_1;
import com.allan.secondlearn.mediaRecord.MediaRecordAudio;
import com.allan.secondlearn.simpleAudioRecordV1.SimplePCMAudioRecord;
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
        if (view.getId() == R.id.buttonSimpleStart0) {
            if (hasRecordingAndInterrupt()) {
                return;
            }
            SimplePCMAudioRecord.SAMPLE_RATE = 16000;
            SimplePCMAudioRecord.CANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
            SimplePCMAudioRecord.FORMAT = AudioFormat.ENCODING_PCM_16BIT;
            mRecord = new SimplePCMAudioRecord();
            mRecord.start();
        } else if (view.getId() == R.id.buttonSimpleStop0) {
            stopRecord();
        } else if (view.getId() == R.id.buttonSimpleStart) {
            if (hasRecordingAndInterrupt()) {
                return;
            }
            mRecord = new SimpleWavAudioRecord();
            mRecord.start();
        } else if (view.getId() == R.id.buttonSimpleStop) {
            stopRecord();
        }
    }

    private boolean hasRecordingAndInterrupt() {
        if (mRecord != null && mRecord.isRecording()) {
            MainUIManager.get().toastSnackbar(getWindow().getDecorView(), "录制正在进行，请停止！");
            return true;
        }
        return false;
    }

    private void stopRecord() {
        if (mRecord != null) {
            mRecord.stop();
        }

        MainUIManager.get().toastSnackbar(getWindow().getDecorView(), "录制已经停止");
    }

    private void pauseRecord() {
        if (mRecord != null && mRecord instanceof IRecord) {
            ((IRecord) mRecord).pause();
        }
        MainUIManager.get().toastSnackbar(getWindow().getDecorView(), "录制已经暂停");
    }

    private int mIndexOfDirectStart = -1;
    private Button secondStartButton;

    public void onClickedAudioRecordV2(View view) {

        if (view.getId() == R.id.buttonDirectSelectParam) {
            if (secondStartButton == null) {
                secondStartButton = findViewById(R.id.buttonDirectStart);
            }
            mIndexOfDirectStart++;
            PcmInfo[] infos = SimpleWavAudioRecord2_0.getAllPcmInfos();
            if (mIndexOfDirectStart == infos.length) {
                mIndexOfDirectStart = 0;
            }
            secondStartButton.setText(String.format("%s 开始", infos[mIndexOfDirectStart].getMask()));
            return;
        }

        if (view.getId() == R.id.buttonDirectStart) {
            if (mIndexOfDirectStart == -1) {
                MainUIManager.get().toastSnackbar(view, "参数错误！");
                return;
            }

            if (hasRecordingAndInterrupt()) {
                return;
            }

            PcmInfo pcminfo = SimpleWavAudioRecord2_0.getAllPcmInfos()[mIndexOfDirectStart];
            mRecord = new SimpleWavAudioRecord2_0("v2_" + pcminfo.getMask()+ ".wav", pcminfo);
            mRecord.start();
        } else if (view.getId() == R.id.buttonDirectStop) {
            stopRecord();
        }
    }

    public void onClickedMediaRecord(View view) {
        if (view.getId() == R.id.buttonMediaStart) {
            if (hasRecordingAndInterrupt()) {
                return;
            }
            mRecord = new MediaRecordAudio();
            mRecord.start();
        } else if (view.getId() == R.id.buttonMediaStop) {
            stopRecord();
        } else if (view.getId() == R.id.buttonMediaPause) {
            pauseRecord();
        } else if (view.getId() == R.id.buttonMediaResume) {
            ((IRecord)mRecord).resume(null);
        }
    }

    public void onClickedAudioRecordV3(View view) {
        if (view.getId() == R.id.buttonResumeStart) {
            if (hasRecordingAndInterrupt()) {
                return;
            }

            //TODO mRecord = new ResumeWavAudioRecord3_0(); 简单的wait
            mRecord = new ResumeWavAudioRecord3_1(); //3.1 文件拼接
            mRecord.start();
        } else if (view.getId() == R.id.buttonResumeStop) {
            stopRecord();
        } else if (view.getId() == R.id.buttonResumeResume) {
           if(mRecord != null && mRecord instanceof IRecord) {
               ((IRecord)mRecord).resume(Environment.getExternalStorageDirectory() + File.separator + "testV3Resume.wav");
           }
        } else if (view.getId() == R.id.buttonResumePause) {
            pauseRecord();
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
