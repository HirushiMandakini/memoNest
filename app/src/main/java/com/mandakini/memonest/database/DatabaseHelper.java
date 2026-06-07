package com.mandakini.memonest.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "memonest.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_DRAFTS = "drafts";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_IS_UPLOADED = "is_uploaded";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_DRAFTS_TABLE =
                "CREATE TABLE " + TABLE_DRAFTS + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_TITLE + " TEXT,"
                        + COLUMN_CONTENT + " TEXT,"
                        + COLUMN_IMAGE_URI + " TEXT,"
                        + COLUMN_CREATED_AT + " TEXT,"
                        + COLUMN_IS_UPLOADED + " INTEGER DEFAULT 0"
                        + ")";

        db.execSQL(CREATE_DRAFTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRAFTS);

        onCreate(db);
    }
}