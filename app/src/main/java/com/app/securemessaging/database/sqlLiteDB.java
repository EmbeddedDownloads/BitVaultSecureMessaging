package com.app.securemessaging.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <p>
 * This singleton class create table in Sqlite DB
 */


public class sqlLiteDB extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "secure_messaging.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =

            "CREATE TABLE " + ContractClass.FeedEntry.TABLE_NAME + " (" +
                    ContractClass.FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ContractClass.FeedEntry.MESSAGE + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.ATTACHMENT + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.RECEIVER_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.SENDER_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.TXD_ID + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.TYPE + TEXT_TYPE + COMMA_SEP +     // SEND,INBOX,DRAFT
                    ContractClass.FeedEntry.STATUS + TEXT_TYPE + COMMA_SEP +   // PENDING,FAIL,SUCCESS
                    ContractClass.FeedEntry.TIME_IN_MILLI + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.TIME + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.MESSAGE_TYPE + TEXT_TYPE + COMMA_SEP +     // BITVAULT,BITATTACH,PC
                    ContractClass.FeedEntry.TAG + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.SESSIONKEY + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.WALLETTYPE + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.MESSAGE_FEE + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.WALLET_NAME + TEXT_TYPE + COMMA_SEP +
                    ContractClass.FeedEntry.IS_NEW + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ContractClass.FeedEntry.TABLE_NAME;

    private static sqlLiteDB objSqlLiteDB;


    private sqlLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Through this we will create only pne instance of sqllite
    public static sqlLiteDB getInstance(Context mContext) {
        if (objSqlLiteDB == null) {
            objSqlLiteDB = new sqlLiteDB(mContext);
        }
        return objSqlLiteDB;
    }


    // This is Overrided method called once , it checks whther table exist in DB if not then create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * This is Overrided method called when any version of DB is changed
     *
     * @param db         -- db
     * @param oldVersion -- Old version of the DB
     * @param newVersion -- New Version of the DB
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
        db.execSQL(SQL_DELETE_ENTRIES);
    }


}
