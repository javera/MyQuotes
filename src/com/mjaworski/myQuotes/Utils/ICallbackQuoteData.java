package com.mjaworski.myQuotes.Utils;

import com.mjaworski.myQuotes.DB.Model.Quote;

public interface ICallbackQuoteData
{
    void passQuote(Quote quote, boolean isDifferent);
}
