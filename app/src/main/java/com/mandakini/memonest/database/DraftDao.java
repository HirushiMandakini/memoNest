package com.mandakini.memonest.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mandakini.memonest.models.Draft;

import java.util.ArrayList;
import java.util.List;

public class DraftDao {

    private DatabaseHelper databaseHelper;

    public DraftDao(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public long insertDraft(Draft draft) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, draft.getTitle());
        values.put(DatabaseHelper.COLUMN_CONTENT, draft.getContent());
        values.put(DatabaseHelper.COLUMN_IMAGE_URI, draft.getImageUri());
        values.put(DatabaseHelper.COLUMN_CREATED_AT, draft.getCreatedAt());
        values.put(DatabaseHelper.COLUMN_IS_UPLOADED, draft.getIsUploaded());

        long result = db.insert(DatabaseHelper.TABLE_DRAFTS, null, values);
        db.close();

        return result;
    }

    public List<Draft> getAllDrafts() {
        List<Draft> draftList = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_DRAFTS,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                Draft draft = cursorToDraft(cursor);
                draftList.add(draft);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return draftList;
    }

    public Draft getDraftById(int id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_DRAFTS,
                null,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Draft draft = null;

        if (cursor.moveToFirst()) {
            draft = cursorToDraft(cursor);
        }

        cursor.close();
        db.close();

        return draft;
    }

    public int updateDraft(Draft draft) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, draft.getTitle());
        values.put(DatabaseHelper.COLUMN_CONTENT, draft.getContent());
        values.put(DatabaseHelper.COLUMN_IMAGE_URI, draft.getImageUri());
        values.put(DatabaseHelper.COLUMN_IS_UPLOADED, draft.getIsUploaded());

        int result = db.update(
                DatabaseHelper.TABLE_DRAFTS,
                values,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(draft.getId())}
        );

        db.close();

        return result;
    }

    public int deleteDraft(int id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int result = db.delete(
                DatabaseHelper.TABLE_DRAFTS,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        db.close();

        return result;
    }
    public void deleteMultipleDrafts(List<Integer> ids) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        for (int id : ids) {
            db.delete(
                    DatabaseHelper.TABLE_DRAFTS,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)}
            );
        }

        db.close();
    }
    public List<Draft> searchDrafts(String keyword) {
        List<Draft> draftList = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_DRAFTS,
                null,
                DatabaseHelper.COLUMN_TITLE + " LIKE ? OR " + DatabaseHelper.COLUMN_CONTENT + " LIKE ?",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"},
                null,
                null,
                DatabaseHelper.COLUMN_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                Draft draft = cursorToDraft(cursor);
                draftList.add(draft);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return draftList;
    }

    public void markAsUploaded(int id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_UPLOADED, 1);

        db.update(
                DatabaseHelper.TABLE_DRAFTS,
                values,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        db.close();
    }

    private Draft cursorToDraft(Cursor cursor) {
        Draft draft = new Draft();

        draft.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        draft.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
        draft.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENT)));
        draft.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI)));
        draft.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));
        draft.setIsUploaded(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_UPLOADED)));

        return draft;
    }
}