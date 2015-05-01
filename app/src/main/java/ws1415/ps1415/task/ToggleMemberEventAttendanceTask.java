package ws1415.ps1415.task;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.net.ServiceProvider;
import ws1415.ps1415.activity.ShowEventsActivity;

/**
 * Created by Daniel on 21.11.2014.
 */
public class ToggleMemberEventAttendanceTask extends ExtendedTask<Void, Void, Boolean> {
    private long keyId;
    private String email;
    private boolean attending;
    private Context context;

    public ToggleMemberEventAttendanceTask(ExtendedTaskDelegate delegate, long keyId, String email, boolean attending, Context context) {
        super(delegate);
        this.keyId = keyId;
        this.email = email;
        this.attending = attending;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (email == null) {
            publishError("Email should not be null.");
            return null;
        }
        try {
            ServiceProvider.getService().userEndpoint().createMember(email).execute();
            if (!attending) {
                ServiceProvider.getService().eventEndpoint().addMemberToEvent(keyId, email).execute();
                return !attending;
            }
            else {
                ServiceProvider.getService().eventEndpoint().removeMemberFromEvent(keyId, email).execute();
                return !attending;
            }

        } catch (IOException e) {
            e.printStackTrace();
            publishError(e.getMessage());
        }
        return null;
    }

    @Override
    public void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Intent refreshIntent = new Intent(ShowEventsActivity.REFRESH_EVENTS_ACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
    }

}
