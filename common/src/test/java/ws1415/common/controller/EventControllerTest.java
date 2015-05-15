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
import static org.junit.Assert.assertNull;
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
        // TODO Testevent um Bilder und Route erweitern
        testevent1 = new Event();
        testevent1.setTitle("Testevent #1");
        // 12.05.2015 17:00 Uhr
        testevent1.setDate(new DateTime(1431442800000l));
        testevent1.setRouteFieldFirst(2);
        testevent1.setRouteFieldLast(3);
        testevent1.setDescription(new Text().setValue("Hier steht die Beschreibung des Testevents"));
        testevent1.setMeetingPlace("Münster, Ludgerikreisel");
        testevent1.setFee(200);

        testevent1 = ServiceProvider.getService().eventEndpoint().createEvent(testevent1).execute();
    }

    @Test
    public void testListEventsMetaData() {
        EventController.listEventsMetaData(new ExtendedTaskDelegateAdapter<Void, List<EventMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<EventMetaData> events) {
                assertNotNull("Es existieren keine Events auf dem Server", events);
                // Prüfen, ob die Metadaten des Testevents in den abgerufenen Metadaten enthalten sind
                for (EventMetaData metaData : events) {
                    if (metaData.getId().equals(testevent1.getId())) {
                        assertEquals("Der Titel der abgerufenen Metadaten stimmt nicht mit dem Titel des Testevents überein",
                                testevent1.getTitle(), metaData.getTitle());
                        assertEquals("Das icon der abgerufenen Metadaten stimmt nicht mit dem Icon des Testevents überein",
                                testevent1.getIcon(), metaData.getIcon());
                        assertEquals("Das Datum der abgerufenen Metadaten stimmt nicht mit dem Datum des Testevents überein",
                                testevent1.getDate(), metaData.getDate());

                        return;
                    }
                }
                fail("Die Eventmetadaten enthalten das Testevent nicht");
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        });
    }

    @Test
    public void testGetEvent() throws InterruptedException {
        EventController.getEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
            public void taskDidFinish(ExtendedTask task, Event event) {
                assertNotNull(event);
                assertEquals("id", testevent1.getId(), event.getId());
                assertEquals("title", testevent1.getTitle(), event.getTitle());
                assertEquals("icon", testevent1.getIcon(), event.getIcon());
                assertEquals("date", testevent1.getDate(), event.getDate());
                assertEquals("routeFieldFirst", testevent1.getRouteFieldFirst(), event.getRouteFieldFirst());
                assertEquals("routeFieldLast", testevent1.getRouteFieldLast(), event.getRouteFieldLast());
                assertEquals("notificationSend", testevent1.getNotificationSend(), event.getNotificationSend());
                assertEquals("headerImage", testevent1.getHeaderImage(), event.getHeaderImage());
                assertEquals("description", testevent1.getDescription(), event.getDescription());
                assertEquals("meetingPlace", testevent1.getMeetingPlace(), event.getMeetingPlace());
                assertEquals("fee", testevent1.getFee(), event.getFee());

                if (testevent1.getMemberList() != null) {
                    assertTrue(testevent1.getMemberList().containsAll(event.getMemberList()));
                    assertTrue(event.getMemberList().containsAll(testevent1.getMemberList()));
                } else {
                    assertNull(event.getMemberList());
                }
                if (testevent1.getImages() != null) {
                    assertTrue(testevent1.getImages().containsAll(event.getImages()));
                    assertTrue(event.getImages().containsAll(testevent1.getImages()));
                } else {
                    assertNull(event.getImages());
                }

                assertEquals("route", testevent1.getRoute(), event.getRoute());
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        }, testevent1.getId());
    }

    @Test
    public void testCreateEvent() {
        final Event neuesEvent = new Event();
        neuesEvent.setTitle("Neues Event");
        neuesEvent.setDate(new DateTime(1431419000000l));
        neuesEvent.setRouteFieldFirst(19);
        neuesEvent.setRouteFieldLast(23);
        neuesEvent.setDescription(new Text().setValue("Hier steht die Beschreibung des neuen Events"));
        neuesEvent.setMeetingPlace("Münster, Hauptbahnhof");
        neuesEvent.setFee(100);

        EventController.createEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Event event) {
                assertNotNull(event);
                assertNotNull("id", event.getId());
                assertEquals("title", neuesEvent.getTitle(), event.getTitle());
                assertEquals("icon", neuesEvent.getIcon(), event.getIcon());
                assertEquals("date", neuesEvent.getDate(), event.getDate());
                assertEquals("routeFieldFirst", neuesEvent.getRouteFieldFirst(), event.getRouteFieldFirst());
                assertEquals("routeFieldLast", neuesEvent.getRouteFieldLast(), event.getRouteFieldLast());
                assertEquals("notificationSend", neuesEvent.getNotificationSend(), event.getNotificationSend());
                assertEquals("headerImage", neuesEvent.getHeaderImage(), event.getHeaderImage());
                assertEquals("description", neuesEvent.getDescription(), event.getDescription());
                assertEquals("meetingPlace", neuesEvent.getMeetingPlace(), event.getMeetingPlace());
                assertEquals("fee", neuesEvent.getFee(), event.getFee());

                if (neuesEvent.getMemberList() != null) {
                    assertTrue(neuesEvent.getMemberList().containsAll(event.getMemberList()));
                    assertTrue(event.getMemberList().containsAll(neuesEvent.getMemberList()));
                } else {
                    assertNull(event.getMemberList());
                }
                if (neuesEvent.getImages() != null) {
                    assertTrue(neuesEvent.getImages().containsAll(event.getImages()));
                    assertTrue(event.getImages().containsAll(neuesEvent.getImages()));
                } else {
                    assertNull(event.getImages());
                }

                assertEquals("route", neuesEvent.getRoute(), event.getRoute());
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        }, neuesEvent);
    }

    @After
    public void teardown() throws IOException {
        ServiceProvider.getService().eventEndpoint().deleteEvent(testevent1.getId()).execute();
        testevent1 = null;
    }

}
