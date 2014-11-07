package ws1415.veranstalterapp;



import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.appspot.skatenight_ms.skatenightAPI.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Fragment zum begutachten der Metainformationen der erstellten Veranstaltung.
 *
 * Created by Bernd Eissing, Marting Wrodarczyk on 21.10.2014.
 */
public class ShowInformationFragment extends Fragment {
    private ActionBar actionBar;
    private View view;
    private SimpleDateFormat dateFormat;

    /**
     * Erstellt die View und updatet die Informationen in dem ShowInformationFragment.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_show_information, container, false);

        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        actionBar = getActivity().getActionBar();

        HoldTabsActivity.updateInformation();

        return view;
    }

    /**
     * Übernimmt die Informationen aus dem übergebenen Event-Objekt in die GUI-Elemente.
     *
     * @param e Das neue Event-Objekt.
     */
    public void setEventInformation(Event e) {
        TextView dateView = (TextView) view.findViewById(R.id.show_info_date_textview);
        TextView locationView = (TextView) view.findViewById(R.id.show_info_location_textview);
        TextView routeView = (TextView) view.findViewById(R.id.show_info_route_textview);
        TextView feeView = (TextView) view.findViewById(R.id.show_info_fee_textview);
        TextView descriptionView = (TextView) view.findViewById(R.id.show_info_description_textview);
        if (e != null) {
            actionBar.getTabAt(0).setText(e.getTitle());
            dateView.setText(dateFormat.format(new Date(e.getDate().getValue())));
            locationView.setText(e.getLocation());
            routeView.setText(e.getRoute().getName());
            feeView.setText(e.getFee());
            if (e.getDescription() != null) {
                descriptionView.setText(e.getDescription().getValue());
            }
        } else {
            dateView.setText(getString(R.string.show_info_empty_text));
            locationView.setText(getString(R.string.show_info_empty_text));
            routeView.setText(getString(R.string.show_info_empty_text));
            feeView.setText(getString(R.string.show_info_empty_text));
            descriptionView.setText(getString(R.string.show_info_empty_text));
        }
    }


}
