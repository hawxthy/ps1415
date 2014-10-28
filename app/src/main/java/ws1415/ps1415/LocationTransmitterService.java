package ws1415.ps1415;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocationTransmitterService extends Service {
    public LocationTransmitterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
