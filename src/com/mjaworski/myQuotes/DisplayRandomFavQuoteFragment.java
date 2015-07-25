
package com.mjaworski.myQuotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import butterknife.InjectView;
import butterknife.Views;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.mjaworski.myQuotes.DB.Model.Quote;
import com.mjaworski.myQuotes.Utils.ICallbackQuoteData;
import com.mjaworski.myQuotes.Utils.IDataChanged;
import com.mjaworski.myQuotes.Utils.MsgUtils;
import com.mjaworski.myQuotes.Utils.TypefaceTextView;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

public class DisplayRandomFavQuoteFragment extends Fragment implements ICallbackQuoteData, OnClickListener, IDataChanged
{
    protected ShareActionProvider mShareActionProvider;

    private static final String WORKER_FRAGMENT_TAG = "QuoteDisplayDataDeliverer";

    @InjectView(R.id.quote_text)
    TypefaceTextView txtQuote;
    @InjectView(R.id.quote_author)
    TextView txtAuthor;
    @InjectView(R.id.quote_source)
    TextView txtSourceTitle;
    @InjectView(R.id.btn_share_quote)
    Button btnShare;
    @InjectView(R.id.btn_next_fav_quote)
    Button btnNext;
    @InjectView(R.id.btn_add_quote)
    Button btnAdd;

    @InjectView(R.id.fav_quote_buttons_wrapper)
    LinearLayout llQuoteExistsButtons;

    @InjectView(R.id.no_fav_quote_buttons_wrapper)
    LinearLayout llQuoteNotExistsButtons;

    protected QuoteModelFragment mWorkFragment;

    /**
     * Create a fragment that displays a random favourite quote (just quotation, author and source).
     */
    public static DisplayRandomFavQuoteFragment newInstance()
    {
        DisplayRandomFavQuoteFragment frag = new DisplayRandomFavQuoteFragment();

        return (frag);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getFragmentManager();

        // Check to see if we have retained the worker fragment.
        mWorkFragment = (QuoteModelFragment) fm.findFragmentByTag(WORKER_FRAGMENT_TAG);

        // If not retained (or first time running), we need to create it.
        if (mWorkFragment == null)
        {
            mWorkFragment = new QuoteModelFragment();
            // Tell it who it is working with.
            mWorkFragment.setTargetFragment(this, 0);
            fm.beginTransaction().add(mWorkFragment, WORKER_FRAGMENT_TAG).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // inflate the layout and set references to widgets existing in xml
        View result = inflater.inflate(R.layout.display_fav_quote, container, false);
        Views.inject(this, result);
        setHasOptionsMenu(true);
        setButtonClickListeners();

        return (result);
    }

    private void setButtonClickListeners()
    {
        btnNext.setOnClickListener(this);
        btnShare.setOnClickListener(this);
    }

    @Override
    public void onResume()
    {
        retrieveRandomQuotes();
        super.onResume();
    }

    private void retrieveRandomQuotes()
    {
        if (getActivity() != null && mWorkFragment.isAdded())
        {
            mWorkFragment.retrieveRandomQuote();
        }
        else
        {
            mWorkFragment.retrieveRandomQuote(getActivity().getApplicationContext());
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_share_quote:
                doShare();
                break;

            case R.id.btn_next_fav_quote:
                mWorkFragment.retrieveAnotherRandomQuote();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setSubtitle(null);
        inflater.inflate(R.menu.quote_fav_display, menu);
        if (llQuoteNotExistsButtons.getVisibility() == View.VISIBLE)
        {
            menu.findItem(R.id.edit).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Call to update the share intent
    private void doShare()
    {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, Quote.prepareShareText(
                txtQuote.getText().toString(),
                txtAuthor.getText().toString(),
                txtSourceTitle.getText().toString()));
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quote");
        shareIntent.setType("text/plain");

        startActivity(shareIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.edit:
                Intent i = new Intent(this.getActivity(), AddEditQuoteActivity.class);
                i.putExtra(AddEditQuoteActivity.QUOTE_ID, mWorkFragment.getCurrentQuoteId());
                getActivity().startActivityForResult(i, HomeFragmentPager.REQUEST_CODE_EDIT);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    private void setUIelementsVisibility(boolean hasFavQuote)
    {
        if (hasFavQuote)
        {

            llQuoteExistsButtons.setVisibility(View.VISIBLE);
            llQuoteNotExistsButtons.setVisibility(View.GONE);

            this.txtAuthor.setVisibility(View.VISIBLE);
            this.txtSourceTitle.setVisibility(View.VISIBLE);
        }
        else
        {
            llQuoteExistsButtons.setVisibility(View.GONE);
            llQuoteNotExistsButtons.setVisibility(View.VISIBLE);

            this.txtAuthor.setVisibility(View.GONE);
            this.txtSourceTitle.setVisibility(View.GONE);
        }
        getSupportActivity().invalidateOptionsMenu();
    }

    @Override
    public void passQuote(Quote quote, boolean isDifferent)
    {
        if (quote == null)
        {
            setUIelementsVisibility(false);

            this.txtQuote.setText(getString(R.string.no_fav_quotes_msg));

            btnAdd.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent i = new Intent(DisplayRandomFavQuoteFragment.this.getActivity(), AddEditQuoteActivity.class);
                    getActivity().startActivityForResult(i, HomeFragmentPager.REQUEST_CODE_ADD);

                }
            });
        }
        else
        {
            if (isDifferent)
            {
                setUIelementsVisibility(true);

                this.txtQuote.setText(quote.getQuotation() == null ? "" : quote.getQuotation());
                if (quote.getFrom() != null)
                {

                    this.txtAuthor.setVisibility(View.VISIBLE);
                    this.txtSourceTitle.setVisibility(View.VISIBLE);
                    this.txtSourceTitle.setText(quote.getFrom().getSourceTitle());
                    this.txtAuthor.setText(quote.getAuthorName());
                }
                else
                {
                    this.txtAuthor.setVisibility(View.GONE);
                    this.txtSourceTitle.setVisibility(View.GONE);
                }
            }
            else
            {
                // means we are only getting the same quote...
                MsgUtils.info("No other favourite quotes", getActivity());
            }
        }
    }

    @Override
    public void dataChanged()
    {
        retrieveRandomQuotes();
    }
}
