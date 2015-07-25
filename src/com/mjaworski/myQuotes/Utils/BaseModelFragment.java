
package com.mjaworski.myQuotes.Utils;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import org.holoeverywhere.app.Fragment;

/**
 * @author Marek Jaworski Just a base class that implements proper executeAsyncTask as well as specifies
 *         setRetainInstance to true
 */
public class BaseModelFragment extends Fragment
{
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
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
}
