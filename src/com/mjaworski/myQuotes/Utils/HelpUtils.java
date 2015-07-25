
package com.mjaworski.myQuotes.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.webkit.WebView;

import com.mjaworski.myQuotes.QuoteModelFragment;
import com.mjaworski.myQuotes.R;
import com.mjaworski.myQuotes.DB.DatabaseHelper;
import com.mjaworski.myQuotes.DB.DatabasePopulator;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.widget.TextView;

import java.sql.SQLException;

/**
 * A set of helper methods for showing contextual help information in the app.
 */
public class HelpUtils
{
    public static void showAbout(FragmentActivity activity)
    {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_about");
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new AboutDialog().show(ft, "dialog_about");
    }

    public static class AboutDialog extends DialogFragment
    {

        private static final String VERSION_UNAVAILABLE = "N/A";

        public AboutDialog()
        {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            // Get app version
            PackageManager pm = getActivity().getPackageManager();
            String packageName = getActivity().getPackageName();
            String versionName;
            try
            {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                versionName = info.versionName;
            }
            catch (PackageManager.NameNotFoundException e)
            {
                versionName = VERSION_UNAVAILABLE;
            }

            // Build the about body view and append the link to see OSS licenses
            SpannableStringBuilder aboutBody = new SpannableStringBuilder();
            aboutBody.append(Html.fromHtml(getString(R.string.about_body, versionName)));

            SpannableString licensesLink = new SpannableString(getString(R.string.about_licenses));
            licensesLink.setSpan(new ClickableSpan()
            {
                @Override
                public void onClick(View view)
                {
                    HelpUtils.showOpenSourceLicenses(getActivity());
                }
            }, 0, licensesLink.length(), 0);
            aboutBody.append("\n");
            aboutBody.append(licensesLink);
            
            
            SpannableString addContentLink = new SpannableString(getString(R.string.about_add_content));
            addContentLink.setSpan(new ClickableSpan()
            {
                @Override
                public void onClick(View view)
                {
                    QuoteModelFragment.executeAsyncTask(new InsertSampleDataTask(getActivity()));                    
                }
            }, 0, addContentLink.length(), 0);
            aboutBody.append("\n\n");
            aboutBody.append(addContentLink);
            
            SpannableString clearContentLink = new SpannableString(getString(R.string.about_clear_content));
            clearContentLink.setSpan(new ClickableSpan()
            {
                @Override
                public void onClick(View view)
                {
                    QuoteModelFragment.executeAsyncTask(new DeleteDataTask(getActivity()));
                }
            }, 0, clearContentLink.length(), 0);
            aboutBody.append("\n\n");
            aboutBody.append(clearContentLink);

            
            
            

            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            TextView aboutBodyView = (TextView) layoutInflater.inflate(R.layout.dialog_about, null);
            aboutBodyView.setText(aboutBody);
            aboutBodyView.setMovementMethod(new LinkMovementMethod());

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_about)
                    .setView(aboutBodyView)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }

    public static void showOpenSourceLicenses(FragmentActivity activity)
    {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_licenses");
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new OpenSourceLicensesDialog().show(ft, "dialog_licenses");
    }

    public static class OpenSourceLicensesDialog extends DialogFragment
    {

        public OpenSourceLicensesDialog()
        {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            WebView webView = new WebView(getActivity());
            webView.loadUrl("file:///android_asset/oss.html");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.about_licenses)
                    .setView(webView)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }


    private static class InsertSampleDataTask extends AsyncTask<Void, Void, Void>
    {
        private Activity ctxt;
        
        public InsertSampleDataTask(Activity ctxt)
        {
            this.ctxt = ctxt;
        }

        @Override
        protected Void doInBackground(Void... nothing)
        {
            DatabasePopulator dp = new DatabasePopulator(ctxt);
            dp.generateData();
            return null;

        }

        @Override
        public void onPostExecute(Void arg0)
        {
            MsgUtils.confirm("Added - restart app", ctxt);
        }
    }

    private static class DeleteDataTask extends AsyncTask<Void, Void, Void>
    {
        private Activity ctxt;
        private Exception error = null;
        
        public DeleteDataTask(Activity ctxt)
        {
            this.ctxt = ctxt;
        }

        @Override
        protected Void doInBackground(Void... nothing)
        {
            try
            {
                DatabaseHelper.getInstance(ctxt.getApplicationContext()).dropTables();
                DatabaseHelper.getInstance(ctxt.getApplicationContext()).createTables();
            }
            catch (SQLException e)
            {
                error = e;
            }
            
            return null;

        }

        @Override
        public void onPostExecute(Void arg0)
        {
            if (error == null)
            {
                MsgUtils.confirm("Cleared - restart app", ctxt);
            }
            else
            {
                MsgUtils.alert("Error when clearing", ctxt);
            }
        }
    }
}
