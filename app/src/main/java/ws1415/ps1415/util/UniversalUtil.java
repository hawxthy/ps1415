package ws1415.ps1415.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.Calendar;

import ws1415.common.gcm.GCMUtil;
import ws1415.common.net.ServiceProvider;
import ws1415.ps1415.Constants;
import ws1415.ps1415.activity.RegisterActivity;

/**
 * Created by Martin on 23.05.2015.
 */
public abstract class UniversalUtil {
    private static final String TAG = "Skatenight";

    /**
     * Prüft ob der Benutzer eingeloggt ist.
     *
     * @param activity Activity
     */
    public static void checkLogin(Activity activity) {
        Context context = activity.getApplicationContext();
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(context, "server:client_id:" + Constants.WEB_CLIENT_ID);

        if (PrefManager.getSelectedUserMail(context).equals("")) {
            Intent intent = new Intent(context, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            credential.setSelectedAccountName(PrefManager.getSelectedUserMail(context));
            ServiceProvider.login(credential);
            initGCM(activity);
        }
    }

    /**
     * Initialisiert GCM.
     *
     * @param activity Activity
     */
    public static void initGCM(Activity activity) {
        // GCM initialisieren
        Context context = activity.getApplicationContext();
        if (GCMUtil.checkPlayServices(activity)) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            String regid = LocalGCMUtil.getRegistrationId(context);

            if (regid.isEmpty()) {
                LocalGCMUtil.registerInBackground(context, gcm);
            } else {
                LocalGCMUtil.sendRegistrationIdToBackend(regid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    /**
     * Berechnet das Alter zu dem Geburtsdatum das in dem Calendar angegeben ist.
     *
     * @param dob Calendar
     * @return Alter
     */
    public static int calculateAge(Calendar dob) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR))
            age--;
        return age;
    }

    /**
     * Zeigt einen Toast mit der übergebenen Nachricht an.
     *
     * @param context Context
     * @param message Inhalt des Toasts
     */
    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


}
