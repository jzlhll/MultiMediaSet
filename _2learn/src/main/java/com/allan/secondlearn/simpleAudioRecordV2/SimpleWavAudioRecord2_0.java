package com.allan.secondlearn.simpleAudioRecordV2;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.allan.baselib.MyLog;
import com.allan.baselib.ThreadPoolUtils;
import com.allan.secondlearn.ISimpleRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * SimplePCMAudioRecord是一个实现了普通录制声音的代码。只能录制PCM。不支持停止的模式。而且PCM不能播放。
 * 2.0版本则将使用randomAccessFile直接先写入头；再录制；再最后stop的时候跳回去写入length即可。
 */
public class SimpleWavAudioRecord2_0 implements ISimpleRecord {
    private static final String TAG = SimpleWavAudioRecord2_0.class.getSimpleName();
    private AudioRecord mAudioRecord;
    public static final int SAMPLE_RATE = 44100; //采样率
    public static final int CANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//双声道;//AudioFormat.CHANNEL_IN_MONO;//单声道
    public static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;//音频格式
    private int mMinBufferSize;
    private byte[] mData;
    private boolean mIsRecording = false;

    private static final String FILE_NAME = Environment.getExternalStorageDirectory() + File.separator + "test2_0.wav";

    public SimpleWavAudioRecord2_0() {
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
                MyLog.d(TAG, "min buff size " + mMinBufferSize);
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        CANNEL_CONFIG, FORMAT, mMinBufferSize);
                mData = new byte[mMinBufferSize];

                mAudioRecord.startRecording();
                mIsRecording = true;
                File f = new File(FILE_NAME);
                if (f.exists()) {
                    f.delete();
                }//先删掉，再重建RandomAccessFile文件

                RandomAccessFile file = null;
                try {
                    file = new RandomAccessFile(f, "rw");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                PCM2WavUtil2_0 pcm = new PCM2WavUtil2_0(SAMPLE_RATE, CANNEL_CONFIG, FORMAT);
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
    public void stop() {
        mIsRecording = false;
    }
}
