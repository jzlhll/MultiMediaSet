package com.allan.secondlearn;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.allan.baselib.BuildConfig;
import com.allan.baselib.MyLog;
import com.allan.pcm.PcmInfo;

/**
 * 将pcm音频文件转换为wav音频文件
 */
public class PCMAndWavUtil {
    /**
     * 采样率
     */
    private int mSampleRate;
    /**
     * 声道数
     */
    private int mChannel;

    private byte encodingBit;

    private int encodingFmt;
    /**
     * @param sampleRate sample rate、采样率
     * @param channel channel、声道
     */
    public PCMAndWavUtil(int sampleRate, int channel, int encodingFmt) {
        this.mSampleRate = sampleRate;
        this.mChannel = channel;
        this.encodingFmt = encodingFmt;
        switch (encodingFmt) {
            case AudioFormat.ENCODING_PCM_8BIT:
                encodingBit = 8;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                encodingBit = 16;
                break;
        }
    }


    /**
     * 添加wav头；尚缺他的length
     * 这是首先预制一个头，等录制完成后，endPcmHeader
     */
    public void addPcmHeader(@NonNull RandomAccessFile file) throws IOException{
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = mSampleRate;
        int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        long byteRate = encodingBit * mSampleRate * channels / 8;
        totalAudioLen = 0; //暂时值为空
        totalDataLen = totalAudioLen + 36;

        writeWaveFileHeader(file, totalAudioLen, totalDataLen,
                longSampleRate, channels, byteRate, encodingBit);
    }
    /**
     * 结束一个文件PCm的Wav文件
     * @param file
     * @param totalAudioLen
     * @throws IOException
     */
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
    /**
     * resume的时候，追加header。TODO 目前没有检测是否文件参数与之前相同。比如buffsize等。
     *
     * fileSize = totalDataLen + 8
     * fileSize = (totalAudioLen + 36) + 8
     */
    public void appendOldPcmHeader(@NonNull RandomAccessFile file, long oldFileSize, long newTotalAudioDataLen) throws IOException {
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

    /**
     * 加入wav文件头
     */
    private static void writeWaveFileHeader(@NonNull Object file, long totalAudioLen, long totalDataLen,
                                            long longSampleRate, int channels, long byteRate, byte encodingBit)
            throws IOException {
        byte[] header = new byte[44];
        // https://www.cnblogs.com/ranson7zop/p/7657874.html 此篇文章界面了多种wav的格式；我们只研究PCM的格式的wav重建Header
        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff); //fileLength = totalDataLen + 8
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
        header[16] = 16; //可以是 16、 18 、20、40 等; 1(0x0001)	PCM/非压缩格式 16; 2(0x0002	Microsoft ADPCM	18; 49(0x0031)	GSM 6.10	20
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1; //1表示pcm
        header[21] = 0;
        // channel number
        header[22] = (byte) channels;
        header[23] = 0;
        //采样频率
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //数据传输速率 声道数×采样频率×每样本的数据位数/8 : channels * sampleRate * encodingFmt / 8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align 数据块对齐单位 声道数×位数 / 8
        header[32] = (byte) (2 * encodingBit / 8);
        header[33] = 0;
        // bits per sample
        header[34] = encodingBit;
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
        if (file instanceof FileOutputStream) {
            ((FileOutputStream)file).write(header, 0, 44);
        } else if (file instanceof RandomAccessFile) {
            ((RandomAccessFile)file).write(header, 0, 44);
        }
    }

    /**
     * pcm文件转wav文件
     *
     * @param inFilename 源文件路径
     * @param outFilename 目标文件路径
     */
    public void pcmToWav(String inFilename, String outFilename) {
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = mSampleRate;
        int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        long byteRate = encodingBit * mSampleRate * channels / 8;
        int mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, encodingFmt);
        byte[] data = new byte[mBufferSize];
        try (FileInputStream in = new FileInputStream(inFilename);
             FileOutputStream out = new FileOutputStream(outFilename)) {
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate, encodingBit);
            while (in.read(data) != -1) {
                out.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PcmInfo getInfo(@Nullable String filePath) {
        assert filePath != null;
        return getInfo(new File(filePath));
    }

    public static PcmInfo getInfo(@Nullable File file) {
        assert file != null;
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[44];
            if (fileInputStream.read(bytes, 0, 44) == -1) {
                return null;
            }
            int sampleRate = ((bytes[24] &0xff)  + (bytes[25] &0xff) * 16*16 +
                    (bytes[26] &0xff)* 16*16* 16*16 + (bytes[27] &0xff)* 16*16* 16*16* 16*16);

            int channelNum = bytes[22];

            int encodingFmt = 0;
            switch (bytes[34]) {
                case 8:
                    encodingFmt = AudioFormat.ENCODING_PCM_8BIT;
                    break;
                case 16:
                    encodingFmt = AudioFormat.ENCODING_PCM_16BIT;
                    break;
            }

            return new PcmInfo(null, sampleRate, channelNum, encodingFmt);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
