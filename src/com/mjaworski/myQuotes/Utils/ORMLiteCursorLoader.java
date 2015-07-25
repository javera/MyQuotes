
package com.mjaworski.myQuotes.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;


public class ORMLiteCursorLoader<T> extends AbstractCursorLoader
{

    PreparedQuery<T> query = null;
    RuntimeExceptionDao<T, ?> dao = null;

    /**
     * Creates a fully-specified SQLiteCursorLoader. See
     * {@link SQLiteDatabase#rawQuery(SQLiteDatabase, String, String[]) SQLiteDatabase.rawQuery()} for documentation on
     * the meaning of the parameters. These will be passed as-is to that call.
     */
    public ORMLiteCursorLoader(Context context, PreparedQuery<T> query, RuntimeExceptionDao<T, ?> dao)
    {
        super(context);
        this.query = query;
        this.dao = dao;
    }

    /**
     * Runs on a worker thread and performs the actual database query to retrieve the Cursor.
     */
    @Override
    protected Cursor buildCursor()
    {
        Cursor cursor = null;

        // when you are done, prepare your query and build an iterator
        CloseableIterator<T> iterator = dao.iterator(query);
        try
        {
            // get the raw results which can be cast under Android
            AndroidDatabaseResults results = (AndroidDatabaseResults) iterator.getRawResults();
            cursor = results.getRawCursor();
        }
        finally
        {
//            iterator.closeQuietly();
        }

        return (cursor);
    }
}
