package com.runvision.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FaceDataHelper extends SQLiteOpenHelper {
    public static String TAG = FaceDataHelper.class.getSimpleName();


    private final static String CREATE_USER_TABLE = "create table tUser(id INTEGER primary key autoincrement,userID TEXT,userName TEXT, userType TEXT, createTime TEXT,imageId TEXT);";
    private final static String CREATE_RECORD_TABLE="create table tRecord(id INTEGER primary key autoincrement,userID TEXT,userName TEXT,userType,TEXT,sorce TEXT,comperResult TEXT,createTime TEXT,snapImageID TEXT,templateImageID TEXT)";

    public FaceDataHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_RECORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
