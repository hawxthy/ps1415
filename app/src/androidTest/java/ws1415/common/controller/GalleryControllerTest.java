package ws1415.common.controller;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Gallery;
import com.skatenight.skatenightAPI.model.Picture;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

/**
 * Testet die Methoden des Gallery-Controllers.
 * @author Richard Schulze
 */
public class GalleryControllerTest extends AuthenticatedAndroidTestCase {
    // Enthält Bilder, die in der tearDown-Methode des Tests gelöscht werden sollen
    private List<Picture> picturesToDelete = new LinkedList<>();

    private Picture picture1;

    // Testgallery inklusive Event und Route
    private Route route;
    private Event event;
    private Gallery testGallery;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Sicherstellen, dass der eingeloggte Benutzer auf dem Server angemeldet ist und ein Admin ist
        if (!ServiceProvider.getService().userEndpoint().existsUser(ServiceProvider.getEmail()).execute().getValue()) {
            ServiceProvider.getService().userEndpoint().createUser(ServiceProvider.getEmail()).execute();
        }
        assertTrue("user has to be an admin", ServiceProvider.getService().roleEndpoint().isAdmin(ServiceProvider.getEmail()).execute().getValue());

        final CountDownLatch signal = new CountDownLatch(1);
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream("image/test.png");
            assertNotNull("test file could not be found", is);

