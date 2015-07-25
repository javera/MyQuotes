
package com.mjaworski.myQuotes;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.holoeverywhere.LayoutInflater;

public class TagQuoteListFragment extends DisplayQuoteListFragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mTagId = getArguments().getInt(BUNDLE_TAG_ID_INDEX, 0);
        mTagName = getArguments().getString(BUNDLE_TAG_NAME_INDEX);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

}
