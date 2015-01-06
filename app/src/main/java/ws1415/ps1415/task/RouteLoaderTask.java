package ws1415.ps1415.task;

import java.io.IOException;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.ServiceProvider;

/**
 * Created by Pascal Otto on 19.12.14.
 */
public class RouteLoaderTask extends ExtendedTask<Void, Void, String> {
    private long keyId;

    public RouteLoaderTask(ExtendedTaskDelegate delegate, long keyId) {
        super(delegate);
        this.keyId = keyId;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return ServiceProvider.getService().skatenightServerEndpoint().getEventRoute(keyId).execute().getValue();
        }
        catch (IOException e) {
            publishError(e.getMessage());
            return null;
        }
    }
}
