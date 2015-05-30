package ws1415.ps1415.controller;

import android.test.suitebuilder.annotation.SmallTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.model.Conversation;
import ws1415.common.model.LocalMessageType;
import ws1415.common.model.Message;

/**
 * Diese Testklasse wird dazu genutzt die Funktionalitäten des Controllers für die Zugriffe
 * auf die Nachrichtendatenbank zu testen.
 *
 * Folgender E-Mail Account muss auf dem Gerät registriert sein um die Tests auszuführen:
 * - {@code USER_MAIL}
 * Dazu muss dieser in den Einstellungen des Gerätes mit dem Passwort: "skatenight123" hinzugefügt
 * werden.
 *
 * @author Martin Wrodarczyk
 */
public class MessageDbControllerTest extends AuthenticatedAndroidTestCase{
    public static final String USER_MAIL = "skatenight.user1@gmail.com";
    private static boolean initialized = false;
    private MessageDbController dbController;

    public static final String TEST_MAIL_1 = "test1@gmail.com";
    public static final String TEST_PICTURE_1 = "AMIfv96gv3ZbEm8My5vOuUUXg_MVvpW4rj8v9-m7P61uysQmA" +
            "-c7o-8ASdcADqeLOqrd9P9PA-YybnKv2u_6X0mlaAP4UQ6wsyaEjxwvWW5t8IXb0lM_2oJ5D7SvEiCOLPVrwQt" +
            "JrGboh7BvdIsmsiAaA7LHZJ9qxATu7Lwk_2Fd-dN-io13Cyc";
    public static final String TEST_FIRST_NAME_1 = "Vorname1";
    public static final String TEST_LAST_NAME_1 = "Nachname1";

    public static final String TEST_MAIL_2 = "test2@gmail.com";
    public static final String TEST_PICTURE_2 = "AMIfv94qnjFRX93THqnkjfkcup9gr1ww3Mz1uxSb791JZeHtj2r" +
            "MGbzYcHGVWqItVLCtjyrNj3rXgk8qvHI5H4_po8-_N7ixf91rmSbdlxp7rbx_9azj1yqrU7LE2Gv381JiL_6prs" +
            "odfAnIVYE9dkrmhvKS4AdLRFb5sfyqMWvo5b_-6Q5WfsU";
    public static final String TEST_FIRST_NAME_2 = "Vorname2";
    public static final String TEST_LAST_NAME_2 = "Nachname2";

    public static final String TEST_NEW_PICTURE = "AMIfv94OQgm79Pnf97a8CcovXXqfHgbqNisPjFqlLNZa8MDIl" +
            "z15vytoN9Fx00-B2t3hGjjmngApfW1suNGH441JNb5toU1w_4BfvdjPqoG31fRJlXMypMnGbyOolqD4qSiz2_L8" +
            "X5b3ne54cyHAbRjrXgc5FOlzry5wKBt5CK_8LMVwZrIfV7c";
    public static final String TEST_NEW_FIRST_NAME = "Vorname3";
    public static final String TEST_NEW_LAST_NAME = "Nachname3";

    public static final String TEST_MAIL_PICTURE = "test3@gmail.com";

    public static final String TEST_MESSAGE_CONTENT_1 = "Hallo!";
    public static final Date TEST_MESSAGE_DATE_SEND_1 = new Date(1430846930789L);
    public static final LocalMessageType TEST_MESSAGE_TYPE_1 = LocalMessageType.INCOMING;

    public static final String TEST_MESSAGE_CONTENT_2 = "Hi!";
    public static final Date TEST_MESSAGE_DATE_SEND_2 = new Date(1430847009086L);
    public static final LocalMessageType TEST_MESSAGE_TYPE_2 = LocalMessageType.OUTGOING_NOT_RECEIVED;

    private static final Conversation TEST_CONVERSATION_1 = new Conversation(TEST_MAIL_1, TEST_PICTURE_1,
            TEST_FIRST_NAME_1, TEST_LAST_NAME_1);
    private static final Conversation TEST_CONVERSATION_2 = new Conversation(TEST_MAIL_2, TEST_PICTURE_2,
            TEST_FIRST_NAME_2, TEST_LAST_NAME_2);
    private static final List<Conversation> TEST_LIST_CONVERSATIONS = Arrays.asList(TEST_CONVERSATION_1, TEST_CONVERSATION_2);

