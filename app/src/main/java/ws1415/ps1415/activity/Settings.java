package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import ws1415.ps1415.Constants;
import ws1415.ps1415.R;


/**
 * Erstellt ein Einstellungsmenü mit Hilfe der settings.xml.
 *
 * @author Tristan Rust
 */
public class Settings extends PreferenceActivity {

    private SharedPreferences prefs;
    private GoogleAccountCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

    }
}
