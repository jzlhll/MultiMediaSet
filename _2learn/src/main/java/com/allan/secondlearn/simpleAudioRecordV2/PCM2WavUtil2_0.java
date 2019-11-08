package com.allan.secondlearn.simpleAudioRecordV2;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.IOException;
import java.io.RandomAccessFile;

import androidx.annotation.NonNull;

import com.allan.baselib.MyLog;

/**
 * 将pcm音频文件转换为wav音频文件
 */
public class PCM2WavUtil2_0 {

    /**
     * 缓存的音频大小
     */
    private int mBufferSize;
    /**
     * 采样率
     */
    private int mSampleRate;
    /**
     * 声道数
     */
    private int mChannel;


    /**
     * @param sampleRate sample rate、采样率
     * @param channel channel、声道
     * @param encoding Audio data format、音频格式
     */
    public PCM2WavUtil2_0(int sampleRate, int channel, int encoding) {
        this.mSampleRate = sampleRate;
        this.mChannel = channel;
        this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, encoding);
    }


    /**
     * 添加wav头；尚缺他的length
     */
    public void addPcmHeader(@NonNull RandomAccessFile file) throws IOException{
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = mSampleRate;
        int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        long byteRate = 16 * mSampleRate * channels / 8;
        byte[] data = new byte[mBufferSize];
        totalAudioLen = 0; //暂时值为空
        totalDataLen = totalAudioLen + 36;

        writeWaveFileHeader(file, totalAudioLen, totalDataLen,
                longSampleRate, channels, byteRate);
    }


    /**
     * 加入wav文件头
     */
    private void writeWaveFileHeader(@NonNull RandomAccessFile file, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt ' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align
        header[32] = (byte) (2 * 16 / 8);
        header[33] = 0;
        // bits per sample
        header[34] = 16;
        header[35] = 0;
        //data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        file.write(header, 0, 44);
    }


    public void endPcmHeader(@NonNull RandomAccessFile file, long totalAudioLen) throws IOException {
        long totalDataLen = totalAudioLen + 36;
        MyLog.d("endPcmHeader totalAudioLen:" + totalAudioLen + " totalData: " + totalDataLen);
        byte[] head4_7 = new byte[] {
            (byte) (totalDataLen & 0xff),
            (byte) ((totalDataLen >> 8) & 0xff),
            (byte) ((totalDataLen >> 16) & 0xff),
            (byte) ((totalDataLen >> 24) & 0xff)
        };
        file.seek(4);
        file.write(head4_7, 0, 4);
        byte[] head40_43 = new byte[] {
            (byte) (totalAudioLen & 0xff),
            (byte) ((totalAudioLen >> 8) & 0xff),
            (byte) ((totalAudioLen >> 16) & 0xff),
            (byte) ((totalAudioLen >> 24) & 0xff)
        };
        file.seek(40);
        file.write(head40_43, 0, 4);
    }
}