    /**
     * Löscht alle bisherigen Konversationen und fügt zwei neue Konversationen der Datenbank
     * hinzu. Dabei wird geprüft, ob die neuen Datensätze der Datenbank hinzugefügt worden sind.
     *
     * @throws Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        changeAccount(USER_MAIL);
        dbController = MessageDbController.getInstance(getContext());

        if(!initialized) {
            dbController.deleteAllConversation();
            initialized = true;
        }

        boolean succeed = dbController.insertConversation(TEST_CONVERSATION_1);
        assertTrue(succeed);

        succeed = dbController.insertConversation(TEST_CONVERSATION_2);
        assertTrue(succeed);
    }

    /**
     * Löscht die beiden Konversationen die in {@link #setUp()}} erstellt worden sind.
     */
    @Override
    public void tearDown(){
        boolean succeed = dbController.deleteConversation(TEST_CONVERSATION_1.getEmail());
        assertTrue(succeed);

        succeed = dbController.deleteConversation(TEST_CONVERSATION_2.getEmail());
        assertTrue(succeed);
    }

    /**
     * Prüft das Abrufen einer Konversation, wobei eine der Konversationen, die in {@link #setUp()}
     * erstellt worden sind, abgerufen wird.
     */
    @SmallTest
    public void testGetConversation() {
        Conversation conversation = dbController.getConversation(TEST_MAIL_1);

        assertNotNull(conversation);
        assertEquals(TEST_MAIL_1, conversation.getEmail());
        assertEquals(TEST_PICTURE_1, conversation.getPictureKey());
        assertEquals(TEST_FIRST_NAME_1, conversation.getFirstName());
        assertEquals(TEST_LAST_NAME_1, conversation.getLastName());
        assertEquals(0, conversation.getCountNewMessages());
        assertNull(conversation.getLastMessage());
    }

    /**
     * Prüft das Löschen einer Konversation. Dabei werden Nachrichten erstellt, die nach
     * dem Löschen der Konversation gelöscht sein sollten.
     */
    @SmallTest
    public void testDeleteConversation() {
        Message message = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1,
                TEST_MESSAGE_TYPE_1);
        long messageId = dbController.insertMessage(TEST_MAIL_1, message);
        assertTrue(messageId >= 0);

        boolean succeed = dbController.deleteConversation(TEST_MAIL_1);
        assertTrue(succeed);

        Conversation conversation = dbController.getConversation(TEST_MAIL_1);
        assertNull(conversation);

        List<Message> dbMessages = dbController.getAllMessages(TEST_MAIL_1);
        assertTrue(dbMessages.size() == 0);

