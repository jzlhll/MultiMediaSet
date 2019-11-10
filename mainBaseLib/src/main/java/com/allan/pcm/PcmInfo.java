package com.allan.pcm;

import androidx.annotation.NonNull;

public class PcmInfo {
    public PcmInfo() {
    }

    public PcmInfo(String mask, int sampleRate, int channelConfig, int encodingFmt) {
        this.sampleRate = sampleRate;
        this.channelConfig = channelConfig;
        this.encodingFmt = encodingFmt;
        this.mask = mask;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
    }

    public int getEncodingFmt() {
        return encodingFmt;
    }

    public void setEncodingFmt(int encodingFmt) {
        this.encodingFmt = encodingFmt;
    }

    private int sampleRate;
    private int channelConfig;
    private int encodingFmt;
    private String mask;

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    @NonNull
    @Override
    public String toString() {
        return mask + " :sampleRate " + sampleRate + " channel " + channelConfig + " fmt " + encodingFmt;
    }
}
