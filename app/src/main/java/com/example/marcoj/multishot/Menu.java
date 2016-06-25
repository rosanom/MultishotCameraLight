package com.example.marcoj.multishot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Menu extends ActionBarActivity {

    private static final String TAG = "MENU";
    private String fileName, formattedDate, rootDirectory, videoFolder, extension;
    private Button shot_btn, settings_btn, viewMap_btn;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //directory app
        rootDirectory=preferences.getString("rootDirectory","");
        //cartella per il video
        videoFolder=preferences.getString("videoFolder","");
        //estensione file
        extension=".mp4";

        shot_btn=(Button)findViewById(R.id.button1);
        viewMap_btn=(Button)findViewById(R.id.button2);
        settings_btn=(Button)findViewById(R.id.button3);

        //listener pulsate fotocamera
        shot_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
        //listener pulsante mappa
        viewMap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMap();
            }
        });
        //listener pulsante impostazioni
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(v.getContext(), SettingsActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //onPause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(this, "OnPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //Toast.makeText(this, "Resume", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void launchCamera(){
        final int REQUEST_VIDEO_CAPTURE=1;
        //prende la data e l'ora
        Calendar c=Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy-HHmmss");
        formattedDate = df.format(c.getTime());
        int seconds=Integer.parseInt(preferences.getString("seconds", "-1"));

        /* Create an Intent that will start the Menu-Activity. */
        Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        //indica la destinazione della foto (funzione esterna)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(writeVideo()));
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, seconds);

        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
    }

    public void startMap(){
        Intent mapIntent=new Intent(this, MapActivity.class);
        startActivity(mapIntent);
    }

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

    protected File writeVideo(){
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
    /*
    public void openGallery(){
        Intent intent = new Intent(Intent.ACTION_VIEW,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("image/*");
        //startActivity(Intent.createChooser(intent,"seleziona la galleria"));
        startActivity(intent);
    }
    */

}
