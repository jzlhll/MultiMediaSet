package com.allan.ijkplayer;

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
        return "halo，给下存储权限吧？";
    }
}
