
package com.mjaworski.myQuotes.DB;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.util.SparseIntArray;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.mjaworski.myQuotes.R;
import com.mjaworski.myQuotes.DB.Model.Author;
import com.mjaworski.myQuotes.DB.Model.Quote;
import com.mjaworski.myQuotes.DB.Model.QuoteTag;
import com.mjaworski.myQuotes.DB.Model.Source;
import com.mjaworski.myQuotes.DB.Model.Tag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DatabasePopulator
{
    private static final String LOG_TAG = "database_populator";
    private Context ctxt;
    private RuntimeExceptionDao<Tag, Integer> tagDao;
    private RuntimeExceptionDao<Source, Integer> sourceDao;
    private RuntimeExceptionDao<Quote, Integer> quoteDao;
    private RuntimeExceptionDao<QuoteTag, Integer> quoteTagDao;

    public DatabasePopulator(Context ctxt)
    {
        this.ctxt = ctxt;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctxt);

        tagDao = dbHelper.getTagRuntimeDao();
        sourceDao = dbHelper.getSourceRuntimeDao();
        quoteDao = dbHelper.getQuoteRuntimeDao();
        quoteTagDao = dbHelper.getQuoteTagRuntimeDao();
    }

    private String getPath()
    {
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator +
        ctxt.getString(R.string.app_folder) + File.separator + ctxt.getString(R.string.covers_folder);
        
        
        return path;
    }
    
    public void generateData()
    {
        final Random rng = new Random();

        final String picPath = getPath() + File.separator + "bc_";
        final int picSuffixMax = 20;

        final int dateRangeInMS = 1000 * 60 * 60 * 24 * 365;
        final long now = System.currentTimeMillis();

        String[] names = ctxt.getResources().getStringArray(R.array.array_names);
        String[] titles = ctxt.getResources().getStringArray(R.array.array_books);
        String[] quotes = ctxt.getResources().getStringArray(R.array.array_quotes);
        String[] tags = ctxt.getResources().getStringArray(R.array.array_tags);

        ArrayList<Author> authorsObj = new ArrayList<Author>();

        for (String name : names)
        {
            authorsObj.add(new Author(name));
        }

        ArrayList<Source> sourcesObj = new ArrayList<Source>();

        Source s = null;
        for (String title : titles)
        {
            String picPathFull = null;
            
            if (rng.nextDouble() < 0.9)
            {
                picPathFull = picPath + rng.nextInt(picSuffixMax) + ".jpg";
            }

            s = new Source(title, authorsObj.get(rng.nextInt(authorsObj.size())), picPathFull);
            

            Log.i(LOG_TAG, "creating source:" + s.toString());
            
            try
            {
                sourceDao.createOrUpdate(s);
                sourcesObj.add(s);
            }
            catch (Exception e)
            {
                Log.w(LOG_TAG, "source:" + s.toString() + " exists");
                e.printStackTrace();
            }
        }

        Date d = null;
        ArrayList<Quote> quotesObj = new ArrayList<Quote>();
        for (String quote : quotes)
        {
            Quote q = new Quote(quote, sourcesObj.get(rng.nextInt(sourcesObj.size())));
            d = new Date(now - rng.nextInt(dateRangeInMS));

            q.setTimeStampModified(d);
            
            q.setIsFavourite(rng.nextFloat() > 0.9); // 10% of quotes will be favourite
            
            Log.i(LOG_TAG, "creating quote:" + q.toString() + "(" + d + ")");
            quotesObj.add(q);
            quoteDao.createOrUpdate(q);
        }

        final int tagsCount = tags.length;
        final int quotesCount = quotes.length;

        ArrayList<Tag> tagsObj = new ArrayList<Tag>();
        Tag t = null;
        List<Tag> matchingTag = null;
        for (String tag : tags)
        {
            matchingTag =  tagDao.queryForEq(Tag.TAG_FIELD_NAME, tag);
            if (matchingTag.size() > 0)
            {
                tagsObj.add(matchingTag.get(0));
            }
            else
            {
                t = new Tag(tag);
                Log.i(LOG_TAG, "creating tag:" + t.toString());
                tagDao.createOrUpdate(t);
                tagsObj.add(t);
            }
        }

        for (int quoteId = 0; quoteId < quotesCount; quoteId++)
        {
            int howManyTagsToAdd = rng.nextInt(5); // 0-4 tags

            SparseIntArray used = new SparseIntArray(tagsCount);
            for (int i = 0; i < howManyTagsToAdd; i++)
            {
                int randomTagIndex;
                do
                {
                    randomTagIndex = rng.nextInt(tagsCount);
                }
                while (used.get(randomTagIndex) != 0);

                quoteTagDao.create(new QuoteTag(quotesObj.get(quoteId), tagsObj.get(randomTagIndex)));
                used.put(randomTagIndex, 1);
            }

        }
        
        copyCoverAssets();

    }

    private void copyCoverAssets()
    {        
        AssetManager assetManager = ctxt.getAssets();
        String[] files = null;
        final String coversFolder = "covers";
        
        String pathToCoversOnDevice = getPath() + File.separator;

        File f = new File(pathToCoversOnDevice);
        f.mkdirs();
        
        try
        {
            files = assetManager.list(coversFolder);
        }
        catch (IOException e)
        {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files)
        {
            InputStream in = null;
            OutputStream out = null;
            try
            {
                in = assetManager.open(coversFolder + File.separator + filename);
                out = new FileOutputStream(pathToCoversOnDevice + filename);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            }
            catch (IOException e)
            {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }
}
