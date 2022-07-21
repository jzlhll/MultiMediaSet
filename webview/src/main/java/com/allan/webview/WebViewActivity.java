package com.allan.webview;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @Author allan.jiang
 * @Date: 2022/06/02 17:54
 * @Description
 */
public class WebViewActivity extends AppCompatActivity {
    private VoiceWebView mVoice;

    private boolean mIsFirst;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout rl = new FrameLayout(this);
        WebView wb = new WebView(this);
        mVoice = new VoiceWebView(wb);
        rl.addView(wb);
        setContentView(rl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsFirst) {
            mIsFirst = false;
            mVoice.loadUrl();
        }
    }
}
