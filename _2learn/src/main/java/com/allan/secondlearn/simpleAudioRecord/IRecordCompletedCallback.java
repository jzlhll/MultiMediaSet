package com.allan.secondlearn.simpleAudioRecord;

import java.io.File;

public interface IRecordCompletedCallback {
    void onComplete(File file);
}
