package com.mjaworski.myQuotes.DB;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.mjaworski.myQuotes.DB.Model.Author;
import com.mjaworski.myQuotes.DB.Model.Quote;
import com.mjaworski.myQuotes.DB.Model.QuoteTag;
import com.mjaworski.myQuotes.DB.Model.Source;
import com.mjaworski.myQuotes.DB.Model.Tag;

import junit.framework.Assert;

import java.sql.SQLException;
import java.util.List;

public class DatabaseTester
{
    private Context ctxt;
   
    public DatabaseTester(Context ctxt)
    {
        this.ctxt = ctxt;
    }
    

    public void testDBCreate()
    {
        // /// TEST DB
        RuntimeExceptionDao<Tag, Integer> tagTestDao = DatabaseHelper.getInstance(ctxt).getTagRuntimeDao();
        RuntimeExceptionDao<Source, Integer> sourceTestDao = DatabaseHelper.getInstance(ctxt).getSourceRuntimeDao();
        RuntimeExceptionDao<Author, Integer> authorTestDao = DatabaseHelper.getInstance(ctxt).getAuthorRuntimeDao();
        RuntimeExceptionDao<Quote, Integer> quoteTestDao = DatabaseHelper.getInstance(ctxt).getQuoteRuntimeDao();

        testDBCreateTags(tagTestDao);
        testDBCreateSources(sourceTestDao);
        testDBCreateAuthors(authorTestDao);
        try
        {
            testDBCreateQuotes(tagTestDao, sourceTestDao, authorTestDao, quoteTestDao);
        }
        catch (SQLException e)
        {
            Log.e(DatabaseHelper.class.getName(), "Failed when doing create quotes test", e);
            throw new RuntimeException(e);
        }

    }

    private void testDBCreateQuotes(RuntimeExceptionDao<Tag, Integer> tagTestDao,
            RuntimeExceptionDao<Source, Integer> sourceTestDao,
            RuntimeExceptionDao<Author, Integer> authorTestDao,
            RuntimeExceptionDao<Quote, Integer> quoteTestDao) throws SQLException
    {
        // create 2 instances of authors
        Author hemingway = new Author("Ernest Hemingway");
        authorTestDao.create(hemingway);

        Author sapkowski = new Author("Andrzej Sapkowski");
        authorTestDao.create(sapkowski);

        // create 3 instances of source titles
        Source oldManAndSea = new Source("The Old Man and the Sea", hemingway);
        sourceTestDao.create(oldManAndSea);

        Source bellTolls = new Source("For Whom the Bell Tolls", hemingway);
        sourceTestDao.create(bellTolls);

        Source wiedzmin = new Source("Wiedźmin", sapkowski);
        sourceTestDao.create(wiedzmin);

        // create an associated Quote for the author, book and tag
        // Quote from The Old Man and the Sea by Hemingway, both sad and
        // religion
        String quote1text = "‘Ay,′ he said aloud. There is no translation for this word and perhaps it is just a noise such as a man might make, involuntarily, feeling the nail go through his hands and into the wood.";
        Quote quote1 = new Quote(quote1text, oldManAndSea);
        quoteTestDao.create(quote1);

        // create an associated Quote for the author, book and tag
        // Quote from The Old Man and the Sea by Hemingway, both sad and
        // religion
        String quote2text = "Because thou art a miracle of deafness... It is not that thou art stupid. Thou art simply deaf. One who is deaf cannot hear music. Neither can he hear the radio. So he might say, never having heard them, that such things do not exist.";
        Quote quote2 = new Quote(quote2text, bellTolls);
        quoteTestDao.create(quote2);



        // / NOW CHECK TAGS!

        // create 3 instances of tags
        Tag weird = new Tag("Weird");
        tagTestDao.create(weird);

        Tag religion = new Tag("Religion");
        tagTestDao.create(religion);

        Tag sad = new Tag("Sad");
        tagTestDao.create(sad);

        Tag happy = new Tag("Happy");
        tagTestDao.create(happy);

        RuntimeExceptionDao<QuoteTag, Integer> quoteTagRuntimeDao = DatabaseHelper.getInstance(ctxt).getQuoteTagRuntimeDao();
        // link the quote and the tag together in the join table
        QuoteTag quote1tag1 = new QuoteTag(quote1, religion);
        quoteTagRuntimeDao.create(quote1tag1);

        // have quote have a second tag
        QuoteTag quote1tag2 = new QuoteTag(quote1, sad);
        quoteTagRuntimeDao.create(quote1tag2);

        // this time 2nd quote has the same tag as the 1st one (sad)
        QuoteTag quote2tag2 = new QuoteTag(quote2, sad);
        quoteTagRuntimeDao.create(quote2tag2);

        /*
         * Now go back and do various queries to look things up.
         */

        /*
         * show me all of a user's posts:
         */
        // quote1 should have 2 tags
        List<Tag> quote1tags = DatabaseHelper.getInstance(ctxt).lookupTagsForQuote(quote1);
        Assert.assertEquals(2, quote1tags.size());
        Assert.assertEquals(religion.getId(), quote1tags.get(0).getId());
        Assert.assertEquals(religion.getTag(), quote1tags.get(0).getTag());
        Assert.assertEquals(sad.getId(), quote1tags.get(1).getId());
        Assert.assertEquals(sad.getTag(), quote1tags.get(1).getTag());

        // quote2 should have only 1 tag
        List<Tag> quote2tags = DatabaseHelper.getInstance(ctxt).lookupTagsForQuote(quote2);
        Assert.assertEquals(1, quote2tags.size());
        Assert.assertEquals(sad.getTag(), quote2tags.get(0).getTag());

        /*
         * show me all of the quotes that have a tag 'religion'.
         */
        // 'religion' should only have 1 corresponding quote
        List<Quote> tag1quotes = DatabaseHelper.getInstance(ctxt).lookupQuotesforTag(religion);
        Assert.assertEquals(1, tag1quotes.size());
        Assert.assertEquals(quote1.getId(), tag1quotes.get(0).getId());

        // 'sad' should have 2 corresponding quotes
        List<Quote> tag2quotes = DatabaseHelper.getInstance(ctxt).lookupQuotesforTag(sad);
        Assert.assertEquals(2, tag2quotes.size());
        Assert.assertEquals(quote1.getId(), tag2quotes.get(0).getId());
        Assert.assertEquals(quote1.getQuotation(), tag2quotes.get(0).getQuotation());
        Assert.assertEquals(quote2.getId(), tag2quotes.get(1).getId());
        Assert.assertEquals(quote2.getQuotation(), tag2quotes.get(1).getQuotation());

    }
    
