package ws1415.common.controller;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventMetaData;
import com.skatenight.skatenightAPI.model.Text;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testet die Methoden des EventController.
 * @author Richard Schulze
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE, emulateSdk = 18)
public class EventControllerTest {
    private Event testevent1;

    @Before
    public void setup() throws IOException {
        List<String> images = new LinkedList<>();
        images.add("img1.png");
        images.add("img2.png");
        images.add("img3.png");
        testevent1 = ServiceProvider.getService().eventEndpoint().createEvent("icon.png", "Testevent #1",
                new DateTime(new Date()), "header.png", "Schlossplatz", 200, images,
                new Text().setValue("Beschreibungstext")).execute();
    }

    @Test
    public void testListEventsMetaData() {
        EventController.listEventsMetaData(new ExtendedTaskDelegateAdapter<Void, List<EventMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<EventMetaData> events) {
                assertNotNull("Es existieren keine Events auf dem Server", events);
                // Prüfen, ob die Metadaten des Testevents in den abgerufenen Metadaten enthalten sind
                for (EventMetaData metaData : events) {
                    if (metaData.getEventId().equals(testevent1.getId())) {
                        assertEquals("Der Titel der abgerufenen Metadaten stimmt nicht mit dem Titel des Testevents überein",
                                testevent1.getMetaData().getTitle(), metaData.getTitle());
                        assertEquals("Das icon der abgerufenen Metadaten stimmt nicht mit dem Icon des Testevents überein",
                                testevent1.getMetaData().getIcon(), metaData.getIcon());
                        assertEquals("Das Datum der abgerufenen Metadaten stimmt nicht mit dem Datum des Testevents überein",
                                testevent1.getMetaData().getDate(), metaData.getDate());

                        return;
                    }
                }
                fail("Die Eventmetadaten enthalten das Testevent nicht");
            }
        });
    }

    @Test
    public void testGetEvent() throws InterruptedException {
        EventController.getEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
            public void taskDidFinish(ExtendedTask task, Event event) {
                assertNotNull(event);
                assertEquals("Die Id der abgerufenen Metadaten stimmt nicht mit der Id des Testevents überein",
                        testevent1.getMetaData().getId(), event.getMetaData().getId());
                assertEquals("Der Titel der abgerufenen Metadaten stimmt nicht mit dem Titel des Testevents überein",
                        testevent1.getMetaData().getTitle(), event.getMetaData().getTitle());
                assertEquals("Das Icon der abgerufenen Metadaten stimmt nicht mit dem Icon des Testevents überein",
                        testevent1.getMetaData().getIcon(), event.getMetaData().getIcon());
                assertEquals("Das Datum der abgerufenen Metadaten stimmt nicht mit dem Datum des Testevents überein",
                        testevent1.getMetaData().getDate(), event.getMetaData().getDate());

            }
        }, testevent1.getMetaData().getId(), testevent1.getId());
    }

    @After
    public void teardown() throws IOException {
        ServiceProvider.getService().eventEndpoint().deleteEvent(testevent1.getMetaData().getId()).execute();
        testevent1 = null;
    }

}
