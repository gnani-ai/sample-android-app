package com.rondaulagupu.android.voicenavigation;

import android.app.Application;

import com.gnani.speechtotext.Recorder;

public class SpeechToText extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Recorder.init("","");
    }
}
