package ws1415.common.controller;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.EventMetaData;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.model.EventRole;
import ws1415.common.model.Role;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

/**
 * Testet die Methoden des EventController.
 * @author Richard Schulze
 */
public class EventControllerTest extends AuthenticatedAndroidTestCase {
    private Route route1;
    private Route route2;

    private Event testevent1;

    // Speichert eine Liste von Events, die während der test erstellt wurden und in der tearDown-Methode
    // gelöscht werden sollen
    private List<Event> eventsToDelete = new LinkedList<>();

    public void setUp() throws Exception {
        super.setUp();

        // Sicherstellen, dass die EndUser-Objekte für die im Test verwendeten Accounts existieren
        // Für diesen Test werden 2 Accounts benötigt, der erste Account muss ein Admin sein
        for (int i = 0; i < 2; i++) {
            if (!ServiceProvider.getService().userEndpoint().existsUser(getAccountMail(i)).execute().getValue()) {
                ServiceProvider.getService().userEndpoint().createUser(getAccountMail(i)).execute();
            }
        }
        if (!ServiceProvider.getService().roleEndpoint().isAdmin(getAccountMail(0)).execute().getValue()) {
            ServiceProvider.getService().roleEndpoint().assignGlobalRole(Role.ADMIN.getId(), getAccountMail(0)).execute();
        }


        route1 = new Route();
        route1.setLength("5 km");
        route1.setName("Route 1");
        // TODO Routenpunkte, Wegpunkte und Routendaten
        route1 = ServiceProvider.getService().routeEndpoint().addRoute(route1).execute();

        route2 = new Route();
        route2.setLength("10 km");
        route2.setName("Route 2");
        // TODO Routenpunkte, Wegpunkte und Routendaten
        route2 = ServiceProvider.getService().routeEndpoint().addRoute(route2).execute();

        // TODO Testevent um Bilder erweitern
        testevent1 = new Event();
        testevent1.setTitle("Testevent #1");
        // 12.05.2015 17:00 Uhr
        testevent1.setDate(new DateTime(1431442800000l));
        testevent1.setRouteFieldFirst(2);
        testevent1.setRouteFieldLast(3);
        testevent1.setDescription(new Text().setValue("Hier steht die Beschreibung des Testevents"));
        testevent1.setMeetingPlace("Münster, Ludgerikreisel");
        testevent1.setFee(200);
        testevent1.setRoute(route1);

        testevent1 = ServiceProvider.getService().eventEndpoint().createEvent(testevent1).execute();
    }

    public void testAssignRole() throws IOException, InterruptedException {
        // Zweiten Benutzer dem Testevent beitreten lassen
        changeAccount(1);
        ServiceProvider.getService().eventEndpoint().joinEvent(testevent1.getId()).execute();

        // Zum Veranstalter-Account wechseln
        changeAccount(0);
        // Testevent mit den aktualisierten Teilnehmerdaten erneut abrufen
        testevent1 = ServiceProvider.getService().eventEndpoint().getEvent(testevent1.getId()).execute();

        assertEquals("wrong initial role for " + getAccountMail(1), EventRole.PARTICIPANT.name(), testevent1.getMemberList().get(getAccountMail(1)));

        final CountDownLatch signal = new CountDownLatch(1);
        EventController.assignRole(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, testevent1.getId(), getAccountMail(1), EventRole.MARSHALL);
        signal.await(10, TimeUnit.SECONDS);

        // Testevent mit den aktualisierten Teilnehmerdaten erneut abrufen
        testevent1 = ServiceProvider.getService().eventEndpoint().getEvent(testevent1.getId()).execute();
        assertEquals("wrong role change for " + getAccountMail(1), EventRole.MARSHALL.name(), testevent1.getMemberList().get(getAccountMail(1)));
    }

    public void testListEventsMetaData() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);

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

                        signal.countDown();
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
        signal.await(10, TimeUnit.SECONDS);
    }

    public void testGetEvent() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);

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
                    assertTrue(testevent1.getMemberList().keySet().containsAll(event.getMemberList().keySet()));
                    assertTrue(event.getMemberList().keySet().containsAll(testevent1.getMemberList().keySet()));
                } else {
                    assertNull(event.getMemberList());
                }
                if (testevent1.getImages() != null) {
                    assertTrue(testevent1.getImages().containsAll(event.getImages()));
                    assertTrue(event.getImages().containsAll(testevent1.getImages()));
                } else {
                    assertNull(event.getImages());
                }

                assertNotNull("route", event.getRoute());
                assertEquals("route id", testevent1.getRoute().getId(), event.getRoute().getId());
                assertEquals("route name", testevent1.getRoute().getName(), event.getRoute().getName());
                assertEquals("route length", testevent1.getRoute().getLength(), event.getRoute().getLength());
                // TODO
