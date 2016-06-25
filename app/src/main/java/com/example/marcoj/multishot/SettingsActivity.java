package com.example.marcoj.multishot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.util.ArrayList;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SETTINGS";
    private static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();


        //int a=preferences.getInt("seconds",0);
        int b=Integer.parseInt( prefs.getString("seconds", "-1") );
        //boolean c=preferences.getBoolean("deleteVideo", false);
        boolean d=prefs.getBoolean("deleteVideo",false);
        Log.i(TAG,"------------------");
        //Log.i(TAG,"seconds: "+a);
        Log.i(TAG,"localSeconds: "+b);
        //Log.i(TAG,"deleteVideo: "+c);
        Log.i(TAG,"localDeleteVideo: "+d);
        Log.i(TAG,"------------------");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        prefs.unregisterOnSharedPreferenceChangeListener(this);
        this.finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("seconds")) {
            int localSeconds = Integer.parseInt( sharedPreferences.getString(key, "-3") );
            Log.i(TAG,"---localSeconds: "+localSeconds);
            int seconds = Integer.parseInt( prefs.getString("seconds", "-1") );
            Log.i(TAG,"---seconds: "+seconds);

        }
        else if(key.equals("deleteVideo")) {
            boolean localDeleteVideo= sharedPreferences.getBoolean(key, false);
            Log.i(TAG,"---localDeleteVideo: "+localDeleteVideo);
            boolean deleteVideo = prefs.getBoolean("deleteVideo", false);
            Log.i(TAG,"---deleteVideo: "+deleteVideo);
            /*
            if(localDeleteVideo!=deleteVideo){
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean("deletevideo", false);
                edit.commit();
                Log.i(TAG, "deleteVideo filed changed.");
            }
            */
        }
        else if(key.equals("frameRate")) {
            int localFrameRate = Integer.parseInt(sharedPreferences.getString(key, "-3"));
            Log.i(TAG,"---localFrameRate: "+localFrameRate);
            int frameRate = Integer.parseInt(prefs.getString("frameRate", "-1"));
            Log.i(TAG,"---frameRate: "+frameRate);
        }
        else{
            Log.i(TAG,"---"+key);
        }
    }

    public static int getFps(){
        int frameRate = Integer.parseInt(prefs.getString("frameRate", "-1"));
        return frameRate;
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            int fps=SettingsActivity.getFps();
            ArrayList<String> options=new ArrayList<String>();
            ArrayList<String> optionsVal=new ArrayList<String>();

            ListPreference fpsList=(ListPreference)findPreference("frameRate");

            options.add("2 fps");
            optionsVal.add("2");
            if(fps>=4) {
                options.add("4 fps");
                optionsVal.add("4");
            }
            if(fps>=5) {
                options.add("5 fps");
                optionsVal.add("5");
            }
            if(fps==10) {
                options.add("10 fps");
                optionsVal.add("10");
            }
            CharSequence entries[]=options.toArray(new CharSequence[options.size()]);
            CharSequence values[]=optionsVal.toArray(new CharSequence[optionsVal.size()]);

            fpsList.setEntries(entries);
            fpsList.setEntryValues(values);
        }
    }
}
