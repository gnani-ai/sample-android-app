/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rondaulagupu.android.voicenavigation;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;


import com.gnani.speech.ListenerGrpc;
import com.gnani.speech.SpeechChunk;
import com.gnani.speech.TranscriptChunk;

import com.google.protobuf.ByteString;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;


public class SpeechService extends Service {

    public interface Listener {

        /**
         * Called when a new piece of text was recognized by the Speech API.
         *
         * @param transcript  actual text after speech to text conversion
         * @param asr asr
         * @param isFinal {@code true} when the API finished processing audio.
         */
        void onSpeechRecognized(String transcript, String asr, boolean isFinal);

        void onError(Throwable t);

        void onComplete();

    }

    private static final String TAG = "SpeechService";


    private final SpeechBinder mBinder = new SpeechBinder();
    private final ArrayList<Listener> mListeners = new ArrayList<>();

    private ListenerGrpc.ListenerStub mApiG;

    private volatile AccessTokenTask mAccessTokenTask;


    private ManagedChannel channelG;




    private String mFileName = null;
    private String mFilePath = null;

    FileOutputStream os = null;





    private final StreamObserver<TranscriptChunk> mResponseObserverG
            = new StreamObserver<TranscriptChunk>() {
        @Override
        public void onNext(TranscriptChunk response) {

            String transcript = response.getTranscript();
            String asr = response.getAsr();
            boolean isfinal=response.getIsFinal();



            if (transcript != null) {
                for (Listener listener : mListeners) {
                    listener.onSpeechRecognized(transcript, asr, isfinal);

                }
            }

        }

        @Override
        public void onError(Throwable t) {

            for (Listener listener : mListeners) {
                listener.onError(t);

            }
            Log.e(TAG, "Error calling the API.", t);

        }

        @Override
        public void onCompleted() {
            Log.i(TAG, "API completed.");
            for (Listener listener : mListeners) {
                listener.onComplete();

            }
        }

    };


    private StreamObserver<SpeechChunk> mRequestObserverG;

    public static SpeechService from(IBinder binder) {
        return ((SpeechBinder) binder).getService();
    }

    @Override
    public void onCreate() {
        super.onCreate();


        fetchAccessToken();


    }

    private void fetchAccessToken() {
        if (mAccessTokenTask != null) {
            return;
        }
        mAccessTokenTask = new AccessTokenTask();
        mAccessTokenTask.execute();
    }


    /**
     * Async Task to for sending headers to STT API
     */
    private class AccessTokenTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            return null;
        }

        @Override
        protected void onPostExecute(Void accessToken) {
            Metadata header = new Metadata();
            Metadata.Key<String> token = Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER);
            header.put(token, "");
            Metadata.Key<String> lang = Metadata.Key.of("lang", Metadata.ASCII_STRING_MARSHALLER);
            header.put(lang, "hin_IN");
            Metadata.Key<String> akey = Metadata.Key.of("accesskey", Metadata.ASCII_STRING_MARSHALLER);
            header.put(akey, "");
            Metadata.Key<String> audioformat = Metadata.Key.of("audioformat", Metadata.ASCII_STRING_MARSHALLER);
            header.put(audioformat, "wav");
            Metadata.Key<String> encoding = Metadata.Key.of("encoding", Metadata.ASCII_STRING_MARSHALLER);
            header.put(encoding, "pcm16");
            Metadata.Key<String> sad = Metadata.Key.of("silence", Metadata.ASCII_STRING_MARSHALLER);
            header.put(sad, "yes");
            Metadata.Key<String> email = Metadata.Key.of("email", Metadata.ASCII_STRING_MARSHALLER);
            header.put(email, "aakashravi@gnani.ai");
            channelG = ManagedChannelBuilder.forAddress("asr.gnani.ai", 443).build();

            mApiG = ListenerGrpc.newStub(channelG);
            mApiG = MetadataUtils.attachHeaders(mApiG, header);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mApiG != null) {

            if (channelG != null && !channelG.isShutdown()) {
                try {
                    channelG.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error shutting down the gRPC channel.", e);
                }
            }
            mApiG = null;
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void addListener(@NonNull Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        mListeners.remove(listener);
    }

    /**
     * Starts recognizing speech audio.
     * It will call a method of proto file
     */
    public void startRecognizing() {


        mRequestObserverG = mApiG.doSpeechToText(mResponseObserverG);



    }

    /**
     * Recognizes the speech audio. This method shogetString(R.string.email)uld be called every time a chunk of byte buffer
     * is ready.
     *
     * @param data The audio data.
     * @param size The number of elements that are actually relevant in the {@code data}.
     */
    public void recognize(byte[] data, int size) {
        if (mRequestObserverG == null) {
            return;
        }


        try {


            mRequestObserverG.onNext(SpeechChunk.newBuilder()
                    .setToken("aakashravi@gnani.ai")
                    .setContent(ByteString.copyFrom(data, 0, size))
                    .build());

        } catch (Exception e) {
            Log.d("SpeechService", "" + e);
        }
    }



    public void finishRecognizing() {
        if (mRequestObserverG == null) {
            return;
        }

        Log.d("SpeechService", "finished recognizing function");


        mRequestObserverG.onCompleted();
        mRequestObserverG = null;
    }


    private class SpeechBinder extends Binder {

        SpeechService getService() {
            return SpeechService.this;
        }

    }


}


