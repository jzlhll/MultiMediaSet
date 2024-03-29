package com.allan.secondlearn.audioRecordV3;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.allan.baselib.MyLog;
import com.allan.baselib.ThreadPoolUtils;
import com.allan.secondlearn.IRecord;
import com.allan.secondlearn.PCMAndWavUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * SimplePCMAudioRecord是一个实现了普通录制声音的代码。只能录制PCM。不支持停止的模式。而且PCM不能播放。
 * 2.0版本则将使用randomAccessFile直接先写入头；再录制；再最后stop的时候跳回去写入length即可。
 * 3.0版本，支持简单的通过锁的等待和resume，通过暂停线程。
 */
public class ResumeWavAudioRecord3_0 implements IRecord {
    private static final String TAG = ResumeWavAudioRecord3_0.class.getSimpleName();
    private volatile AudioRecord mAudioRecord;
    public static final int SAMPLE_RATE = 44100; //采样率
    public static final int CANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//双声道;//AudioFormat.CHANNEL_IN_MONO;//单声道
    public static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;//音频格式
    private int mMinBufferSize;
    private byte[] mData;
    private volatile boolean mIsRecording = false; //TODO 这里搞了三个变量，改成enum St好一点
    private boolean mIsPause = false;

    private final Object PAUSELOCK = new Object();

    public ResumeWavAudioRecord3_0() {
    }

    @Override
    public void start() {
        ThreadPoolUtils.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                if (mAudioRecord != null) {
                    throw new RuntimeException("错误init");
                }
                mMinBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CANNEL_CONFIG, FORMAT);
                MyLog.d(TAG, "min buff size " + mMinBufferSize + " ; " + SAMPLE_RATE + " " + CANNEL_CONFIG + " " + FORMAT);
                if (mMinBufferSize < 0) {
                    MyLog.e(TAG, "Error ! min buff size " + mMinBufferSize);
                    return;
                }
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        CANNEL_CONFIG, FORMAT, mMinBufferSize);
                mData = new byte[mMinBufferSize];

                mAudioRecord.startRecording();
                mIsRecording = true;
                synchronized (PAUSELOCK) {
                    mIsPause = false;
                }
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "test3_0.wav");
                if (f.exists()) {
                    f.delete();
                }//先删掉，再重建RandomAccessFile文件

                RandomAccessFile file = null;
                try {
                    file = new RandomAccessFile(f, "rw");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                PCMAndWavUtil pcm = new PCMAndWavUtil(SAMPLE_RATE, CANNEL_CONFIG, FORMAT);
                long dataLength = 0;
                if (null != file) {
                    try {
                        pcm.addPcmHeader(file);
                        MyLog.d("成功添加fake head！");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while (mIsRecording) {
                        int read = mAudioRecord.read(mData, 0, mMinBufferSize);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                file.write(mData);
                                dataLength += mData.length;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        synchronized (PAUSELOCK) {
                            while(mIsPause) {
                                try {
                                    PAUSELOCK.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
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

                    if(file != null) {
                        pcm.endPcmHeader(file, dataLength);
                        file.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                synchronized (PAUSELOCK) {
                    mIsPause = false;
                }
                MyLog.d(TAG, "release in run()");
                if (mAudioRecord != null) {
                    mAudioRecord.release();
                }
                mData = null;
                mAudioRecord = null;
                MyLog.d(TAG,"release!!!");
            }
        });
    }

    @Override
    public boolean isRecording() {
        return mAudioRecord != null;
    }

    @Override
    public void stop() {
        mIsRecording = false;
    }

    @Override
    public void resume(String file) {
        synchronized (PAUSELOCK) {
            mIsPause = false;
            PAUSELOCK.notify();
        }
    }

    @Override
    public void pause() {
        synchronized (PAUSELOCK) {
            mIsPause = true;
        }
    }
}
