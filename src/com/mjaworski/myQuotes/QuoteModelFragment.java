
package com.mjaworski.myQuotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.mjaworski.myQuotes.DB.DatabaseHelper;
import com.mjaworski.myQuotes.DB.Model.Quote;
import com.mjaworski.myQuotes.DB.Model.Tag;
import com.mjaworski.myQuotes.Utils.BaseModelFragment;
import com.mjaworski.myQuotes.Utils.ICallbackQuoteData;
import com.mjaworski.myQuotes.Utils.ICallbackTagsData;
import com.mjaworski.myQuotes.Utils.ImageUtils;
import com.mjaworski.myQuotes.Utils.MsgUtils;

import java.util.List;

public class QuoteModelFragment extends BaseModelFragment
{
    private static String LOG_TAG = "QuoteModelFragment";

    private Quote quote = null;
    private List<Tag> tagsExistingInUI = null;
    private QuoteFromDBTask retrieveQuoteTask = null;
    private Uri quoteCoverURI = null;
    private Bitmap quoteCoverImage = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    public void setIsFavourite(boolean newFavouriteValue)
    {
        executeAsyncTask(new SetFavouriteTask(newFavouriteValue), getActivity().getApplicationContext());
    }
    

    public void setIsFavourite(boolean newFavouriteValue, int quoteId)
    {
        executeAsyncTask(new SetFavouriteTask(newFavouriteValue, quoteId), getActivity().getApplicationContext());
    }


    /**
     * Called when data is loaded from the db, now just display it in the UI
     */
    synchronized private void deliverQuoteToTheForm(boolean isDifferent)
    {
        retrieveQuoteTask = null;
        final ICallbackQuoteData parentFragment = (ICallbackQuoteData) getTargetFragment();
        parentFragment.passQuote(quote, isDifferent);
    }

    /**
     * Called when data is loaded from the db, now just display it in the UI
     */
    synchronized private void deliverTagsToTheForm()
    {
        final ICallbackTagsData parentFragment = (ICallbackTagsData) getTargetFragment();
        parentFragment.passTags(tagsExistingInUI);
    }

    synchronized private void deliverCoverImageModel()
    {
        if (quoteCoverImage != null)
        {
            final DisplayQuoteFragment parentFragment = (DisplayQuoteFragment) getTargetFragment();
            parentFragment.setCover(quoteCoverImage);
        }
    }

    /**
     * Loads {@link com.mjaworski.myQuotes.DB.Model.Quote quote} from db (or uses retained instance) and displays it in
     * the UI
     * 
     * @param quoteID quote id
     */
    synchronized public void retrieveQuoteById(int quoteID)
    {
        if (quoteID > 0)
        {
            if (quote == null || tagsExistingInUI == null)
            {
                if (retrieveQuoteTask == null)
                {
                    retrieveQuoteTask = new QuoteFromDBTask(quoteID);
                    executeAsyncTask(retrieveQuoteTask, getActivity().getApplicationContext());
                }
            }
        }
    }

    public void clearStoredQuote()
    {
        this.quote = null;
        if (retrieveQuoteTask != null)
        {
            retrieveQuoteTask.cancel(true);
        }
        this.retrieveQuoteTask = null;
    }

    /**
     * Loads {@link com.mjaworski.myQuotes.DB.Model.Quote quote} from db (or uses retained instance) and displays it in
     * the UI
     * 
     * @param quoteID quote id
     */
    synchronized public void retrieveRandomQuote()
    {
        retrieveRandomQuote(getActivity().getApplicationContext());
    }

    /**
     * Loads {@link com.mjaworski.myQuotes.DB.Model.Quote quote} from db (or uses retained instance) and displays it in
     * the UI
     * 
     * @param quoteID quote id
     */
    synchronized public void retrieveRandomQuote(Context ctxt)
    {

        if (quote == null || tagsExistingInUI == null)
        {
            if (retrieveQuoteTask == null)
            {
                retrieveQuoteTask = new QuoteFromDBTask(true, -1);
                executeAsyncTask(retrieveQuoteTask, ctxt);
            }
        }
    }

