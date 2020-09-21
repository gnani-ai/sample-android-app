package com.rondaulagupu.android.voicenavigation;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gnani.speechtotext.Recorder;
import com.gnani.speechtotext.SpeechService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rodolfonavalon.shaperipplelibrary.ShapeRipple;
import com.rodolfonavalon.shaperipplelibrary.model.Circle;

import butterknife.ButterKnife;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivityNew extends AppCompatActivity implements SpeechService.Listener, Recorder.RecordingStatusListener
         {

    private static final int REQUEST_PERMISSION_CODE = 1;
    public static final String TYPE = "application/pdf";


    Button mRecorderButton;


    ShapeRipple mRipple;


    TextView textView;



    private boolean isRecorderPressed = false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        mRipple=(ShapeRipple)findViewById(R.id.ripple);
        mRecorderButton=(Button)findViewById(R.id.fab);
        textView=(TextView)findViewById(R.id.textView4);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (!checkPermission()) {
            requestPermission();
        }

        Recorder.bind(MainActivityNew.this);

        mRipple.setRippleShape(new Circle());
        mRecorderButton.setText("click & speak");



        mRecorderButton.setOnClickListener(v -> {

            Recorder.onRecord(("hin_IN"));


            isRecorderPressed = !isRecorderPressed;

            if (isRecorderPressed) {

                mRipple.setVisibility(View.VISIBLE);
                mRecorderButton.setText("stop");

            } else {
                mRipple.setVisibility(View.INVISIBLE);
                mRecorderButton.setText("start");

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
             public void onRecordingStatus(final boolean status) {

                 Log.e("STATUS", " " + status);

                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         if (status) {
                    System.out.println("status"+status);
                         } else {
                             System.out.println("status"+status);
                         }
                     }
                 });
             }

             @Override
             public void onSpeechRecognized(final String text, String asr, boolean isFinal) {

                 Log.e("text", " " + text);
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {

                         System.out.println("asr"+text);

                         textView.setText(text);



                     }
                 });

             }

             @Override
             public void onError(Throwable t) {
                 Log.e("on_ERROR", " " + t);
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                       //  btnS.setText("START");

                         System.out.println("error"+t.toString());
                     }
                 });
             }

             @Override
             protected void onDestroy() {
                 super.onDestroy();

                 Recorder.unbind(MainActivityNew.this);
             }





         }
