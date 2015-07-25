
package com.mjaworski.myQuotes.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mjaworski.myQuotes.R;
import com.mjaworski.myQuotes.DB.Model.Author;
import com.mjaworski.myQuotes.DB.Model.Quote;
import com.mjaworski.myQuotes.DB.Model.QuoteTag;
import com.mjaworski.myQuotes.DB.Model.Source;
import com.mjaworski.myQuotes.DB.Model.Tag;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper
{
    private static final String LOG_TAG = "com.jaworski.myQuotes.db.DatabaseHelper";

    private static final String DATABASE_NAME = "com.jaworski.myQuotes.db";
    private static final int DATABASE_VERSION = 33;

    // the DAO object we use to access the Quotes table
    // private Dao<Quote, Integer> quoteDao = null;
    private RuntimeExceptionDao<Quote, Integer> quoteRuntimeDao = null;

    // the DAO object we use to access the Tags table
    // private Dao<Tag, Integer> tagDao = null;
    private RuntimeExceptionDao<Tag, Integer> tagRuntimeDao = null;

    // the DAO object we use to access the Authors table
    // private Dao<Author, Integer> authorDao = null;
    private RuntimeExceptionDao<Author, Integer> authorRuntimeDao = null;

    // the DAO object we use to access the Sources table
    // private Dao<Source, Integer> sourceDao = null;
    private RuntimeExceptionDao<Source, Integer> sourceRuntimeDao = null;

    // the DAO object we use to access the mapping table Quote<->Tag
    // private Dao<QuoteTag, Integer> quoteTagDao = null;
    private RuntimeExceptionDao<QuoteTag, Integer> quoteTagRuntimeDao = null;

    private static DatabaseHelper singleton = null;
    private Context ctxt = null;

    public synchronized static DatabaseHelper getInstance(Context ctxt)
    {
        if (singleton == null)
        {
            singleton = new DatabaseHelper(ctxt.getApplicationContext());
        }
        return (singleton);
    }

    private DatabaseHelper(Context ctxt)
    {
        super(ctxt, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
        this.ctxt = ctxt;
    }

    // public DatabaseHelper(Context context)
    // {
    // super(context, DATABASE_NAME, null, DATABASE_VERSION,
    // R.raw.ormlite_config);
    // }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource)
    {
        try
        {
            Log.i(LOG_TAG, "onCreate");
            Log.w(LOG_TAG, "###################ON CREATE##############################");
            createTables();

        }
        catch (SQLException e)
        {
            Log.e(LOG_TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        try
        {
            Log.i(LOG_TAG, "onUpgrade");

            dropTables();
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
            


        }
        catch (SQLException e)
        {
            Log.e(LOG_TAG, "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    // //////// TAG DAOs

   

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Tag class. It will create it or
     * just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<Tag, Integer> getTagRuntimeDao()
    {
        if (tagRuntimeDao == null)
        {
            tagRuntimeDao = getRuntimeExceptionDao(Tag.class);
        }
        return tagRuntimeDao;
    }

    // //////// QUOTE DAOs

 
    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Quote class. It will create it
     * or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<Quote, Integer> getQuoteRuntimeDao()
    {
        if (quoteRuntimeDao == null)
        {
            quoteRuntimeDao = getRuntimeExceptionDao(Quote.class);
        }
        return quoteRuntimeDao;
    }

    // //////// AUTHOR DAOs


    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Author class. It will create it
     * or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<Author, Integer> getAuthorRuntimeDao()
    {
        if (authorRuntimeDao == null)
        {
            authorRuntimeDao = getRuntimeExceptionDao(Author.class);
        }
        return authorRuntimeDao;
    }

    // //////// SOURCE DAOs

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Source class. It will create it
     * or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<Source, Integer> getSourceRuntimeDao()
    {
        if (sourceRuntimeDao == null)
        {
            sourceRuntimeDao = getRuntimeExceptionDao(Source.class);
        }
        return sourceRuntimeDao;
    }

    // //////// QUOTE<->TAG MAPPING DAOs

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our QuoteTag class. It will create
     * it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<QuoteTag, Integer> getQuoteTagRuntimeDao()
    {
        if (quoteTagRuntimeDao == null)
        {
            quoteTagRuntimeDao = getRuntimeExceptionDao(QuoteTag.class);
        }
        return quoteTagRuntimeDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close()
    {
        super.close();
        // quoteDao = null;
        // tagDao = null;
        // sourceDao = null;
        // authorDao = null;
        // quoteTagDao = null;
    }

    public void dropTables() throws SQLException
    {
        TableUtils.dropTable(connectionSource, Source.class, true);
        TableUtils.dropTable(connectionSource, Tag.class, true);
        TableUtils.dropTable(connectionSource, Author.class, true);
        TableUtils.dropTable(connectionSource, Quote.class, true);
        TableUtils.dropTable(connectionSource, QuoteTag.class, true);
    }

    public void createTables() throws SQLException
    {
        TableUtils.createTable(connectionSource, Tag.class);
        TableUtils.createTable(connectionSource, Source.class);
        TableUtils.createTable(connectionSource, Author.class);
        TableUtils.createTable(connectionSource, Quote.class);
        TableUtils.createTable(connectionSource, QuoteTag.class);
    }

    public List<Quote> getAllQuotes()
    {
        List<Quote> quotes = null;

        quotes = getQuoteRuntimeDao().queryForAll();

        return quotes;
    }

    public List<Author> getAllAuthors()
    {
        List<Author> authors = null;

        authors = getAuthorRuntimeDao().queryForAll();

        return authors;
    }

    public List<Source> getAllSourceTitles()
    {
        List<Source> sources = null;

        sources = getSourceRuntimeDao().queryForAll();

        return sources;
    }

    public List<Tag> getAllTags()
    {
        List<Tag> tags = null;

        tags = getTagRuntimeDao().queryForAll();

        return tags;
    }

    public List<Quote> lookupQuotesforTag(Tag tag) throws SQLException
    {
        RuntimeExceptionDao<QuoteTag, Integer> quoteTagRuntimeDao = getQuoteTagRuntimeDao();
        RuntimeExceptionDao<Quote, Integer> quoteRuntimeDao = getQuoteRuntimeDao();

        QueryBuilder<QuoteTag, Integer> quoteTagQb = quoteTagRuntimeDao.queryBuilder();

        // just select the quote-id field
        quoteTagQb.selectColumns(QuoteTag.QUOTE_ID_FIELD_NAME);
        // you could also just pass in tag here
        quoteTagQb.where().eq(QuoteTag.TAG_ID_FIELD_NAME, tag);

        // build our outer query for Quote objects
        QueryBuilder<Quote, Integer> quoteQb = quoteRuntimeDao.queryBuilder();
        // where the id matches in the quote-id from the inner query
        quoteQb.where().in(Quote.QUOTE_ID_FIELD_NAME, quoteTagQb);
        PreparedQuery<Quote> query = quoteQb.prepare();

        // query.setArgumentHolderValue(0, tag);
        return quoteRuntimeDao.query(query);
    }

    public List<Tag> lookupTagsForQuote(Quote quote1) throws SQLException
    {
        RuntimeExceptionDao<QuoteTag, Integer> quoteTagRuntimeDao = getQuoteTagRuntimeDao();
        RuntimeExceptionDao<Tag, Integer> tagRuntimeDao = getTagRuntimeDao();

        QueryBuilder<QuoteTag, Integer> quoteTagQb = quoteTagRuntimeDao.queryBuilder();

        // just select the tag-id field
        quoteTagQb.selectColumns(QuoteTag.TAG_ID_FIELD_NAME);
        // you could also just pass in quote here
        quoteTagQb.where().eq(QuoteTag.QUOTE_ID_FIELD_NAME, quote1);

        // build our outer query for Tag objects
        QueryBuilder<Tag, Integer> tagQb = tagRuntimeDao.queryBuilder();
        // where the id matches in the tag-id from the inner query
        tagQb.where().in(Tag.TAG_ID_FIELD_NAME, quoteTagQb);
        PreparedQuery<Tag> query = tagQb.prepare();

        return tagRuntimeDao.query(query);
    }

    public void clearDB()
    {
        try
        {
            dropTables();
            createTables();
        }
        catch (SQLException e)
        {
            Log.e(LOG_TAG, "Can't clear database", e);
            throw new RuntimeException(e);
        }

    }

    public Quote getQuote(int quoteID)
    {
        return getQuoteRuntimeDao().queryForId(quoteID);
        // TODO: exceptions, etc
    }

    public List<Tag> getTagsAssignedToQuote(Quote quote)
    {
        // TODO Auto-generated method stub

        List<Tag> tags = new ArrayList<Tag>();
        try
        {
            tags = lookupTagsForQuote(quote);
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return tags;
    }

    public void setFavouriteByQuoteId(boolean isFavourite, int quoteId)
    {
        Quote quoteToChange = getQuote(quoteId);

        quoteToChange.setIsFavourite(isFavourite);

        getQuoteRuntimeDao().update(quoteToChange);
    }

    public int getRandomFavouriteQuoteId(int previousQuoteID)
    {
        List<Quote> favQuotes = getQuoteRuntimeDao().queryForEq(Quote.QUOTE_IS_FAVOURITE_FIELD_NAME, true);

        if (favQuotes.size() == 1) // only one favourite
        { return favQuotes.get(0).getId(); }

        if (favQuotes.size() > 0) // many favourites
        {

            Random rng = new Random();

            Quote favQuote;
            do
            {
                favQuote = favQuotes.get(rng.nextInt(favQuotes.size()));
            }
            while (favQuote.getId() == previousQuoteID); // means it's the same...

            return favQuote.getId();
        }
        else
        // no favourites
        {

            return -1;
        }
    }
}
