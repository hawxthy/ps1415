package ws1415.veranstalterapp.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Field;

import ws1415.veranstalterapp.Adapter.AnnounceCursorAdapter;
import ws1415.veranstalterapp.activity.HoldTabsActivity;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.task.CreateEventTask;
import ws1415.veranstalterapp.util.EventUtils;

/**
 * Fragment zum Veröffentlichen von neuen Veranstaltungen.
 *
 * Created by Bernd Eissing, Marting Wrodarczyk on 21.10.2014.
 */
public class AnnounceInformationFragment extends Fragment {
    //Adapter für die ListView listView
    private AnnounceCursorAdapter listAdapter;

    // Die ListView von der xml datei fragment_announce_information
    private ListView listView;

    // Die Buttons für Cancel, Apply und Edit
    private Button applyButton;
    private Button cancelButton;
    private Button editButton;

    // das neu erstellte Event
    private Event event;

    /**
     * Erstellt die View, initialisiert die Attribute, setzt die Listener für die Buttons.
     *
     * @param inflater           Übersetzt die xml Datei in View Elemente
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announce_information, container, false);

        // Erstelle ein neues Event und füge die Standardattribute in die ArrayList ein.
        event = new Event();
        event.setDynamicFields(new ArrayList<Field>());
        EventUtils.getInstance(getActivity()).setStandardFields(event);
        listAdapter = new AnnounceCursorAdapter(getActivity(), event.getDynamicFields());

        listView = (ListView) view.findViewById(R.id.fragment_announce_information_list_view);
        listView.setAdapter(listAdapter);

        applyButton = (Button) view.findViewById(R.id.announce_info_apply_button);
        cancelButton = (Button) view.findViewById(R.id.announce_info_cancel_button);
        editButton = (Button) view.findViewById(R.id.announce_info_edit_button);

        setButtonListener();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    /**
     * Methode zum setzen der Listener für die Buttons "timePickerButton", "datePickerButton",
     * "applyButton", "cancelButton", "routePickerButton".
     */
    private void setButtonListener() {
        // Setze die Listener für Cancel und Apply Buttons
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!listAdapter.getEditMode()) {
                    cancelInfo(true);
                }
                Toast.makeText(getActivity(), getResources().getString(R.string.announce_info_edit_mode_string), Toast.LENGTH_LONG);
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!listAdapter.getEditMode()) {
                    applyInfo();
                }
                Toast.makeText(getActivity(), getResources().getString(R.string.announce_info_edit_mode_string), Toast.LENGTH_LONG);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(editButton.getText().equals(getResources().getString(R.string.announce_info_start_edit_button))){
                    listAdapter.startEditMode();
                    editButton.setText(getResources().getString(R.string.announce_info_exit_edit_button));
                }else if(editButton.getText().equals(getResources().getString(R.string.announce_info_exit_edit_button))){
                    listAdapter.exitEditMode();
                    editButton.setText(getResources().getString(R.string.announce_info_start_edit_button));
                }
            }
        });
    }

    /**
     * Liest die eingegebenen Informationen aus, erstellt ause diesen ein Event und fügt dieses
     * Event dem Server hinzu.
     */
    public void applyInfo() {

        // Zeige einen Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.areyousure);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int titleId = EventUtils.getInstance(getActivity()).getUniqueFieldId(EventUtils.TYPE.TITLE, event);

                // Überprüfen ob wirklich alle daten des Events gesetzt sind
                if (titleId != -1 && !((EditText) listView.getChildAt(titleId).findViewById(R.id.list_view_item_announce_information_simpletext_editText)).getText().toString().isEmpty()){

                    // Setze die Attribute vom Event
                    EventUtils.getInstance(getActivity()).setEventInfo(event, listView);

                    // Erstelle Event auf dem Server
                    new CreateEventTask().execute(event);

                    // Benachrichtige den Benutzer mit einem Toast
                    Toast.makeText(getActivity(), getResources().getString(R.string.eventcreated), Toast.LENGTH_LONG).show();

                    // Setze die Attribute von Event auf den Standard
                    EventUtils.getInstance(getActivity()).setStandardFields(event);

                    // Update die Informationen in ShowInformationFragment
                    HoldTabsActivity.updateInformation();
                } else {
                    cancelInfo(false);
                }

            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Gibt momentan einen Toast aus, wenn der Benutzer auf den Cancel Button drückt
     * (löscht alle Eingaben) oder wenn der Benutze ein unvollständiges Event hinzufügen will
     */
    public void cancelInfo(boolean allSet) {
        if (allSet) {
            Toast.makeText(getActivity(), "Wurde noch nicht erstellt", Toast.LENGTH_LONG).show();
            EventUtils.getInstance(getActivity()).setStandardFields(event);
            listAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), "Nicht alle notwendigen Felder ausgefüllt", Toast.LENGTH_LONG).show();
        }
    }
}
