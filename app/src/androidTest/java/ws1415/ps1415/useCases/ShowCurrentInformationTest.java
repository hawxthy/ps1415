package ws1415.ps1415.useCases;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.util.Date;

import ws1415.ps1415.Activities.ShowInformationActivity;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;

/**
 * Testet den Use Case "Aktuelle Informationen anzeigen".
 *
 * @author Tristan
 */
public class ShowCurrentInformationTest extends ActivityInstrumentationTestCase2<ShowInformationActivity> {

    private ShowInformationActivity mActivity;

    private TextView dateView;
    private TextView locationView;
    private TextView feeView;
    private TextView descriptionView;
    private Button mapButton;

    private Event testEvent;
    private Route testRoute;

    /**
     * Stellt eine Verbindung zum Testserver her und bereitet die Testdaten vor.
     */
    public ShowCurrentInformationTest() {
        // Bezug auf die ShowInformationActivity
        super(ShowInformationActivity.class);

        // Testdaten erstellen
        testEvent = new Event();
        testEvent.setTitle("Testtitel");
        testEvent.setDate(new DateTime(new Date()));
        testEvent.setFee("3");
        testEvent.setLocation("Testort");
        testEvent.setDescription(new Text().setValue("Lorem ipsum dolor sit amet, consetetur " +
                "sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore " +
                "magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo " +
                "dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est " +
                "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing " +
                "elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna " +
                "aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores " +
                "et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum " +
                "dolor sit amet."));
        testRoute = new Route();
        testRoute.setName("Testroute");
        testRoute.setLength("10 km");
        testRoute.setRouteData(new Text().setValue("yrb|H_upm@AB?X??IBIDIFGHGJENELAL?DAB?B?B?@?B?" +
                "B?B?F?H@F@H@F@FBF@FDHDFDFDDFDFBF@D?FLDNBRBT@LD\\BZBL@^@X@j@At@?|AAd@?@?n@@r@Bb@D" +
                "^PhAVbBJj@Jp@Hj@TrBAr@HbB??yA\\{@XQFQDWHUH_@D]ECA}@Sa@MUGi@My@QC?OCOCI?ODYBUDa@J" +
                "c@Nc@RKFMJQPOTa@d@_BvB_@d@UXSPSLSJOFG@SDKB_@Dw@FQGs@DqBNK@QBO@OAWC_AMMC]Gg@IQCOA" +
                "[CWAa@?m@@e@@mBJI@SBO@c@FSD_@HC@g@L_@L[LOHC@_@PKFWNMHEB??IHIJGFGJEFGJEJA?CFENKd@" +
                "Qp@CHIXK\\[v@Yr@OX??a@q@QYOUIIk@w@a@q@Ua@Qa@S_@??aCoFQa@Q_A??[T??qAqGQkAKm@G_@Im" +
                "@MeAY_F??`AEb@Ah@Hv@Ft@Fl@HP@LA??BA@ABIBMBU@K@I@I@E?EBEBIBGFGJKVUTQRKRIJEn@WJCHC" +
                "FADAHANCHA^EPAPENAFABABAFEHEDGJKRWFG??LENCLAR@NBHYXw@ZaAL_@BMBKDQ@QR_BFq@Di@HO?u" +
                "@???a@Aa@?a@Ag@@U@W@SBOAIBM@AF[HYNa@Rk@Tg@Xa@??R[JWJa@Lk@d@kBF[DY??b@wBNo@FG@EDM" +
                "DIDKNWFI^]HK\\U??RMPMLM@C??Za@HQRe@Tk@l@uAP]Nk@Lo@DSF_@??DuB?Y?YBmAF}@??j@@\\BVB" +
                "VB~ATRBPDPDXDz@F??PRNHJLHRHPRh@DTHT??LZTd@Xf@JNNVb@l@`BdBbBjB??BNhBvB??LJ\\VJ@LP" +
                "j@t@FLXh@Vp@BF??b@vANh@d@fBh@pB@HDXBRBTB^Dj@?BBVCZ"));
        testEvent.setRoute(testRoute);
    }

    @Override
    /**
     * Überträgt die Testdaten auf den Server.
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

        mActivity = getActivity();

        dateView = (TextView) mActivity.findViewById(R.id.show_info_date_textview);
        locationView = (TextView) mActivity.findViewById(R.id.show_info_location_textview);
        feeView = (TextView) mActivity.findViewById(R.id.show_info_fee_textview);
        descriptionView = (TextView) mActivity.findViewById(R.id.show_info_description_textview);
        mapButton = (Button) mActivity.findViewById(R.id.show_info_map_button);

        // Test-Event setzen
        ServiceProvider.getService().skatenightServerEndpoint().setEventTestMethod(testEvent).execute();

    }

    /**
     * Stellt eine Verbindung zum Testserver her und bereitet die Testdaten vor.
     */
    public void testPreConditions() {
        assertNotNull("dateView is inizalized", dateView.getText() != null);
        assertNotNull("locationView is inizalized", locationView.getText() != null);
        assertNotNull("feeViewView is inizalized", feeView.getText() != null);
        assertNotNull("descriptionView is inizalized", descriptionView.getText() != null);
    }

    public void testCurrentInformationUI() {
        String dateText         = (String) dateView.getText();
        String locationText     = (String) locationView.getText();
        String feeText          = (String) feeView.getText();
        String descriptionText  = (String) descriptionView.getText();

        // Vergleich der Event Daten mit denen aus den Views
        assertEquals("wrong date", testEvent.getDate().getValue(), dateText);
        assertEquals("wrong location", testEvent.getLocation(), locationText);
        assertEquals("wrong fee", testEvent.getFee(), feeText);
        assertEquals("wrong description", testEvent.getDescription().getValue(), descriptionText);
    }

}









