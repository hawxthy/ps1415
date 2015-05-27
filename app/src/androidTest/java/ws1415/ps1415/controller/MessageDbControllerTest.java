package ws1415.ps1415.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.common.util.ImageUtil;
import ws1415.ps1415.R;
import ws1415.common.model.Conversation;
import ws1415.common.model.LocalMessageType;
import ws1415.common.model.Message;

/**
 * Diese Testklasse wird dazu genutzt die Funktionalitäten des Controllers für die Zugriffe
 * auf die Nachrichtendatenbank zu testen.
 *
 * Der aufgeführte E-Mail Account "skatenight.user1@gmail.com" muss zum Testen auf dem Gerät
 * registriert sein. Dazu muss dieser in den Einstellungen des Gerätes mit dem Passwort:
 * "skatenight123" hinzugefügt werden.
 */
public class MessageDbControllerTest extends AuthenticatedAndroidTestCase{
    private static boolean initialized = false;

    public static final String TEST_MAIL = "skatenight.user1@gmail.com";
    public static final String TEST_FIRST_NAME = "Martin";
    public static final String TEST_LAST_NAME = "Müller";

    public static final String TEST_MAIL_2 = "test2@gmail.com";
    public static final String TEST_FIRST_NAME_2 = "Peter";
    public static final String TEST_LAST_NAME_2 = "Meier";

    public static final String TEST_NEW_FIRST_NAME = "Peter";
    public static final String TEST_NEW_LAST_NAME = "Meier";

    public static final String TEST_MAIL_PICTURE = "test3@gmail.com";

    public static final String TEST_MESSAGE_CONTENT_1 = "Hallo!";
    public static final Date TEST_MESSAGE_DATE_SEND_1 = new Date(1430846930789L);
    public static final LocalMessageType TEST_MESSAGE_TYPE_1 = LocalMessageType.INCOMING;

    public static final String TEST_MESSAGE_CONTENT_2 = "Hi!";
    public static final Date TEST_MESSAGE_DATE_SEND_2 = new Date(1430847009086L);
    public static final LocalMessageType TEST_MESSAGE_TYPE_2 = LocalMessageType.OUTGOING_NOT_RECEIVED;

    private MessageDbController dbController;
    private static final Conversation TEST_CONVERSATION = new Conversation(TEST_MAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
    private static final Conversation TEST_CONVERSATION_2 = new Conversation(TEST_MAIL_2, TEST_FIRST_NAME_2, TEST_LAST_NAME_2);
    private static final List<Conversation> TEST_LIST_CONVERSATIONS = Arrays.asList(TEST_CONVERSATION, TEST_CONVERSATION_2);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        changeAccount(TEST_MAIL);
        dbController = MessageDbController.getInstance(getContext());

        if(!initialized) {
            dbController.deleteAllConversation();
            initialized = true;
        }

        boolean succeed = dbController.insertConversation(TEST_CONVERSATION);
        assertTrue(succeed);

        succeed = dbController.insertConversation(TEST_CONVERSATION_2);
        assertTrue(succeed);
    }

    @Override
    public void tearDown(){
        boolean succeed = dbController.deleteConversation(TEST_CONVERSATION.getEmail());
        assertTrue(succeed);

        succeed = dbController.deleteConversation(TEST_CONVERSATION_2.getEmail());
        assertTrue(succeed);
    }

    /**
     * Prüft das Erstellen einer Konversation mit einem Profilbild.
     */
    @SmallTest
    public void testCreateConversation() {
        final Bitmap testImage = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.default_picture);
        final byte[] testImageByte = ImageUtil.BitmapToByteArrayJPEG(testImage);
        Conversation testConversation = new Conversation(TEST_MAIL_PICTURE, TEST_FIRST_NAME, TEST_LAST_NAME);

        boolean succeed = dbController.insertConversation(testConversation);
        assertTrue(succeed);

        Conversation conversation = dbController.getConversation(TEST_MAIL_PICTURE);

        assertNotNull(conversation);
        assertEquals(TEST_MAIL_PICTURE, conversation.getEmail());
        assertEquals(TEST_FIRST_NAME, conversation.getFirstName());
        assertEquals(TEST_LAST_NAME, conversation.getLastName());

