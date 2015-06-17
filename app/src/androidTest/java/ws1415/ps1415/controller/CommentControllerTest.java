package ws1415.ps1415.controller;

import com.google.api.client.util.IOUtils;
import com.skatenight.skatenightAPI.model.Comment;
import com.skatenight.skatenightAPI.model.CommentContainerData;
import com.skatenight.skatenightAPI.model.CommentData;
import com.skatenight.skatenightAPI.model.CommentDataList;
import com.skatenight.skatenightAPI.model.CommentFilter;
import com.skatenight.skatenightAPI.model.Event;
import com.skatenight.skatenightAPI.model.Picture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.model.PictureVisibility;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Testet die Methoden des CommentController.
 * @author Richard Schulze
 */
public class CommentControllerTest extends AuthenticatedAndroidTestCase {
    private File testImage;

    private Picture testPicture;
    private CommentData testComment;

    private List<CommentData> commentsToDelete;

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

        final CountDownLatch signal = new CountDownLatch(1);
        GalleryController.uploadPicture(new ExtendedTaskDelegateAdapter<Void, Picture>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Picture picture) {
                testPicture = picture;
                signal.countDown();
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                fail(message);
            }
        }, testImage, "Picture 1", "Das erste Testbild", PictureVisibility.PUBLIC, null);
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));

        testComment = ServiceProvider.getService().commentEndpoint().addComment(Picture.class.getSimpleName(),
                testPicture.getId(), "Testkommentar").execute();

        commentsToDelete = new LinkedList<>();
    }

    public void testAddComment() throws Exception {
        final String TEST_COMMENT = "Neuer Testkommentar";

        final CountDownLatch signal = new CountDownLatch(1);
        CommentController.addComment(new ExtendedTaskDelegateAdapter<Void, CommentData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, CommentData commentData) {
                commentsToDelete.add(commentData);

                assertNotNull("no comment returned", commentData);
                assertEquals("wrong author", ServiceProvider.getEmail(), commentData.getAuthor());
                assertEquals("wrong comment", TEST_COMMENT, commentData.getComment());
                signal.countDown();
            }
        }, Picture.class.getSimpleName(), testPicture.getId(), TEST_COMMENT);
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
    }

    public void testEditComment() throws Exception {
        final String EDIT_COMMENT = "Editierter Kommentar";

        final CountDownLatch signal = new CountDownLatch(1);
        CommentController.editComment(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, testComment.getId(), EDIT_COMMENT);
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));

        CommentFilter filter = new CommentFilter();
        filter.setLimit(2);
        filter.setContainerClass(Picture.class.getSimpleName());
        filter.setContainerId(testPicture.getId());
        CommentDataList commentList = ServiceProvider.getService().commentEndpoint().listComments(filter).execute();
        assertNotNull("no comments fetched", commentList);
        assertNotNull("no comments fetched", commentList.getComments());
        assertEquals("wrong comment count", 1, commentList.getComments().size());
        CommentData editedComment = commentList.getComments().get(0);
        assertEquals("wrong comment id", testComment.getId(), editedComment.getId());
        assertEquals("wrong comment data", EDIT_COMMENT, editedComment.getComment());
        assertEquals("date changed", testComment.getDate(), editedComment.getDate());
        assertEquals("author changed", testComment.getAuthor(), editedComment.getAuthor());
    }

    public void testDeleteComment() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        CommentController.deleteComment(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, testComment.getId());
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));

        // Es sollten keine Kommentare mehr gefunden werden
        CommentFilter filter = new CommentFilter();
        filter.setLimit(1);
        filter.setContainerClass(Picture.class.getSimpleName());
        filter.setContainerId(testPicture.getId());
        CommentDataList commentList = ServiceProvider.getService().commentEndpoint().listComments(filter).execute();
        assertTrue("comments fetched", commentList == null || commentList.getComments() == null || commentList.getComments().isEmpty());
    }

    public void testListComments() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        CommentFilter filter = new CommentFilter();
        filter.setLimit(2);
        filter.setContainerClass(Picture.class.getSimpleName());
        filter.setContainerId(testPicture.getId());
        CommentController.listComments(new ExtendedTaskDelegateAdapter<Void, List<CommentData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<CommentData> commentList) {
                assertNotNull("no comments fetched", commentList);
                assertEquals("wrong count", 1, commentList.size());
                assertEquals("wrong id", testComment.getId(), commentList.get(0).getId());
                signal.countDown();
            }
        }, filter);
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
    }

    public void testGetCommentContainer() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        CommentController.getCommentContainer(new ExtendedTaskDelegateAdapter<Void, CommentContainerData>() {
            @Override
            public void taskDidFinish(ExtendedTask task, CommentContainerData commentContainerData) {
                assertNotNull("no comment container fetched", commentContainerData);
                signal.countDown();
            }
        }, Picture.class.getSimpleName(), testPicture.getId());
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
    }

    public void tearDown() throws Exception {
        super.tearDown();

        testImage.delete();

        if (testPicture != null) {
            ServiceProvider.getService().galleryEndpoint().deletePicture(testPicture.getId()).execute();
            testPicture = null;
        }

        if (testComment != null) {
            ServiceProvider.getService().commentEndpoint().deleteComment(testComment.getId()).execute();
            testComment = null;
        }

        if (commentsToDelete != null) {
            for (CommentData c : commentsToDelete) {
                ServiceProvider.getService().commentEndpoint().deleteComment(c.getId()).execute();
            }
            commentsToDelete = null;
        }
    }
}