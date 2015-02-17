package ws1415.ps1415.util;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Created by Daniel on 10.02.2015.
 */
public class LocalStorageUtil {
    private SharedPreferences  mPrefs  ;
    private Editor prefsEditor ;

    public LocalStorageUtil(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefsEditor = mPrefs.edit();
    }

    public void saveObject(LocalAnalysisData data, String id){
        Gson gson = new Gson();
        String json = gson.toJson(data);
        prefsEditor.putString(id, json);
        prefsEditor.commit();
        //Toast.makeText(getApplicationContext(), "Object Stored", 1000).show();
    }

    public LocalAnalysisData getData(String id){
        Gson gson = new Gson();
        String json = mPrefs.getString(id, "");
        LocalAnalysisData data = gson.fromJson(json, LocalAnalysisData.class);
        return data ;
    }




}
