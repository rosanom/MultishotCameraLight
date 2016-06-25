package com.example.marcoj.multishot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends ActionBarActivity {

    static int REQUEST_VIDEO_CAPTURE=1;
    private static final String TAG = "SPLASH";
    private String fileName, formattedDate, videoFolder, extension, rootDirectory;
    private int frameRate, seconds;
    private boolean deleteVideo;
    private SharedPreferences preferences;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 10;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.logo);

        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(preferences.contains("seconds")) { //verifichiamo che il file delle sharedPreferences esiste
            Log.i(TAG, "sharedPreferences esiste");

            rootDirectory = preferences.getString("rootDirectory", "");
            videoFolder = preferences.getString("videoFolder", "");
            frameRate = Integer.parseInt(preferences.getString("frameRate", "-1"));
            seconds = Integer.parseInt( preferences.getString("seconds", "-1") );
            deleteVideo = preferences.getBoolean("deleteVideo", false);

            Log.i(TAG, "seconds: "+seconds);
            Log.i(TAG, "framerate: "+frameRate);

        }
        else{
            Log.i(TAG, "sharedPreferences NON esiste");
            // è la directory della memoria esterna more: http://stackoverflow.com/questions/15402702/how-to-create-application-folder-in-android
            File externalPath=android.os.Environment.getExternalStorageDirectory();
            String folder="MultishotCameraLight";
            rootDirectory=externalPath+ (File.separator) +folder;
            frameRate=2;
            seconds=2;
            //cartella per il video
            videoFolder="temp";
            deleteVideo=false;
            //inserimenti dei valori nelle sharedPreferences
            SharedPreferences.Editor edit=preferences.edit();
            edit.clear();
            edit.putString("rootDirectory", rootDirectory);
            edit.putString("videoFolder", videoFolder);
            edit.putString("frameRate", "" + frameRate);
            edit.putString("seconds", "" + seconds);
            edit.putBoolean("deleteVideo", deleteVideo);
            edit.commit();
        }
        //estensione file
        extension=".mp4";
        //prende la data e l'ora
        Calendar c=Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy-HHmmss");
        formattedDate = df.format(c.getTime());
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                /* Create an Intent that will start the Menu-Activity. */
                Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                //indica la destinazione della foto (funzione esterna)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(writeVideo()));
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, seconds);

                startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
            }
        }, 1000);



        // Set up the user interaction to manually show or hide the system UI.
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
*/
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //controllare l'esito dell'operazione dal resultcode
        //Toast.makeText(getApplicationContext(),"resultcode: "+resultCode,Toast.LENGTH_SHORT).show();
        //resultCode positivo=-1, altrimenti 0
        Intent nextActivity;
        if(resultCode==-1){
            nextActivity=new Intent(this,Preview.class);
            //passa info all'activity "Preview"
            //nextActivity.putExtra("mainPathName", rootDirectory);//  0/xxx
            //nextActivity.putExtra("videoFolder", videoFolder); // yyy
            nextActivity.putExtra("videoFileName", fileName);
            this.startActivity(nextActivity);
        }
        else{
            nextActivity=new Intent(this,Menu.class);
            this.startActivity(nextActivity);
        }
        this.finish();
    }

    private File writeVideo(){
        String fullVideoPath=rootDirectory+ (File.separator) +videoFolder+ (File.separator); //0/xxx/temp/
        //crea la directory per lo storage delle foto dell'app
        File path = new File(fullVideoPath);
        if(!path.exists()) {
            path.mkdirs();
            Log.i("TAG", "Pathname inesistente. Creazione in corso..");
        }
        //crea un file col nome del video
        fileName="video_"+formattedDate+extension;
        File file = new File(path, fileName);
        Log.i("TAG", "File scritto. Pathname: "+file.toString());
        return file;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(10); //immediately fullscreen
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
