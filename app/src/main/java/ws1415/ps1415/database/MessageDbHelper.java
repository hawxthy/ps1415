package ws1415.ps1415.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ws1415.ps1415.model.Conversation;
import ws1415.ps1415.model.Message;
import ws1415.ps1415.model.LocalMessageType;

/**
 * Der MessageDatabase verwaltet die Datenbank der Nachrichten und Konversationen.
 *
 * @author Martin Wrodarczyk
 */
public class MessageDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "messageManager";

    // Singelton Instanz
    private static MessageDbHelper instance;

    // Tabellen
    public static final String TABLE_CONVERSATION = "conversations";
    public static final String TABLE_MESSAGE = "messages";

    // Conversation Spalten
    public static final String KEY_ID_CONVERSATION = "email";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_COUNT_NEW_MESSAGES = "countNewMessages";
    public static final String FOREIGN_KEY_LAST_MESSAGE = "lastMessage";

    // Message Spalten
    public static final String KEY_ID_MESSAGE = "id";
    public static final String KEY_SEND_DATE = "sendDate";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_TYPE = "type";
    public static final String FOREIGN_KEY_CONVERSATION = "conversation_id";

    // Tabellen Create Statements
    private static final String CREATE_TABLE_CONVERSATION = "CREATE TABLE "
            + TABLE_CONVERSATION + "(" + KEY_ID_CONVERSATION + " TEXT PRIMARY KEY,"
            + KEY_FIRST_NAME + " TEXT," + KEY_LAST_NAME + " TEXT,"
            + KEY_COUNT_NEW_MESSAGES + " INTEGER," + FOREIGN_KEY_LAST_MESSAGE + " INTEGER,"
            + " FOREIGN KEY (" + FOREIGN_KEY_LAST_MESSAGE + ") REFERENCES " + TABLE_MESSAGE + " ("
            + KEY_ID_MESSAGE + "))";

    private static final String CREATE_TABLE_MESSAGE = "CREATE TABLE "
            + TABLE_MESSAGE + "(" + KEY_ID_MESSAGE + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_SEND_DATE + " INTEGER," + KEY_CONTENT + " TEXT,"
            + KEY_TYPE + " INTEGER" + ")";

    private static final String ALTER_TABLE_MESSAGE_FOREIGN_KEY = "ALTER TABLE "
            + TABLE_MESSAGE + " ADD COLUMN " + FOREIGN_KEY_CONVERSATION + " TEXT "
            + "REFERENCES " + TABLE_CONVERSATION + "(" + KEY_ID_CONVERSATION + ")"
            + " ON DELETE CASCADE";

    public static final String JOIN_CONVERSATION_MESSAGE_ID = "SELECT * FROM "
            + TABLE_CONVERSATION + " LEFT JOIN " + TABLE_MESSAGE + " ON " + TABLE_CONVERSATION
            + "." + FOREIGN_KEY_LAST_MESSAGE + " = " + TABLE_MESSAGE + "." + KEY_ID_MESSAGE
            + " WHERE " + TABLE_CONVERSATION + "." + KEY_ID_CONVERSATION + " = ?";

    public static final String JOIN_CONVERSATION_MESSAGE = "SELECT * FROM "
            + TABLE_CONVERSATION + " LEFT JOIN " + TABLE_MESSAGE + " ON " + TABLE_CONVERSATION
            + "." + FOREIGN_KEY_LAST_MESSAGE + " = " + TABLE_MESSAGE + "." + KEY_ID_MESSAGE;

    private MessageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        context.deleteDatabase(DATABASE_NAME);
    }

    public static synchronized MessageDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MessageDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Wird beim Erstellen der Datenbank aufgerufen. Diese Methode erstellt die Tabellen.
     *
     * @param db Datenbank
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * Zu Beginn, die Message Tabelle, anschließend Conversation-Tabelle erstellen, sodass der
         * Fremdschlüssel auf eine Spalte zeigt und zuletzt in der Message-Tabelle den
         * Fremschlüssel auf Conversation hinzufügen.
         */
        db.execSQL(CREATE_TABLE_MESSAGE);
        db.execSQL(CREATE_TABLE_CONVERSATION);
        db.execSQL(ALTER_TABLE_MESSAGE_FOREIGN_KEY);
    }

    /**
     * Wird beim upgraden der Datenbank auf eine neue Version aufgerufen. Derzeit löscht diese
     * Methode bei einer neuen Version die Tabellen und erstellt die Tabellen der neuen Version.
     *
     * @param db         Datenbank
     * @param oldVersion alte Version
     * @param newVersion neue Version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATION);

        onCreate(db);
    }

    /**
     * Wird beim Öffnen der Datenbank aufgerufen. Sorgt dafür dass kaskadierendes Löschen aktiviert
     * ist.
     *
     * @param db Datenbank
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    //-- Methoden für die Conversation-Tabelle -- //

    /**
     * Erstellt und speichert persistent eine Conversation.
     *
     * @param conversation Zu speichernde Conversation
     */
    public boolean insertConversation(Conversation conversation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID_CONVERSATION, conversation.getEmail());
        values.put(KEY_FIRST_NAME, conversation.getFirstName());
        values.put(KEY_LAST_NAME, conversation.getLastName());
        values.put(KEY_COUNT_NEW_MESSAGES, 0);

        long rowId = db.insert(TABLE_CONVERSATION, null, values);

        return !(rowId == -1);
    }

    /**
     * Ruft die Konversation mit der Person mit der angegebenen E-Mail ab.
     *
     * @param email E-Mail Adresse der Person
     * @return Konversation mit der Person
     */
    public Conversation getConversation(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(JOIN_CONVERSATION_MESSAGE_ID, new String[]{email});

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
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(JOIN_CONVERSATION_MESSAGE, null);

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
        if (cursor.isNull(cursor.getColumnIndex(KEY_ID_MESSAGE))) {
            lastMessage = null;
        } else {
            lastMessage = new Message(
                    cursor.getInt(cursor.getColumnIndex(KEY_ID_MESSAGE)),
                    getDate(cursor.getLong(cursor.getColumnIndex(KEY_SEND_DATE))),
                    cursor.getString(cursor.getColumnIndex(KEY_CONTENT)),
                    LocalMessageType.getValue(cursor.getInt(cursor.getColumnIndex(KEY_TYPE))));
        }

        // Konversation abrufen
        return new Conversation(
                cursor.getString(cursor.getColumnIndex(KEY_ID_CONVERSATION)),
                cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME)),
                cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME)),
                cursor.getInt(cursor.getColumnIndex(KEY_COUNT_NEW_MESSAGES)),
                lastMessage);
    }

    /**
     * Löscht eine Conversation, wobei dazugehörige Nachrichten kaskadierend gelöscht werden.
     *
     * @param email E-Mail Adresse der dazugehörigen Person
     */
    public boolean deleteConversation(String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        int deletedRows = db.delete(TABLE_CONVERSATION, KEY_ID_CONVERSATION + "=?",
                new String[]{email});

        return deletedRows == 1;

    }

    /**
     * Aktualisiert den Vor- und Nachnamen der Person, zu der die Konversation gehört.
     *
     * @param email     E-Mail Adresse der Person
     * @param firstName Neuer Vorname
     * @param lastName  Neuer Nachname
     * @return true, falls Aktualisierung erfolgreich, false andernfalls
     */
    public boolean updateConversation(String email, String firstName, String lastName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FIRST_NAME, firstName);
        values.put(KEY_LAST_NAME, lastName);

        int updatedRows = db.update(TABLE_CONVERSATION, values, KEY_ID_CONVERSATION + " = ?",
                new String[]{email});

        return updatedRows == 1;
    }

    /**
     * Aktualisiert die Anzahl neuer Nachrichten zu einer Konversation.
     *
     * @param email            E-Mail Adresse der Person
     * @param countNewMessages Neue Anzahl neuer Nachrichten
     * @return true, falls Aktualisierung erfolgreich, false andernfalls
     */
    public boolean updateConversation(String email, int countNewMessages) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COUNT_NEW_MESSAGES, countNewMessages);

        int updatedRows = db.update(TABLE_CONVERSATION, values, KEY_ID_CONVERSATION + " = ?",
                new String[]{email});

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
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SEND_DATE, persistDate(message.getSendDate()));
        values.put(KEY_CONTENT, message.getContent());
        values.put(KEY_TYPE, message.getType().getId());
        values.put(FOREIGN_KEY_CONVERSATION, conversationMail);

        long rowId = db.insert(TABLE_MESSAGE, null, values);

        values.clear();
        values.put(FOREIGN_KEY_LAST_MESSAGE, rowId);

        db.update(TABLE_CONVERSATION, values, KEY_ID_CONVERSATION + "=?",
                new String[]{conversationMail});

        return rowId;
    }

    /**
     * Prüft, ob das Sendedatum der Nachricht mit der gespeicherten Nachricht übereinstimmt.
     * Stimmt es überein, wird die Nachricht auf "erhalten" gesetzt.
     *
     * @param conversationMail E-Mail der Konversationsperson
     * @param messageId Id der Nachricht
     * @param sendDate Sendedatum
     * @return true, falls update erfolgreich, false andernfalls
     */
    public boolean updateMessageReceived(String conversationMail, long messageId, long sendDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + KEY_SEND_DATE + " FROM " + TABLE_MESSAGE
                        + " WHERE " + KEY_ID_MESSAGE + "=?" + " AND " + KEY_SEND_DATE + "=?",
                new String[]{String.valueOf(messageId), String.valueOf(sendDate)});

        if (!cursor.moveToFirst()) return false;

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, LocalMessageType.OUTGOING_RECEIVED.getId());

        int updatedRow = db.update(TABLE_MESSAGE, values, KEY_ID_MESSAGE + "=?",
                new String[]{String.valueOf(messageId)});

        return updatedRow == messageId;
    }

    /**
     * Gibt eine Liste aller Nachrichten einer Conversation aus.
     *
     * @param conversationMail E-Mail Adresse der Person der Konversation
     * @return Liste aller Nachrichten in einer Konversation
     */
    public List<Message> getAllMessages(String conversationMail) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGE + " WHERE "
                + FOREIGN_KEY_CONVERSATION + "=?", new String[]{conversationMail});

        if (!cursor.moveToFirst()) {
            cursor.close();
            return new ArrayList<>();
        }

        List<Message> messages = new ArrayList<>();
        Message message;
        while (!cursor.isAfterLast()) {
            message = new Message(
                    cursor.getInt(cursor.getColumnIndex(KEY_ID_MESSAGE)),
                    getDate(cursor.getLong(cursor.getColumnIndex(KEY_SEND_DATE))),
                    cursor.getString(cursor.getColumnIndex(KEY_CONTENT)),
                    LocalMessageType.getValue(cursor.getInt(cursor.getColumnIndex(KEY_TYPE))));
            messages.add(message);
            cursor.moveToNext();
        }

        cursor.close();
        return messages;
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
}
