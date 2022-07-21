package com.opengl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.allan.baselib.MyLog;
import com.example.opengl.R;

public class _8Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        MyLog.d("enterrrrrrr");
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                String host = uri.getHost();
                String dataString = intent.getDataString();
                MyLog.d("host:"+host);
                MyLog.d("dataString:" + dataString);
            }
        }

        finish();

        //setContentView(R.layout.activity__8);
//        /* 以下是重点 */
////        GLSurfaceView demoGlv = (GLSurfaceView) findViewById(R.id.glSurfaceView);
////        // 设置OpenGL版本(一定要设置)
////        demoGlv.setEGLContextClientVersion(2);
////        // 设置渲染器(后面会着重讲这个渲染器的类)
////        demoGlv.setRenderer(new MyRenderer());
////        // 设置渲染模式为连续模式(会以60fps的速度刷新)
////        demoGlv.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); //RENDERMODE_CONTINUOUSLY
////        /* 重点结束 */
    }
}