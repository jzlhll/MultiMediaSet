package com.allan.mediaactor;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.allan.baselib.MyLog;

public class MediaExtractorUtil {
    private static final String TAG = "MediaExtractor";
    public MediaExtractorUtil() {
        mMediaExtractor = new MediaExtractor();
    }

    private static final String FILE_PATH = "/sdcard/org_video";
    private final MediaExtractor mMediaExtractor;

    public void extractorVideo() throws Exception{
        //1.设置数据源
        int videoIndex = -1;
        mMediaExtractor.setDataSource(FILE_PATH);
        int trackCount = mMediaExtractor.getTrackCount();
        MyLog.d(TAG, "trackCount " + trackCount);
        //2. 获取视频轨道index
        for(int i = 0; i < trackCount;i++) {
            MediaFormat trackFmt = mMediaExtractor.getTrackFormat(i);
            String mime = trackFmt.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("video/")) {
                videoIndex = i;
                break;
            }
        }
        if (videoIndex == -1) {
            return;
        }
        mMediaExtractor.selectTrack(videoIndex);
    }
}
