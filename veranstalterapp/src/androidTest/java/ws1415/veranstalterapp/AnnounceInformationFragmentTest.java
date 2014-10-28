package ws1415.veranstalterapp;

import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by Bernd Eissing on 24.10.2014.
 */
public class AnnounceInformationFragmentTest extends ActivityInstrumentationTestCase2<AnnounceInformationFragment>{
    private AnnounceInformationFragment activity;

    public AnnounceInformationFragmentTest(){
        super(AnnounceInformationFragment.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    public void testSetTime(){

    }
}
