package ws1415.ps1415;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.appspot.skatenight_ms.skatenightAPI.model.Event;


public class ShowInformationActivity extends Activity {
    private static final String MEMBER_TITLE = "show_infomation_member_title";
    private static final String MEMBER_DATE = "show_infomation_member_date";
    private static final String MEMBER_LOCATION = "show_infomation_member_location";
    private static final String MEMBER_FEE = "show_infomation_member_fee";
    private static final String MEMBER_DESCRIPTION = "show_infomation_member_description";
    private static final String MEMBER_ROUTE = "show_infomation_member_route";

    private String title;
    private String date;
    private String location;
    private String fee;
    private String description;
    private String route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showinformation);

        Button mapButton = (Button) findViewById(R.id.show_info_map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (route != null) {
                    Intent intent = new Intent(ShowInformationActivity.this, ShowRouteActivity.class);
                    intent.putExtra(ShowRouteActivity.EXTRA_ROUTE, route);
                    startActivity(intent);
                }
            }
        });

        if (savedInstanceState != null) {
            title = savedInstanceState.getString(MEMBER_TITLE);
            date = savedInstanceState.getString(MEMBER_DATE);
            location = savedInstanceState.getString(MEMBER_LOCATION);
            fee = savedInstanceState.getString(MEMBER_FEE);
            description = savedInstanceState.getString(MEMBER_DESCRIPTION);
            route = savedInstanceState.getString(MEMBER_ROUTE);
            updateGUI();
        }
        else {
            new QueryEventTask().execute(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(MEMBER_TITLE, title);
        outState.putString(MEMBER_DATE, date);
        outState.putString(MEMBER_LOCATION, location);
        outState.putString(MEMBER_FEE, fee);
        outState.putString(MEMBER_DESCRIPTION, description);
        outState.putString(MEMBER_ROUTE, route);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.example, menu);
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

    /**
     * Übernimmt die Informationen aus dem übergebenen Event-Objekt in die GUI-Elemente.
     * @param e Das neue Event-Objekt.
     */
    public void setEventInformation(Event e) {
        if (e != null) {
            title =  e.getTitle();
            date = e.getDate();
            location = e.getLocation();
            fee = e.getFee();
            description = e.getDescription().getValue();
            route = "qcpaGr|upL~AfAH]t@{D^}BVyAWUYQHi@FuA?oEgBZsGlAoDn@iE|@wBpAyAnAKLy@mDM_@ISYg@c@QqAbBsBhBg@h@MNu@BEBaBJ}GVcA@gAn@_BbAm@^sHvEkD|BmAvAYXQJUFQA_AyAwA_CoEcHcE_FoDyEsBsDk@o@_BeBKVKR?@EFmAfCc@tAo@bDwFfUqBlIm@lBaKjWwIpTiD`Jg@Pc@ViA|@oAOm@RWf@E^Dj@HTLZ`AZRn@?LaAtCcA~Ck@dAqDjEwBdCcB`CuKzOyExGiAnAgA`A}BjBeF~EgBfCo@dAaCbEyBlDgCxD}RzXqDbEyEjEkMzKiMvK{JzIyBnBQAiBj@uBf@qAD}DUaFYo@FoA`@_Ar@gA~AYv@aCpLq@bC_BxCgIxMiArC_AnEaAnDiB`DqA~FeB|D_@xBKrFm@ra@NvG@vHXlCh@zBx@|An@t@f@N`@^t@xCrBpEnAjB~BlCbEzDr@lAPTTBJOXQ`@Qp@BxABXA~@KpA_@|BWzEFjAO~Dw@~@?d@LjAx@d@h@tAvCzBjEj@x@xD|CbCv@zIA|CA|@Lv@\\z@v@`BfBzC`CdCvCt@lBb@xDZhGh@`CjBvCrAfAv@`Av@pC@h@}@nFQx@_FlUeHb]oA|E}FbOwAdDiEfIqBrEq@jCcEvTs@vD}@lEaCfJqFhSsDpO_EpU_Dl[qAtIsGtXaDfLkE`NqCnK{D|SyB~M}@|Hy@dMe@fWUfO]hIg@lHaApJ_B~JiAjF}CdLsCpJaBjJi@fHOtEIpF?|AQd@Gn@{@tMOp@[`@YLuAF{@@eBQ}Fe@wBM{Iq@qBEs@JsGzBsPzJsAj@kBd@sKtBkC`@wCTuDPmAe@kFcFeDkAcC}As@y@oD}EuD}Dy@s@dAcBTo@Un@eAbB|CtCrDtElAbB\\b@U`@q@bA{BtCiLpNgDtDsBz@qBTsAGcFkAgDWyCFhBtOl@xF|@tEn@lDDnD@~DZlCpAhHFrAEbFVtDCzF`@|Ki@rI@`BVvDXrCb@dBhBjFXxAFzBe@|JAz@RrBZv@Xd@\\h@wAtBu@dAmD`Fw@~BgAbJa@bF{B`Z{@`]|@`LHh^?dH{@jEcA`DiChFgBdEsAvEg@pDsAlQqEfo@qBpg@ChAQ`@kApEmEdJyBzGmAhCyF`KcAfCy@~C]fGUrBw@bDmD|LeAbFInAF~BN|CU|KTdM`BbSPpIjA~HZxKl@|Ka@tJw@dIaAzEmDvF_BjE[x@Wb@a@Jk@h@kBtAi@`@Tx@jBmAlAeA";
        } else {
            title = null;
            date = null;
            location = null;
            fee = null;
            description = null;
            route = null;
        }
        updateGUI();
    }

    /**
     * Überträgt die in den Variablen gespeicherten Informationen auf das GUI.
     */
    private void updateGUI() {
        TextView dateView = (TextView) findViewById(R.id.show_info_date_textview);
        TextView locationView = (TextView) findViewById(R.id.show_info_location_textview);
        TextView feeView = (TextView) findViewById(R.id.show_info_fee_textview);
        TextView descriptionView = (TextView) findViewById(R.id.show_info_description_textview);
        Button mapButton = (Button) findViewById(R.id.show_info_map_button);

        if (title != null &&
                date != null &&
                location != null &&
                fee != null &&
                description != null) {
            setTitle(title);
            dateView.setText(date);
            locationView.setText(location);
            feeView.setText(fee);
            descriptionView.setText(description);
            mapButton.setEnabled(true);
        }
        else {
            setTitle("leer");
            dateView.setText("leer");
            locationView.setText("leer");
            feeView.setText("leer");
            descriptionView.setText("leer");
            mapButton.setEnabled(false);
        }
    }
}