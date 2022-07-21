package com.allan.firstlearn;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * @Author allan.jiang
 * @Date: 2022/05/07 12:21
 * @Description
 */
public class AnimHelper {
    private ObjectAnimator animTranslate;
    private ObjectAnimator animAlpha;
    private ObjectAnimator animScale;

    void init(CircleImageView imageView) {
        animTranslate = ObjectAnimator.ofFloat(imageView, "translationX", 0, -60);
        animTranslate.setInterpolator(new AccelerateInterpolator());
        animAlpha = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f);
        animScale = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.92f);
    }

    void start() {
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(animTranslate).with(animAlpha).with(animScale);
        animSet.setDuration(2000L);
        animSet.start();
    }
}
