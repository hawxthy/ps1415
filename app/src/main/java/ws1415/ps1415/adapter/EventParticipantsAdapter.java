package ws1415.ps1415.adapter;

import android.app.Activity;

import com.skatenight.skatenightAPI.model.UserListData;

import java.util.List;
import java.util.Map;

import ws1415.ps1415.R;
import ws1415.ps1415.model.EventRole;

/**
 * Adapter für die Teilnehmer eines Events. Erweitert den UserAdapter um die Anzeige der Rolle, die
 * der Benutzer im Event besitzt.
 * @author Richard Schulze
 */
public class EventParticipantsAdapter extends UserListAdapter {
    private Activity activity;
    private Map<String, EventRole> roles;

    /**
     * Erstellt einen neuen UserAdapter, der zusätzlich die Rolle des Teilnehmers anzeigt.
     * @param userMails    Die anzuzeigenden Benutzer.
     * @param roles        Die Rollen der Benutzer. Key ist die E-Mail.
     * @param activity     Die Activity, die den Adapter enthält.
     */
    public EventParticipantsAdapter(List<String> userMails, Map<String, EventRole> roles, Activity activity) {
        super(userMails, activity);
        this.activity = activity;
        this.roles = roles;
    }

    @Override
    protected String setUpSecondaryText(UserListData userListData) {
        String secondaryText = super.setUpSecondaryText(userListData);
        if (!secondaryText.isEmpty()) {
            secondaryText += ", ";
        }
        secondaryText += activity.getResources().getStringArray(R.array.event_roles)[roles.get(userListData.getEmail()).ordinal()];
        return secondaryText;
    }
}
