package com.allan.multimediaset;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.allan.others.LanyaActivity;
import com.allan.sound.ThirdMainActivity;
import com.allan.baselib.IModulePermission;
import com.allan.firstlearn.FirstLearnActivity;
import com.allan.secondlearn.SecondActivity;
import com.allan.test.TestActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final boolean BIG_THAN_6_0 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("allan", "on create...");
        setContentView(R.layout.activity_main);

        new Thread(()->{
            try {
                Bundle bundle = new Bundle();
                Uri uri = Uri.parse("content://com.tld.albumprovider");
                getContentResolver().call(uri, "getUrlListRequest", null, bundle);
                Log.d("allan", "call provider");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private static final int RC_STORAGE_PERMISSION = 101;
    private static final int RC_STORAGE_AUDIO_PERMISSION = 102;
    private static final int RC_STORAGE_PERMISSION_3 = 103;
    private static final int RC_TEST = 104;
    private static final int RC_LANYA = 105;

    private ModuleItem[] moduleItems;

    private void initModuleItems() {
        moduleItems = new ModuleItem[5];
        moduleItems[0] = new ModuleItem();
    }

    public void onClickedFirstBtn(View v) {
        Log.d("allan", "onClickedFirstBtn");
        if (BIG_THAN_6_0) {
            IModulePermission mp = new com.allan.firstlearn.ModelPermissions();
            if (EasyPermissions.hasPermissions(this, mp.getPermissions())) {
                // Already have permission, do the thing
                // ...
                startFirstBtn();
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(this, mp.getShowWords(),
                        RC_STORAGE_PERMISSION, mp.getPermissions());
            }
        } else {
            startFirstBtn();
        }
    }

    @AfterPermissionGranted(RC_STORAGE_PERMISSION)//easypermissions注解要求不能带参数哦。比如上面那个方法就会有问题
    private void startFirstBtn() {
        startActivity(new Intent(this, FirstLearnActivity.class));
    }

    public void onClickedSecondBtn(View v) {
        if (BIG_THAN_6_0) {
            IModulePermission mp = new com.allan.secondlearn.ModelPermissions();
            if (EasyPermissions.hasPermissions(this, mp.getPermissions())) {
                // Already have permission, do the thing
                // ...
                startSecondBtn();
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(this, mp.getShowWords(),
                        RC_STORAGE_AUDIO_PERMISSION, mp.getPermissions());
            }
        } else {
            startSecondBtn();
        }
    }

    @AfterPermissionGranted(RC_STORAGE_AUDIO_PERMISSION) //easypermissions注解要求不能带参数哦。比如上面那个方法就会有问题
    private void startSecondBtn() {
        startActivity(new Intent(this, SecondActivity.class));
    }

    public void onClickedThirdBtn(View v) {
        if (BIG_THAN_6_0) {
            IModulePermission mp = new com.allan.sound.ModelPermissions();
            if (EasyPermissions.hasPermissions(this, mp.getPermissions())) {
                // Already have permission, do the thing
                // ...
                startThirdBtn();
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(this, mp.getShowWords(),
                        RC_STORAGE_PERMISSION_3, mp.getPermissions());
            }
        } else {
            startThirdBtn();
        }
    }

    @AfterPermissionGranted(RC_STORAGE_PERMISSION_3) //easypermissions注解要求不能带参数哦。比如上面那个方法就会有问题
    private void startThirdBtn() {
        startActivity(new Intent(this, ThirdMainActivity.class));
    }

    public void onClickedTest2Btn(View v) {
        startActivity(new Intent("the5learn://com.allan.mediaactor/test"));
    }

    public void onClickedTestBtn(View v) {
        if (BIG_THAN_6_0) {
            IModulePermission mp = new com.allan.test.ModelPermissions();
            if (EasyPermissions.hasPermissions(this, mp.getPermissions())) {
                // Already have permission, do the thing
                // ...
                startTestBtn();
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(this, mp.getShowWords(),
                        RC_TEST, mp.getPermissions());
            }
        } else {
            startTestBtn();
        }
    }

    @AfterPermissionGranted(RC_TEST) //easypermissions注解要求不能带参数哦。比如上面那个方法就会有问题
    public void startTestBtn() {
        startActivity(new Intent(this, TestActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void onClickLanyaBtn(View view) {
        if (BIG_THAN_6_0) {
            IModulePermission mp = new com.allan.others.ModelPermissions();
            if (EasyPermissions.hasPermissions(this, mp.getPermissions())) {
                // Already have permission, do the thing
                // ...
                startLanyaBtn();
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(this, mp.getShowWords(),
                        RC_LANYA, mp.getPermissions());
            }
        } else {
            startLanyaBtn();
        }
    }

    @AfterPermissionGranted(RC_LANYA) //easypermissions注解要求不能带参数哦。比如上面那个方法就会有问题
    public void startLanyaBtn() {
        startActivity(new Intent(this, LanyaActivity.class));
    }
}
