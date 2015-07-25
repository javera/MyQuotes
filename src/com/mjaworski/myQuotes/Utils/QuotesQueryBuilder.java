package com.mjaworski.myQuotes.Utils;

import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;

import com.mjaworski.myQuotes.TagQuoteListFragment;
import com.mjaworski.myQuotes.DB.Model.Author;
import com.mjaworski.myQuotes.DB.Model.Quote;
import com.mjaworski.myQuotes.DB.Model.QuoteTag;
import com.mjaworski.myQuotes.DB.Model.Source;
import com.mjaworski.myQuotes.DB.Model.Tag;

public class QuotesQueryBuilder
{
    private final static String[] defaultQuoteColumns = new String[]
            {
                Quote.QUOTE_ID_FIELD_NAME,
                Quote.QUOTE_QUOTATION_FIELD_NAME,
                Quote.QUOTE_IS_FAVOURITE_FIELD_NAME,
                Quote.QUOTE_MODIFIED_FIELD_NAME,
                Source.SOURCE_TITLE_FIELD_NAME,
                Source.SOURCE_IMAGE_PATH_FIELD_NAME,
                Author.AUTHOR_NAME_FIELD_NAME
            };
    

    private final static String defaultJoinedTables =
            "Quote JOIN Source ON Quote." + Quote.SOURCE_ID_FIELD_NAME + " = Source." + Source.SOURCE_ID_FIELD_NAME +
            " JOIN Author ON Source." + Source.AUTHOR_ID_FIELD_NAME + " = Author." + Author.AUTHOR_ID_FIELD_NAME;

    

    private final static String[] defaultQuoteForTagColumns = new String[]
            {
                Quote.QUOTE_ID_FIELD_NAME,
                Quote.QUOTE_QUOTATION_FIELD_NAME,
                Quote.QUOTE_IS_FAVOURITE_FIELD_NAME,
                Quote.QUOTE_MODIFIED_FIELD_NAME,
                Source.SOURCE_TITLE_FIELD_NAME,
                Source.SOURCE_IMAGE_PATH_FIELD_NAME,
                Author.AUTHOR_NAME_FIELD_NAME,
                QuoteTag.TAG_ID_FIELD_NAME,
            };
    
    private final static String tagJoinedTables =
            "Quote JOIN Source ON Quote." + Quote.SOURCE_ID_FIELD_NAME + " = Source." + Source.SOURCE_ID_FIELD_NAME +
            " JOIN Author ON Source." + Source.AUTHOR_ID_FIELD_NAME + " = Author." + Author.AUTHOR_ID_FIELD_NAME + 
            " JOIN quotetag ON Quote." + Quote.QUOTE_ID_FIELD_NAME + " = Quotetag." + QuoteTag.QUOTE_ID_FIELD_NAME;
    
    
    public static String buildQuoteListQuery()
    {
        return buildQuoteListQuery(null, null, true);
    }
    
    public static String buildQuoteListQuery(String textFilter)
    {
        return buildQuoteListQuery(textFilter, null, true);
    }
    
    public static String buildQuoteListQuery(String textFilter, String orderBy, boolean ascending)
    {
        String whereClause = null;
        if (!TextUtils.isEmpty(textFilter))
        {
            final String sqlFilter = "'%" + textFilter + "%'";
            whereClause = String.format("%2$s LIKE %1$s OR %3$s LIKE %1$s OR %4$s LIKE %1$s",
                    sqlFilter,
                    Source.SOURCE_TITLE_FIELD_NAME,
                    Author.AUTHOR_NAME_FIELD_NAME,
                    Quote.QUOTE_QUOTATION_FIELD_NAME);
        }
        
        String orderByClause = null;
        if (!TextUtils.isEmpty(orderBy))
        {
            final String sort = ascending ? " ASC" : " DESC";
            orderByClause = orderBy + sort;
        }
        
        return SQLiteQueryBuilder.buildQueryString(true, defaultJoinedTables, defaultQuoteColumns, whereClause, null, null, orderByClause, null);
    }
    
    public static String buildQuoteForTagListQuery(String textFilter, String orderBy, boolean ascending, int tagId)
    {
        String whereClause = String.format("%1$s = %2$s ", QuoteTag.TAG_ID_FIELD_NAME, tagId);
        if (!TextUtils.isEmpty(textFilter))
        {
            final String sqlFilter = "'%" + textFilter + "%'";
            whereClause += String.format("%2$s LIKE %1$s OR %3$s LIKE %1$s OR %4$s LIKE %1$s",
                    sqlFilter,
                    Source.SOURCE_TITLE_FIELD_NAME,
                    Author.AUTHOR_NAME_FIELD_NAME,
                    Quote.QUOTE_QUOTATION_FIELD_NAME);
        }
        
        String orderByClause = null;
        if (!TextUtils.isEmpty(orderBy))
        {
            final String sort = ascending ? " ASC" : " DESC";
            orderByClause = orderBy + sort;
        }
        
        return SQLiteQueryBuilder.buildQueryString(true, tagJoinedTables, defaultQuoteForTagColumns, whereClause, null, null, orderByClause, null);
    }

    public static String buildTagListQuery()
    {
        return SQLiteQueryBuilder.buildQueryString(true, "tag" , new String[] {Tag.TAG_ID_FIELD_NAME + " as _id", Tag.TAG_FIELD_NAME}, null, null, null, Tag.TAG_FIELD_NAME, null);
    }
}
