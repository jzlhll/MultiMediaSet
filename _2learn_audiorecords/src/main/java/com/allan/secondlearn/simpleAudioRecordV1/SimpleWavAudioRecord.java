package com.allan.secondlearn.simpleAudioRecordV1;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.allan.baselib.MyLog;
import com.allan.baselib.ThreadPoolUtils;
import com.allan.secondlearn.ISimpleRecord;
import com.allan.secondlearn.PCMAndWavUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.allan.secondlearn.simpleAudioRecordV1.SimplePCMAudioRecord.FILE_NAME_WAV;

/**
 * 这是写完了以后再进行的2次处理。那他么的岂不是要将整个文件拷贝一次？！
 */
public class SimpleWavAudioRecord implements ISimpleRecord, IRecordCompletedCallback {
    private SimplePCMAudioRecord simplePCMAudioRecord;

    public SimpleWavAudioRecord() {
        simplePCMAudioRecord = new SimplePCMAudioRecord();
        simplePCMAudioRecord.setCompletedCallback(this);
    }

    @Override
    public boolean isRecording() {
        return simplePCMAudioRecord.isRecording();
    }

    @Override
    public void start() {
        simplePCMAudioRecord.start();
    }

    @Override
    public void stop() {
        simplePCMAudioRecord.stop();
    }

    @Override
    public void onComplete(File file) {
        if (file.exists()) {
            PCMAndWavUtil pcmToWavUtil = new PCMAndWavUtil(SimplePCMAudioRecord.SAMPLE_RATE,
                    SimplePCMAudioRecord.CANNEL_CONFIG, SimplePCMAudioRecord.FORMAT);
            File wavFile = new File(FILE_NAME_WAV);
            if (wavFile.exists()) {
                wavFile.delete();
            }
            //这是写完了以后再进行的2次处理。那他么的岂不是要将整个文件拷贝一次？！
            pcmToWavUtil.pcmToWav(file.getAbsolutePath(), wavFile.getAbsolutePath());
        }
    }
}

