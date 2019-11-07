package com.allan.firstlearn;

import android.Manifest;

import com.allan.baselib.IModulePermission;

public class ModelPermissions implements IModulePermission {

    @Override
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };
    }

    @Override
    public String getShowWords() {
        return "小哥哥小姐姐，给下存储权限好伐？";
    }
}
