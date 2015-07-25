
package com.mjaworski.myQuotes.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.v4.content.Loader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class SQLiteCursorLoader extends AbstractCursorLoader
{

    SQLiteOpenHelper db = null;
    String rawQuery = null;
    String[] args = null;

    /**
     * Creates a fully-specified SQLiteCursorLoader. See
     * {@link SQLiteDatabase#rawQuery(SQLiteDatabase, String, String[]) SQLiteDatabase.rawQuery()} for documentation on
     * the meaning of the parameters. These will be passed as-is to that call.
     */
    public SQLiteCursorLoader(Context context, SQLiteOpenHelper db,
            String rawQuery, String[] args)
    {
        super(context);
        this.db = db;
        this.rawQuery = rawQuery;
        this.args = args;
    }

    /**
     * Runs on a worker thread and performs the actual database query to retrieve the Cursor.
     */
    @Override
    protected Cursor buildCursor()
    {
        return (db.getReadableDatabase().rawQuery(rawQuery, args));
    }

    /**
     * Writes a semi-user-readable roster of contents to supplied output.
     */
    @Override
    public void dump(String prefix, FileDescriptor fd,
            PrintWriter writer, String[] args)
    {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print("rawQuery=");
        writer.println(rawQuery);
        writer.print(prefix);
        writer.print("args=");
        writer.println(Arrays.toString(args));
    }

    public void insert(String table, String nullColumnHack,
            ContentValues values)
    {
        new InsertTask(this).execute(db, table, nullColumnHack, values);
    }

    public void update(String table, ContentValues values,
            String whereClause, String[] whereArgs)
    {
        new UpdateTask(this).execute(db, table, values, whereClause,
                whereArgs);
    }

    public void replace(String table, String nullColumnHack,
            ContentValues values)
    {
        new ReplaceTask(this).execute(db, table, nullColumnHack, values);
    }

    public void delete(String table, String whereClause,
            String[] whereArgs)
    {
        new DeleteTask(this).execute(db, table, whereClause, whereArgs);
    }

    public void execSQL(String sql, Object[] bindArgs)
    {
        new ExecSQLTask(this).execute(db, sql, bindArgs);
    }

    private class InsertTask extends
            ContentChangingTask<Object, Void, Void>
    {
        InsertTask(SQLiteCursorLoader loader)
        {
            super(loader);
        }

        @Override
        protected Void doInBackground(Object... params)
        {
            SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
            String table = (String) params[1];
            String nullColumnHack = (String) params[2];
            ContentValues values = (ContentValues) params[3];

            db.getWritableDatabase().insert(table, nullColumnHack, values);

            return (null);
        }
    }

    private class UpdateTask extends
            ContentChangingTask<Object, Void, Void>
    {
        UpdateTask(SQLiteCursorLoader loader)
        {
            super(loader);
        }

        @Override
        protected Void doInBackground(Object... params)
        {
            SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
            String table = (String) params[1];
            ContentValues values = (ContentValues) params[2];
            String where = (String) params[3];
            String[] whereParams = (String[]) params[4];

            db.getWritableDatabase()
                    .update(table, values, where, whereParams);

            return (null);
        }
    }

    private class ReplaceTask extends
            ContentChangingTask<Object, Void, Void>
    {
        ReplaceTask(SQLiteCursorLoader loader)
        {
            super(loader);
        }

        @Override
        protected Void doInBackground(Object... params)
        {
            SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
            String table = (String) params[1];
            String nullColumnHack = (String) params[2];
            ContentValues values = (ContentValues) params[3];

            db.getWritableDatabase().replace(table, nullColumnHack, values);

            return (null);
        }
    }

    private class DeleteTask extends
            ContentChangingTask<Object, Void, Void>
    {
        DeleteTask(SQLiteCursorLoader loader)
        {
            super(loader);
        }

        @Override
        protected Void doInBackground(Object... params)
        {
            SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
            String table = (String) params[1];
            String where = (String) params[2];
            String[] whereParams = (String[]) params[3];

            db.getWritableDatabase().delete(table, where, whereParams);

            return (null);
        }
    }

    private class ExecSQLTask extends
            ContentChangingTask<Object, Void, Void>
    {
        ExecSQLTask(SQLiteCursorLoader loader)
        {
            super(loader);
        }

        @Override
        protected Void doInBackground(Object... params)
        {
            SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
            String sql = (String) params[1];
            Object[] bindParams = (Object[]) params[2];

            db.getWritableDatabase().execSQL(sql, bindParams);

            return (null);
        }
    }

    public static abstract class ContentChangingTask<T1, T2, T3> extends
            AsyncTask<T1, T2, T3>
    {
        private Loader<?> loader = null;

        ContentChangingTask(Loader<?> loader)
        {
            this.loader = loader;
        }

        @Override
        protected void onPostExecute(T3 param)
        {
            loader.onContentChanged();
        }
    }

}
