package com.allan.ijkplayer.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class FixSize {
    public static void changeVideoSizeV2(MediaPlayer mediaPlayer, SurfaceView surfaceView, Context context) {
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        if (context.getResources().getConfiguration().orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceView.getWidth(), (float) videoHeight / (float) surfaceView.getHeight());
        } else{
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth/(float) surfaceView.getHeight()),(float) videoHeight/(float) surfaceView.getWidth());
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight));
    }

    /**
     * 修改预览View的大小,以用来适配屏幕
     */
    public static void changeVideoSize(MediaPlayer mMediaPlayer, SurfaceView surfaceView, Context context) {
        int videoWidth = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();
        int deviceWidth = context.getResources().getDisplayMetrics().widthPixels;
        int deviceHeight = context.getResources().getDisplayMetrics().heightPixels;

        boolean isPortrait = context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        Log.e("allan", "isPortrait=" + isPortrait);
        Log.e("allan", "changeVideoSize: deviceHeight=" + deviceHeight + " deviceWidth=" + deviceWidth);
        Log.e("allan", "changeVideoSize: videoHeight=" + videoHeight + " videoWidth=" + videoWidth);
        //下面进行求屏幕比例,因为横竖屏会改变屏幕宽度值,所以为了保持更小的值除更大的值.
        //isPort = false）横屏；  deviceHeight=1080 deviceWidth=2259     videoHeight=360 videoWidth=480
        //isPort true 竖屏：      deviceHeight=2259 deviceWidth=1080     videoHeight=360 videoWidth=480
        //因此，横竖屏系统的device宽高变化了，而视频宽高不会变

        //计算高：宽    对于手机而言这个值就是肯定小于1.手机一般 都是高比宽小:
        float devicePercent = isPortrait ? ((float) deviceWidth / deviceHeight) : ((float) deviceHeight / deviceWidth);
        float videoPercent = ((float) videoHeight / videoWidth);
        Log.e("allan", "devicePercent: " + devicePercent + " videoPercent " + videoPercent);

        int layoutWidth, layoutHeight;
        //拉伸数值
        float scaleSize = 1.3f; //TODO 建议1.0 ~ 1.3

        //不论是横屏还是竖屏：如果视频固定，手机固定，这个数值devicePercent < videoPercent的关系就是固定的
        //因此，判断这个是为了适配不同的手机；而不是适配横竖屏

        if ((devicePercent < videoPercent && isPortrait) ||
                (devicePercent > videoPercent && !isPortrait)) { //比手机比例大&竖屏 或者 视频比例小于手机 & 横屏；则固定宽，调整高
            layoutWidth = deviceWidth;
            layoutHeight = (int)(((float) deviceWidth) / videoWidth * videoHeight);
            int scaleHeight = (int)(layoutHeight *scaleSize);
            if (deviceHeight > scaleHeight) {
                layoutHeight = scaleHeight;
            } else {
                layoutHeight = scaleHeight;
            }
        } else {
            layoutHeight = deviceHeight;
            layoutWidth = (int)(((float) deviceHeight) / videoHeight * videoWidth);
            int scaleWidth = (int)(layoutWidth * scaleSize);
            if (deviceWidth > scaleWidth) {
                layoutWidth = scaleWidth;
            } else {
                layoutWidth = scaleWidth;
            }
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
        layoutParams.width = layoutWidth;
        layoutParams.height = layoutHeight;
        Log.e("allan", "after changeVideoSize: layoutHeight=" + layoutHeight + "  layoutWidth=" + layoutWidth);
        surfaceView.setLayoutParams(layoutParams);
    }
}
