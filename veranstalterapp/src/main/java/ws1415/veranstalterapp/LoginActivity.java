package ws1415.veranstalterapp;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;


public class LoginActivity extends Activity {
    static final int REQUEST_ACCOUNT_PICKER = 2;

    private GoogleAccountCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        credential = GoogleAccountCredential.usingAudience(this,
                "server:client_id:"+Constants.WEB_CLIENT_ID);

        // Kein accountName gesetzt, also AccountPicker aufrufen
        if (credential.getSelectedAccountName() == null) {
            login();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        LoginTask loginTask = new LoginTask();
                        loginTask.execute(this);
                    }
                }
                break;
        }
    }

    /**
     * Führt die StartActivityForResult aus
     */
    public void login() {
        startActivityForResult(credential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public GoogleAccountCredential getCredential() {
        return credential;
    }

}
