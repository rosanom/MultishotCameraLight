package com.example.marcoj.multishot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG="MAP";
    private boolean isFirstStart;
    private GoogleMap mMap;
    private ArrayList<LatLng> coordinates;
    private String[] photoData, photoTime, photoName;
    private MediaScannerConnection msc;
    private Map<Marker,String> markerPhotoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markerPhotoName=new HashMap<Marker,String>();
        isFirstStart=true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        //mi collego al db, popolo l'arraylist. Se il db è vuoto, apro la mappa con il mondo intero e lancio un toast.
        //altrimenti creo i markers che devono aprire però la galleria con la foto interessata..
        if(isFirstStart) {
            coordinates = new ArrayList<LatLng>();

            DBhandler dbIstance = new DBhandler(this);
            Log.i(TAG, "connesso al db");

            ArrayList<Photo> p = dbIstance.getElements();
            photoData = new String[p.size()];
            photoTime = new String[p.size()];
            photoName = new String[p.size()];
            for (int i = 0; i < p.size(); i++) {
                LatLng ll = new LatLng(p.get(i).getLatitude(), p.get(i).getLongitude());
                coordinates.add(i, ll);
                Log.i(TAG, "elemento " + i + " inserito.");

                String datatime = p.get(i).getFilename().substring(6, 19); //prendo la data e l'ora dal nome dell'immagine ( 14062016-1229 )
                photoData[i] = datatime.substring(0, 2) + "-" + datatime.substring(2, 4) + "-" + datatime.substring(4, 8); //solo la data ( 14-06-2016 )
                photoTime[i] = datatime.substring(9, 11) + ":" + datatime.substring(11, 13); //solo l'ora ( 12:29 )
                photoName[i] = p.get(i).getFilename();
            }
        }
        isFirstStart=false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if(coordinates.size()!=0) {
            for (int i = 0; i < coordinates.size(); i++) {

                Marker marker=mMap.addMarker(new MarkerOptions()
                                            .position(coordinates.get(i))
                                            .title("Foto scattata il " + photoData[i] + " alle ore " + photoTime[i]));
                marker.showInfoWindow();

                markerPhotoName.put(marker,photoName[i]); //inserisco la coppia marker/nomefile nella tabella hash
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates.get(coordinates.size() - 1), 18f));
        }
        else{
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0), 2f)); //mondo intero
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //scanFile();
                SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String rootDirectory=prefs.getString("rootDirectory","");
                String filename=markerPhotoName.get(marker); //prendo il nome del file corrispondente al marker dalla tabella hash
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(rootDirectory + File.separator + filename)), "image/*");
                startActivity(intent);
                return false;
            }

        });
    }
/*
    private void scanFile(){
        msc=new MediaScannerConnection(this,this);
        msc.connect();
    }
    @Override
    public void onMediaScannerConnected() {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String rootDirectory=prefs.getString("rootDirectory","");
        //video_14062016-154320picture_1.jpg
        Log.i(TAG, "path: "+rootDirectory+ File.separator +"video_14062016-161900picture_0.jpg");
        msc.scanFile(rootDirectory + File.separator + "video_14062016-161900picture_0.jpg", "image/*");
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
        Log.i(TAG, "scan disconnesso");
        msc.disconnect();
    }
*/
}