        succeed = dbController.insertConversation(TEST_CONVERSATION_1);
        assertTrue(succeed);
    }

    /**
     * Prüft das Aktualisieren einer Konversation. Dabei wird geprüft, ob man die
     * Profilinformationen zu dem Benutzer geändert werden können.
     */
    @SmallTest
    public void testUpdateConversation() {
        boolean succeed = dbController.updateConversation(TEST_MAIL_1, TEST_NEW_PICTURE,
                TEST_NEW_FIRST_NAME, TEST_NEW_LAST_NAME);
        assertTrue(succeed);

        Conversation conversation = dbController.getConversation(TEST_MAIL_1);
        assertEquals(TEST_NEW_PICTURE, conversation.getPictureKey());
        assertEquals(TEST_NEW_FIRST_NAME, conversation.getFirstName());
        assertEquals(TEST_NEW_LAST_NAME, conversation.getLastName());
    }

    @SmallTest
    public void testNewMessagesCount() {
        Conversation conversation = dbController.getConversation(TEST_MAIL_1);
        int oldCountNewMessages = conversation.getCountNewMessages();

        Message message1 = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1, TEST_MESSAGE_TYPE_1);
        assertTrue(dbController.insertMessage(TEST_MAIL_1, message1) >= 0);

        conversation = dbController.getConversation(TEST_MAIL_1);
        assertEquals(oldCountNewMessages+1, conversation.getCountNewMessages());

        assertTrue(dbController.resetNewMessages(TEST_MAIL_1));

        conversation = dbController.getConversation(TEST_MAIL_1);
        assertEquals(conversation.getCountNewMessages(), 0);
    }

    /**
     * Prüft ob die Liste aller Konversationen richtig abgerufen werden.
     */
    @SmallTest
    public void testListConversations() {
        List<Conversation> conversationsDb = dbController.getAllConversations();

        assertEquals(conversationsDb.size(), TEST_LIST_CONVERSATIONS.size());

        List<String> testConversationEmails = Arrays.asList(TEST_CONVERSATION_1.getEmail(),
                TEST_CONVERSATION_2.getEmail());
        List<String> dbConversationEmails = new ArrayList<>();

        for(Conversation c : conversationsDb){
            dbConversationEmails.add(c.getEmail());
        }

        assertTrue(testConversationEmails.containsAll(dbConversationEmails)
                && dbConversationEmails.containsAll(testConversationEmails));
    }

    /**
     * Prüft ob die Liste aller Nachrichten zu einer Konversation richtig abgerufen werden. Dazu
     * wird die Größe der Liste und die Ids der Nachrichten geprüft.
     */
    @SmallTest
    public void testListMessages() {
        List<Message> testMessages = new ArrayList<>();

        Message message1 = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1, TEST_MESSAGE_TYPE_1);
        long IdMessage1 = dbController.insertMessage(TEST_MAIL_1, message1);
        assertTrue(IdMessage1 >= 0);
        testMessages.add(message1);

        Message message2 = new Message(TEST_MESSAGE_DATE_SEND_2, TEST_MESSAGE_CONTENT_2, TEST_MESSAGE_TYPE_2);
        long IdMessage2 = dbController.insertMessage(TEST_MAIL_1, message2);
        assertTrue(IdMessage2 >= 0);
        testMessages.add(message2);

        List<Message> messagesDb = dbController.getAllMessages(TEST_MAIL_1);

        assertEquals(testMessages.size(), messagesDb.size());

        List<Long> testIds = Arrays.asList(IdMessage1, IdMessage2);
        List<Long> testContentsDb = new ArrayList<>();

        for (Message message : messagesDb) {
            testContentsDb.add(message.get_id());
        }

        // Prüfe Inhalt der Nachrichten
        assertTrue(testIds.containsAll(testContentsDb)
                && testContentsDb.containsAll(testIds));
    }

    /**
     * Prüft ob beim Erstellen von neuen Nachrichten, die letzte Nachricht zu einer Konversation
     * richtig aktualisiert wird.
     */
    @SmallTest
    public void testLastMessage(){
        Message message = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1, TEST_MESSAGE_TYPE_1);
        long messageId = dbController.insertMessage(TEST_MAIL_1, message);
        assertTrue(messageId >= 0);

        Conversation dbConversation = dbController.getConversation(TEST_MAIL_1);
        assertNotNull(dbConversation.getLastMessage());
        assertEquals(messageId, dbConversation.getLastMessage().get_id());

        message = new Message(TEST_MESSAGE_DATE_SEND_2, TEST_MESSAGE_CONTENT_2, TEST_MESSAGE_TYPE_2);
        messageId = dbController.insertMessage(TEST_MAIL_1, message);
        assertTrue(messageId >= 0);

        dbConversation = dbController.getConversation(TEST_MAIL_1);
        assertNotNull(dbConversation.getLastMessage());
        assertEquals(messageId, dbConversation.getLastMessage().get_id());
    }

    /**
     * Tested, ob die Bestätigung eines Nachrichtenempfangs richtig abgespeichert wird.
     */
    @SmallTest
    public void testUpdateReceivedMessage(){
        Message message = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1, TEST_MESSAGE_TYPE_1);
        long messageId = dbController.insertMessage(TEST_MAIL_1, message);
        assertTrue(messageId >= 0);
        assertTrue(dbController.updateMessageReceived(messageId, TEST_MESSAGE_DATE_SEND_1.getTime()));

        Message dbMessage = dbController.getAllMessages(TEST_MAIL_1).get(0);
        assertEquals(LocalMessageType.OUTGOING_RECEIVED, dbMessage.getType());
    }

    /**
     * Prüft das Löschen einer Nachricht. Nachdem eine Nachricht gelöscht wird, soll die letzte
     * Nachricht in einer Konversation auf die davor zuletzt gesendete bzw. empfangene Nachricht
     * gesetzt werden. Existiert sonst keine Nachricht sollte der Wert wieder <code>null</code>
     * sein.
     */
    @SmallTest
    public void deleteMessage(){
        Message message = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1, TEST_MESSAGE_TYPE_1);
        long idFirstMessage = dbController.insertMessage(TEST_MAIL_1, message);
        assertTrue(idFirstMessage >= 0);

        message = new Message(TEST_MESSAGE_DATE_SEND_2, TEST_MESSAGE_CONTENT_2, TEST_MESSAGE_TYPE_2);
        long idSecondMessage = dbController.insertMessage(TEST_MAIL_1, message);
        assertTrue(idSecondMessage >= 0);

        Conversation conversation = dbController.getConversation(TEST_MAIL_1);
        assertEquals(conversation.getLastMessage().get_id(), idSecondMessage);

        boolean succeed = dbController.deleteMessage(TEST_MAIL_1, idSecondMessage);
        assertTrue(succeed);

        conversation = dbController.getConversation(TEST_MAIL_1);
        assertEquals(conversation.getLastMessage().get_id(), idFirstMessage);

        succeed = dbController.deleteMessage(TEST_MAIL_1, idFirstMessage);
        assertTrue(succeed);

        conversation = dbController.getConversation(TEST_MAIL_1);
        assertNull(conversation.getLastMessage());
    }
}
