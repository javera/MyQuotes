
package com.mjaworski.myQuotes.DB.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Source
{
    public static final String SOURCE_ID_FIELD_NAME = "id";
    public static final String AUTHOR_ID_FIELD_NAME = "author_id";
    public static final String SOURCE_TITLE_FIELD_NAME = "sourceTitle";
    public static final String SOURCE_IMAGE_PATH_FIELD_NAME = "imagePath";

    @DatabaseField(generatedId = true, columnName = SOURCE_ID_FIELD_NAME)
    private int _id;

    @DatabaseField(uniqueCombo = true, columnName = SOURCE_TITLE_FIELD_NAME)
    private String sourceTitle;
    @DatabaseField
    private String imagePath;
    
    @DatabaseField(uniqueCombo = true, foreign = true, columnName = AUTHOR_ID_FIELD_NAME, foreignAutoRefresh = true, foreignAutoCreate = true)
    private Author author;

    public Source()
    {
        // needed for ORMLite
    }

    
    public Author getAuthor()
    {
        return author;
    }

    public void setAuthor(Author author)
    {
        this.author = author;
    }

    public Source(String sourceTitle, Author authorObj, String imagePath)
    {
        this.sourceTitle = sourceTitle;
        this.imagePath = imagePath;
        this.author = authorObj;
    }

    public Source(String sourceTitle, Author authorObj)
    {
        this(sourceTitle, authorObj, null);
    }

    public String getSourceTitle()
    {
        if (sourceTitle == null)
        {
            return "";
        }
        return sourceTitle;
    }

    public void setSourceTitle(String sourceTitle)
    {
        this.sourceTitle = sourceTitle;
    }

    public int getId()
    {
        return _id;
    }

    public void setId(int id)
    {
        this._id = id;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }


    @Override
    public String toString()
    {
        return this.sourceTitle;
    }

    public String getStringSimpleForm()
    {
        StringBuilder sb = new StringBuilder();
        
        if (sourceTitle != null && sourceTitle != "")
        {
            sb.append("from ").append(sourceTitle).append(" ");
        }
        
        if (author != null && author.getName() != null && author.getName() != "")
        {
            sb.append("by: ").append(author.getName());
        }
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        // Return true if the objects are identical.
        // (This is just an optimization, not required for correctness.)
        if (this == o) { return true; }

        // Return false if the other object has the wrong type.
        // This type may be an interface depending on the interface's
        // specification.
        if (!(o instanceof Source)) { return false; }

        // Cast to the appropriate type.
        // This will succeed because of the instanceof, and lets us access
        // private fields.
        Source lhs = (Source) o;

        // Check each field. Primitive fields, reference fields, and nullable
        // reference fields are all treated differently.
        return (sourceTitle == null ? lhs.sourceTitle == null : sourceTitle.equals(lhs.sourceTitle))
                &&
               (author == null ? lhs.author == null : author.equals(lhs.author));

    }


    @Override
    public int hashCode()
    {
        // Start with a non-zero constant.
        int result = 17;

        result = 31 * result + (sourceTitle == null ? 0 : sourceTitle.hashCode());
        result = 31 * result + (author == null ? 0 : author.hashCode());

        return result;
    }


    public String getHtmlShareText()
    {
        // <p style="text-align:right;font-family:'Roboto Condensed', Arial, Helvetica, sans-serif; color:000000;">
        // <strong>&ndash;author_name</strong><br />
        // <em>source_title</em>
        // </p>
        boolean shouldOutputAnything = false;
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("<p style=\"text-align:right;font-family:'Roboto Condensed', Arial, Helvetica, sans-serif; color:#333;\">");
        

        
        if (author != null && author.getName() != null && author.getName() != "")
        {
            
            sb.append("<strong>&ndash;").append(author.getName()).append("</strong><br />");

            shouldOutputAnything = true;
        }
        
        if (sourceTitle != null && sourceTitle != "")
        {
            sb.append("<em>").append(sourceTitle).append("</em>");
            shouldOutputAnything = true;
        }
        sb.append("</p>");        
        
        return shouldOutputAnything ? sb.toString() : "";
    }
    
    
    
}
