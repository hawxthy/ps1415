package ws1415.ps1415.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ws1415.ps1415.database.MessageDbHelper;
import ws1415.common.model.Conversation;
import ws1415.common.model.LocalMessageType;
import ws1415.common.model.Message;

/**
 * Created by Martin on 16.05.2015.
 */
public class MessageDbController {
    // Singelton Instanz
    private static MessageDbController instance;

    private static MessageDbHelper dbHelper;
    private Context mContext;

    private MessageDbController(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized MessageDbController getInstance(Context context) {
        if (instance == null) {
            instance = new MessageDbController(context.getApplicationContext());
        }
        dbHelper = MessageDbHelper.getInstance(context);
        return instance;
    }

    //-- Methoden für die Conversation-Tabelle -- //

    /**
     * Erstellt und speichert persistent eine Conversation.
     *
     * @param conversation Zu speichernde Conversation
     */
    public boolean insertConversation(Conversation conversation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageDbHelper.KEY_ID_CONVERSATION, conversation.getEmail());
        values.put(MessageDbHelper.KEY_PICTURE, conversation.getPictureKey());
        values.put(MessageDbHelper.KEY_FIRST_NAME, conversation.getFirstName());
        values.put(MessageDbHelper.KEY_LAST_NAME, conversation.getLastName());
        values.put(MessageDbHelper.KEY_COUNT_NEW_MESSAGES, 0);

        long rowId = db.insert(MessageDbHelper.TABLE_CONVERSATION, null, values);
        return !(rowId == -1);
    }

    /**
     * Prüft ob bereits eine Konversation existiert.
     *
     * @param userMail E-Mail Adresse der Konversation
     * @return true, falls Konversation existiert, false andernfalls
     */
    public boolean existsConversation(String userMail){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + MessageDbHelper.KEY_ID_CONVERSATION + " FROM " +
                MessageDbHelper.TABLE_CONVERSATION + " WHERE " + MessageDbHelper.KEY_ID_CONVERSATION + "=?",
                new String[]{userMail});

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    /**
     * Ruft die Konversation mit der Person mit der angegebenen E-Mail ab.
     *
     * @param userMail E-Mail Adresse der Person
     * @return Konversation mit der Person
     */
    public Conversation getConversation(String userMail) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(MessageDbHelper.JOIN_CONVERSATION_MESSAGE_ID, new String[]{userMail});

        if (!cursor.moveToFirst()) return null;
        Conversation conversation = getConversation(cursor);

        cursor.close();

