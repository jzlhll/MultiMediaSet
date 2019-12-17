package com.allan.ijkplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class PlayerVideoActivity extends Activity {
    NormalMediaPlayerView mView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_layout);
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            Uri uri = getIntent().getData();
            InstanceData.fileUri = uri;
            if (uri != null) {
                String str = Uri.decode(uri.getEncodedPath());
                Toast.makeText(getApplicationContext(), "url:" + uri, Toast.LENGTH_LONG).show();
            }
        }
    }
}
