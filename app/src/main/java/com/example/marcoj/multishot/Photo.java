package com.example.marcoj.multishot;

/**
 * Created by marcoj on 09/06/16.
 */
public class Photo {
    private String filename;
    private double latitude, longitude;

    public Photo(String filename, double latitude, double longitude){
        this.filename=filename;
        this.latitude=latitude;
        this.longitude=longitude;
    }
    public String getFilename(){
        return this.filename;
    }
    public double getLatitude(){
        return this.latitude;
    }
    public double getLongitude(){
        return this.longitude;
    }
}
