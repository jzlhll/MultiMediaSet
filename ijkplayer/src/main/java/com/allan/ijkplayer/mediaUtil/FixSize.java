package com.allan.ijkplayer.mediaUtil;

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
        int surfaceWidth = context.getResources().getDisplayMetrics().widthPixels;
        int surfaceHeight = context.getResources().getDisplayMetrics().heightPixels;
        float max;
        if (context.getResources().getConfiguration().orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth,(float) videoHeight / (float) surfaceHeight);
        } else{
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth/(float) surfaceHeight),(float) videoHeight/(float) surfaceWidth);
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
        Log.e("allan", "changeVideoSize: deviceHeight=" + deviceHeight + "deviceWidth=" + deviceWidth);
        Log.e("allan", "changeVideoSize: videoHeight=" + videoHeight + "videoWidth=" + videoWidth);
        float devicePercent = 0;
        //下面进行求屏幕比例,因为横竖屏会改变屏幕宽度值,所以为了保持更小的值除更大的值.
        if (context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) { //竖屏
            devicePercent = (float) deviceWidth / (float) deviceHeight; //竖屏状态下宽度小与高度,求比
        } else { //横屏
            devicePercent = (float) deviceHeight / (float) deviceWidth; //横屏状态下高度小与宽度,求比
        }

        if (videoWidth > videoHeight) { //判断视频的宽大于高,那么我们就优先满足视频的宽度铺满屏幕的宽度,然后在按比例求出合适比例的高度
            videoWidth = deviceWidth;//将视频宽度等于设备宽度,让视频的宽铺满屏幕
            videoHeight = (int) (deviceWidth * devicePercent);//设置了视频宽度后,在按比例算出视频高度
        } else {  //判断视频的高大于宽,那么我们就优先满足视频的高度铺满屏幕的高度,然后在按比例求出合适比例的宽度
            if (context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {//竖屏
                videoHeight = deviceHeight;
                /**
                 * 接受在宽度的轻微拉伸来满足视频铺满屏幕的优化
                 */
                float videoPercent = (float) videoWidth / (float) videoHeight;//求视频比例 注意是宽除高 与 上面的devicePercent 保持一致
                float differenceValue = Math.abs(videoPercent - devicePercent);//相减求绝对值
                if (differenceValue < 0.01) { //如果小于0.3比例,那么就放弃按比例计算宽度直接使用屏幕宽度
                    videoWidth = deviceWidth;
                } else {
                    videoWidth = (int) (videoWidth / devicePercent);//注意这里是用视频宽度来除
                }

            } else { //横屏
                videoHeight = deviceHeight;
                videoWidth = (int) (deviceHeight * devicePercent);
            }
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
        layoutParams.width = videoWidth;
        layoutParams.height = videoHeight;
        Log.e("allan", "after changeVideoSize: videoHeight=" + videoHeight + "videoWidth=" + videoWidth);
        surfaceView.setLayoutParams(layoutParams);
    }
}
