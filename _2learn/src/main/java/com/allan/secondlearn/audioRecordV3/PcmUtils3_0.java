package com.allan.secondlearn.audioRecordV3;

import com.allan.baselib.MyLog;
import com.allan.secondlearn.simpleAudioRecordV2.PCM2WavUtil2_0;

import java.io.IOException;
import java.io.RandomAccessFile;

import androidx.annotation.NonNull;

public class PcmUtils3_0 extends PCM2WavUtil2_0 {
    /**
     * @param sampleRate sample rate、采样率
     * @param channel    channel、声道
     * @param encoding   Audio data format、音频格式
     */
    public PcmUtils3_0(int sampleRate, int channel, int encoding) {
        super(sampleRate, channel, encoding);
    }

    /**
     * resume的时候，追加header。TODO 目前没有检测是否文件参数与之前相同。比如buffsize等。
     *
     * fileSize = totalDataLen + 8
     * fileSize = (totalAudioLen + 36) + 8
     */
    public void appendEndPcmHeader(@NonNull RandomAccessFile file, long oldFileSize, long newTotalAudioDataLen) throws IOException {
        long oldAudioDataLen = oldFileSize - 8 - 36;
        MyLog.d("append end pcm oldAudioDataLen " + oldAudioDataLen);
        long totalAudioLen = newTotalAudioDataLen + oldAudioDataLen;
        MyLog.d("append end pcm totalAudioLen " + totalAudioLen);
        long totalDataLen = totalAudioLen + 36;
        MyLog.d("append end pcm totalDataLen " + totalDataLen);

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