        succeed = dbController.deleteConversation(TEST_MAIL_PICTURE);
        assertTrue(succeed);
    }

    /**
     * Prüft das Erstellen und Abrufen einer Konversation.
     */
    @SmallTest
    public void testGetConversation() {
        Conversation conversation = dbController.getConversation(TEST_MAIL);

        assertNotNull(conversation);
        assertEquals(TEST_MAIL, conversation.getEmail());
        assertEquals(TEST_FIRST_NAME, conversation.getFirstName());
        assertEquals(TEST_LAST_NAME, conversation.getLastName());
        assertEquals(0, conversation.getCountNewMessages());
    }

    /**
     * Prüft das Löschen einer Konversation. Dabei werden Nachrichten erstellt, die nach
     * dem Löschen der Konversation gelöscht sein sollten.
     */
    @SmallTest
    public void testDeleteConversation() {
        Message message = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1,
                TEST_MESSAGE_TYPE_1);
        dbController.insertMessage(TEST_MAIL, message);

        boolean succeed = dbController.deleteConversation(TEST_MAIL);
        assertTrue(succeed);

        Conversation conversation = dbController.getConversation(TEST_MAIL);
        assertNull(conversation);

        List<Message> dbMessages = dbController.getAllMessages(TEST_MAIL);
        assertTrue(dbMessages.size() == 0);

        succeed = dbController.insertConversation(TEST_CONVERSATION);
        assertTrue(succeed);
    }

    /**
     * Prüft das Aktualisieren einer Konversation. Dabei wird erst geprüft, ob der Vorname und
     * Nachname in der Konversation geändert werden kann und anschließend ob man die Anzahl der
     * noch nicht gelesenen Nachrichten ändern kann.
     */
    @SmallTest
    public void testUpdateConversation() {
        boolean succeed = dbController.updateConversation(TEST_MAIL, TEST_NEW_FIRST_NAME, TEST_NEW_LAST_NAME);
        assertTrue(succeed);

        Conversation conversation = dbController.getConversation(TEST_MAIL);
        assertEquals(TEST_NEW_FIRST_NAME, conversation.getFirstName());
        assertEquals(TEST_NEW_LAST_NAME, conversation.getLastName());

        int oldCountNewMessages = conversation.getCountNewMessages();

        conversation = dbController.getConversation(TEST_MAIL);
        assertEquals(oldCountNewMessages, conversation.getCountNewMessages());

        dbController.resetNewMessages(TEST_MAIL);

        conversation = dbController.getConversation(TEST_MAIL);
        assertEquals(conversation.getCountNewMessages(), 0);
    }

    /**
     * Prüft ob die Liste aller Konversationen richtig abgerufen werden.
     */
    @SmallTest
    public void testListConversations() {
        List<Conversation> conversationsDb = dbController.getAllConversations();

        // Prüfe Größe
        assertEquals(conversationsDb.size(), TEST_LIST_CONVERSATIONS.size());

        List<String> testConversationEmails = Arrays.asList(TEST_CONVERSATION.getEmail(),
                TEST_CONVERSATION_2.getEmail());
        List<String> dbConversationEmails = new ArrayList<>();

        for(Conversation c : conversationsDb){
            dbConversationEmails.add(c.getEmail());
        }

        // Prüfe Emails
        assertTrue(testConversationEmails.containsAll(dbConversationEmails)
                && dbConversationEmails.containsAll(testConversationEmails));
    }

    /**
     * Prüft ob die Liste aller Nachrichten zu einer Konversation richtig abgerufen werden. Dazu
     * wird die Größe der Liste und die Inhalte der Nachrichten geprüft.
     */
    @SmallTest
    public void testListMessages() {
        List<Message> testMessages = new ArrayList<>();

        Message message1 = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1, TEST_MESSAGE_TYPE_1);
        assertTrue(dbController.insertMessage(TEST_MAIL, message1) >= 0);
        testMessages.add(message1);

        Message message2 = new Message(TEST_MESSAGE_DATE_SEND_2, TEST_MESSAGE_CONTENT_2, TEST_MESSAGE_TYPE_2);
        assertTrue(dbController.insertMessage(TEST_MAIL, message2) >= 0);
        testMessages.add(message2);

        List<Message> messagesDb = dbController.getAllMessages(TEST_MAIL);

        // Prüfe Größe
        assertEquals(testMessages.size(), messagesDb.size());

        List<String> testContents = Arrays.asList(TEST_MESSAGE_CONTENT_1, TEST_MESSAGE_CONTENT_2);
        List<String> testContentsDb = new ArrayList<>();

        for (Message message : messagesDb) {
            testContentsDb.add(message.getContent());
        }

        // Prüfe Inhalt der Nachrichten
        assertTrue(testContents.containsAll(testContentsDb)
                && testContentsDb.containsAll(testContents));
    }

    /**
     * Prüft ob beim Erstellen von neuen Nachrichten, die letzte Nachricht zu einer Konversation
     * richtig aktualisiert wird.
     */
    @SmallTest
    public void testLastMessage(){
        Message message = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1, TEST_MESSAGE_TYPE_1);
        dbController.insertMessage(TEST_MAIL, message);

        Conversation dbConversation = dbController.getConversation(TEST_MAIL);

        assertEquals(TEST_MESSAGE_CONTENT_1, dbConversation.getLastMessage().getContent());

        message = new Message(TEST_MESSAGE_DATE_SEND_2, TEST_MESSAGE_CONTENT_2, TEST_MESSAGE_TYPE_2);
        dbController.insertMessage(TEST_MAIL, message);

        dbConversation = dbController.getConversation(TEST_MAIL);

        assertEquals(TEST_MESSAGE_CONTENT_2, dbConversation.getLastMessage().getContent());
    }

    /**
     * Tested, ob die Bestätigung eines Nachrichtenempfangs richtig abgespeichert wird.
     */
    @SmallTest
    public void testUpdateReceivedMessage(){
        Message message = new Message(TEST_MESSAGE_DATE_SEND_1, TEST_MESSAGE_CONTENT_1, TEST_MESSAGE_TYPE_1);
        long id = dbController.insertMessage(TEST_MAIL, message);
        assertTrue(dbController.updateMessageReceived(id, TEST_MESSAGE_DATE_SEND_1.getTime()));

        Message dbMessage = dbController.getAllMessages(TEST_MAIL).get(0);
        assertEquals(LocalMessageType.OUTGOING_RECEIVED, dbMessage.getType());
    }
}
