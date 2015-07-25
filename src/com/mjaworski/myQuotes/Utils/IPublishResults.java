
package com.mjaworski.myQuotes.Utils;

public interface IPublishResults
{
    void taskSucceeded(String msg);

    void taskFailed(String msg);

    void setProgress(int currentValue, int maxValue, String msg);

}
