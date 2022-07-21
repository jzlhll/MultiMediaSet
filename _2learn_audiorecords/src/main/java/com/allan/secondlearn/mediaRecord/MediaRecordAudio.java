package com.allan.secondlearn.mediaRecord;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import com.allan.baselib.MyLog;
import com.allan.secondlearn.IRecord;

import java.io.File;
import java.io.IOException;

public class MediaRecordAudio implements IRecord {
    private MediaRecorder mMediaRecorder;
    private volatile St mCurrentSt = St.NOT_INIT;
    private enum St {
        NOT_INIT,
        RECORDING,
        PAUSING,
    }

    private final static String FILE_NAME = Environment.getExternalStorageDirectory() + File.separator + "testMediaRecord.amr";

    @Override
    public boolean isRecording() {
        return mCurrentSt == St.RECORDING;
    }

    @Override
    public void start() {
        if (mCurrentSt != St.NOT_INIT) {
            throw new RuntimeException("哈哈乱搞咯");
        }
        mMediaRecorder = new MediaRecorder();//初始实例化。
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//音频输入源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);//设置输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);//设置编码格式
        mMediaRecorder.setAudioEncodingBitRate(16000);
        File file = new File(FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        mMediaRecorder.setOutputFile(file.getAbsolutePath());//设置输出文件的路径
        try {
            mMediaRecorder.prepare();//准备录制
            mMediaRecorder.start();//开始录制
            mCurrentSt = St.RECORDING;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (mCurrentSt == St.RECORDING || mCurrentSt == St.PAUSING) {
            mMediaRecorder.stop();
        }
        if (mMediaRecorder != null) mMediaRecorder.release();
        mMediaRecorder = null;
        mCurrentSt = St.NOT_INIT;
    }

    @Override
    public void resume(String filePath) {
        if (mCurrentSt == St.PAUSING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                MyLog.d("resumeeeee");
                mMediaRecorder.resume();
                mCurrentSt = St.RECORDING;
            }
        }
    }

    @Override
    public void pause() {
        if (mCurrentSt == St.RECORDING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                MyLog.d("pauseeeee");
                mMediaRecorder.pause();
                mCurrentSt = St.PAUSING;
            }
        }
    }
}