        return conversation;
    }

    /**
     * Gibt die Liste aller Konversationen aus.
     *
     * @return Alle Konversationen
     */
    public List<Conversation> getAllConversations() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(MessageDbHelper.JOIN_CONVERSATION_MESSAGE, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return new ArrayList<>();
        }

        List<Conversation> conversations = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            conversations.add(getConversation(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return conversations;
    }

    /**
     * Hilfsmethode um aus dem Cursor das Conversationsojekt abzurufen.
     *
     * @param cursor Cursor
     * @return Conversation an der Cursorzeile
     */
    private Conversation getConversation(Cursor cursor) {
        // letzte Nachricht abrufen
        Message lastMessage;
        if (cursor.isNull(cursor.getColumnIndex(MessageDbHelper.KEY_ID_MESSAGE))) {
            lastMessage = null;
        } else {
            lastMessage = new Message(
                    cursor.getInt(cursor.getColumnIndex(MessageDbHelper.KEY_ID_MESSAGE)),
                    getDate(cursor.getLong(cursor.getColumnIndex(MessageDbHelper.KEY_SEND_DATE))),
                    cursor.getString(cursor.getColumnIndex(MessageDbHelper.KEY_CONTENT)),
                    LocalMessageType.getValue(cursor.getInt(cursor.getColumnIndex(MessageDbHelper.KEY_TYPE))));
        }

        // Konversation abrufen
        return new Conversation(
                cursor.getString(cursor.getColumnIndex(MessageDbHelper.KEY_ID_CONVERSATION)),
                cursor.getString(cursor.getColumnIndex(MessageDbHelper.KEY_PICTURE)),
                cursor.getString(cursor.getColumnIndex(MessageDbHelper.KEY_FIRST_NAME)),
                cursor.getString(cursor.getColumnIndex(MessageDbHelper.KEY_LAST_NAME)),
                cursor.getInt(cursor.getColumnIndex(MessageDbHelper.KEY_COUNT_NEW_MESSAGES)),
                lastMessage);
    }

    /**
     * Löscht eine Conversation, wobei dazugehörige Nachrichten kaskadierend gelöscht werden.
     *
     * @param userMail E-Mail Adresse der dazugehörigen Person
     */
    public boolean deleteConversation(String userMail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int deletedRows = db.delete(MessageDbHelper.TABLE_CONVERSATION, MessageDbHelper.KEY_ID_CONVERSATION + "=?",
                new String[]{userMail});

        return deletedRows == 1;
    }

    /**
     * Aktualisiert den Vor- und Nachnamen der Person, zu der die Konversation gehört.
     *
     * @param userMail     E-Mail Adresse der Person
     * @param firstName Neuer Vorname
     * @param lastName  Neuer Nachname
     * @return true, falls Aktualisierung erfolgreich, false andernfalls
     */
    public boolean updateConversation(String userMail, String firstName, String lastName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageDbHelper.KEY_FIRST_NAME, firstName);
        values.put(MessageDbHelper.KEY_LAST_NAME, lastName);

        int updatedRows = db.update(MessageDbHelper.TABLE_CONVERSATION, values, MessageDbHelper.KEY_ID_CONVERSATION + " = ?",
                new String[]{userMail});

        return updatedRows == 1;
    }

    /**
     * Aktualisiert das Profilbild der Person, zu der die Konversation gehört.
     *
     * @param userMail E-Mail Adresse der Person
     * @param pictureKey Neues Profilbild
     * @return true, falls Aktualisierung erfolgreich, false andernfalls
     */
    public boolean updateConversation(String userMail, String pictureKey){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageDbHelper.KEY_PICTURE, pictureKey);

        int updatedRows = db.update(MessageDbHelper.TABLE_CONVERSATION, values, MessageDbHelper.KEY_ID_CONVERSATION + " = ?",
                new String[]{userMail});

        return updatedRows == 1;
    }

    /**
     * Setzt die Anzahl der neuen Nachrichten auf 0.
     *
     * @param userMail            E-Mail Adresse der Person
     * @return true, falls Aktualisierung erfolgreich, false andernfalls
     */
    public boolean resetNewMessages(String userMail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageDbHelper.KEY_COUNT_NEW_MESSAGES, 0);

        int updatedRows = db.update(MessageDbHelper.TABLE_CONVERSATION, values, MessageDbHelper.KEY_ID_CONVERSATION + " = ?",
                new String[]{userMail});

        return updatedRows == 1;
    }

    //-- Methoden für die Message-Tabelle -- //

    /**
     * Erstellt eine Nachricht zu der gehörigen Conversation.
     *
     * @param conversationMail E-Mail Adresse zu der Konversation
     * @param message          Nachricht
     * @return
     */
    public long insertMessage(String conversationMail, Message message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageDbHelper.KEY_SEND_DATE, persistDate(message.getSendDate()));
        values.put(MessageDbHelper.KEY_CONTENT, message.getContent());
        values.put(MessageDbHelper.KEY_TYPE, message.getType().getId());
        values.put(MessageDbHelper.FOREIGN_KEY_CONVERSATION, conversationMail);

        long rowId = db.insert(MessageDbHelper.TABLE_MESSAGE, null, values);

        Log.d("ROWID", String.valueOf(rowId));

        values.clear();
        values.put(MessageDbHelper.FOREIGN_KEY_LAST_MESSAGE, rowId);

        db.update(MessageDbHelper.TABLE_CONVERSATION, values, MessageDbHelper.KEY_ID_CONVERSATION + "=?",
                new String[]{conversationMail});

        if(message.getType().equals(LocalMessageType.INCOMING)) increaseNewMessages(conversationMail);

        return rowId;
    }

    /**
     * Erhöht die Anzahl der neuen Nachrichten um 1.
     *
     * @param email            E-Mail Adresse der Person
     * @return true, falls Aktualisierung erfolgreich, false andernfalls
     */
    private boolean increaseNewMessages(String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageDbHelper.KEY_COUNT_NEW_MESSAGES, getConversation(email).getCountNewMessages() + 1);

        int updatedRows = db.update(MessageDbHelper.TABLE_CONVERSATION, values, MessageDbHelper.KEY_ID_CONVERSATION + " = ?",
                new String[]{email});

        return updatedRows == 1;
    }

    /**
     * Prüft, ob das Sendedatum der Nachricht mit der gespeicherten Nachricht übereinstimmt.
     * Stimmt es überein, wird die Nachricht auf "erhalten" gesetzt.
     *
     * @param messageId Id der Nachricht
     * @param sendDate Sendedatum
     * @return true, falls update erfolgreich, false andernfalls
     */
    public boolean updateMessageReceived(long messageId, long sendDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + MessageDbHelper.KEY_SEND_DATE + " FROM " + MessageDbHelper.TABLE_MESSAGE
                        + " WHERE " + MessageDbHelper.KEY_ID_MESSAGE + "=?" + " AND " + MessageDbHelper.KEY_SEND_DATE + "=?",
                new String[]{String.valueOf(messageId), String.valueOf(sendDate)});

        if (!cursor.moveToFirst()){
            cursor.close();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(MessageDbHelper.KEY_TYPE, LocalMessageType.OUTGOING_RECEIVED.getId());

        int updatedRow = db.update(MessageDbHelper.TABLE_MESSAGE, values, MessageDbHelper.KEY_ID_MESSAGE + "=?",
                new String[]{String.valueOf(messageId)});

        cursor.close();
        return updatedRow == 1;
    }

    /**
     * Gibt eine Liste aller Nachrichten einer Conversation aus.
     *
     * @param userMail E-Mail Adresse der Person der Konversation
     * @return Liste aller Nachrichten in einer Konversation
     */
    public List<Message> getAllMessages(String userMail) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + MessageDbHelper.TABLE_MESSAGE + " WHERE "
                + MessageDbHelper.FOREIGN_KEY_CONVERSATION + "=?", new String[]{userMail});

        if (!cursor.moveToFirst()) {
            cursor.close();
            return new ArrayList<>();
        }

        List<Message> messages = new ArrayList<>();
        Message message;
        while (!cursor.isAfterLast()) {
            message = new Message(
                    cursor.getInt(cursor.getColumnIndex(MessageDbHelper.KEY_ID_MESSAGE)),
                    getDate(cursor.getLong(cursor.getColumnIndex(MessageDbHelper.KEY_SEND_DATE))),
                    cursor.getString(cursor.getColumnIndex(MessageDbHelper.KEY_CONTENT)),
                    LocalMessageType.getValue(cursor.getInt(cursor.getColumnIndex(MessageDbHelper.KEY_TYPE))));
            messages.add(message);
            cursor.moveToNext();
        }

        cursor.close();
        return messages;
    }

    /**
     * Löscht eine Nachricht aus der Datenbank.
     *
     * @param messageId Id der Nachricht
     * @return true, falls Vorgang erfolgreich, false andernfalls
     */
    public boolean deleteMessage(long messageId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(MessageDbHelper.TABLE_MESSAGE, MessageDbHelper.KEY_ID_MESSAGE +  "=?",
                new String[]{String.valueOf(messageId)}) > 0;
    }

    // -- Hilfsmethoden -- //

    public static Long persistDate(Date date) {
        if (date != null) {
            return date.getTime();
        }
        return null;
    }

    public static Date getDate(Long time) {
        return new Date(time);
    }

    // Für das Testen
    public void deleteAllConversation(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MessageDbHelper.TABLE_CONVERSATION, null, null);
    }


}
