package com.allan.secondlearn.simpleAudioRecordV1;

import java.io.File;

public interface IRecordCompletedCallback {
    void onComplete(File file);
}
