package com.allan.ijkplayer.viewmodel;

import android.net.Uri;

import com.tencent.mmkv.MMKV;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PlayingSaveData {
    public PlayingSaveData() {
        init();
    }

    public static class Data {
        public String uri;
        public int currentPosition;
    }

    static final String KEY_URI_PREFIX = "playingUri";
    static final String KEY_POSITION_PREFIX = "playingCurrentPosition";

    public static final int COMPLETE_POSITION = -100;

    private HashMap<String, Integer> mData; //当前播放的位置

    private void init() {
        mData = new HashMap<>(2);
        MMKV kv = MMKV.defaultMMKV();
        //TODO 这里随便采用了一种方式存储的。
        String[] data = kv.allKeys();
        String tempUri;
        String tempSufix;

        String EMP = "";

        if (data != null && data.length > 0) {
            for (String key : data) {
                if (key.contains(KEY_URI_PREFIX)) {
                    tempUri = kv.getString(key, null);
                    if (tempUri != null) {
                        tempSufix = tempUri.replace(KEY_URI_PREFIX, EMP);
                        mData.put(tempUri, kv.getInt(KEY_POSITION_PREFIX + tempSufix, 0));
                    }
                }
            }
        }
    }

    public int read(Uri uri) {
        Iterator<Map.Entry<String, Integer>> entries = mData.entrySet().iterator();
        MMKV kv = MMKV.defaultMMKV();
        String uriStr = uri.toString();
        int index = 0;
        while (entries.hasNext()) {
            Map.Entry entry = entries.next();
            String key = (String) entry.getKey();
            if (key.equals(uriStr)) {
                return (Integer) entry.getValue();
            }
        }
        return 0;
    }

    public void update(Uri uri, int position) {
        if (COMPLETE_POSITION == position) {
            mData.remove(uri.toString());
            return;
        }

        mData.put(uri.toString(), position);
    }

    public void save() {
        Iterator<Map.Entry<String, Integer>> entries = mData.entrySet().iterator();
        MMKV kv = MMKV.defaultMMKV();
        int index = 0;
        while (entries.hasNext()) {
            Map.Entry entry = entries.next();
            String key = (String) entry.getKey();
            Integer value = (Integer) entry.getValue();
            kv.putString(KEY_URI_PREFIX + index, key);
            kv.putInt(KEY_POSITION_PREFIX + index, value);
        }
    }
}
