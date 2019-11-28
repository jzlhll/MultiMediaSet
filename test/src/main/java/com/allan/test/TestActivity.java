package com.allan.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.File;

public class TestActivity extends Activity implements View.OnClickListener{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_layout);
        findViewById(R.id.button).setOnClickListener(this);
    }

    private void resetNames(String path) {
        File[] files = new File(Environment.getExternalStorageDirectory() + File.separator +
                path).listFiles();
        for (File f:files) {
            File newfile = new File(f.getParentFile() + File.separator + (f.getName().replace("百家讲坛_易中天", "") ));
            f.renameTo(newfile);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button) {
            resetNames("易中天MP3");
            resetNames("易中天");
        }
    }
}
