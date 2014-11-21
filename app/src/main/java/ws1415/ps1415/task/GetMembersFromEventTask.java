package ws1415.ps1415.task;

import android.os.AsyncTask;

import com.skatenight.skatenightAPI.model.Member;

import java.io.IOException;
import java.util.List;

import ws1415.ps1415.Activities.ShowRouteActivity;
import ws1415.ps1415.ServiceProvider;

/**
 * Created by Pascal Otto on 21.11.14.
 */
public class GetMembersFromEventTask extends AsyncTask<Void, Void, List<Member>> {
    private ShowRouteActivity view;
    private long keyId;

    public GetMembersFromEventTask(ShowRouteActivity view, long keyId) {
        this.view = view;
        this.keyId = keyId;
    }

    @Override
    protected List<Member> doInBackground(Void... params) {
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getMembersFromEvent(keyId).execute().getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Member> members) {
        view.updateMembers(members);
    }
}
