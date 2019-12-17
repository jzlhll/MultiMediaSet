package com.allan.ijkplayer.base;

import android.net.Uri;

public interface IPlayCallback {

    /**
     * 其实不论是否是播放完成、暂停、错误等，都会导致停止播放。就需要回调这个
     *
     * @param currentPosition 当前播放的秒数
     */
    void onVideoStopped(Uri uri, int currentPosition);

    /**
     * 当开始播放，我们就记录当前的名字
     * @param uri 播放的uri
     * @return 返回应该seek的position
     */
    int onVideoStarted(Uri uri);
}
