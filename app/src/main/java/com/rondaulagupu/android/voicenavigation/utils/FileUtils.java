package com.rondaulagupu.android.voicenavigation.utils;

import android.os.Environment;

import java.io.File;

public class FileUtils {

    //Private so that objects can't be created
    private FileUtils() {}

    private static final String AUDIO_RECORDER_FOLDER = "WavAudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";

    private static String audioSavePathInDevice = null;
    private static String userNameInFile = "recording";
    private static String timeAsName = "";

    public static String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

        if (tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    public static void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    public static String getFilename(){

        audioSavePathInDevice = getFolderPath() + File.separatorChar + userNameInFile + timeAsName + AUDIO_RECORDER_FILE_EXT_WAV;
        return audioSavePathInDevice;
    }

    private static String getFolderPath() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(file.getAbsoluteFile(), timeAsName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static String getTxtFilename() {
        audioSavePathInDevice = getFolderPath() + File.separatorChar + userNameInFile + timeAsName + ".txt";
        return audioSavePathInDevice;
    }
}
