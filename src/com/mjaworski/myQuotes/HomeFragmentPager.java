
package com.mjaworski.myQuotes;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.mjaworski.myQuotes.DB.DatabasePopulator;
import com.mjaworski.myQuotes.Utils.HelpUtils;
import com.mjaworski.myQuotes.Utils.IDataChanged;
import com.mjaworski.myQuotes.Utils.MsgUtils;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;

import java.util.Locale;

public class HomeFragmentPager extends Activity
{

    public static final String UPDATED_QUOTES = "Updated_quotes";

    public static final int REQUEST_CODE_FROM_FRAGMENT = 0;
    public static final int REQUEST_CODE_ADD = 1;

    public static final int REQUEST_CODE_EDIT = 2;

    public static final int REQUEST_CODE_DELETE = 3;
    public static final int REQUEST_CODE_CHANGE_FAV = 4;

    public static final int REQUEST_CODE_VIEW = 5;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory.
     * If this becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tabs_pager);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        PagerTabStrip pagerTitleStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        pagerTitleStrip.setDrawFullUnderline(true);
        pagerTitleStrip.setTabIndicatorColorResource(R.color.emphasis_semi_transparent);

        mViewPager.setCurrentItem(1);
        mViewPager.setOffscreenPageLimit(3);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.fragment_tabs_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                return (true);
            case R.id.about:
                HelpUtils.showAbout(this);
                return (true);
            case R.id.createnew:
                Intent i = new Intent(this, AddEditQuoteActivity.class);
                startActivityForResult(i, REQUEST_CODE_ADD);
                return (true);

        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    public void onBackPressed()
    {
        if (mViewPager.getCurrentItem() == 0)
        {
            Fragment fragmentWithChildren = mSectionsPagerAdapter.getFragment(0);

            // If the fragment exists and has some back-stack entry
            if (fragmentWithChildren != null
                    && fragmentWithChildren.getChildFragmentManager().getBackStackEntryCount() > 0)
            {
                // Get the fragment fragment manager - and pop the backstack

                fragmentWithChildren.getChildFragmentManager().popBackStack();
            }
            // Else, nothing in the direct fragment back stack
            else
            {
                // Let super handle the back press
                super.onBackPressed();
            }
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ((requestCode == REQUEST_CODE_FROM_FRAGMENT || requestCode == REQUEST_CODE_ADD
                || requestCode == REQUEST_CODE_EDIT || requestCode == REQUEST_CODE_DELETE)
                && resultCode == RESULT_OK)
        {
            // quote added / deleted or modified
            notifyOfDataChange();
        }

        if (requestCode == REQUEST_CODE_VIEW && resultCode == REQUEST_CODE_EDIT)
        {
            notifyOfDataChange();
        }
    }

    public void notifyOfDataChange()
    {
        final IDataChanged listFrag = (IDataChanged) mSectionsPagerAdapter.getFragment(2);
        if (listFrag != null)
        {
            listFrag.dataChanged();
        }

        final IDataChanged randomQuoteFrag = (IDataChanged) mSectionsPagerAdapter.getFragment(1);
        if (randomQuoteFrag != null)
        {
            randomQuoteFrag.dataChanged();
        }

        final IDataChanged tagList = (IDataChanged) mSectionsPagerAdapter.getFragment(0);
        if (tagList != null)
        {
            tagList.dataChanged();
        }
    }

    public void notifyOfFavQuoteDataChange()
    {
        notifyOfDataChange();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        private SparseArray<Fragment> mPageReferenceMap = new SparseArray<Fragment>();

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            if (position == 0)
            {

                Fragment tagListMasterFragment = TagMasterFragment.newInstance();
                mPageReferenceMap.put(position, tagListMasterFragment);
                return tagListMasterFragment;
            }

            if (position == 1)
            {
                Fragment favQuoteFragment = DisplayRandomFavQuoteFragment.newInstance();
                mPageReferenceMap.put(position, favQuoteFragment);
                return favQuoteFragment;
            }

            if (position == 2)
            {

                Fragment quoteListFragment = DisplayQuoteListFragment.newInstance();
                mPageReferenceMap.put(position, quoteListFragment);
                return quoteListFragment;
            }

            return null;
        }

        @Override
        public int getCount()
        {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            Locale l = Locale.getDefault();
            switch (position)
            {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }

        @Override
        public void destroyItem(View container, int position, Object object)
        {

            super.destroyItem(container, position, object);

            mPageReferenceMap.remove(position);
        }

        public Fragment getFragment(int key)
        {
            return mPageReferenceMap.get(key);
        }
    }
}
