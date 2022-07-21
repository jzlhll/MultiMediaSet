package com.allan.secondlearn;

import android.Manifest;

import com.allan.baselib.IModulePermission;

public class ModelPermissions implements IModulePermission {

    @Override
    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };
    }

    @Override
    public String getShowWords() {
        return "大佬，给下录音和存储权限咯？";
    }
}
