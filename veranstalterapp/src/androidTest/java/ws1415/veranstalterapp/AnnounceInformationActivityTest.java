package ws1415.veranstalterapp;

import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by Bernd Eissing on 24.10.2014.
 */
public class AnnounceInformationActivityTest extends ActivityInstrumentationTestCase2<AnnounceInformationActivity>{
    private AnnounceInformationActivity activity;

    public AnnounceInformationActivityTest(){
        super(AnnounceInformationActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    public void testSetTime(){

    }
}
