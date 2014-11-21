package ws1415.ps1415.task;

import android.test.AndroidTestCase;

import com.skatenight.skatenightAPI.model.Member;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.task.UpdateLocationTask;

/**
 * ACHTUNG: Test läuft über den normalen Google-Server, da das @-Zeichen bei einer Anfrage an den
 * lokalen Test-Server nicht richtig kodiert wird. Das Problem scheint ein Bug im Testserver von
 * Google zu sein.
 *
 * Created by Richard on 07.11.2014.
 */
public class UpdateLocationTaskTest extends AndroidTestCase {
    private static final String testMail = "max@mustermann.de";
    private static final String testLocation = "{rd|Ha`lm@";

    public void setUp() throws Exception {
        super.setUp();

        ServiceProvider.setupProductionServerConnection();
    }

    /**
     * Überträgt die Testposition an den Server und prüft, ob die Daten korrekt abgespeichert
     * wurden.
     */
    public void testTask() throws ExecutionException, InterruptedException, IOException {
        UpdateLocationTask task = new UpdateLocationTask();
        task.execute(testMail, testLocation).get();
        Member m = ServiceProvider.getService().skatenightServerEndpoint().getMember(testMail)
                .execute();
        assertNotNull("member is null", m);
        assertEquals("wrong location", testLocation, m.getLocation());
    }
}
