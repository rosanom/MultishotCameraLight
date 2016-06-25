package com.example.marcoj.multishot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Preview extends ActionBarActivity implements MediaScannerConnection.MediaScannerConnectionClient {

    private static final String TAG = "PREVIEW";

    private LocationService locService;
    private boolean mBound, wasStopped, imagesWereShown;
    private String[] videoInfo;
    private ArrayList<String> extractedImagesName;
    private MediaScannerConnection msc;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        //inizializzo
        extractedImagesName=new ArrayList<String>();
        //prendo le info sul video dall'activity precedente
        videoInfo=new String[6];
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        videoInfo[0] = preferences.getString("rootDirectory", "");
        videoInfo[1] = preferences.getString("videoFolder", "");

        Intent intent = getIntent();
        videoInfo[2] = intent.getExtras().getString("videoFileName");
        videoInfo[2]=videoInfo[2].substring(0, videoInfo[2].length() - 4); //tolgo il .mp4 alla fine della stringa

        videoInfo[3] = preferences.getString("frameRate","-1");
        videoInfo[4]=preferences.getString("seconds", "-1");
        videoInfo[5] = ""+preferences.getBoolean("deleteVideo",false);

        //avvio il service per ottenere la geolocalizzazione
        Intent intnt=new Intent(this, LocationService.class);
        startService(intnt);
        Log.i("gps", "service avviato");
        //Toast.makeText(this, "service avviato",Toast.LENGTH_SHORT).show();

        //bound del service
        Intent i = new Intent(this, LocationService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        //ProgressBar progress=(ProgressBar)findViewById(R.id.progressBar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        if(!wasStopped) { //l'activity è stata stoppata? NO
            FrameExtract FE = new FrameExtract(); //parte la classe Async per l'operazione asincrona
            FE.execute(videoInfo);
        }
    }

    @Override
    public void onMediaScannerConnected() {

        msc.scanFile(extractedImagesName.get(extractedImagesName.size() - 1), "image/*");//scan dell'ultimo frame. E' sufficiente
        Log.i(TAG, "Scan effettuato. Abs path: " + extractedImagesName.get(extractedImagesName.size()-1));
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        Log.i(TAG, "scan completed: " + uri + " , path: " + path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
        Log.i(TAG, "scan disconnesso");
        msc.disconnect();
        imagesWereShown=true;
        Log.i(TAG,"Le immagini sono state mostrate?: "+imagesWereShown);
    }

    @Override
    protected void onStop() {
        super.onStop();
        wasStopped=true;
        Log.i(TAG, "STOP activity Preview");

        //Il service è stoppato sia che tutto va per il meglio ma anche se questa activity viene stoppata..
        // Unbind from the service
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        //stop del service geolocalizzazione
        try {
            this.stopService(new Intent(this, LocationService.class));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "pause activity Preview");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("status", "resume activity Preview");
        if(wasStopped && !imagesWereShown) { //activity goes in BG before the images visualization
            //wasStopped=false;
            startScan();
            Log.i(TAG,"startScan");
        }
        else if(imagesWereShown) {
            onBackPressed();
            Log.i(TAG,"aprire menu!");
        }
        else{
            Log.i(TAG, "else..."+wasStopped+" , "+imagesWereShown);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Log.i(TAG, "backPressed");
        wasStopped=true;
        Intent openMenu=new Intent(this,Menu.class);
        this.startActivity(openMenu);

        this.finish();
    }

    public void startScan(){
        if(!wasStopped) {
            msc = new MediaScannerConnection(this, this);
            msc.connect();
            Log.i(TAG, "startScan() connesso");
        }
        else{
            Log.i(TAG, "scan non eseguito. Activity in background.");
        }
    }

    public void saveIntoDB(){
        //locService è la referenza al nostro service
        double latitude=locService.getLatitude();
        double longitude=locService.getLongitude();
        Log.i(TAG, "latitude: "+latitude+" , longitude: "+longitude);
        double latitudeWithOffset, longitudeWithOffset, randLat, randLon;
        Log.i(TAG, "Bounded?: "+mBound);
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
        Log.i(TAG, "Ancora bounded?: " + mBound);
        //stop del service geolocalizzazione
        this.stopService(new Intent(this, LocationService.class));

        //inseriamo i dati delle immagini, se abbiamo le coordinate
        if(latitude!=0 && longitude!=0) {
            DBhandler dbIstance = new DBhandler(this);
            Log.i(TAG, "connesso al db");
            for (int i = 0; i < extractedImagesName.size(); i++) {
                //poichè extractedImagesName è nel formato //0/xxx/aa.jpg, dobbiamo prendere solo aa.jpg
                String[] split = extractedImagesName.get(i).split(File.separator);
                //inseriamo un offset nelle coordinate in modo che successivamente non si sovrappongono i marker nella mappa
                randLat=Math.random()*15; //da 0 a 14,999..
                if( (int)(Math.random()*2) > 0)
                    randLat*=-1; //cambia segno
                randLon=Math.random()*15; //da 0 a 14,999..
                if( (int)(Math.random()*2) > 0)
                    randLon*=-1; //cambia segno
                latitudeWithOffset=latitude+( (randLat/100000) ); //porto il valore random nell'ordine di 10^-5
                longitudeWithOffset=longitude+( (randLon/100000) );
                Log.i(TAG,"randLat: "+randLat+" , latitudeWithOffset: "+latitudeWithOffset);
                Log.i(TAG,"randLon: "+randLon+" , longitudeWithOffset: "+longitudeWithOffset);
                dbIstance.addPhoto(split[split.length-1], latitudeWithOffset, longitudeWithOffset); //aggiungo la riga nel db
            }
            //mostro tutte le righe del db
            ArrayList<Photo> p = dbIstance.getElements();
            for (int i = 0; i < p.size(); i++) {
                Log.i(TAG, "---file n. " + (i + 1) + ": " + p.get(i).getFilename());
            }
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locService = binder.getService();
            mBound = true;
            Log.i(TAG, "bound connesso");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "bound disconnesso");
            mBound = false;
        }
    };


    private class FrameExtract extends AsyncTask<String[], Void, Void> {
        private String mainPathName, videoFolder, videoFileName, picturefileName;
        private Bitmap photo=null;
        private File newImage,video;
        private int i, frameRate, time, millisStep, seconds, nFrameToExtract;
        private boolean deleteVideo;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(String[]... params) {
            mainPathName=params[0][0];
            videoFolder=params[0][1]; //temp
            videoFileName=params[0][2]; //abcds without extension
            frameRate=Integer.parseInt(params[0][3]);
            seconds=Integer.parseInt(params[0][4]); //da usare nel ciclo for dopo i test
            deleteVideo=Boolean.parseBoolean(params[0][5]);
            millisStep=(int)(1000000/frameRate);
            nFrameToExtract=seconds*frameRate;
            Log.i("async", "summary: "+mainPathName+" "+videoFolder+" "+videoFileName+" "+frameRate+" "+seconds+" "+deleteVideo+" "+millisStep);

            //useful when we finish the extraction to delete the video file
            video=new File(mainPathName+ (File.separator) +videoFolder+ (File.separator) +videoFileName+".mp4");
            //creo la directory dei frame da salvare
            FileOutputStream out = null;

            MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
            mRetriever.setDataSource(mainPathName+ (File.separator) +videoFolder+ (File.separator) +videoFileName+".mp4");
            for(i=1; i<=nFrameToExtract; i++) {
                time=(i * millisStep); //giochetto per ottenere il frame con l'indice i
                try {
                    photo=mRetriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                //crea nome della foto
                picturefileName = "picture_"+i;
                try {
                    String imageName=mainPathName+ (File.separator)+videoFileName+picturefileName+ ".jpg";
                    out = new FileOutputStream(/*newImage+ (File.separator) +*/ imageName);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, out); // PNG is a lossless format, the compression factor (100) is ignored
                    out.close();
                    extractedImagesName.add(imageName);
                    Log.i("async", picturefileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { //quando le operazioni asincrone sono terminate
            super.onPostExecute(aVoid);
            if(deleteVideo){
                //eliminare video
                boolean deleted=video.delete();
                Log.i(TAG,"video "+video.getAbsolutePath()+" eliminato: "+deleted);
            }
            //method
            startScan(); //scan delle immagini da visualizzare
            saveIntoDB(); //salvataggio informazioni immagini nel db
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
