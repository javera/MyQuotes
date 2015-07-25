
package com.mjaworski.myQuotes.Utils;

import com.mjaworski.myQuotes.DB.Model.Tag;

import java.util.List;

public interface ICallbackTagsData
{
    void passTags(List<Tag> tags);
}
