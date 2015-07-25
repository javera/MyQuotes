
package com.mjaworski.myQuotes;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.InjectView;
import butterknife.Views;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.mjaworski.myQuotes.DB.DatabaseHelper;
import com.mjaworski.myQuotes.DB.Model.Quote;
import com.mjaworski.myQuotes.DB.Model.Tag;
import com.mjaworski.myQuotes.Utils.ImageUtils;
import com.mjaworski.myQuotes.Utils.MsgUtils;
import com.mjaworski.myQuotes.Utils.TypefaceTextView;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import java.util.List;

public class DisplayQuoteFragment extends Fragment
{
    protected int quoteID;
    protected ShareActionProvider mShareActionProvider;

    private static final String LOG_TAG = "DisplayQuoteFragment";
    private static final String WORKER_FRAGMENT_TAG = "QuoteDisplayDataDeliverer";

    @InjectView(R.id.quote_text)
    TypefaceTextView txtQuote;
    @InjectView(R.id.quote_author)
    TextView txtAuthor;
    @InjectView(R.id.quote_source)
    TextView txtSourceTitle;
    @InjectView(R.id.tags_display_wrapper)
    LinearLayout llTagsWrapper;
    @InjectView(R.id.cover_image)
    ImageView coverImageView;

    protected FormModelFragment mWorkFragment;

    private boolean shouldDisplayFavAction = false;
    private boolean isFavourite;

