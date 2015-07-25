
package com.mjaworski.myQuotes.Utils;

import com.mjaworski.myQuotes.BuildConfig;

public class Logger
{
    private enum LogType
    {
        ERROR,
        WARN,
        INFO,
        DEBUG,
        VERBOSE
    }

    public static String LOG_TAG = "com.mjaworski.myQuotes";

    public static void logError(String msg)
    {
        log(msg, LogType.ERROR);
    }
    
    private static void log(String msg, LogType type)
    {
        if (BuildConfig.DEBUG)
        {
            switch (type)
            {
                case ERROR:
                    android.util.Log.e(LOG_TAG, msg);
                    break;
                case WARN:
                    android.util.Log.w(LOG_TAG, msg);
                    break;
                case INFO:
                    android.util.Log.i(LOG_TAG, msg);
                    break;
                case DEBUG:
                    android.util.Log.d(LOG_TAG, msg);
                    break;
                case VERBOSE:
                    android.util.Log.v(LOG_TAG, msg);
                    break;
            }
            
          
        } 
    }

}
