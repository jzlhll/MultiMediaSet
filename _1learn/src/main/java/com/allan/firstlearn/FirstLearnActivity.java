package com.allan.firstlearn;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;

public class FirstLearnActivity extends Activity {
    private enum MODE {
        IMAGE_VIEW,
        SURFACE_VIEW,
        CUSTOM_VIEW
    }

    private static final MODE DEFAULT_MODE = MODE.CUSTOM_VIEW; //TODO 自行修改三个模式显示咯

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstlearn_main);

    }

    public void onClickedLoad(View view) {
        switch (DEFAULT_MODE) {
            case IMAGE_VIEW:{
                ImageView v = findViewById(R.id.img);
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inSampleSize = 4;
                Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator
                        + "test0.png"));
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri),
                            null , bitmapOptions);
                    v.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            break;
            case SURFACE_VIEW: {
                MyCustomSurfaceView v = findViewById(R.id.sufaceview);
                v.drawByMine();
            }
            break;
            case CUSTOM_VIEW: {
                FirstCustomView v = findViewById(R.id.customview);
                v.drawByMine();
            }
            break;
        }
    }
}
