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
 * 3.1版本，通过文件拼接真实的实现文件的拼接模式。
 */
public class ResumeWavAudioRecord3_1 implements IRecord {
    private static final String TAG = ResumeWavAudioRecord3_1.class.getSimpleName();
    private AudioRecord mAudioRecord;
    public static final int SAMPLE_RATE = 44100; //采样率
    public static final int CANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//双声道;//AudioFormat.CHANNEL_IN_MONO;//单声道
    public static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;//音频格式
    private int mMinBufferSize;
    private byte[] mData;

    private volatile boolean mIsRecording = false;

    private static final String FILE_NAME = Environment.getExternalStorageDirectory() + File.separator + "testV3Resume.wav";

    public ResumeWavAudioRecord3_1() {
    }

    @Override
    public void start() {
        startOrResume(null);
    }

    @Override
    public void stop() {
        mIsRecording = false;
    }

    @Override
    public boolean isRecording() {
        return mIsRecording;
    }

    @Override
    public void resume(String filePath) {
        if (mAudioRecord != null) {
            throw new RuntimeException("错误111");
        }

        if (mIsRecording) {
            throw new RuntimeException("errr222");
        }

        filePath = FILE_NAME; //TODO 这里估计改成同名的。你应该有配置方案处理。传入进来
        startOrResume(filePath);
    }

    private void startOrResume(final String filePath) { //根据参数为null表示全新
        ThreadPoolUtils.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                if (mAudioRecord != null) {
                    throw new RuntimeException("错误init");
                }
                mMinBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CANNEL_CONFIG, FORMAT);
                MyLog.d(TAG, "min buff size " + mMinBufferSize + " ; " + SAMPLE_RATE + " " + CANNEL_CONFIG + " " + FORMAT);
                if (mMinBufferSize < 0) {
                    MyLog.d(TAG, "min buff size " + mMinBufferSize);
                    return;
                }
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        CANNEL_CONFIG, FORMAT, mMinBufferSize);
                mData = new byte[mMinBufferSize];

                MyLog.d("start Or resume " + filePath);
                boolean isAppend = filePath != null;
                mAudioRecord.startRecording();
                mIsRecording = true;
                File f;
                if (isAppend) {
                   f = new File(filePath);
                } else {
                    f = new File(FILE_NAME);
                    if (f.exists()) {
                        f.delete();
                    }//先删掉，再重建RandomAccessFile文件
                }
                long oldFileSize = f.length();
                RandomAccessFile file = null;
                try {
                    file = new RandomAccessFile(f, "rw");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    MyLog.d(TAG, "long file size " + oldFileSize + "; " + file.length());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PCMAndWavUtil pcm = new PCMAndWavUtil(SAMPLE_RATE, CANNEL_CONFIG, FORMAT);
                long dataLength = 0;
                if (null != file) {
                    if (!isAppend) { //如果是全新才进去添加header
                        try {
                            pcm.addPcmHeader(file);
                            MyLog.d("成功添加fake head！");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else { //如果是老文件，直接跳到后面去。
                        try {
                            file.seek(file.length());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
                    MyLog.d(TAG, "run: close file output stream !" + dataLength);
                    if(file != null) {
                        if (!isAppend) {
                            pcm.endPcmHeader(file, dataLength);
                        } else {
                            pcm.appendOldPcmHeader(file, oldFileSize, dataLength);
                        }
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
    public void pause() { //其实跟停止一样。我们也正常让他结束header
        mIsRecording = false;
    }
}
