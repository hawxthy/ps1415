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
 * ACHTUNG: Test läuft über den normalen Google-Server, da das @-Zeichen bei einer Anfrage an den
 * lokalen Test-Server nicht richtig kodiert wird. Das Problem scheint ein Bug im Testserver von
 * Google zu sein.
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
                testLatitude, testLongitude, (long) 0).execute();
    }

    /**
     * Prüft, ob das durch den QueryMemberTask abgerufene Member-Objekt die Testdaten enthält.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void testTask() throws ExecutionException, InterruptedException {
        QueryMemberTask task = new QueryMemberTask();
        Member m = task.execute(new ShowRouteActivity() {
            @Override
            public void drawMembers(Member m) {
                // Methode mit leerem Rumpf überschreiben, da der Callback für den Test nicht relevant ist
            }
            @Override
            public SharedPreferences getSharedPreferences(String tag, int mode) {
                // Mock shared preferences
                return new SharedPreferences() {
                    @Override
                    public Map<String, ?> getAll() {
                        return null;
                    }
                    @Override
                    public String getString(String key, String defValue) {
                        return testMail;
                    }
                    @Override
                    public Set<String> getStringSet(String key, Set<String> defValues) {
                        return null;
                    }
                    @Override
                    public int getInt(String key, int defValue) {
                        return 0;
                    }
                    @Override
                    public long getLong(String key, long defValue) {
                        return 0;
                    }
                    @Override
                    public float getFloat(String key, float defValue) {
                        return 0;
                    }
                    @Override
                    public boolean getBoolean(String key, boolean defValue) {
                        return false;
                    }
                    @Override
                    public boolean contains(String key) {
                        return false;
                    }
                    @Override
                    public Editor edit() {
                        return null;
                    }
                    @Override
                    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

                    }
                    @Override
                    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

                    }
                };
            }
        }).get();
        assertNotNull("member is null", m);
        assertEquals("wrong member email", testMail, m.getEmail());
        // Nach Implementierung des Backends wird als Name die Mail-Adresse verwendet:
        assertEquals("wrong name", testMail, m.getName());
        assertEquals("wrong member latitude", testLatitude, m.getLatitude());
        assertEquals("wrong member longitude", testLongitude, m.getLongitude());
    }
}
