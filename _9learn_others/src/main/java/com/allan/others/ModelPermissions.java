package com.allan.others;

import android.Manifest;

import com.allan.annotation.MyPermission;
import com.allan.baselib.IModulePermission;

@MyPermission
public class ModelPermissions implements IModulePermission {

    @Override
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };
    }

    @Override
    public String getShowWords() {
        return "小哥哥小姐姐，给下蓝牙和存储权限好伐？";
    }
}
