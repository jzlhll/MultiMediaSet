package com.allan.multimediaset;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.allan.baselib.IModulePermission;

public class ModuleItem {
    public String activityName;
    public String tag;
    public IModulePermission modelPermission;

    public static void startActivityByClass(Context context, String activityName) {
        try {
            Class<?> clazz = Class.forName(activityName);
            Intent intent = new Intent(context, clazz);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.e("ModuleItem", "target activity of name:" + activityName + " not exist");
        }
    }
}
