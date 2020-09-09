package com.rondaulagupu.android.voicenavigation;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rodolfonavalon.shaperipplelibrary.ShapeRipple;
import com.rodolfonavalon.shaperipplelibrary.model.Circle;
import com.rondaulagupu.android.voicenavigation.utils.RecorderUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivityNew extends AppCompatActivity
         {

    private static final int REQUEST_PERMISSION_CODE = 1;
    public static final String TYPE = "application/pdf";


    @BindView(R.id.fab)
    FloatingActionButton mRecorderButton;

    @BindView(R.id.ripple)
    ShapeRipple mRipple;

    @BindView(R.id.textView4)
    TextView textView;


    private RecorderUtils mRecorderUtils;

    private boolean isRecorderPressed = false;


    private SpeechService mSpeechService;



    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }
    };



    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final String asr, final boolean isFinal) {

                    Log.d("Transcribe Result", "onSpeechRecognized: " + text+"  "+isFinal);

                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {


                          textView.setText(text);
                      }
                  });


                }

                @Override
                public void onError(Throwable t) {



                }

                @Override
                public void onComplete() {

                    try {

                        Log.e("onComplete", "Called_STT");


                    } catch (Exception e) {
                        Log.e("", "exception", e);
                    }

                }

            };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!checkPermission()) {
            requestPermission();
        }

        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        mRipple.setRippleShape(new Circle());



        mRecorderButton.setOnClickListener(v -> {
            isRecorderPressed = !isRecorderPressed;

            if (isRecorderPressed) {
                mRecorderUtils = new RecorderUtils(getApplicationContext(), mSpeechService);
                mRipple.setVisibility(View.VISIBLE);
                mSpeechService.startRecognizing();
                mRecorderUtils.startRecording();
            } else {
                mRipple.setVisibility(View.INVISIBLE);
                mRecorderUtils.stopRecording();
                mSpeechService.finishRecognizing();
            }
        });


    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Permission methods
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivityNew.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean storagePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean recordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (storagePermission && recordPermission) {
                    Toast toast = Toast.makeText(MainActivityNew.this, "Permission Granted", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(MainActivityNew.this, "Permission Denied. Buttons Disabled!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    mRecorderButton.setEnabled(false);
                }
            }
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);

    }


}
