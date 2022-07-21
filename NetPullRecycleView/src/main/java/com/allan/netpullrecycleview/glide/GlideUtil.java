package com.allan.netpullrecycleview.glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class GlideUtil {
    public static final RequestOptions BlockImageOptions =
            RequestOptions.bitmapTransform(new GlideRoundTransform(12, 0))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .override(284, 152);

    public void func(int drawPlaceId) {
        RequestOptions.bitmapTransform(new GlideRoundTransform(8, 0, GlideRoundTransform.CornerType.TOP))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(drawPlaceId);
    }
}
