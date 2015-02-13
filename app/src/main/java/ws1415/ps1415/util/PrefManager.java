package ws1415.ps1415.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Diese Klasse wird genutzt um Einstellungen vom Benutzer persistent abzuspeichern.
 *
 * @author Martin Wrodarczyk
 */
public class PrefManager {
    private PrefManager(){}

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean getGroupVisibility(Context context, String groupName){
        return getSharedPreferences(context).getBoolean(groupName, false);
    }

    public static void setGroupVisibility(Context context, String groupName, Boolean visibility){
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(groupName, visibility);
        editor.commit();
    }
}
