
package com.mjaworski.myQuotes;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.mjaworski.myQuotes.Utils.IDataChanged;
import com.mjaworski.myQuotes.Utils.IReplaceListener;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.FrameLayout;

public class TagMasterFragment extends Fragment implements IReplaceListener, IDataChanged
{
    // this will act as a fragment container, representing one page in the ViewPager
    
    private static final String INNER_FRAGMENT_TAG_LIST = "tagListFragment";
    private static final String INNER_FRAGMENT_QUOTE_LIST_FOR_TAG = "quoteListForTagFragment";

    public static TagMasterFragment newInstance()
    {
        TagMasterFragment tmf = new TagMasterFragment();
        return tmf;
    }
    
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        TagListFragment tagListFragment  = (TagListFragment) getChildFragmentManager().findFragmentByTag(INNER_FRAGMENT_TAG_LIST);
        TagQuoteListFragment quoteListForTagFragment  = (TagQuoteListFragment) getChildFragmentManager().findFragmentByTag(INNER_FRAGMENT_QUOTE_LIST_FOR_TAG);
        if (tagListFragment != null)
        {
            tagListFragment.onCreateOptionsMenu(menu, inflater);
        }
        if (quoteListForTagFragment != null)
        {
            quoteListForTagFragment.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        FrameLayout fl = new FrameLayout(getActivity());
        fl.setId(10000);
        if (getChildFragmentManager().findFragmentByTag(INNER_FRAGMENT_TAG_LIST) == null)
        {
            TagListFragment tagList = TagListFragment.getInstance();


            getChildFragmentManager().beginTransaction().add(10000, tagList, INNER_FRAGMENT_TAG_LIST).commit();
        }
        
        setHasOptionsMenu(true);
        
        return fl;
    }

    // required because it seems the getChildFragmentManager only "sees"
    // containers in the View of the parent Fragment.
    @Override
    public void onReplace(Bundle args)
    {
        if (getChildFragmentManager().findFragmentByTag(INNER_FRAGMENT_QUOTE_LIST_FOR_TAG) == null)
        {
            TagQuoteListFragment tagQuoteListFrag = new TagQuoteListFragment();
            tagQuoteListFrag.setArguments(args);
            getChildFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)   
                    .replace(10000, tagQuoteListFrag, INNER_FRAGMENT_QUOTE_LIST_FOR_TAG).addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void dataChanged()
    {
        TagListFragment tagListFragment  = (TagListFragment) getChildFragmentManager().findFragmentByTag(INNER_FRAGMENT_TAG_LIST);
        TagQuoteListFragment quoteListForTagFragment  = (TagQuoteListFragment) getChildFragmentManager().findFragmentByTag(INNER_FRAGMENT_QUOTE_LIST_FOR_TAG);
        if (tagListFragment != null)
        {
            tagListFragment.dataChanged();
        }
        if (quoteListForTagFragment != null)
        {
            quoteListForTagFragment.dataChanged();
        }
    }

}

