package com.allan.secondlearn.simpleAudioRecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.allan.baselib.MyLog;
import com.allan.baselib.ThreadPoolUtils;
import com.allan.secondlearn.ISimpleRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
    public void init() {
        simplePCMAudioRecord.init();
    }

    @Override
    public void release() {
        simplePCMAudioRecord.release();
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
            PCM2WavUtil pcmToWavUtil = new PCM2WavUtil(SimplePCMAudioRecord.SAMPLE_RATE,
                    SimplePCMAudioRecord.CANNEL_CONFIG, SimplePCMAudioRecord.FORMAT);
            File wavFile = new File(Environment.getExternalStorageDirectory(), "test.wav");
            if (wavFile.exists()) {
                wavFile.delete();
            }
            //这是写完了以后再进行的2次处理。那他么的岂不是要将整个文件拷贝一次？！
            pcmToWavUtil.pcmToWav(file.getAbsolutePath(), wavFile.getAbsolutePath());
        }
    }
}

/**
 * 这是一个实现了普通录制声音的代码。只能录制PCM。不支持停止的模式。而且PCM不能播放。
 */
class SimplePCMAudioRecord implements ISimpleRecord {
    private static final String TAG = SimplePCMAudioRecord.class.getSimpleName();
    private AudioRecord mAudioRecord;
    public static final int SAMPLE_RATE = 44100; //采样率
    public static final int CANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//双声道;//AudioFormat.CHANNEL_IN_MONO;//单声道
    public static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;//音频格式
    private int mMinBufferSize;
    private byte mData[];
    private boolean mIsRecording = false;
    private boolean mIsLive = false;

    private IRecordCompletedCallback mCompletedCallback;
    public void setCompletedCallback(IRecordCompletedCallback mCompletedCallback) {
        this.mCompletedCallback = mCompletedCallback;
    }

    public SimplePCMAudioRecord() {
    }

    @Override
    public void init() {
        mIsLive = true;
        if (mAudioRecord != null) {
            throw new RuntimeException("错误init");
        }
        mMinBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CANNEL_CONFIG, FORMAT);
        MyLog.d(TAG, "min buff size " + mMinBufferSize);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                CANNEL_CONFIG, FORMAT, mMinBufferSize);
        mData = new byte[mMinBufferSize];
    }

    @Override
    public void release() {
        mIsLive = false;
        if (mIsRecording) {
            mIsRecording = false;
        } else {
            if (mAudioRecord != null) {
                mAudioRecord.release();
            }
            mData = null;
            mAudioRecord = null;
            MyLog.d(TAG,"release!!!");
        }
    }

    @Override
    public void start() {
        ThreadPoolUtils.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                mIsRecording = true;
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "test.pcm");
                if (file.exists()) {
                    file.delete();
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != fos) {
                    while (mIsRecording) {
                        int read = mAudioRecord.read(mData, 0, mMinBufferSize);
                        MyLog.d(TAG, "read...." + read);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                fos.write(mData);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    MyLog.d("isRecord end ! (mIsRecording=" + mIsRecording + ")");
                    if (!mIsRecording) {
                        mAudioRecord.stop();
                    }
                }

                try {
                    MyLog.d(TAG, "run: close file output stream !");
                    if(fos != null) fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (mCompletedCallback != null) {
                    mCompletedCallback.onComplete(file);
                }

                if (!mIsLive) {
                    MyLog.d(TAG, "release in run()");
                    release();
                }
            }
        });
    }

    @Override
    public void stop() {
        mIsRecording = false;
    }
}
