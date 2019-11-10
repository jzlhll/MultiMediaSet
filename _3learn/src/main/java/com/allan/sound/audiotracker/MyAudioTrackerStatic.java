package com.allan.sound.audiotracker;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;

import com.allan.baselib.MyLog;
import com.allan.baselib.ThreadPoolUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * 这种模式下，在play之前只需要把所有数据通过一次write调用传递到AudioTrack中的内部缓冲区，
 * 后续就不必再传递数据了。这种模式适用于像铃声这种内存占用量较小，延时要求较高的文件。
 * 但它也有一个缺点，就是一次write的数据不能太多，否则系统无法分配足够的内存来存储全部数据。
 */
public class MyAudioTrackerStatic {
    private byte[] audioData;
    private AudioTrack audioTrack;
    private St mCurSt = St.NOT_INIT;

    private static final String WAV_FILE = Environment.getExternalStorageDirectory() + File.separator + "test.wav";

    private enum St {
        NOT_INIT,
        INITING,
        INITED,
    }

    private void init(Context context) {
        mCurSt = St.INITING;
        final SoftReference<Context> sf = new SoftReference<>(context);
        ThreadPoolUtils.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                if (sf.get() == null) {
                    mCurSt = St.NOT_INIT;
                    return;
                }

                try (ByteArrayOutputStream out = new ByteArrayOutputStream(12*1024);
                     InputStream in = new FileInputStream(new File(WAV_FILE))) {
                    for (int b; (b = in.read()) != -1;) {
                        out.write(b);
                    }
                    if (sf.get() == null) {
                        mCurSt = St.NOT_INIT;
                        return;
                    }

                    audioData = out.toByteArray();
                    mCurSt = St.INITED;
                    play(null);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    //
                }
            }
        });
    }

    public void play(Context context) {
        if (mCurSt == St.INITING) {
            MyLog.d("状态不对");
            return;
        }
        if (mCurSt == St.NOT_INIT) {
            init(context);
            return;
        }

        //mCurSt = St.Inited

        release();
        if (audioData != null) {
            MyLog.d("audioData length " + audioData.length);
        } else {
            MyLog.d("audioData length null");
        }
        this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                audioData.length, AudioTrack.MODE_STATIC);
        this.audioTrack.write(audioData, 0, audioData.length);
        audioTrack.play();
    }

    public void release() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioData = null;
        }
    }
}
