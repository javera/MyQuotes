
package com.mjaworski.myQuotes.DB.Model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable
public class Quote
{
    public static final String QUOTE_ID_FIELD_NAME = "_id";
    public static final String QUOTE_FULL_ID_FIELD_NAME = "Quote._id";
    public static final String QUOTE_QUOTATION_FIELD_NAME = "quotation";
    public static final String SOURCE_ID_FIELD_NAME = "source_id";
    public static final String QUOTE_IS_FAVOURITE_FIELD_NAME = "is_fav";

    public static final String QUOTE_MODIFIED_FIELD_NAME = "timeStampModified";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @DatabaseField(generatedId = true, columnName = QUOTE_ID_FIELD_NAME)
    private int _id;

    @DatabaseField(columnName = QUOTE_QUOTATION_FIELD_NAME)
    private String quotation;

    @DatabaseField(foreign = true, columnName = SOURCE_ID_FIELD_NAME, foreignAutoRefresh = true, foreignAutoCreate = true)
    private Source from;

    @DatabaseField(columnName = QUOTE_IS_FAVOURITE_FIELD_NAME, index = true)
    private boolean isFavourite;

    @DatabaseField
    private String url;
    @DatabaseField(dataType = DataType.DATE, format = DATETIME_FORMAT)
    private Date timeStampAdded;
    @DatabaseField(dataType = DataType.DATE, columnName = QUOTE_MODIFIED_FIELD_NAME)
    private Date timeStampModified;
    @DatabaseField
    private boolean isDeleted;

    public Quote()
    {
        // needed for ORMLite
    }

    public Quote(String quotation, Source from)
    {
        this(quotation, from, null);
    }

    public Quote(String quotation, Source from, String url)
    {
        this.quotation = quotation;
        this.from = from;
        this.url = url;
        this.timeStampAdded = new Date();
        this.timeStampModified = new Date();
    }

    public String getQuotation()
    {
        if (quotation == null) { return ""; }
        return quotation;
    }

    public void setQuotation(String quotation)
    {
        this.quotation = quotation;
    }

    public Source getFrom()
    {
        return from;
    }

    public void setFrom(Source from)
    {
        this.from = from;
    }

    public String getAuthorName()
    {
        if (getFrom() == null || getFrom().getAuthor() == null)
        {
            return "";
        }
        else
        {
            return getFrom().getAuthor().getName();
        }
    }

    public boolean getIsFavourite()
    {
        return isFavourite;
    }

    public void setIsFavourite(boolean isFavourite)
    {
        this.isFavourite = isFavourite;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Date getTimeStampModified()
    {
        return timeStampModified;
    }

    public void setTimeStampModified(Date timeStampModified)
    {
        this.timeStampModified = timeStampModified;
    }

    public boolean isDeleted()
    {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted)
    {
        this.isDeleted = isDeleted;
    }

    public int getId()
    {
        return _id;
    }

    public void setId(int id)
    {
        this._id = id;
    }

    public Date getTimeStampAdded()
    {
        return timeStampAdded;
    }

    public void setTimeStampAdded(Date timeStampAdded)
    {
        this.timeStampAdded = timeStampAdded;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("“").append(quotation);
        sb.append("”\n").append(from.getStringSimpleForm());
        return sb.toString();
    }

    public String getShareText()
    {
        return Quote.prepareShareText(getQuotation(), getAuthorName(), getFrom().getSourceTitle());
    }

    public static String prepareShareText(String quoteText, String quoteAuthor, String quoteSource)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("“").append(quoteText).append("” ");

        if (quoteSource != null && quoteSource != "")
        {
            sb.append("from ").append(quoteSource).append(" ");
        }

        if (quoteAuthor != null && quoteAuthor != "")
        {
            sb.append("by: ").append(quoteAuthor);
        }

        return sb.toString();
    }

    public String getHtmlShareText()
    {
        // should look sth like:
        // <div style="background:#fafafa; padding: 1em; margin: 0.5em; border: 2px #ccc solid; width:40em">
        // <p style="text-align:center;">
        // <em style="font-family:'Roboto Light', Arial, Helvetica, sans-serif; color:#000;font-size: 1.4em">
        // “quote_text”
        // </em>
        // </p>
        //
        // now content of from.getHtmlShareText();
        //
        // </div>

        StringBuilder sb = new StringBuilder();
        sb.append(
                "<div style=\"background:#fafafa; padding: 1em; margin: 0.5em; border: 2px #ccc solid; width:40em\"><p style=\"text-align:center;\"><em style=\"font-family:'Roboto Light', Arial, Helvetica, sans-serif; color:#000;font-size: 1.4em\">“")
                .append(quotation).append("”</em></p> ").append(from.getHtmlShareText()).append("</div>");
        return sb.toString();
    }
}
