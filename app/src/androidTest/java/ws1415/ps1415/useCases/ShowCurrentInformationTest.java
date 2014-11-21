package ws1415.ps1415.useCases;

import android.test.ActivityInstrumentationTestCase2;

import ws1415.ps1415.Activities.ShowInformationActivity;

/**
 * @author Tristan Rust
 */
public class ShowCurrentInformationTest extends ActivityInstrumentationTestCase2<ShowInformationActivity> {

    private ShowInformationActivity mActivity;

    public ShowCurrentInformationTest() {
        // Bezug auf die ShowInformationActivity
        super(ShowInformationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

        mActivity = getActivity();


    }

}
