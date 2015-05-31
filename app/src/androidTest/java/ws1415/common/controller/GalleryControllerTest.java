package ws1415.common.controller;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.IOUtils;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Gallery;
import com.skatenight.skatenightAPI.model.GalleryMetaData;
import com.skatenight.skatenightAPI.model.Picture;
import com.skatenight.skatenightAPI.model.PictureData;
import com.skatenight.skatenightAPI.model.PictureFilter;
import com.skatenight.skatenightAPI.model.PictureMetaData;
import com.skatenight.skatenightAPI.model.PictureMetaDataList;
import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.model.PictureVisibility;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;

/**
 * Testet die Methoden des Gallery-Controllers.
 * @author Richard Schulze
 */
public class GalleryControllerTest extends AuthenticatedAndroidTestCase {
    private File testImage;

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

        // Die Testdatei über einen InputStream einlesen
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream("image/test.png");
            testImage = File.createTempFile("testimage", ".tmp");
            testImage.deleteOnExit();
            fos = new FileOutputStream(testImage);
            IOUtils.copy(is, fos);
            fos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }

        // Sicherstellen, dass der eingeloggte Benutzer auf dem Server angemeldet ist und ein Admin ist
        if (!ServiceProvider.getService().userEndpoint().existsUser(ServiceProvider.getEmail()).execute().getValue()) {
            ServiceProvider.getService().userEndpoint().createUser(ServiceProvider.getEmail()).set("firstName", "").set("lastName", "").execute();
        }
        assertTrue("user has to be an admin", ServiceProvider.getService().roleEndpoint().isAdmin(ServiceProvider.getEmail()).execute().getValue());

        final CountDownLatch signal = new CountDownLatch(1);
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
        }, testImage, "Picture 1", "Das erste Testbild", PictureVisibility.PUBLIC);
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));

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

    public void testGetGalleryMetaData() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.getGalleryMetaData(new ExtendedTaskDelegateAdapter<Void, GalleryMetaData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, GalleryMetaData galleryMetaData) {
                assertNotNull("GalleryMetaData", galleryMetaData);
                assertEquals("id", testGallery.getId(), galleryMetaData.getId());
                assertEquals("title", testGallery.getTitle(), galleryMetaData.getTitle());

                signal.countDown();
            }
        }, testGallery.getId());
        signal.await(10, TimeUnit.SECONDS);
    }

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
            ServiceProvider.getService().galleryEndpoint().getGalleryMetaData(testGallery.getId()).execute();
        } catch (IOException e) {
            // Testgallery wurde erfolgreich gelöscht: auf null setzen, damit in der tearDown-Methode()
            // nicht erneut gelöscht wird
            testGallery = null;
            return;
        }
        fail("gallery was not deleted");
    }

    /**
     * Testet das stückweise Abrufen (Paging) von Bildern.
     */
    public void testListPictures_Paging() throws InterruptedException, IOException {
        final int TEST_LIMIT = 2;

        // Weitere Testbilder hochladen
        final List<Picture> testPictures = new LinkedList<>();
        testPictures.add(picture1);
        for (int i = 2; i <= 5; i++) {
            final CountDownLatch signal = new CountDownLatch(1);
            GalleryController.uploadPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Picture picture) {
                    testPictures.add(picture);
                    signal.countDown();
                }

                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    fail(message);
                }
            }, testImage, "Picture " + i, "Das " + i + ". Testbild", PictureVisibility.PRIVATE);
            assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
        }
        picturesToDelete.addAll(testPictures);

        // Liste der Testbilder umdrehen, da diese sortiert nach Hochladedatum abgerufen werden
        Collections.reverse(testPictures);

        final PictureFilter filter = new PictureFilter();
        filter.setLimit(TEST_LIMIT);
        filter.setUserId(ServiceProvider.getEmail());
        for (int i = 0; i < 3; i++) {
            final int index = i;
            final CountDownLatch signal = new CountDownLatch(1);
            GalleryController.listPictures(new ExtendedTaskDelegateAdapter<Void, List<PictureMetaData>>() {
                @Override
                public void taskDidFinish(ExtendedTask task, List<PictureMetaData> pictureMetaDataList) {
                    assertNotNull("picture list", pictureMetaDataList);
                    int size = pictureMetaDataList.size();
                    if (index < 2) {
                        // Wenn noch nicht beim letzen Mal abrufen, dann müssen 2 Bilder enthalten sein
                        assertEquals("list size", 2, size);
                    } else {
                        // Beim letzten Mal wird nur ein Bild abgerufen
                        assertTrue("last picture was not fetched", size > 0);
                    }

                    // Prüfen, ob der richtige Teil der Testbilder im Ergebnis enthalten ist
                    for (int i = 0; i < (index == 2 ? 1 : size); i++) {
                        assertEquals("id for testpicture " + (index * TEST_LIMIT + i + 1),
                                testPictures.get(index * TEST_LIMIT + i).getId(),
                                pictureMetaDataList.get(i).getId());
                    }

                    signal.countDown();
                }
            }, filter);
            signal.await(10, TimeUnit.SECONDS);
        }
    }

    /**
     * Testet das Abrufen der Bilder eines Benutzers.
     */
    public void testListPictures_ForUser() throws IOException, InterruptedException {
        // Zum Test ein weiteres Bild unter einem anderen Benutzer hochladen
        changeAccount(1);
        final Picture[] picture2 = {null};
        final CountDownLatch signal1 = new CountDownLatch(1);
        GalleryController.uploadPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Picture picture) {
                picture2[0] = picture;
                signal1.countDown();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        }, testImage, "Picture 2", "Das zweite Testbild (von einem anderen Account hochgeladen).", PictureVisibility.PUBLIC);
        assertTrue("timeout reached", signal1.await(10, TimeUnit.SECONDS));

        // Beim Abrufen der Bilder für den eingeloggten Benutzer sollte das in setUp erstellte
        // Testbild nun nicht mit abgerufen werden
        PictureFilter filter = new PictureFilter();
        filter.setLimit(2);
        filter.setUserId(ServiceProvider.getEmail());
        final CountDownLatch signal2 = new CountDownLatch(1);
        GalleryController.listPictures(new ExtendedTaskDelegateAdapter<Void, List<PictureMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<PictureMetaData> pictureMetaDataList) {
                assertNotNull("no pictures fetched", pictureMetaDataList);
                assertFalse("no pictures fetched", pictureMetaDataList.isEmpty());
                for (PictureMetaData p : pictureMetaDataList) {
                    assertEquals("uploader is not the logged in user", ServiceProvider.getEmail(), p.getUploader());
                }
                signal2.countDown();
            }
        }, filter);
        boolean timeout = !signal2.await(10, TimeUnit.SECONDS);

        // Das erstellte Bild wieder löschen
        ServiceProvider.getService().galleryEndpoint().deletePicture(picture2[0].getId()).execute();

        assertFalse("timeout reached", timeout);
    }

    /**
     * Testet das Abrufen von Bildern mit verschiedenen Sichtbarkeitseinstellungen.
     */
    public void testListPictures_WithVisibility() throws InterruptedException, IOException {
        assertTrue("there have to be at least 2 accounts set up on the test device", getAccountCount() >= 2);
        // Sicherstellen, dass auch der zweite Benutzer ein User-Objekt auf dem Server hat
        // Für den ersten Benutzer wurde dies bereits in der setUp-Methode erledigt
        changeAccount(1);
        if (!ServiceProvider.getService().userEndpoint().existsUser(ServiceProvider.getEmail()).execute().getValue()) {
            ServiceProvider.getService().userEndpoint().createUser(ServiceProvider.getEmail()).set("firstName", "").set("lastName", "").execute();
        }
        changeAccount(0);

        // Weitere Bilder hochladen:
        //      Index 0: privates Bild
        //      Index 1: Bild für Freunde sichtbar
        //      Index 2: Öffentliches Bild
        final Picture[] pictures = new Picture[3];
        for (PictureVisibility visibility : new PictureVisibility[] {PictureVisibility.FRIENDS, PictureVisibility.PRIVATE}) {
            final CountDownLatch signal = new CountDownLatch(1);
            GalleryController.uploadPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Picture picture) {
                    for (int i = pictures.length - 2; i >= 0; i--) {
                        if (pictures[i] == null) {
                            pictures[i] = picture;
                            break;
                        }
                    }
                    picturesToDelete.add(picture);
                    signal.countDown();
                }
                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    fail(message);
                }
            }, testImage, "Testbild", "Testbild.", visibility);
            assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
        }
        pictures[2] = picture1;
        String uploader = ServiceProvider.getEmail();

        // Den Benutzer die eigenen Bilder abrufen lassen: Sollte alle Bilder liefern
        PictureFilter filter = new PictureFilter();
        filter.setLimit(3);
        filter.setUserId(uploader);
        final CountDownLatch signal1 = new CountDownLatch(1);
        GalleryController.listPictures(new ExtendedTaskDelegateAdapter<Void, List<PictureMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<PictureMetaData> pictureMetaDataList) {
                assertNotNull("no pictures fetched", pictureMetaDataList);
                assertEquals("wrong picture count", 3, pictureMetaDataList.size());
                for (int i = 0; i < pictures.length; i++) {
                    assertEquals("wrong picture for index " + i, pictures[i].getId(), pictureMetaDataList.get(i).getId());
                }
                signal1.countDown();
            }
        }, filter);
        assertTrue("timeout reached", signal1.await(10, TimeUnit.SECONDS));

        // Einen Freund die Bilder abrufen lassen: Sollte ein "leeres" Bild anstatt des privaten und
        // die anderen beide Bilder zurück geben
        // Sicherstellen, dass der eingeloggte Benutzer mit dem Ersteller der Bilder befreundet ist
        ServiceProvider.getService().userEndpoint().addFriend(ServiceProvider.getEmail(), getAccountMail(1)).execute();
        changeAccount(1);
        // Cursor zurücksetzen
        filter.setCursorString(null);
        final CountDownLatch signal2 = new CountDownLatch(1);
        GalleryController.listPictures(new ExtendedTaskDelegateAdapter<Void, List<PictureMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<PictureMetaData> pictureMetaDataList) {
                assertNotNull("no pictures fetched", pictureMetaDataList);
                assertEquals("wrong picture count", 3, pictureMetaDataList.size());
                assertEquals("id -1 expected", Long.valueOf(-1), pictureMetaDataList.get(0).getId());
                assertNull("empty picture(blob key) expected", pictureMetaDataList.get(0).getImageBlobKey());
                for (int i = 1; i < pictures.length; i++) {
                    assertEquals("wrong picture for index " + i, pictures[i].getId(), pictureMetaDataList.get(i).getId());
                }
                signal2.countDown();
            }
        }, filter);
        assertTrue("timeout reached", signal2.await(10, TimeUnit.SECONDS));

        // Einen Benutzer, der kein Freund ist, die Bilder abrufen lassen: Sollte nur das öffentliche
        // Bild und zwei "leere" Bilder liefern
        // Sicherstellen, dass der eingeloggte Benutzer nicht mit dem Ersteller der Bilder befreundet ist
        changeAccount(0);
        ServiceProvider.getService().userEndpoint().removeFriend(ServiceProvider.getEmail(), getAccountMail(1)).execute();
        changeAccount(1);
        // Cursor zurücksetzen
        filter.setCursorString(null);
        final CountDownLatch signal3 = new CountDownLatch(1);
        GalleryController.listPictures(new ExtendedTaskDelegateAdapter<Void, List<PictureMetaData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<PictureMetaData> pictureMetaDataList) {
                assertNotNull("no pictures fetched", pictureMetaDataList);
                assertEquals("wrong picture count", 3, pictureMetaDataList.size());
                for (int i = 0; i < pictures.length - 1; i++) {
                    assertEquals("id -1 expected", Long.valueOf(-1), pictureMetaDataList.get(i).getId());
                    assertNull("empty picture(blob key) expected", pictureMetaDataList.get(i).getImageBlobKey());
                }
                assertEquals("public picture expected", pictures[2].getId(), pictureMetaDataList.get(2).getId());
                assertEquals("public picture expected", pictures[2].getImageBlobKey(), pictureMetaDataList.get(2).getImageBlobKey());
                signal3.countDown();
            }
        }, filter);
        assertTrue("timeout reached", signal3.await(10, TimeUnit.SECONDS));
    }

    public void testUploadImage() throws IOException, InterruptedException {
        final String TEST_TITEL = "Testtitel";
        final String TEST_BESCHREIBUNG = "Testbeschreibung";

        final CountDownLatch signal = new CountDownLatch(1);
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
        }, testImage, TEST_TITEL, TEST_BESCHREIBUNG, PictureVisibility.PRIVATE);
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
    }

    public void testGetImage() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.getPicture(new ExtendedTaskDelegateAdapter<Void, PictureData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, PictureData picture) {
                assertNotNull("picture is null", picture);
                assertEquals("id", picture1.getId(), picture.getId());
                assertEquals("title", picture1.getTitle(), picture.getTitle());
                assertEquals("date", picture1.getDate().getValue(), picture.getDate().getValue());
                assertEquals("uploader", picture1.getUploader(), picture.getUploader());
                assertEquals("description", picture1.getDescription().getValue(), picture.getDescription());
                if (picture1.getRatings() != null) {
                    assertEquals("my rating", picture1.getRatings().get(ServiceProvider.getEmail()), picture.getMyRating());
                }
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
        Integer[] ratings = new Integer[] {2, 5};

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
        PictureData picture = null;
        for (int i = 0; i < ratings.length; i++) {
            changeAccount(i);
            picture = ServiceProvider.getService().galleryEndpoint().getPicture(picture1.getId()).execute();
            assertEquals("wrong rating for account " + ServiceProvider.getEmail(), ratings[i], picture.getMyRating());
        }

        // Erwartete durchschnittliche Bewertung berechnen
        double avgRating = 0.0;
        for (int i : ratings) {
            avgRating += i;
        }
        avgRating /= ratings.length;
        assertEquals("wrong average rating", avgRating, picture.getAvgRating());
    }

    public void testAddToAndRemoveFromGallery() throws InterruptedException, IOException {
        // Testbild zur Testgallery hinzufügen
        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.addPictureToGallery(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, picture1.getId(), testGallery.getId());
        signal.await(10, TimeUnit.SECONDS);

        // Weiteres Testbild hochladen, um das Ergebnis zu testen
        final CountDownLatch signal1 = new CountDownLatch(1);
        GalleryController.uploadPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Picture picture) {
                // Bild nach dem Test direkt wieder löschen, da es nur zum Verifizieren der zu testenden Funktion genutzt wird
                picturesToDelete.add(picture);
                signal1.countDown();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        }, testImage, "Picture 2", "Das zweite Testbild (von einem anderen Account hochgeladen).", PictureVisibility.PUBLIC);
        assertTrue("timeout reached", signal1.await(10, TimeUnit.SECONDS));

        // Beim Abrufen der Bilder für die Testgallery sollte nun nur das erste Testbild zurückgegeben werden
        PictureFilter filter = new PictureFilter();
        filter.setLimit(2);
        filter.setGalleryId(testGallery.getId());
        PictureMetaDataList data = ServiceProvider.getService().galleryEndpoint().listPictures(filter).execute();
        assertNotNull("no pictures fetched", data);
        assertNotNull("no pictures fetched", data.getList());
        assertEquals("wrong count of pictures fetched", 1, data.getList().size());
        assertEquals("wrong picture fetched", picture1.getId(), data.getList().get(0).getId());

        // Testbild wieder aus der Testgallery entfernen
        final CountDownLatch signal2 = new CountDownLatch(1);
        GalleryController.removePictureFromGallery(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal2.countDown();
            }
        }, picture1.getId(), testGallery.getId());
        assertTrue("timeout reached", signal2.await(10, TimeUnit.SECONDS));

        // Beim Abrufen der Bilder für die Testgallery sollte nun kein Bild zurückgegeben werden
        filter = new PictureFilter();
        filter.setLimit(2);
        filter.setGalleryId(testGallery.getId());
        data = ServiceProvider.getService().galleryEndpoint().listPictures(filter).execute();
        assertNotNull("no data fetched", data);
        assertNull("pictures fetched", data.getList());
    }

    public void testChangeVisibility() throws InterruptedException, IOException {
        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.changeVisibility(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, picture1.getId(), PictureVisibility.PRIVATE);
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));

        PictureData pictureAfterChange = ServiceProvider.getService().galleryEndpoint().getPicture(picture1.getId()).execute();
        assertEquals("visibility after change", PictureVisibility.PRIVATE.name(), pictureAfterChange.getVisibility());
    }

    public void tearDown() throws Exception {
        super.tearDown();

        testImage.delete();

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
