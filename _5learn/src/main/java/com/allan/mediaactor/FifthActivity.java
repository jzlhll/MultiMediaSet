package com.allan.mediaactor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class FifthActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fifth_main);
        Intent intent = this.getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            // 获取uri中携带的参数，多个参数都可以这样获取
            String openId = uri.getQueryParameter("openId");
        }
    }
}
