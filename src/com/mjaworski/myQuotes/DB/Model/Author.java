
package com.mjaworski.myQuotes.DB.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Author
{
    public static final String AUTHOR_ID_FIELD_NAME = "id";
    public static final String AUTHOR_NAME_FIELD_NAME = "name";
    

    @DatabaseField(generatedId = true, columnName = AUTHOR_ID_FIELD_NAME)
    private int _id;

    @DatabaseField(uniqueIndex = true, columnName = AUTHOR_NAME_FIELD_NAME)
    private String name;

    public Author()
    {
        // needed for ORMLite
    }

    public Author(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getId()
    {
        return _id;
    }

    public void setId(int id)
    {
        this._id = id;
    }

    @Override
    public String toString()
    {
        return getName();
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
        if (!(o instanceof Author)) { return false; }

        // Cast to the appropriate type.
        // This will succeed because of the instanceof, and lets us access
        // private fields.
        Author lhs = (Author) o;

        // Check each field. Primitive fields, reference fields, and nullable
        // reference
        // fields are all treated differently.
        return name == null ? lhs.name == null : name.equals(lhs.name);
    }

    @Override
    public int hashCode()
    {
        // Start with a non-zero constant.
        int result = 17;

        result = 31 * result + (name == null ? 0 : name.hashCode());

        return result;
    }
}
