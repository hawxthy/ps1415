package ws1415.ps1415.task;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import com.skatenight.skatenightAPI.model.Member;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.ShowRouteActivity;

/**
 * Prüft, ob das Member-Objekt für die Test-Email mit den richtigen Parametern heruntergeladen wird,
 * nachdem es zuvor von dem Test aktualisiert wurde.
 *
 * Created by Richard on 07.11.2014.
 */
public class QueryMemberTaskTest extends AndroidTestCase {
    private static final String testMail = "max@mustermann.de";
    private static final double testLongitude = 1.05;
    private static final double testLatitude = 7.23;

    /**
     * Stellt sicher, dass auf dem Server ein Member-Objekt mit der angegebenen Mail und Position
     * existiert.
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();

        // Member-Objekt auf dem Server erstellen lassen
        ServiceProvider.getService().skatenightServerEndpoint().updateMemberLocation(testMail,
                testLatitude, testLongitude, (long) 1).execute();
    }

    /**
     * Prüft, ob das durch den QueryMemberTask abgerufene Member-Objekt die Testdaten enthält.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void testTask() throws ExecutionException, InterruptedException {
        QueryMemberTask task = new QueryMemberTask(null);
        Member m = task.execute(testMail).get();
        assertNotNull("member is null", m);
        assertEquals("wrong member email", testMail, m.getEmail());
        // Nach Implementierung des Backends wird als Name die Mail-Adresse verwendet:
        assertEquals("wrong name", testMail, m.getName());
        assertEquals("wrong member latitude", testLatitude, m.getLatitude());
        assertEquals("wrong member longitude", testLongitude, m.getLongitude());
    }
}
