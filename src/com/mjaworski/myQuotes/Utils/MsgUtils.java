package com.mjaworski.myQuotes.Utils;

import android.app.Activity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MsgUtils
{

    public static void alert(String msg, Activity activity)
    {
        Crouton.clearCroutonsForActivity(activity);
        Crouton.makeText(activity, msg, Style.ALERT).show();        
    }
    

    public static void info(String msg, Activity activity)
    {
        Crouton.clearCroutonsForActivity(activity);
        Crouton.makeText(activity, msg, Style.INFO).show();        
    }

    public static void confirm(String msg, Activity activity)
    {
        Crouton.clearCroutonsForActivity(activity);
        Crouton.makeText(activity, msg, Style.CONFIRM).show();        
    }
}