    /**
     * Create a fragment that displays a quote.
     * 
     * @param quoteID valid quote id
     * @return fragment in edit or create {@link com.mjaworski.myQuotes.enums.Mode Mode}
     */
    public static DisplayQuoteFragment newInstance(int quoteID)
    {
        DisplayQuoteFragment frag = new DisplayQuoteFragment();
        Bundle args = new Bundle();
        args.putInt(DisplayQuoteActivity.QUOTE_ID, quoteID);
        frag.setArguments(args);

        return (frag);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getFragmentManager();

        // Check to see if we have retained the worker fragment.
        mWorkFragment = (FormModelFragment) fm.findFragmentByTag(WORKER_FRAGMENT_TAG);

        // If not retained (or first time running), we need to create it.
        if (mWorkFragment == null)
        {
            mWorkFragment = new FormModelFragment();
            // Tell it who it is working with.
            mWorkFragment.setTargetFragment(this, 0);
            fm.beginTransaction().add(mWorkFragment, WORKER_FRAGMENT_TAG).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // inflate the layout and set references to widgets existing in xml
        View result = inflater.inflate(R.layout.display_quote, container, false);
        Views.inject(this, result);
        setHasOptionsMenu(true);

        quoteID = getArguments().getInt(AddEditQuoteActivity.QUOTE_ID, -1);

        return (result);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        getSupportActionBar().setHomeButtonEnabled(true);
        inflater.inflate(R.menu.quote_display, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        // Locate MenuItem with ShareActionProvider
        MenuItem favButton = menu.findItem(R.id.favourite);

        if (shouldDisplayFavAction)
        {
            favButton.setVisible(true);
            if (isFavourite)
            {
                favButton.setIcon(getResources().getDrawable(R.drawable.ic_star));
            }
            else
            {
                favButton.setIcon(getResources().getDrawable(R.drawable.ic_star_outline));
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent)
    {
        if (mShareActionProvider != null)
        {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent i = new Intent(this.getActivity(), HomeFragmentPager.class);
                startActivity(i);
                return (true);
            case R.id.edit:
                i = new Intent(this.getActivity(), AddEditQuoteActivity.class);
                i.putExtra(AddEditQuoteActivity.QUOTE_ID, quoteID);
                getActivity().startActivityForResult(i, HomeFragmentPager.REQUEST_CODE_EDIT);
                return (true);
            case R.id.favourite:
                boolean newFavouriteValue = !isFavourite; // we are flipping the value
                mWorkFragment.setIsFavourite(newFavouriteValue);
                return (true);

        }
        return (super.onOptionsItemSelected(item));
    }

    /**
     * Called from a worker fragment. Passed data is filled in to the appropriate form fields.
     * 
     * @param quoteToEdit {@link com.mjaworski.myQuotes.DB.Model.Quote Quote} instance to be displayed
     * @param quoteToEditTags List of {@link com.mjaworski.myQuotes.DB.Model.Tag Tags} to be displayed
     */
    @SuppressLint("InlinedApi")
    protected void displayQuoteData(Quote quoteToEdit, List<Tag> quoteToEditTags)
    {
        // first, quote fields
        this.txtQuote.setText(quoteToEdit.getQuotation() == null ? "" : quoteToEdit.getQuotation());
        if (quoteToEdit.getFrom() != null)
        {
            this.txtSourceTitle.setText(quoteToEdit.getFrom().getSourceTitle());
            this.txtAuthor.setText(quoteToEdit.getFrom().getAuthor() == null ? "" : quoteToEdit.getFrom().getAuthor()
                    .getName());
        }

        // now tags
        if (quoteToEditTags != null && !quoteToEditTags.isEmpty())
        {
            for (final Tag t : quoteToEditTags)
            {
                addTagToUI(t);
            }
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, quoteToEdit.getShareText());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quote");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
        {
            shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, quoteToEdit.getHtmlShareText());
        }

        shareIntent.setType("text/plain");

        setShareIntent(shareIntent);

        setupFavActionButtons(quoteToEdit.getIsFavourite());

    }

    public void setupFavActionButtons(boolean quoteIsFavourite)
    {

        shouldDisplayFavAction = true;
        isFavourite = quoteIsFavourite;
        getSupportActivity().supportInvalidateOptionsMenu();

    }

    /**
     * Creates and displays a view in the UI that represents added tag.
     * 
     * @param tag {@link com.mjaworski.myQuotes.DB.Model.Tag Tag} to be added to UI
     */
    public void addTagToUI(final Tag tag)
    {
        TextView tagEntry = (TextView) getLayoutInflater().inflate(R.layout.tag_display_template, null);

        // inflate button from template
        tagEntry.setText(tag.getTag());
        llTagsWrapper.addView(tagEntry);
    }

    /**
     * Used to set the cover image for source
     * 
     * @param coverImage
     */
    public void setCover(Bitmap coverImage)
    {
        coverImageView.setImageBitmap(coverImage);
    }

    /**
     * Returns quote id
     * 
     * @return -1 if not in edit mode, quote id otherwise
     */
    public int getQuoteID()
    {
        return quoteID;
    }

    public static class FormModelFragment extends Fragment
    {

        private Quote quoteToEdit = null;
        private List<Tag> tagsExistingInUI = null;
        private QuoteFromDBTask editQuoteTask = null;
        private Uri coverURI = null;
        private Bitmap coverImage = null;

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);
            setRetainInstance(true);

            deliverCoverImageModel();

            // since our UI fragment does not retain anything, we readd all tags
            deliverAllTagsModel();

            final DisplayQuoteFragment parentFragment = (DisplayQuoteFragment) getTargetFragment();
            retrieveQuoteFromDB(parentFragment.getQuoteID());

        }

        public void setIsFavourite(boolean newFavouriteValue)
        {

            executeAsyncTask(new SetFavouriteTask(newFavouriteValue), getActivity().getApplicationContext());

        }

        /**
         * Called when data is loaded from the db, now just display it in the UI
         */
        synchronized private void deliverQuoteToTheForm()
        {
            final DisplayQuoteFragment parentFragment = (DisplayQuoteFragment) getTargetFragment();
            parentFragment.displayQuoteData(quoteToEdit, tagsExistingInUI);

            if (quoteToEdit.getFrom() != null && quoteToEdit.getFrom().getImagePath() != null)
            {
                coverURI = Uri.parse(quoteToEdit.getFrom().getImagePath());

                final float WIDTH_IN_PX = ImageUtils.COVER_IMAGE_WIDTH_DP * getResources().getDisplayMetrics().density;
                final float HEIGHT_IN_PX = ImageUtils.COVER_IMAGE_HEIGHT_DP
                        * getResources().getDisplayMetrics().density;

                // The image should now be used in the cover ImageView
                executeAsyncTask(new PrepareImageTask(WIDTH_IN_PX, HEIGHT_IN_PX));
            }

        }

        /**
         * Recreating tags after configuration change
         */
        synchronized private void deliverAllTagsModel()
        {
            // recreate tags in UI
            if (tagsExistingInUI != null && !tagsExistingInUI.isEmpty())
            {
                final DisplayQuoteFragment parentFragment = (DisplayQuoteFragment) getTargetFragment();

                for (Tag t : tagsExistingInUI)
                {
                    parentFragment.addTagToUI(t);
                }
            }
        }

        synchronized private void deliverCoverImageModel()
        {
            if (coverImage != null)
            {
                final DisplayQuoteFragment parentFragment = (DisplayQuoteFragment) getTargetFragment();
                parentFragment.setCover(coverImage);
            }
        }

        /**
         * Loads {@link com.mjaworski.myQuotes.DB.Model.Quote quote} from db (or uses retained instance) and displays it
         * in the UI
         * 
         * @param quoteID quote id
         */
        synchronized public void retrieveQuoteFromDB(int quoteID)
        {
            if (quoteID > 0)
            {
                if (quoteToEdit == null || tagsExistingInUI == null)
                {
                    if (editQuoteTask == null)
                    {
                        editQuoteTask = new QuoteFromDBTask(quoteID);
                        executeAsyncTask(editQuoteTask, getActivity().getApplicationContext());
                    }
                }
                else
                {
                    // need to recreate the quote string as it's using custom view, hence the data is not retained
                    final DisplayQuoteFragment parentFragment = (DisplayQuoteFragment) getTargetFragment();
                    parentFragment.setQuote(quoteToEdit.getQuotation());

                    parentFragment.setupFavActionButtons(quoteToEdit.getIsFavourite());
                }
            }
        }

        @TargetApi(11)
        static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params)

        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
            else
            {
                task.execute(params);
            }
        }

