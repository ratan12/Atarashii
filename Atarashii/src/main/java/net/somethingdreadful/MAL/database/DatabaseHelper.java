package net.somethingdreadful.MAL.database;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.FirstTimeInit;
import net.somethingdreadful.MAL.account.AccountService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String NAME = "MAL.db";
    private static final int VERSION = 18;
    private static DatabaseHelper instance;
    private final Context context;

    static final String RELATION_TYPE_ALTERNATIVE = "0";
    static final String RELATION_TYPE_CHARACTER = "1";
    static final String RELATION_TYPE_SIDE_STORY = "2";
    static final String RELATION_TYPE_SPINOFF = "3";
    static final String RELATION_TYPE_SUMMARY = "4";
    static final String RELATION_TYPE_ADAPTATION = "5";
    static final String RELATION_TYPE_RELATED = "6";
    static final String RELATION_TYPE_PREQUEL = "7";
    static final String RELATION_TYPE_SEQUEL = "8";
    static final String RELATION_TYPE_PARENT_STORY = "9";
    static final String RELATION_TYPE_OTHER = "10";

    public static final String COLUMN_ID = "_id";;

    /* title types, working the same way as the relation types
     */
    static final int TITLE_TYPE_JAPANESE = 0;
    static final int TITLE_TYPE_ENGLISH = 1;
    static final int TITLE_TYPE_SYNONYM = 2;
    static final int TITLE_TYPE_ROMAJI = 3;

    static final int MUSIC_TYPE_OPENING = 0;
    static final int MUSIC_TYPE_ENDING = 1;

    private DatabaseHelper(Context context) {
        super(context, NAME, null, VERSION);
        this.context = context;
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(NAME);
    }

    public static boolean DBExists(Context context) {
        File dbFile = context.getDatabasePath(DatabaseHelper.NAME);
        return dbFile.exists();
    }

    @Override
    public String getDatabaseName() {
        return NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Table.create(db).createAccounts();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AppLog.log(Log.INFO, "Atarashii", "DatabaseTest.OnUpgrade(): Upgrading database from version " + oldVersion + " to " + newVersion);
        try {
            /*
              Date: 13-6-2017
              Database version: 18
              Application version: 3.0 Beta 1

              Multi account support
             */
            if (oldVersion < 18) {
                // Drop existing tables if they exist
                List<String> tables = new ArrayList<>();
                Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table';", null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String tableName = cursor.getString(1);
                    if (!tableName.equals("android_metadata") &&
                            !tableName.equals("sqlite_sequence"))
                        tables.add(tableName);
                    cursor.moveToNext();
                }
                cursor.close();

                for(String tableName:tables) {
                    db.execSQL("DROP TABLE IF EXISTS " + tableName);
                }

                // Create new tables to replace the old ones
                onCreate(db);
            }
        } catch (Exception e) {
            // log database failures
            AppLog.initFabric(context);
            AppLog.logTaskCrash("DatabaseHelper", "onUpgrade()", e);

            // Delete database and remove account
            DatabaseHelper.deleteDatabase(context);
            AccountService.Companion.create(context);
            AccountService.Companion.deleteAccount();

            // Restart application
            context.startActivity(new Intent(context, FirstTimeInit.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            System.exit(0);
        }

        AppLog.log(Log.INFO, "Atarashii", "DatabaseTest.OnUpgrade(): Database upgrade finished");
    }
}
