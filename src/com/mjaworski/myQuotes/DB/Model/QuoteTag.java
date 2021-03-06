
package com.mjaworski.myQuotes.DB.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class QuoteTag
{
    public final static String ID_FIELD_NAME = "id";
    public final static String QUOTE_ID_FIELD_NAME = "quote_id";
    public final static String TAG_ID_FIELD_NAME = "tag_id";

    /**
     * This id is generated by the database and set on the object when it is
     * passed to the create method. An id is needed in case we need to update or
     * delete this object in the future.
     */
    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    int id;

    // This is a foreign object which just stores the id from the User object in
    // this table.
    @DatabaseField(foreign = true, columnName = QUOTE_ID_FIELD_NAME)
    Quote quote;

    // This is a foreign object which just stores the id from the Post object in
    // this table.
    @DatabaseField(foreign = true, columnName = TAG_ID_FIELD_NAME)
    Tag tag;

    QuoteTag()
    {
        // needed for ORMLite
    }

    public QuoteTag(Quote quote, Tag tag)
    {
        this.quote = quote;
        this.tag = tag;
    }
}