        private class QuoteFromDBTask extends AsyncTask<Context, Void, Void>
        {
            private int quoteID;

            public QuoteFromDBTask(int quoteID)
            {
                this.quoteID = quoteID;
            }

            @Override
            protected Void doInBackground(Context... ctxt)
            {
                if (quoteToEdit == null)
                    quoteToEdit = DatabaseHelper.getInstance(ctxt[0]).getQuote(quoteID);

                if (tagsExistingInUI == null)
                    tagsExistingInUI = DatabaseHelper.getInstance(ctxt[0]).getTagsAssignedToQuote(quoteToEdit);

                return (null);
            }

            @Override
            public void onPostExecute(Void arg0)
            {
                deliverQuoteToTheForm();
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
                coverImage = ImageUtils.decodeSampledBitmapFromUri(coverURI, width, height);

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
            private boolean newFavouriteValue;
            private Exception error = null;

            public SetFavouriteTask(boolean newFavouriteValue)
            {
                this.newFavouriteValue = newFavouriteValue;
            }

            @Override
            protected Void doInBackground(Context... ctxt)
            {
                try
                {
                    DatabaseHelper.getInstance(ctxt[0]).setFavouriteByQuoteId(newFavouriteValue, quoteToEdit.getId());
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
                    quoteToEdit.setIsFavourite(newFavouriteValue);
                    parentFragment.setupFavActionButtons(newFavouriteValue);
                    MsgUtils.confirm(getString(R.string.ct_updating_succeed), getActivity());
                }
                else
                {
                    MsgUtils.alert(getString(R.string.ct_updating_fav_fail), getActivity());
                }

            }
        }

    }

    public void setQuote(String quotation)
    {
        txtQuote.setText(quotation);
    }

}
