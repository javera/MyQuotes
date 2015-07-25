
package com.mjaworski.myQuotes;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;

import com.mjaworski.myQuotes.DB.DatabaseHelper;
import com.mjaworski.myQuotes.DB.Model.Tag;
import com.mjaworski.myQuotes.Utils.IDataChanged;
import com.mjaworski.myQuotes.Utils.IReplaceListener;
import com.mjaworski.myQuotes.Utils.QuotesQueryBuilder;
import com.mjaworski.myQuotes.Utils.SQLiteCursorLoader;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.TextView;

public class TagListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, IDataChanged, OnItemClickListener
{

    private IReplaceListener mListener;
    private MyCursorAdapter mAdapter;// This is the Adapter being used to display the list's data.

    public static TagListFragment getInstance()
    {
        TagListFragment tlf = new TagListFragment();
        return tlf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mListener = (IReplaceListener) this.getParentFragment();

        return super.onCreateView(inflater, container, savedInstanceState);

    }

    public void dataChanged()
    {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data. In a real
        // application this would come from a resource.
        setEmptyText("No tags to display...");


        getListView().setFastScrollEnabled(true);
        getListView().setClickable(true);
        
        getListView().setOnItemClickListener(this);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new MyCursorAdapter(getActivity(), null);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);

    }

    

    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        // This is called when a new Loader needs to be created. This
        // sample only has one Loader, so we don't care about the ID.

        return new SQLiteCursorLoader(getActivity(), DatabaseHelper.getInstance(getActivity().getApplicationContext()),
                QuotesQueryBuilder.buildTagListQuery(),
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

    public class MyCursorAdapter extends CursorAdapter
    {
        private final LayoutInflater mInflater;
        private int mTagIdIndex;
        private int mTagIndex;
        private boolean hasIndexes;

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
                mTagIdIndex = cursor.getColumnIndex(Tag.TAG_ID_FIELD_NAME);
                mTagIndex = cursor.getColumnIndex(Tag.TAG_FIELD_NAME);
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
            return mInflater.inflate(android.R.layout.simple_list_item_1, null);
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

            final String tag = cursor.getString(mTagIndex);
            
            holder.tagText.setText(tag);
        }
        
        public String getNameForPosition(int position)
        {
            return ((Cursor) getItem(position)).getString(mTagIndex);
        }

        class ViewHolder
        {
            TextView tagText = null;

            ViewHolder(View row)
            {
                this.tagText = (TextView) row.findViewById(android.R.id.text1);
            }
        }

    }

    @Override
    public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id)
    {
        long tagId =  mAdapter.getItemId(position);
        
        Bundle args = new Bundle();
        args.putInt(DisplayQuoteListFragment.BUNDLE_TAG_ID_INDEX, (int) tagId);
        args.putString(DisplayQuoteListFragment.BUNDLE_TAG_NAME_INDEX, mAdapter.getNameForPosition(position));
        
        mListener.onReplace(args);
        
    }
}