    private void testDBCreateAuthors(RuntimeExceptionDao<Author, Integer> authorTestDao)
    {
        // / AUTHORS
        Author testAuthor1 = new Author("Author #1");
        authorTestDao.create(testAuthor1);

        Author testAuthor2 = new Author("Author #2");
        authorTestDao.create(testAuthor2);

        List<Author> newAuthors = authorTestDao.queryForAll();

        // sanity checks
        Assert.assertEquals("Should have found both of the tags", 2, newAuthors.size());
        Assert.assertTrue(authorTestDao.objectsEqual(testAuthor1, newAuthors.get(0)));
        Assert.assertTrue(authorTestDao.objectsEqual(testAuthor2, newAuthors.get(1)));
    }

    private void testDBCreateSources(RuntimeExceptionDao<Source, Integer> sourceTestDao)
    {

        // / SOURCES
        Source testSource1 = new Source("book #1", null);
        sourceTestDao.create(testSource1);

        Source testSource2 = new Source("book #2", null);
        sourceTestDao.create(testSource2);

        List<Source> newSources = sourceTestDao.queryForAll();

        // sanity checks
        Assert.assertEquals("Should have found both of the tags", 2, newSources.size());
        Assert.assertTrue(sourceTestDao.objectsEqual(testSource1, newSources.get(0)));
        Assert.assertTrue(sourceTestDao.objectsEqual(testSource2, newSources.get(1)));

    }

    private void testDBCreateTags(RuntimeExceptionDao<Tag, Integer> tagTestDao)
    {
        // / TAGS
        Tag testTag1 = new Tag("tag #1");
        tagTestDao.create(testTag1);

        Tag testTag2 = new Tag("Tag #2");
        tagTestDao.create(testTag2);

        List<Tag> newTags = tagTestDao.queryForAll();

        // sanity checks
        Assert.assertEquals("Should have found both of the tags", 2, newTags.size());
        Assert.assertTrue(tagTestDao.objectsEqual(testTag1, newTags.get(0)));
        Assert.assertTrue(tagTestDao.objectsEqual(testTag2, newTags.get(1)));
    }
}
