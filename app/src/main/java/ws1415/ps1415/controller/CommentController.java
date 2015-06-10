package ws1415.ps1415.controller;


import com.skatenight.skatenightAPI.model.Comment;
import com.skatenight.skatenightAPI.model.CommentContainerData;
import com.skatenight.skatenightAPI.model.CommentData;
import com.skatenight.skatenightAPI.model.CommentDataList;
import com.skatenight.skatenightAPI.model.CommentFilter;

import java.io.IOException;
import java.util.List;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegate;

/**
 * Bietet eine Schnittstelle zu den Funktionen für Kommentare auf dem Backend.
 * @author Richard Schulze
 */
public class CommentController {

    /**
     * Fügt den Kommentar mit angegebenem Inhalt dem Container zu, der durch die Klasse und ID identifiziert wird.
     * Der eingeloggte Benutzer muss im betreffenden Container Kommentare hinzufügen können.
     * @param handler           Der Handler, dem der erstellte Kommentar übergeben wird.
     * @param containerClass    Die Klasse des Containers, dem der Kommentar hinzugefügt wird.
     * @param containerId       Die ID des Containers.
     * @param comment           Der hinzuzufügende Kommentar.
     */
    public static void addComment(ExtendedTaskDelegate<Void, CommentData> handler, final String containerClass,
                                  final long containerId, final String comment) {
        new ExtendedTask<Void, Void, CommentData>(handler) {
            @Override
            protected CommentData doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().commentEndpoint().addComment(containerClass, containerId, comment).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Editiert den Kommentar mit der angegebenen ID.
     * @param handler      Der Handler, der über den Status des Tasks informiert wird.
     * @param commentId    Die ID des zu editierenden Kommentars.
     * @param comment      Der neue Kommentar.
     */
    public static void editComment(ExtendedTaskDelegate<Void, Void> handler, final long commentId, final String comment) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().commentEndpoint().editComment(commentId, comment).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Löscht den Kommentar mit der angegebenen ID.
     * @param handler      Der Handler, der über den Status des Tasks informiert wird.
     * @param commentId    Die ID des zu löschenden Kommentars.
     */
    public static void deleteComment(ExtendedTaskDelegate<Void, Void> handler, final long commentId) {
        new ExtendedTask<Void, Void, Void>(handler) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ServiceProvider.getService().commentEndpoint().deleteComment(commentId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Ruft die Kommentare ab, die auf den Filter passen. Nach dem Abrufen der Kommentare wird außerdem der
     * CursorString des Filters aktualisiert, sodass ein weiterer Aufruf mit dem Filter die nächsten Kommentare
     * liefert.
     * @param handler    Der Handler, dem die Kommentare übergeben werden.
     * @param filter     Der anzuwendende Filter.
     */
    public static void listComments(ExtendedTaskDelegate<Void, List<CommentData>> handler, final CommentFilter filter) {
        new ExtendedTask<Void, Void, List<CommentData>>(handler) {
            @Override
            protected List<CommentData> doInBackground(Void... params) {
                try {
                    CommentDataList list = ServiceProvider.getService().commentEndpoint().listComments(filter).execute();
                    filter.setCursorString(list.getCursorString());
                    return list.getComments();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    /**
     * Ruft die Daten eines Kommentar-Containers ab.
     * @param handler           Der Handler, dem die Daten übergeben werden.
     * @param containerClass    Die Klasse des abzurufenden Containers.
     * @param containerId       Die ID des abzurufenden Containers.
     */
    public static void getCommentContainer(ExtendedTaskDelegate<Void, CommentContainerData> handler,
                                           final String containerClass, final long containerId) {
        new ExtendedTask<Void, Void, CommentContainerData>(handler) {
            @Override
            protected CommentContainerData doInBackground(Void... params) {
                try {
                    return ServiceProvider.getService().commentEndpoint().getCommentContainer(containerClass, containerId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

}
