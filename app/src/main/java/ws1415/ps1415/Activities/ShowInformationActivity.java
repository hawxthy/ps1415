package ws1415.ps1415.Activities;


import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.skatenight.skatenightAPI.model.Event;

import ws1415.ps1415.Constants;
import ws1415.ps1415.adapter.ShowCursorAdapter;
import ws1415.ps1415.R;
import ws1415.ps1415.task.GetEventTask;

/**
 * Activity zum Begutachten der Metainformationen der erstellten Veranstaltung.
 * <p/>
 * Created by Bernd Eissing, Marting Wrodarczyk on 21.10.2014.
 */
public class ShowInformationActivity extends Activity {
    static final int REQUEST_ACCOUNT_PICKER = 2;
    private GoogleAccountCredential credential;
    private SharedPreferences prefs;

    // Adapter für die ListView von activity_show_information_list_view
    private ShowCursorAdapter listAdapter;

    // Die ListView von der xml datei activity_show_information
    private ListView listView;

    /**
     * Erstellt die View und zeigt die Informationen in der ShowInformationActivity.
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_information);

        long id = getIntent().getLongExtra("event", 0);
        new GetEventTask(this).execute(id);

        // SharePreferences skatenight.app laden
        prefs = this.getSharedPreferences("skatenight.app", Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingAudience(this, "server:client_id:" + Constants.WEB_CLIENT_ID);

        // accountName aus SharedPreferences laden
        if (prefs.contains("accountName")) {
            credential.setSelectedAccountName(prefs.getString("accountName", null));
        }

        // Kein accountName gesetzt, also AccountPicker aufrufen
        if (credential.getSelectedAccountName() == null) {
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
    }

    /**
     * Übernimmt die Informationen aus dem übergebenen Event-Objekt in die GUI-Elemente.
     *
     * @param e Das neue Event-Objekt.
     */
    public void setEventInformation(Event e) {
        if (e != null) {
            setTitle(e.getTitle());
            listAdapter = new ShowCursorAdapter(this, e.getDynamicFields(), e);

            listView = (ListView) findViewById(R.id.activity_show_information_list_view);
            listView.setAdapter(listAdapter);
        }
    }

    /**
     * Callback-Methode fÃŒr den Account-Picker.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);

                        // accountName in den SharedPreferences speichern
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("accountName", accountName);
                        editor.commit();
                    }
                }
                break;
        }
    }
}
