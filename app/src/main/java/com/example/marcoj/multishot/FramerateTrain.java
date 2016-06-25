package com.example.marcoj.multishot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class FramerateTrain extends ActionBarActivity {
    private TextView text, fps;
    private Button closeButton;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_framerate_train);

        progress=(ProgressBar)findViewById(R.id.progressBar2);
        text=(TextView)findViewById(R.id.textView2);
        fps=(TextView)findViewById(R.id.textView3);
        closeButton=(Button)findViewById(R.id.button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        trainFrameRate rate = new trainFrameRate(); //parte la classe Async per l'operazione asincrona
        rate.execute();
    }

    public void calibrationEnd(int fr){
        text.setText("");
        fps.setText(fr+"fps");
        fps.setVisibility(View.VISIBLE);
        closeButton.setVisibility(View.VISIBLE);
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        Intent i=new Intent(this,SettingsActivity.class);
        startActivity(i);
        this.finish();
    }



    private class trainFrameRate extends AsyncTask<String[], Void, Void> {
        private final static String TAG="ASYNC";
        private String videoPath;
        private Bitmap frame1=null, frame2=null;
        private int i, frameRate, time, frameRateResult, trainPixel, testPixel;
        private float trainPixelColor, testPixelColor;
        private float increments[]=new float[4];
        private int frameRateValues[]=new int[4];
        private boolean maxFrameRate;
        private SharedPreferences preferences;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(String[]... params) {
            /*
            videoPath="android.resource://" + getPackageName() + (File.separator) + R.raw.train_video;
            Log.i(TAG,"videoPath: "+videoPath);
            */
            AssetFileDescriptor afd=getResources().openRawResourceFd(R.raw.train_video);
            MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
            mRetriever.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());

            preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            frameRate=Integer.parseInt(preferences.getString("frameRate", "-1"));
            Log.i(TAG,"il framerate è uguale a: "+frameRate);

            increments[0]=0.1f; increments[1]=0.2f; increments[2]=0.3f; increments[3]=0.5f;
            frameRateValues[0]=10; frameRateValues[1]=5; frameRateValues[2]=4; frameRateValues[3]=2;

            try {
                frame1 = mRetriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
                trainPixel=frame1.getPixel(10,10);
                trainPixelColor = Color.red(testPixel);
                Log.i(TAG, "colore pixel: "+trainPixelColor);
            }catch(Exception e) {
                e.printStackTrace();
            }
            for(i=0;i<4 && !maxFrameRate; i++){
                time=(int)((increments[i]*1000000)); //giochetto per ottenere il frame con l'indice i
                try{
                    frame2=mRetriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST);
                    testPixel=frame2.getPixel(10, 10);
                    testPixelColor= Color.red(testPixel);
                    Log.i(TAG, "colore pixel: "+testPixelColor);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }

                if( !(frame1.sameAs(frame2))){ //se i 2 frame sono diversi
                    Log.i(TAG,"framerate massimo: "+frameRateValues[i]);
                    maxFrameRate=true;
                    frameRateResult=frameRateValues[i];
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putString("frameRate", ""+frameRateValues[i]);
                    editor.commit();
                }
                else{
                    Log.i(TAG,"");
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { //quando le operazioni asincrone sono terminate
            super.onPostExecute(aVoid);
            calibrationEnd(frameRateResult);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
