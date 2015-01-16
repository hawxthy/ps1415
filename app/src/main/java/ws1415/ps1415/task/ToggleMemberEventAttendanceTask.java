package ws1415.ps1415.task;

import java.io.IOException;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.ServiceProvider;

/**
 * Created by Daniel on 21.11.2014.
 */
public class ToggleMemberEventAttendanceTask extends ExtendedTask<Void, Void, Boolean> {
    private long keyId;
    private String email;
    private boolean attending;

    public ToggleMemberEventAttendanceTask(ExtendedTaskDelegate delegate, long keyId, String email, boolean attending) {
        super(delegate);
        this.keyId = keyId;
        this.email = email;
        this.attending = attending;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (email == null) {
            publishError("Email should not be null.");
            return null;
        }
        try {
            ServiceProvider.getService().skatenightServerEndpoint().createMember(email).execute();
            if (!attending) {
                ServiceProvider.getService().skatenightServerEndpoint().addMemberToEvent(keyId, email).execute();
                return !attending;
            }
            else {
                ServiceProvider.getService().skatenightServerEndpoint().removeMemberFromEvent(keyId, email).execute();
                return !attending;
            }

        } catch (IOException e) {
            e.printStackTrace();
            publishError(e.getMessage());
        }
        return null;
    }
}