            GalleryController.uploadPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Picture picture) {
                    picture1 = picture;
                    signal.countDown();
                }

                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    fail(message);
                }
            }, is, "Picture 1", "Das erste Testbild");
            assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
        } finally {
            if (is != null) {
                is.close();
            }
        }

        // Testroute für das Testevent erstellen
        route = new Route();
        route.setLength("5 km");
        route.setName("Route 1");
        // TODO Routenpunkte, Wegpunkte und Routendaten
        route = ServiceProvider.getService().routeEndpoint().addRoute(route).execute();

        // Testevent erstellen, das als Container für die Gallery dient
        event = new Event();
        event.setTitle("Container-Event");
        event.setDate(new DateTime(1431442800000l));
        event.setRouteFieldFirst(19);
        event.setRouteFieldLast(23);
        event.setDescription(new Text().setValue("Dient als Container für die zu testende Gallery"));
        event.setMeetingPlace("Ort");
        event.setFee(100);
        event.setRoute(route);
        event = ServiceProvider.getService().eventEndpoint().createEvent(event).execute();

        testGallery = new Gallery();
        testGallery.setTitle("Testgallery");
        testGallery.setContainerClass(Event.class.getSimpleName());
        testGallery.setContainerId(event.getId());
        testGallery = ServiceProvider.getService().galleryEndpoint().createGallery(testGallery).execute();
    }

    // TODO getGallery() testen

    public void testCreateGallery() throws InterruptedException, IOException {
        // Testroute für das Testevent erstellen
        Route route = new Route();
        route.setLength("5 km");
        route.setName("Route 1");
        // TODO Routenpunkte, Wegpunkte und Routendaten
        route = ServiceProvider.getService().routeEndpoint().addRoute(route).execute();

        // Testevent erstellen, das als Container für die Gallery dient
        Event event = new Event();
        event.setTitle("Container-Event");
        event.setDate(new DateTime(1431442800000l));
        event.setRouteFieldFirst(19);
        event.setRouteFieldLast(23);
        event.setDescription(new Text().setValue("Dient als Container für die zu testende Gallery"));
        event.setMeetingPlace("Ort");
        event.setFee(100);
        event.setRoute(route);
        event = ServiceProvider.getService().eventEndpoint().createEvent(event).execute();
        final Long eventId = event.getId();

        final String TEST_TITLE = "Testgallery";

        final Gallery neueGallery = new Gallery();
        neueGallery.setTitle(TEST_TITLE);
        neueGallery.setContainerClass(Event.class.getSimpleName());
        neueGallery.setContainerId(event.getId());

        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.createGallery(new ExtendedTaskDelegateAdapter<Void, Gallery>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Gallery gallery) {
                assertNotNull("gallery is null", gallery);
                assertNotNull("gallery was not saved", gallery.getId());
                assertEquals("title", TEST_TITLE, gallery.getTitle());
                assertEquals("container class", Event.class.getSimpleName(), gallery.getContainerClass());
                assertEquals("container id", eventId, gallery.getContainerId());

                // ID der Gallery setzen, damit diese in der Testmethode gelöscht werden kann
                neueGallery.setId(gallery.getId());

                signal.countDown();
            }
        }, neueGallery);
        signal.await(10, TimeUnit.SECONDS);

        ServiceProvider.getService().galleryEndpoint().deleteGallery(neueGallery.getId()).execute();
        ServiceProvider.getService().eventEndpoint().deleteEvent(event.getId()).execute();
        ServiceProvider.getService().routeEndpoint().deleteRoute(route.getId()).execute();
    }

    public void testEditGallery() throws InterruptedException {
        final String TEST_TITLE = "Angepasster Titel";

        testGallery.setTitle(TEST_TITLE);

        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.editGallery(new ExtendedTaskDelegateAdapter<Void, Gallery>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Gallery gallery) {
                assertNotNull("gallery is null", gallery);
                assertEquals("id changed", testGallery.getId(), gallery.getId());
                assertEquals("title", TEST_TITLE, gallery.getTitle());
                assertEquals("container class", testGallery.getContainerClass(), gallery.getContainerClass());
                assertEquals("container id", testGallery.getContainerId(), gallery.getContainerId());

                signal.countDown();
            }
        }, testGallery);
        signal.await(10, TimeUnit.SECONDS);
    }

    public void testDeleteGallery() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.deleteGallery(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, testGallery.getId());
        signal.await(10, TimeUnit.SECONDS);

        // Beim Abrufen der Testgallery sollte nun ein Fehler auftreten
        try {
            ServiceProvider.getService().galleryEndpoint().getGallery(testGallery.getId()).execute();
        } catch (IOException e) {
            // Testgallery wurde erfolgreich gelöscht: auf null setzen, damit in der tearDown-Methode()
            // nicht erneut gelöscht wird
            testGallery = null;
            return;
        }
        fail("gallery was not deleted");
    }

    public void testUploadImage() throws IOException, InterruptedException {
        final String TEST_TITEL = "Testtitel";
        final String TEST_BESCHREIBUNG = "Testbeschreibung";

        final CountDownLatch signal = new CountDownLatch(1);
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream("image/test.png");
            assertNotNull("test file could not be found", is);

            GalleryController.uploadPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Picture picture) {
                    assertNotNull("picture is null", picture);
                    assertNotNull("id is null", picture.getId());

                    picturesToDelete.add(picture);

                    assertEquals("title", TEST_TITEL, picture.getTitle());
                    assertEquals("description", TEST_BESCHREIBUNG, picture.getDescription().getValue());
                    assertNotNull("picture does not have a blob key", picture.getImageBlobKey());

                    signal.countDown();
                }

                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    fail(message);
                }
            }, is, TEST_TITEL, TEST_BESCHREIBUNG);
            assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public void testGetImage() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.getPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Picture picture) {
                assertNotNull("picture is null", picture);
                assertEquals("id", picture1.getId(), picture.getId());
                assertEquals("title", picture1.getTitle(), picture.getTitle());
                assertEquals("date", picture1.getDate().getValue(), picture.getDate().getValue());
                assertEquals("uploader", picture1.getUploader(), picture.getUploader());
                assertEquals("description", picture1.getDescription().getValue(), picture.getDescription().getValue());
                assertEquals("ratings", picture1.getRatings(), picture.getRatings());
                assertEquals("avgRating", picture1.getAvgRating(), picture.getAvgRating());
                assertEquals("imageBlobKey", picture1.getImageBlobKey(), picture.getImageBlobKey());
                signal.countDown();
            }
        }, picture1.getId());
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
    }

    public void testDeleteImage() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.deletePicture(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, picture1.getId());
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));

        // Beim Versuch das Bild abzurufen sollte nun ein Fehler auftreten
        try {
            ServiceProvider.getService().galleryEndpoint().getPicture(picture1.getId()).execute();
        } catch (IOException e) {
            // Falls ein Fehler abgefangen wurde, dann Test als "bestanden" beenden
            return;
        }
        // Sonst Test fehlschlagen lassen
        fail("picture1 was not deleted");
    }

    public void testRateImage() throws InterruptedException, IOException {
        assertTrue("not enough test accounts on device", getAccountCount() >= 2);

        // Speichert die Bewertungen, die von den Accounts mit Index 0 und 1 abgegeben werden
        int[] ratings = new int[] {2, 5};

        // Zwei Bewertungen von unterschiedlichen Accounts für das Bild erstellen
        for (int i = 0; i < ratings.length; i++) {
            changeAccount(i);
            final CountDownLatch signal = new CountDownLatch(1);
            GalleryController.ratePicture(new ExtendedTaskDelegateAdapter<Void, Void>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Void aVoid) {
                    signal.countDown();
                }
            }, picture1.getId(), ratings[i]);
            assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
        }

        // Bild neu abrufen und die Bewertungen prüfen
        Picture picture = ServiceProvider.getService().galleryEndpoint().getPicture(picture1.getId()).execute();
        assertNotNull("no ratings found", picture.getRatings());
        assertNotNull("no average rating found", picture.getAvgRating());

        assertEquals("wrong count of ratings", ratings.length, picture.getRatings().size());
        for (int i = 0; i < ratings.length; i++) {
            assertNotNull("no rating for " + getAccountMail(i), picture.getRatings().get(getAccountMail(i)));
            assertEquals("wrong rating for account " + getAccountMail(i), ratings[i],
                    ((BigDecimal) picture.getRatings().get(getAccountMail(i))).intValue());
        }

        // Erwartete durchschnittliche Bewertung berechnen
        double avgRating = 0.0;
        for (int i : ratings) {
            avgRating += i;
        }
        avgRating /= ratings.length;
        assertEquals("wrong average rating", avgRating, picture.getAvgRating());
    }

    public void tearDown() throws Exception {
        super.tearDown();

        // Sicherstellen, dass der Benutzer, der die Bilder hochgeladen hat, angemeldet ist
        changeAccount(0);

        for (Picture p : picturesToDelete) {
            ServiceProvider.getService().galleryEndpoint().deletePicture(p.getId()).execute();
        }
        picturesToDelete = new LinkedList<>();

        if (picture1 != null) {
            ServiceProvider.getService().galleryEndpoint().deletePicture(picture1.getId()).execute();
            picture1 = null;
        }

        if (testGallery != null) {
            ServiceProvider.getService().galleryEndpoint().deleteGallery(testGallery.getId());
            testGallery = null;
        }
        if (event != null) {
            ServiceProvider.getService().eventEndpoint().deleteEvent(event.getId()).execute();
            event = null;
        }
        if (route != null) {
            ServiceProvider.getService().routeEndpoint().deleteRoute(route.getId()).execute();
            route = null;
        }
    }
}
