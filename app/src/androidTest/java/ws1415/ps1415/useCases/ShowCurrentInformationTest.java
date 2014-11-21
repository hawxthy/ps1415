package ws1415.ps1415.useCases;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.TextView;

import ws1415.ps1415.Activities.ShowInformationActivity;
import ws1415.ps1415.R;

/**
 * Testet den Use Case "Aktuelle Informationen anzeigen".
 *
 * @author Tristan Rust
 */
public class ShowCurrentInformationTest extends ActivityInstrumentationTestCase2<ShowInformationActivity> {

    private ShowInformationActivity mActivity;

    private TextView dateView = (TextView) mActivity.findViewById(R.id.show_info_date_textview);
    private TextView locationView = (TextView) mActivity.findViewById(R.id.show_info_location_textview);
    private TextView feeView = (TextView) mActivity.findViewById(R.id.show_info_fee_textview);
    private TextView descriptionView = (TextView) mActivity.findViewById(R.id.show_info_description_textview);
    private Button mapButton = (Button) mActivity.findViewById(R.id.show_info_map_button);

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
