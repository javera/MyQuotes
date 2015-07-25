
package com.mjaworski.myQuotes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnCloseListener;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.j256.ormlite.misc.TransactionManager;
import com.mjaworski.myQuotes.DB.DatabaseHelper;
import com.mjaworski.myQuotes.DB.Model.Author;
import com.mjaworski.myQuotes.DB.Model.Quote;
import com.mjaworski.myQuotes.DB.Model.Source;
import com.mjaworski.myQuotes.Utils.BaseModelFragment;
import com.mjaworski.myQuotes.Utils.IDataChanged;
import com.mjaworski.myQuotes.Utils.MsgUtils;
import com.mjaworski.myQuotes.Utils.QuotesQueryBuilder;
import com.mjaworski.myQuotes.Utils.SQLiteCursorLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.ToggleButton;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class DisplayQuoteListFragment extends ListFragment
        implements OnQueryTextListener, OnCloseListener, LoaderManager.LoaderCallbacks<Cursor>, IDataChanged
{
    private static final String LOG_TAG = "DisplayQuoteListFragment";
    public static final String BUNDLE_ORDER_BY_INDEX = "CurrentOrderBy";
    public static final String BUNDLE_ORDER_BY_ID_INDEX = "CurrentOrderById";
    public static final String BUNDLE_ORDER_BY_ASCENDING_INDEX = "CurrentOrderByAscending";
    public static final String BUNDLE_FILTER_INDEX = "CurrentFilter";
    public static final String BUNDLE_TAG_ID_INDEX = "TagId";
    public static final String BUNDLE_TAG_NAME_INDEX = "TagName";

    private DisplayImageOptions options;

    // search
    private MenuItem mSearchItem;
    private CursorAdapter mAdapter;// This is the Adapter being used to display the list's data.
    private SearchView mSearchView; // The SearchView for doing filtering.
    private String mCurFilter; // If non-null, this is the current filter the user has provided.

    // sorting
    private MenuItem mSortItem;
    private MySpinner mSpinner;
    private int mCurrentOrderById;
    private boolean mOrderByAscending;

    // tags
    protected int mTagId;
    protected String mTagName;

    private TextView mTextTagName;

    /**
     * Create a fragment that displays a random favourite quote (just quotation, author and source).
     */
    public static DisplayQuoteListFragment newInstance()
    {
        DisplayQuoteListFragment frag = new DisplayQuoteListFragment();

        return (frag);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_ORDER_BY_ID_INDEX, mCurrentOrderById);
        outState.putBoolean(BUNDLE_ORDER_BY_ASCENDING_INDEX, mOrderByAscending);
        outState.putString(BUNDLE_FILTER_INDEX, mCurFilter);
        outState.putInt(BUNDLE_TAG_ID_INDEX, mTagId);
        outState.putString(BUNDLE_TAG_NAME_INDEX, mTagName);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.cover_template)
                .cacheInMemory()
                .displayer(new RoundedBitmapDisplayer(2))
                .build();

        // Give some text to display if there is no data. In a real
        // application this would come from a resource.
        setEmptyText("No quotes to display...");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        getListView().setFastScrollEnabled(true);

        // restore state
        if (savedInstanceState != null)
        {
            // Restore last state for checked position.
            mCurrentOrderById = savedInstanceState.getInt(BUNDLE_ORDER_BY_ID_INDEX, -1);
            mOrderByAscending = savedInstanceState.getBoolean(BUNDLE_ORDER_BY_ASCENDING_INDEX, true);
            mCurFilter = savedInstanceState.getString(BUNDLE_FILTER_INDEX);
            mTagId = savedInstanceState.getInt(BUNDLE_TAG_ID_INDEX, 0);
            mTagName = savedInstanceState.getString(BUNDLE_TAG_NAME_INDEX);

            mSpinner.setSelection(mCurrentOrderById);
        }
        else
        {
            // means it'a first time the fragment is loaded
            // set current order by id to -1, mSpinner.setSelection will update it to 0
            mCurrentOrderById = -1;
            // choose the first item
            mSpinner.setSelection(0);
        }

        if (mTagName != null)
        {
            mTextTagName.setVisibility(View.VISIBLE);
            mTextTagName.setText(mTagName);
        }
        else
        {
            mTextTagName.setVisibility(View.GONE);
        }

        getListView().setClickable(false);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new MyCursorAdapter(getActivity(), null);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        ListView lv = (ListView) layout.findViewById(android.R.id.list);
        ViewGroup parent = (ViewGroup) lv.getParent();

        // Remove ListView and add CustomView in its place
        int lvIndex = parent.indexOfChild(lv);
        parent.removeViewAt(lvIndex);
        LinearLayout mLinearLayout = (LinearLayout) inflater.inflate(R.layout.quote_list, container, false);
        parent.addView(mLinearLayout, lvIndex);

        mTextTagName = (TextView) layout.findViewById(R.id.tag_info);

        mSpinner = new MySpinner(getSupportActionBar().getThemedContext());
        mSpinner.setSaveEnabled(false);

        ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource
                (
                        getSupportActionBar().getThemedContext(),
                        R.array.sorting_options,
                        R.layout.sherlock_spinner_item
                );

        spinnerArrayAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerArrayAdapter);

        OnItemSelectedListener spinnerListener = new MyOnItemSelectedListener();
        mSpinner.setOnItemSelectedEvenIfUnchangedListener(spinnerListener);

        return layout; // super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume()
    {
        updateActionBarTitles();
        super.onResume();
    }

    public class MySpinner extends Spinner
    {
        OnItemSelectedListener listener;

        public MySpinner(Context ctxt)
        {
            super(ctxt);
        }

        @Override
        public void setSelection(int position)
        {
            super.setSelection(position);
            if (listener != null)
                listener.onItemSelected(null, null, position, 0);
        }

        public void setOnItemSelectedEvenIfUnchangedListener(
                OnItemSelectedListener listener)
        {
            this.listener = listener;
        }

    }

    private String getCurrentDBOrderBy()
    {
        final String[] columnNames = getResources().getStringArray(R.array.sorting_db_columns);

        if (mCurrentOrderById < 0 || mCurrentOrderById >= columnNames.length)
        {
            Log.e(LOG_TAG, "error when selecting sort order");
            return null;
        }

        return columnNames[mCurrentOrderById];
    }

    public void dataChanged()
    {
        getLoaderManager().restartLoader(0, null, this);
    }

    public class MyOnItemSelectedListener implements OnItemSelectedListener
    {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
        {

            if (pos == mCurrentOrderById)
            {
                // user chose the same sort column, means we need to switch asc/desc
                mOrderByAscending = !mOrderByAscending;

            }
            else
            {
                mCurrentOrderById = pos;
                final String currentOrderBy = getCurrentDBOrderBy();
                if (currentOrderBy.contentEquals(Quote.QUOTE_IS_FAVOURITE_FIELD_NAME)
                        || currentOrderBy.contentEquals(Quote.QUOTE_MODIFIED_FIELD_NAME))
                {
                    // the default sorting for these is to show them as favourites first or newest first
                    mOrderByAscending = false;
                }
                else
                {
                    mOrderByAscending = true;
                }

            }

            updateActionBarTitles();

            getLoaderManager().restartLoader(0, null, DisplayQuoteListFragment.this);
            if (mSortItem != null)
            {
                mSortItem.collapseActionView();
            }
        }

        public void onNothingSelected(AdapterView<?> parent)
        {
            Log.w(LOG_TAG, "nothing selected");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // Place an action bar item for searching.
        inflater.inflate(R.menu.display_quote_list, menu);

        mSearchItem = menu.findItem(R.id.search_in_list);
        mSearchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        mSearchView = new SearchView(getActivity());
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchItem.setActionView(mSearchView);

        mSortItem = menu.add("Sort");
        mSortItem.setIcon(R.drawable.ic_action_sort);
        mSortItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        mSortItem.setActionView(mSpinner);

        mSearchItem.setOnActionExpandListener(new OnActionExpandListener()
        {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {

                mSearchView.setQuery("", false);
                return true; // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                // get focus
                item.getActionView().requestFocus();
                if (mSortItem.isActionViewExpanded())
                {
                    mSortItem.collapseActionView();
                }
                return true; // Return true to expand action view
            }
        });

        mSortItem.setOnActionExpandListener(new OnActionExpandListener()
        {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {

                return true; // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                if (mSearchItem.isActionViewExpanded())
                {
                    mSearchItem.collapseActionView();
                }
                return true; // Return true to expand action view
            }
        });

        updateActionBarTitles();
    }

    private void updateActionBarTitles()
    {
        if (mSpinner != null && mSpinner.getSelectedItem() != null)
        {
            getSupportActionBar().setTitle(mSpinner.getSelectedItem().toString());
            getSupportActionBar().setSubtitle(mOrderByAscending ? "Ascending" : "Descending");
        }
    }

    public boolean onQueryTextChange(String newText)
    {
        // Called when the action bar search text has changed. Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;

        // Don't do anything if the filter hasn't actually changed.
        // Prevents restarting the loader when restoring state.
        if (mCurFilter == null && newFilter == null) { return true; }
        if (mCurFilter != null && mCurFilter.equals(newFilter)) { return true; }
        mCurFilter = newFilter;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return true;
    }

    @Override
    public boolean onClose()
    {
        if (!TextUtils.isEmpty(mSearchView.getQuery()))
        {
            mSearchView.setQuery(null, true);
        }

        mSearchItem.collapseActionView();

        return true;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        // This is called when a new Loader needs to be created.

        String query;
        if (mTagId > 0)
        {
            // means we are actually displaying quotes that belong to a certain tag
            query = QuotesQueryBuilder.buildQuoteForTagListQuery(mCurFilter, getCurrentDBOrderBy(), mOrderByAscending,
                    mTagId);
        }
        else
        {
            query = QuotesQueryBuilder.buildQuoteListQuery(mCurFilter, getCurrentDBOrderBy(), mOrderByAscending);
        }

        return new SQLiteCursorLoader(getActivity(), DatabaseHelper.getInstance(getActivity().getApplicationContext()),
                query,
                null);

    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);

        // The list should now be shown.
        if (isResumed())
        {
            setListShown(true);
        }
        else
        {
            setListShownNoAnimation(true);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader)
    {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    private void setSearchQueryText(final String query)
    {
        mSearchItem.expandActionView();
        mSearchView.setQuery(query, false);
    }

    public class MyCursorAdapter extends CursorAdapter
    {
        private final LayoutInflater mInflater;
        private int mQuoteIdIndex;
        private int mQuotationIndex;
        private int mAuthorNameIndex;
        private int mSourceTitleIndex;
        private int mImageUriIndex;
        private int mIsFavIndex;
        private int mModifiedIndex;
        private boolean hasIndexes;
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat(Quote.DATETIME_FORMAT);

        MyCursorAdapter(Context ctxt, Cursor cursor)
        {
            super(ctxt, cursor, 0);

            mInflater = LayoutInflater.from(ctxt);
        }

        @Override
        public boolean hasStableIds()
        {
            return true;
        };

        private void setIndexes(Cursor cursor)
        {
            if (!hasIndexes && cursor != null)
            {
                mQuoteIdIndex = cursor.getColumnIndex(Quote.QUOTE_ID_FIELD_NAME);
                mQuotationIndex = cursor.getColumnIndex(Quote.QUOTE_QUOTATION_FIELD_NAME);
                mIsFavIndex = cursor.getColumnIndex(Quote.QUOTE_IS_FAVOURITE_FIELD_NAME);
                mAuthorNameIndex = cursor.getColumnIndex(Author.AUTHOR_NAME_FIELD_NAME);
                mSourceTitleIndex = cursor.getColumnIndex(Source.SOURCE_TITLE_FIELD_NAME);
                mImageUriIndex = cursor.getColumnIndex(Source.SOURCE_IMAGE_PATH_FIELD_NAME);
                mModifiedIndex = cursor.getColumnIndex(Quote.QUOTE_MODIFIED_FIELD_NAME);
                hasIndexes = true;
            }

        }

        @Override
        public void changeCursor(Cursor cursor)
        {
            setIndexes(cursor);
            super.changeCursor(cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            return mInflater.inflate(R.layout.quote_list_item, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor)
        {
            setIndexes(cursor);

            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder == null)
            {
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            final String quotation = cursor.getString(mQuotationIndex);
            final String authorName = cursor.getString(mAuthorNameIndex);
            final String sourceTitle = cursor.getString(mSourceTitleIndex);
            final String imagePath = cursor.getString(mImageUriIndex);
            final String modifiedDateString = cursor.getString(mModifiedIndex);
            final int quoteID = cursor.getInt(mQuoteIdIndex);
            final boolean isFav = cursor.getShort(mIsFavIndex) == 1 ? true : false;

            holder.quote.setText(quotation);
            holder.source.setText(sourceTitle.length() == 0 ? "Unknown" : sourceTitle);
            holder.author.setText(authorName.length() == 0 ? "Unknown" : authorName);

            final LinearLayout hiddenPart = holder.hiddenRowPart;
            final ImageView expanderImg = holder.imgExpander;

            holder.visibleRowPart.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    if (hiddenPart.getVisibility() == View.GONE)
                    {
                        hiddenPart.setVisibility(View.VISIBLE);
                        expanderImg.setImageResource(R.drawable.expander_close_holo_light);
                    }
                    else
                    {
                        hiddenPart.setVisibility(View.GONE);
                        expanderImg.setImageResource(R.drawable.expander_open_holo_light);
                    }

                }
            });

            // five hidden part buttons
            holder.btnDelete.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    AlertDialog diaBox = AskOption();
                    diaBox.show();
                }

                private AlertDialog AskOption()
                {
                    AlertDialog myQuittingDialogBox = new AlertDialog.Builder(DisplayQuoteListFragment.this
                            .getActivity())
                            // set message, title, and icon
                            .setTitle(R.string.delete)
                            .setMessage("Do you want to Delete this quote?")
                            .setIcon(R.drawable.ic_action_delete)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    BaseModelFragment.executeAsyncTask(new DeleteQuoteTask(quoteID),
                                            getActivity().getApplicationContext());
                                }
                            })
                            .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    return myQuittingDialogBox;

                }
            });

            holder.btnEdit.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Intent i = new Intent(getActivity(), AddEditQuoteActivity.class);
                    i.putExtra(AddEditQuoteActivity.QUOTE_ID, quoteID);
                    getActivity().startActivityForResult(i, HomeFragmentPager.REQUEST_CODE_EDIT);
                }
            });

            holder.btnShare.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, Quote.prepareShareText(
                            quotation, authorName, sourceTitle));
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quote");
                    shareIntent.setType("text/plain");
                    startActivity(shareIntent);
                }
            });

            holder.btnMoreFromTitle.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    if (sourceTitle.equals(""))
                    {
                        MsgUtils.alert(getString(R.string.ct_source_blank), getActivity());
                        return;
                    }

                    setSearchQueryText(sourceTitle);
                }
            });

            holder.btnMoreFromAuthor.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    if (authorName.equals(""))
                    {
                        MsgUtils.alert(getString(R.string.ct_author_blank), getActivity());
                        return;
                    }

                    setSearchQueryText(authorName);

                }
            });

            String modifiedDateParsed = "";
            if (modifiedDateString != null && modifiedDateString.length() > 0)
            {

                try
                {
                    final Date date = dateFormatter.parse(modifiedDateString);

                    modifiedDateParsed = DateUtils.formatDateTime(getActivity(), date.getTime(),
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
                }
                catch (ParseException e)
                {
                    Log.e(LOG_TAG, "Error while parsing date... " + e.getMessage());
                }
            }

            holder.modified.setText(modifiedDateParsed);

            holder.isFavourite.setChecked(isFav);
            holder.isFavourite.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final ToggleButton isFavTB = (ToggleButton) v;
                    BaseModelFragment.executeAsyncTask(new SetFavouriteTask(isFavTB.isChecked(), quoteID),
                            getActivity().getApplicationContext());

                }
            });

            ImageLoader.getInstance().displayImage(imagePath == null || imagePath == "" ? null : "file://" + imagePath,
                    holder.cover,
                    DisplayQuoteListFragment.this.options);

        }

        class ViewHolder
        {
            ImageView cover = null;
            TextView quote = null;
            TextView source = null;
            TextView author = null;
            TextView modified = null;
            ToggleButton isFavourite = null;
            RelativeLayout visibleRowPart = null;
            LinearLayout hiddenRowPart = null;
            ImageButton btnDelete = null;
            ImageButton btnEdit = null;
            ImageButton btnShare = null;
            ImageButton btnMoreFromAuthor = null;
            ImageButton btnMoreFromTitle = null;
            ImageView imgExpander = null;

            ViewHolder(View row)
            {
                this.cover = (ImageView) row.findViewById(R.id.row_img);
                this.quote = (TextView) row.findViewById(R.id.row_quote_text);
                this.author = (TextView) row.findViewById(R.id.row_author);
                this.source = (TextView) row.findViewById(R.id.row_source);
                this.modified = (TextView) row.findViewById(R.id.row_modified);
                this.isFavourite = (ToggleButton) row.findViewById(R.id.row_favourite);
                this.imgExpander = (ImageView) row.findViewById(R.id.caret_down);

                this.visibleRowPart = (RelativeLayout) row.findViewById(R.id.row_visible_part);
                this.hiddenRowPart = (LinearLayout) row.findViewById(R.id.row_hidden_part);
                this.btnDelete = (ImageButton) row.findViewById(R.id.row_hidden_delete);
                this.btnEdit = (ImageButton) row.findViewById(R.id.row_hidden_edit);
                this.btnShare = (ImageButton) row.findViewById(R.id.row_hidden_share);
                this.btnMoreFromAuthor = (ImageButton) row.findViewById(R.id.row_hidden_author);
                this.btnMoreFromTitle = (ImageButton) row.findViewById(R.id.row_hidden_book);
            }
        }

        private class DeleteQuoteTask extends AsyncTask<Context, Void, Void>
        {
            private final int quoteId;
            private Exception error = null;

            public DeleteQuoteTask(int quoteId)
            {
                this.quoteId = quoteId;
            }

            @Override
            protected Void doInBackground(Context... ctxt)
            {
                try
                {
                    final DatabaseHelper db = DatabaseHelper.getInstance(ctxt[0]);

                    TransactionManager.callInTransaction(db.getConnectionSource(),
                            new Callable<Void>()
                            {
                                public Void call() throws Exception
                                {
                                    db.getQuoteRuntimeDao().deleteById(quoteId);
                                    return null;
                                }
                            });
                }
                catch (SQLException e)
                {
                    this.error = e;
                }

                return null;
            }

            @Override
            public void onPostExecute(Void nothing)
            {
                if (error == null)
                {
                    MsgUtils.confirm(getString(R.string.ct_deleting_succeed), getActivity());
                    ((HomeFragmentPager) getActivity()).notifyOfDataChange();
                }
                else
                {
                    MsgUtils.alert(getString(R.string.ct_deleting_fail), getActivity());
                }
            }
        }

        private class SetFavouriteTask extends AsyncTask<Context, Void, Void>
        {
            private final boolean newFavouriteValue;
            private final int quoteId;
            private Exception error = null;

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
                    MsgUtils.confirm(getString(R.string.ct_updating_succeed), getActivity());
                    ((HomeFragmentPager) getActivity()).notifyOfFavQuoteDataChange();
                }
                else
                {
                    MsgUtils.alert(getString(R.string.ct_updating_fav_fail), getActivity());
                }

            }
        }
    }
}
