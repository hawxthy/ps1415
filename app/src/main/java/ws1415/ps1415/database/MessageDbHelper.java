package ws1415.ps1415.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ws1415.common.net.ServiceProvider;

/**
 * Der MessageDatabase verwaltet die Datenbank der Nachrichten und Konversationen.
 *
 * @author Martin Wrodarczyk
 */
public class MessageDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static String DATABASE_USER_ID;
    private static String DATABASE_NAME = "messageManager_";

    // Singelton Instanz
    private static MessageDbHelper instance;

    // Tabellen
    public static final String TABLE_CONVERSATION = "conversations";
    public static final String TABLE_MESSAGE = "messages";

    // Conversation Spalten
    public static final String KEY_ID_CONVERSATION = "email";
    public static final String KEY_PICTURE = "picture";
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
            + KEY_PICTURE + " TEXT,"
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
        super(context, DATABASE_NAME + ServiceProvider.getEmail(), null, DATABASE_VERSION);
        DATABASE_USER_ID = ServiceProvider.getEmail();
    }

    public static synchronized MessageDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MessageDbHelper(context.getApplicationContext());
        }
        else if(!DATABASE_USER_ID.equals(ServiceProvider.getEmail())){
            instance = new MessageDbHelper(context.getApplicationContext());
            DATABASE_USER_ID = ServiceProvider.getEmail();
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
}
