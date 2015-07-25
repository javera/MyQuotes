
package com.mjaworski.myQuotes;

import android.content.Intent;
import android.os.Bundle;

import org.holoeverywhere.app.Activity;

public class DisplayQuoteActivity extends Activity
{

    public static final String QUOTE_ID = "quote_id";
    private DisplayQuoteFragment displayQuoteFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null)
        {
            // if missing quote_id then new quote, else existing quote
            int quoteID = getIntent().getIntExtra(QUOTE_ID, -1);

            displayQuoteFragment = DisplayQuoteFragment.newInstance(quoteID);

            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, displayQuoteFragment).commit();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == HomeFragmentPager.REQUEST_CODE_EDIT && resultCode == RESULT_OK)
        {
            setResult(HomeFragmentPager.REQUEST_CODE_EDIT);
            finish();
        }
    }

}
