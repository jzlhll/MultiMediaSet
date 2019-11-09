package com.allan.multimediaset;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.allan.audiotrack.ThirdMainActivity;
import com.allan.baselib.IModulePermission;
import com.allan.baselib.MyLog;
import com.allan.firstlearn.FirstLearnActivity;
import com.allan.secondlearn.SecondActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final boolean BIG_THAN_6_0 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private static final int RC_STORAGE_PERMISSION = 101;
    private static final int RC_STORAGE_AUDIO_PERMISSION = 102;
    private static final int RC_STORAGE_PERMISSION_3 = 103;

    public void onClickedFirstBtn(View v) {
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
            IModulePermission mp = new com.allan.audiotrack.ModelPermissions();
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