    public void retrieveAnotherRandomQuote()
    {
        final int prevQuoteId = quote.getId();

        clearStoredQuote();

        retrieveQuoteTask = new QuoteFromDBTask(true, prevQuoteId);
        executeAsyncTask(retrieveQuoteTask, getActivity().getApplicationContext());

    }

    private class QuoteFromDBTask extends AsyncTask<Context, Void, Void>
    {
        private int quoteID;

        boolean randomFavourite = false;
        private int prevQuoteId;

        boolean differentFromPrevious = false;

        public QuoteFromDBTask(int quoteID)
        {
            this.quoteID = quoteID;
        }

        public QuoteFromDBTask(boolean randomFavourite, int prevQuoteId)
        {
            this.randomFavourite = randomFavourite;
            this.prevQuoteId = prevQuoteId;
        }

        @Override
        protected Void doInBackground(Context... ctxt)
        {
            DatabaseHelper db = DatabaseHelper.getInstance(ctxt[0]);

            if (randomFavourite)
            {
                quoteID = db.getRandomFavouriteQuoteId(prevQuoteId);

                if (quoteID == -1)
                {
                    // no such quote....
                    quote = null;
                    return null;
                }
                else if (quoteID == prevQuoteId)
                {
                    // means there are no other quotes
                    differentFromPrevious = false;
                }
                else
                {
                    // means we got a new quote
                    differentFromPrevious = true;
                }
            }

            if (quote == null)
                quote = db.getQuote(quoteID);

            return (null);
        }

        @Override
        public void onPostExecute(Void arg0)
        {
            deliverQuoteToTheForm(differentFromPrevious);
        }
    }

    private class PrepareImageTask extends AsyncTask<Context, Void, Void>
    {
        private float width;
        private float height;

        public PrepareImageTask(float width, float height)
        {
            this.width = width;
            this.height = height;
        }

        @Override
        protected Void doInBackground(Context... ctxt)
        {
            // Image captured and saved to fileUri specified in the Intent
            quoteCoverImage = ImageUtils.decodeSampledBitmapFromUri(quoteCoverURI, width, height);

            return (null);
        }

        @Override
        public void onPostExecute(Void arg0)
        {
            deliverCoverImageModel();
        }
    }

    private class SetFavouriteTask extends AsyncTask<Context, Void, Void>
    {
        private final boolean newFavouriteValue;
        private final int quoteId;
        private Exception error = null;

        public SetFavouriteTask(boolean newFavouriteValue)
        {
            this(newFavouriteValue, quote.getId());
        }

        public SetFavouriteTask(boolean newFavouriteValue, int quoteId)
        {
            this.newFavouriteValue = newFavouriteValue;
            this.quoteId = quoteId;
        }

        @Override
        protected Void doInBackground(Context... ctxt)
        {
            try
            {
                DatabaseHelper.getInstance(ctxt[0]).setFavouriteByQuoteId(newFavouriteValue, quoteId);
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, "Error while changing is favourite " + e.getMessage());
                error = e;
            }

            return null;
        }

        @Override
        public void onPostExecute(Void arg0)
        {
            if (error == null)
            {
                final DisplayQuoteFragment parentFragment = (DisplayQuoteFragment) getTargetFragment();
                quote.setIsFavourite(newFavouriteValue);
                parentFragment.setupFavActionButtons(newFavouriteValue);
                MsgUtils.confirm(getString(R.string.ct_updating_succeed), getActivity());
            }
            else
            {
                MsgUtils.alert(getString(R.string.ct_updating_fav_fail), getActivity());
            }

        }
    }

    public int getCurrentQuoteId()
    {
        return quote.getId();
    }

}
