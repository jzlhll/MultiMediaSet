package com.opengl;

import com.allan.baselib.IModulePermission;

public class ModulePermission implements IModulePermission {
    @Override
    public String[] getPermissions() {
        return new String[0];
    }

    @Override
    public String getShowWords() {
        return null;
    }
}