//                assertEquals("route data", testevent1.getRoute().getRouteData().getValue(), event.getRoute().getRouteData().getValue());
//                assertEquals("route points", testevent1.getRoute().getRoutePoints(), event.getRoute().getRoutePoints());
//                assertEquals("route waypoints", testevent1.getRoute().getWaypoints(), event.getRoute().getWaypoints());

                signal.countDown();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        }, testevent1.getId());
        signal.await(10, TimeUnit.SECONDS);
    }

    public void testCreateEvent() throws InterruptedException, IOException {
        final CountDownLatch signal = new CountDownLatch(1);

        final Event neuesEvent = new Event();
        neuesEvent.setTitle("Neues Event");
        neuesEvent.setDate(new DateTime(1431442800000l));
        neuesEvent.setRouteFieldFirst(19);
        neuesEvent.setRouteFieldLast(23);
        neuesEvent.setDescription(new Text().setValue("Hier steht die Beschreibung des neuen Events"));
        neuesEvent.setMeetingPlace("Münster, Hauptbahnhof");
        neuesEvent.setFee(100);
        neuesEvent.setRoute(route2);

        EventController.createEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Event event) {
                assertNotNull(event);
                assertNotNull("id", event.getId());
                assertEquals("title", neuesEvent.getTitle(), event.getTitle());
                assertEquals("icon", neuesEvent.getIcon(), event.getIcon());
                assertEquals("date", neuesEvent.getDate().getValue(), event.getDate().getValue());
                assertEquals("routeFieldFirst", neuesEvent.getRouteFieldFirst(), event.getRouteFieldFirst());
                assertEquals("routeFieldLast", neuesEvent.getRouteFieldLast(), event.getRouteFieldLast());
                assertEquals("headerImage", neuesEvent.getHeaderImage(), event.getHeaderImage());
                assertEquals("description", neuesEvent.getDescription(), event.getDescription());
                assertEquals("meetingPlace", neuesEvent.getMeetingPlace(), event.getMeetingPlace());
                assertEquals("fee", neuesEvent.getFee(), event.getFee());

                assertTrue("host not contained in member list", event.getMemberList().containsKey(ServiceProvider.getEmail()));
                assertEquals("host has wrong event role in created event", EventRole.HOST.name(), event.getMemberList().get(ServiceProvider.getEmail()));
                if (neuesEvent.getImages() != null) {
                    assertTrue(neuesEvent.getImages().containsAll(event.getImages()));
                    assertTrue(event.getImages().containsAll(neuesEvent.getImages()));
                } else {
                    assertNull(event.getImages());
                }

                assertNotNull("route", event.getRoute());
                assertEquals("route id", neuesEvent.getRoute().getId(), event.getRoute().getId());
                assertEquals("route name", neuesEvent.getRoute().getName(), event.getRoute().getName());
                assertEquals("route length", neuesEvent.getRoute().getLength(), event.getRoute().getLength());
                // TODO
//                assertEquals("route data", neuesEvent.getRoute().getRouteData().getValue(), event.getRoute().getRouteData().getValue());
//                assertEquals("route points", neuesEvent.getRoute().getRoutePoints(), event.getRoute().getRoutePoints());
//                assertEquals("route waypoints", neuesEvent.getRoute().getWaypoints(), event.getRoute().getWaypoints());

                eventsToDelete.add(event);
                signal.countDown();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        }, neuesEvent);
        signal.await(10, TimeUnit.SECONDS);
    }

    public void testEditEvent() throws InterruptedException {
        // Event editieren
        // TODO Testevent um Bilder erweitern
        testevent1.setTitle("Editierter Titel");
        // 12.05.2015 17:00 Uhr
        testevent1.setDate(new DateTime(1441242800000l));
        testevent1.setRouteFieldFirst(4);
        testevent1.setRouteFieldLast(10);
        testevent1.setDescription(new Text().setValue("Hier steht eine veränderte Beschreibung des Testevents"));
        testevent1.setMeetingPlace("Hamburg");
        testevent1.setFee(150);
        testevent1.setRoute(route2);

        final CountDownLatch signal = new CountDownLatch(1);
        EventController.editEvent(new ExtendedTaskDelegateAdapter<Void, Event>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Event event) {
                assertNotNull(event);
                assertEquals("id", testevent1.getId(), event.getId());
                assertEquals("title", testevent1.getTitle(), event.getTitle());
                assertEquals("icon", testevent1.getIcon(), event.getIcon());
                assertEquals("date", testevent1.getDate().getValue(), event.getDate().getValue());
                assertEquals("routeFieldFirst", testevent1.getRouteFieldFirst(), event.getRouteFieldFirst());
                assertEquals("routeFieldLast", testevent1.getRouteFieldLast(), event.getRouteFieldLast());
                assertEquals("headerImage", testevent1.getHeaderImage(), event.getHeaderImage());
                assertEquals("description", testevent1.getDescription(), event.getDescription());
                assertEquals("meetingPlace", testevent1.getMeetingPlace(), event.getMeetingPlace());
                assertEquals("fee", testevent1.getFee(), event.getFee());

                signal.countDown();
            }
        }, testevent1);
        signal.await(10, TimeUnit.SECONDS);
    }

    public void testDeleteEvent() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        EventController.deleteEvent(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, testevent1.getId());
        signal.await(10, TimeUnit.SECONDS);

        try {
            ServiceProvider.getService().eventEndpoint().getEvent(testevent1.getId()).execute();
        } catch (IOException e) {
            // Das Abrufen des Events sollte einen Fehler verursachen, da es nicht länger existiert
            e.printStackTrace();
            return;
        }
        // Falls kein Fehler beim Abrufen des Events geworfen wird, ist das Löschen fehlgeschlagen
        fail("event could not be deleted");
    }

    public void testJoinEvent() throws InterruptedException {
        assertTrue("Test kann nicht durchgeführt werden, da mindestens 2 Accounts auf dem Testgerät benötigt werden",
                getAccountCount() >= 2);

        final CountDownLatch signal = new CountDownLatch(1);
        // Account wechseln, damit ein zweiter Benutzer dem Event beitritt
        changeAccount(1);
        EventController.joinEvent(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        }, testevent1.getId());
        signal.await(10, TimeUnit.SECONDS);

        // Event neu abrufen, damit die Teilnehmerdaten aktualisiert sind
        Event event = null;
        try {
            event = ServiceProvider.getService().eventEndpoint().getEvent(testevent1.getId()).execute();
        } catch (IOException e) {
            fail("Event konnte nicht erneut vom Server abgerufen werden");
            throw new RuntimeException(e);
        }

        assertTrue("Teilnehmer ist dem Event nicht beigetreten", event.getMemberList().containsKey(ServiceProvider.getEmail()));
    }

    public void testLeaveEvent() throws InterruptedException, IOException {
        assertTrue("Test kann nicht durchgeführt werden, da mindestens 2 Accounts auf dem Testgerät benötigt werden",
                getAccountCount() >= 2);

        final CountDownLatch signal = new CountDownLatch(1);
        // Account wechseln, damit ein zweiter Benutzer dem Event beitritt
        changeAccount(1);
        // Dem Event zunächst beitreten. Damit dies synchron geschieht, wird direkt die Endpoint-Methode
        // über den ServiceProvider aufgerufen
        ServiceProvider.getService().eventEndpoint().joinEvent(testevent1.getId()).execute();
        // Event neu abrufen, damit die Teilnehmerdaten aktualisiert sind und sicherstellen, dass der
        // Benutzer angemeldet ist
        Event event = ServiceProvider.getService().eventEndpoint().getEvent(testevent1.getId()).execute();
        assertTrue("Teilnehmer ist dem Event nicht beigetreten", event.getMemberList().containsKey(ServiceProvider.getEmail()));

        EventController.leaveEvent(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        }, testevent1.getId());
        signal.await(10, TimeUnit.SECONDS);

        // Event neu abrufen, damit die Teilnehmerdaten aktualisiert sind
        try {
            event = ServiceProvider.getService().eventEndpoint().getEvent(testevent1.getId()).execute();
        } catch (IOException e) {
            fail("Event konnte nicht erneut vom Server abgerufen werden");
            throw new RuntimeException(e);
        }

        assertFalse("Teilnehmer hat das Event nicht verlassen", event.getMemberList().containsKey(ServiceProvider.getEmail()));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        // Auf den Admin-Account wechseln, damit die Testdaten gelöscht werden können
        changeAccount(0);

        ServiceProvider.getService().eventEndpoint().deleteEvent(testevent1.getId()).execute();
        testevent1 = null;
        for (Event e : eventsToDelete) {
            ServiceProvider.getService().eventEndpoint().deleteEvent(e.getId()).execute();
        }
        eventsToDelete = new LinkedList<>();

        ServiceProvider.getService().routeEndpoint().deleteRoute(route1.getId()).execute();
        route1 = null;
        ServiceProvider.getService().routeEndpoint().deleteRoute(route2.getId()).execute();
        route2 = null;
    }

}
