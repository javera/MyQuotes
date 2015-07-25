
package com.mjaworski.myQuotes;

import android.content.Intent;
import android.os.Bundle;

import de.keyboardsurfer.android.widget.crouton.Crouton;

import org.holoeverywhere.app.Activity;

public class AddEditQuoteActivity extends Activity
{
    AddEditQuoteFragment formFragment;

    public static final String QUOTE_ID = "quote_id";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String sharedText = "";
        if (Intent.ACTION_SEND.equals(action) && type != null)
        {

            if ("text/plain".equals(type))
            {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }

        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null)
        {
            // if missing quote_id then new quote, else existing quote
            int quoteID = getIntent().getIntExtra(QUOTE_ID, -1);
            if (sharedText != null)
            {
                formFragment = AddEditQuoteFragment.newInstance(quoteID, sharedText);
            }
            else
            {
                formFragment = AddEditQuoteFragment.newInstance(quoteID);
            }

            

            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, formFragment).commit();

        }



    }

    @Override
    protected void onDestroy()
    {
        // Cancels all pending Croutons
        Crouton.cancelAllCroutons();
        // Clear all Croutons for an Activity
        Crouton.clearCroutonsForActivity(this);

        super.onDestroy();
    }

    public void quoteUpdated()
    {
        setResult(RESULT_OK);
        finish();
    }

}
