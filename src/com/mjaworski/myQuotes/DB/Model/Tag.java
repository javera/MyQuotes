
package com.mjaworski.myQuotes.DB.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class Tag
{
    public static final String TAG_ID_FIELD_NAME = "id";

    public static final String TAG_FIELD_NAME = "tag";

    @DatabaseField(generatedId = true, columnName = TAG_ID_FIELD_NAME)
    private int id;

    @DatabaseField(uniqueIndex = true, columnName = TAG_FIELD_NAME )
    private String tag;

    public Tag()
    {
        // needed for ORMLite
    }

    public Tag(String tag)
    {
        this.tag = tag;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
    
    @Override
    public String toString()
    {
        return tag;
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
        if (!(o instanceof Tag)) { return false; }

        // Cast to the appropriate type.
        // This will succeed because of the instanceof, and lets us access
        // private fields.
        Tag lhs = (Tag) o;

        // Check each field. Primitive fields, reference fields, and nullable
        // reference fields are all treated differently.
        return tag == null ? lhs.tag == null : tag.equals(lhs.tag);
    }

    @Override
    public int hashCode()
    {
        // Start with a non-zero constant.
        int result = 17;

        result = 31 * result + (tag == null ? 0 : tag.hashCode());

        return result;
    }

}
