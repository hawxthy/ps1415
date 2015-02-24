package ws1415.ps1415.services;

import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import ws1415.ps1415.LocationTransmitterService;

/**
 * Testet den LocationTransmitterService.
 *
 * @author Tristan Rust
 *
 */
public class LocationTransmitterServiceTest extends ServiceTestCase<LocationTransmitterService> {

    public LocationTransmitterServiceTest() {
        super(LocationTransmitterService.class);
    }

    /**
     * Wird vor jeder Testmethode ausgeführt. Dient dazu, um den System Context zu erhalten.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Testet startup des Services.
     */
    @SmallTest
    public void testStartable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), LocationTransmitterService.class);
        startService(startIntent);
    }

    /**
     * Prüft, ob bindService funktioniert.
     */
    @SmallTest
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), LocationTransmitterService.class);
        IBinder service = bindService(startIntent);
    }

}



















