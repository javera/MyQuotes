
package com.mjaworski.myQuotes;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView.OnEditorActionListener;

import butterknife.InjectView;
import butterknife.Views;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.mjaworski.myQuotes.DB.DatabaseHelper;
import com.mjaworski.myQuotes.DB.Model.Author;
import com.mjaworski.myQuotes.DB.Model.Quote;
import com.mjaworski.myQuotes.DB.Model.QuoteTag;
import com.mjaworski.myQuotes.DB.Model.Source;
import com.mjaworski.myQuotes.DB.Model.Tag;
import com.mjaworski.myQuotes.Utils.IPublishResults;
import com.mjaworski.myQuotes.Utils.ImageUtils;
import com.mjaworski.myQuotes.Utils.MsgUtils;
import com.mjaworski.myQuotes.enums.Mode;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.AutoCompleteTextView;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class AddEditQuoteFragment extends Fragment implements android.view.View.OnClickListener, IPublishResults
{
    int quoteID;

    private static final String LOG_TAG = "AddEditQuoteFragment";
    private static final String WORKER_FRAGMENT_TAG = "DataDeliverer";
    private static final char TAG_DELIMITER = ',';
    
    private static final String QUOTE_TEXT = "QuoteText";

    @InjectView(R.id.quote)
    EditText quote;
    @InjectView(R.id.author)
    AutoCompleteTextView author;
    @InjectView(R.id.title)
    AutoCompleteTextView sourceTitle;
    @InjectView(R.id.tag)
    AutoCompleteTextView tags;
    @InjectView(R.id.form4)
    LinearLayout tagWrapper;
    @InjectView(R.id.tags_added_container)
    LinearLayout tagsContainer;
    @InjectView(R.id.add_tag)
    ImageButton tagAdd;
    @InjectView(R.id.quote_favourite)
    CheckBox favouriteButton;
    @InjectView(R.id.cover_image)
    ImageView coverImageView;
    @InjectView(R.id.image_from_camera)
    Button fromCamera;
    @InjectView(R.id.image_from_disk)
    Button fromDisk;
    @InjectView(R.id.image_from_google)
    Button fromGoogle;

    private Mode mode;

    private FormModelFragment mWorkFragment;

    /**
     * Create a fragment that allows adding new and modifying existing quotes.
     * 
     * @param quoteID if you want to edit quote, provide its id, otherwise pass -1
     * @return fragment in edit or create {@link com.mjaworski.myQuotes.enums.Mode Mode}
     */
    public static AddEditQuoteFragment newInstance(int quoteID)
    {
        return newInstance(quoteID, "");
    }
    
    public static AddEditQuoteFragment newInstance(int quoteID, String sharedText)
    {
        AddEditQuoteFragment frag = new AddEditQuoteFragment();
        Bundle args = new Bundle();
        args.putInt(AddEditQuoteActivity.QUOTE_ID, quoteID);
        args.putString(AddEditQuoteFragment.QUOTE_TEXT, sharedText);
        frag.setArguments(args);

        return (frag);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getFragmentManager();

        // Check to see if we have retained the worker fragment.
        mWorkFragment = (FormModelFragment) fm.findFragmentByTag(WORKER_FRAGMENT_TAG);

        // If not retained (or first time running), we need to create it.
        if (mWorkFragment == null)
        {
            mWorkFragment = new FormModelFragment();
            // Tell it who it is working with.
            mWorkFragment.setTargetFragment(this, 0);
            fm.beginTransaction().add(mWorkFragment, WORKER_FRAGMENT_TAG).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // get quoteID and set the mode of the fragment
        quoteID = getArguments().getInt(AddEditQuoteActivity.QUOTE_ID, -1);
        if (quoteID == -1)
        {
            mode = Mode.CREATE;
        }
        else
        {
            mode = Mode.EDIT;
        }

        // inflate the layout and set references to widgets existing in xml
        View result = inflater.inflate(R.layout.create_form, container, false);
        Views.inject(this, result);
        setHasOptionsMenu(true);

        setupButtonListeners();

        String sharedText = getArguments().getString(AddEditQuoteFragment.QUOTE_TEXT);
        if (sharedText == null)
        {
            sharedText = "";
        }
        // quote can be long, allow it to be multi-line
        quote.setSingleLine(false);
        quote.setText(sharedText);

        // TODO: set visible when implemented
        fromGoogle.setVisibility(View.GONE);

        return (result);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // show default menu only when in edit mode
        if (mode == null || mode == Mode.EDIT)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(getResources().getText(R.string.edit));
            inflater.inflate(R.menu.quote_edit, menu);
        }
        else
        {
            LayoutInflater menuInflater = (LayoutInflater) getActivity().getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            View customActionBarView = menuInflater.inflate(R.layout.actionbar_custom_view_done_discard, null);

            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(this);
            customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(this);

            // Show the custom action bar view and hide the normal Home icon and title.
            final ActionBar actionBar = getSupportActionBar();

            actionBar.setDisplayOptions(
                    ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);

            actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent i = new Intent(this.getActivity(), HomeFragmentPager.class);
                startActivity(i);
                return (true);
            case R.id.delete:
                delete();
                return (true);
            case R.id.save:
                save();
                return (true);

        }
        return (super.onOptionsItemSelected(item));
    }

    private void setupButtonListeners()
    {
        fromCamera.setOnClickListener(this);
        fromGoogle.setOnClickListener(this);
        fromDisk.setOnClickListener(this);
        tagAdd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.actionbar_discard:
                discard();
                break;
            case R.id.actionbar_done:
                save();
                break;
            case R.id.add_tag:
                addTagFromEditText();
                break;
            case R.id.image_from_camera:
                MsgUtils.info(getString(R.string.ct_opening_camera), getActivity());
                mWorkFragment.captureImageFromCamera();
                break;
            case R.id.image_from_google:
                // TODO: from google books
                MsgUtils.info(getString(R.string.ct_opening_google_image_search), getActivity());
                break;
            case R.id.image_from_disk:
                MsgUtils.info(getString(R.string.ct_opening_image_picker), getActivity());
                mWorkFragment.captureImageFromStorage();
                break;
            default:
                break;
        }

    }

    /**
     * To be used from data deliverer. Adds data to the auto-complete text widgets in the form and sets up appropriate
     * listeners to handle choosing elements from auto-complete suggestions
     * 
     * @param allAuthors A list of {@link com.mjaworski.myQuotes.DB.Model.Author Author} objects
     * @param allSources A list of {@link com.mjaworski.myQuotes.DB.Model.Source Source} objects
     * @param allTags A list of {@link com.mjaworski.myQuotes.DB.Model.Tag Tag} objects
     */
    public void setAutocompletes(List<Author> allAuthors, List<Source> allSources, List<Tag> allTags)
    {
        // AUTHOR
        ArrayAdapter<Author> authorAdapter = new ArrayAdapter<Author>(
                this.getActivity(), android.R.layout.simple_dropdown_item_1line, allAuthors);
        author.setAdapter(authorAdapter);

        // SOURCE
        ArrayAdapter<Source> sourcesAdapter = new ArrayAdapter<Source>(
                this.getActivity(), android.R.layout.simple_dropdown_item_1line, allSources);
        sourceTitle.setAdapter(sourcesAdapter);

        // clicking on source suggestion should update the cover that is associated with it and the author
        sourceTitle.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View tagText, int position, long rowID)
            {
                final Source clickedSource = (Source) parent.getAdapter().getItem(position);

                final String chosenSourceImagePath = clickedSource.getImagePath();
                if (chosenSourceImagePath != null && chosenSourceImagePath != "")
                {
                    coverImageView.setImageURI(Uri.parse(chosenSourceImagePath));
                }
                else
                {
                    coverImageView.setImageDrawable(getResources().getDrawable(R.drawable.cover_template));
                }

                final String chosenAuthorName = clickedSource.getAuthor() == null ? "" : clickedSource.getAuthor()
                        .getName();
                author.setText(chosenAuthorName);
            }
        });

        // tags:
        ArrayAdapter<Tag> tagsAdapter = new ArrayAdapter<Tag>(
                this.getActivity(), android.R.layout.simple_dropdown_item_1line, allTags);
        tags.setAdapter(tagsAdapter);

        // clicking on tag suggestion should add the tag and clear the editText
        tags.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View tagText, int position, long rowID)
            {
                final Tag chosenTag = (Tag) parent.getAdapter().getItem(position);
                addTagToUI(chosenTag);
                mWorkFragment.tagAddedToUI(chosenTag);
                tags.getText().clear();
            }
        });

        // if user adds comma, then that should trigger adding tag as well
        tags.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            } // not needed, interface requirement

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            } // not needed, interface requirement

            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0) // has some text
                {
                    // if last char is tag delimiter then add, otherwise do nothing
                    final char lastChar = s.charAt(s.length() - 1);
                    if (lastChar == TAG_DELIMITER)
                        addTagFromEditText();
                }
            }

        });

        // if user chooses 'add' on keyboard, then add tag
        tags.setImeActionLabel(getString(R.string.add_tag), EditorInfo.IME_ACTION_DONE);
        tags.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event)
            {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    addTagFromEditText();
                    handled = true;
                }
                return handled;
            }
        });
    }

    /**
     * Called from a worker fragment. Passed data is filled in to the appropriate form fields.
     * 
     * @param quoteToEdit {@link com.mjaworski.myQuotes.DB.Model.Quote Quote} instance to be displayed
     * @param quoteToEditTags List of {@link com.mjaworski.myQuotes.DB.Model.Tag Tags} to be displayed
     */
    private void fillFormWithQuoteData(Quote quoteToEdit, List<Tag> quoteToEditTags)
    {
        // first, quote fields
        this.quote.setText(quoteToEdit.getQuotation() == null ? "" : quoteToEdit.getQuotation());
        if (quoteToEdit.getFrom() != null)
        {
            this.sourceTitle.setText(quoteToEdit.getFrom().getSourceTitle());
            this.author.setText(quoteToEdit.getFrom().getAuthor() == null ? "" : quoteToEdit.getFrom().getAuthor()
                    .getName());
        }
        this.favouriteButton.setChecked(quoteToEdit.getIsFavourite());

        // now tags
        if (quoteToEditTags != null && !quoteToEditTags.isEmpty())
        {
            for (final Tag t : quoteToEditTags)
            {
                addTagToUI(t);
            }
        }
    }

    /**
     * Helper method that gets contents of Tag EditText, does some validation and adds it
     */
    private void addTagFromEditText()
    {
        // remove any trailing spaces, and tag delimiters
        final String tagText = tags.getText().toString().replaceAll("" + TAG_DELIMITER, "").trim();
        if (tagText.length() > 0) // there is some text - add new tag
        {
            final Tag newTag = new Tag(tagText);

            addTagToUI(newTag);
            mWorkFragment.tagAddedToUI(newTag);
            tags.getText().clear();
        }
        else
        // no tag input, inform user
        {
            MsgUtils.alert(getString(R.string.ct_missing_tag), getActivity());
        }
    }

    /**
     * Creates and displays a clickable view in the UI that represents added tag.
     * 
     * @param tag {@link com.mjaworski.myQuotes.DB.Model.Tag Tag} to be added to UI
     */
    public void addTagToUI(final Tag tag)
    {
        // inflate button from template
        Button tagButton = (Button) getLayoutInflater().inflate(R.layout.tag_button, null);
        tagButton.setText(tag.getTag());
        tagsContainer.addView(tagButton, 0); // adding to the front of the list

        // when tag is clicked, remove from UI and worker fragment
        tagButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // remove from UI
                tagsContainer.removeView(v);
                // remove from worker thread
                mWorkFragment.tagRemoveFromUI(tag);
            }
        });
    }

    /**
     * Used to set the cover image for source
     * 
     * @param coverImage
     */
    public void setCover(Bitmap coverImage)
    {
        coverImageView.setImageBitmap(coverImage);
    }

    /**
     * Some simple validation of data entered by user. If not valid, user will get a msg in UI
     * 
     * @return <code>true</code> if form is valid, <code>false</code> otherwise
     */
    private boolean validateForm()
    {
        StringBuilder sbProblems = new StringBuilder();
        boolean valid = true;

        if (quote.getText().toString().trim().length() < 1)
        {
            valid = false;
            sbProblems.append(getString(R.string.ct_validation_empty_quote));
        }

        if (!valid)
        {
            MsgUtils.alert(sbProblems.toString(), getActivity());
        }
        return valid;
    }

    /**
     * First validates the form, the asynchronously adds or updates the quote
     */
    private void save()
    {
        if (validateForm())
        {
            final String actionHint = mode == Mode.CREATE ? getString(R.string.ct_adding)
                    : getString(R.string.ct_updating);

            MsgUtils.info(actionHint, getActivity());

            Author authorObj = new Author(author.getText().toString());
            Source sourceObj = new Source(sourceTitle.getText().toString(), authorObj);

            Quote quoteObj = new Quote(quote.getText().toString(), sourceObj);
            quoteObj.setIsFavourite(favouriteButton.isChecked());

            if (tags.getText().length() > 0)
            {
                addTagFromEditText();
                // user might have forgotten to add the tag
            }

            if (mode == Mode.CREATE)
            {
                mWorkFragment.addNewQuote(quoteObj, this);
            }
            else if (mode == Mode.EDIT)
            {
                mWorkFragment.updateQuote(quoteObj, this);
            }

        }

    }

    /**
     * Displays msg it is deleting and asynchronously deletes the quote
     */
    private void delete()
    {
        MsgUtils.alert(getString(R.string.ct_deleting), getActivity());
        mWorkFragment.deleteQuote(this);
    }

    /**
     * Closes the activity
     */
    private void discard()
    {
        MsgUtils.info(getString(R.string.ct_discarding), getActivity());

        getActivity().finish();
    }

    @Override
    public void taskSucceeded(String msg)
    {
        MsgUtils.confirm(msg, getActivity());
    }

    @Override
    public void taskFailed(String msg)
    {
        MsgUtils.alert(msg, getActivity());
    }

    @Override
    public void setProgress(int currentValue, int maxValue, String msg)
    {
        // TODO: delete in Interface or add implementation
    }

    /**
     * What mode the form is in
     * 
     * @return <code>Edit</code> or <code>Create</code> {@link com.mjaworski.myQuotes.enums.Mode Mode}
     */
    private Mode getMode()
    {
        return mode;
    }

    /**
     * Returns quote id
     * 
     * @return -1 if not in edit mode, quote id otherwise
     */
    public int getQuoteID()
    {
        return quoteID;
    }

    public static class FormModelFragment extends Fragment
    {
        private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
        private static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 200;

        private List<Author> allAuthors = null;
        private List<Source> allSources = null;
        private List<Tag> allTags = null;
        private AutocompletesFromDBTask autocompletesTask = null;

        private Quote quoteToEdit = null;
        private List<Tag> tagsExistingInUI = null;
        private List<Tag> tagsRemoved = new ArrayList<Tag>();
        private QuoteFromDBTask editQuoteTask = null;

        private Uri coverURI = null;
        private Bitmap coverImage = null;

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);
            setRetainInstance(true);

            deliverAutoCompletesModel();
            deliverCoverImageModel();

            // since our UI fragment does not retain anything, we readd all tags
            deliverAllTagsModel();

            final AddEditQuoteFragment parentFragment = (AddEditQuoteFragment) getTargetFragment();
            if (parentFragment.getMode() == Mode.EDIT) // if in edit mode, reload quotes data to form
            {
                retrieveQuoteFromDB(parentFragment.getQuoteID());
            }

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            switch (requestCode)
            {
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                    if (resultCode == org.holoeverywhere.app.Activity.RESULT_OK)
                    {
                        final float WIDTH_IN_PX = ImageUtils.COVER_IMAGE_WIDTH_DP
                                * getResources().getDisplayMetrics().density;
                        final float HEIGHT_IN_PX = ImageUtils.COVER_IMAGE_HEIGHT_DP
                                * getResources().getDisplayMetrics().density;

                        // The image should now be used in the cover ImageView

                        executeAsyncTask(new PrepareImageTask(WIDTH_IN_PX, HEIGHT_IN_PX));

                    }
                    else if (resultCode == org.holoeverywhere.app.Activity.RESULT_CANCELED)
                    {
                        coverURI = null;
                    }
                    else
                    {
                        MsgUtils.alert(getString(R.string.ct_image_from_camera_failed), getActivity());
                    }
                    break;

                case PICK_IMAGE_ACTIVITY_REQUEST_CODE:
                {
                    if (resultCode == org.holoeverywhere.app.Activity.RESULT_OK)
                    {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {
                                MediaStore.Images.Media.DATA
                        };

                        Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null,
                                null, null);
                        cursor.moveToFirst();

                        final int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        final String originalFilePath = cursor.getString(columnIndex);
                        cursor.close();

                        coverURI = Uri.parse(originalFilePath);

                        final float WIDTH_IN_PX = ImageUtils.COVER_IMAGE_WIDTH_DP
                                * getResources().getDisplayMetrics().density;
                        final float HEIGHT_IN_PX = ImageUtils.COVER_IMAGE_HEIGHT_DP
                                * getResources().getDisplayMetrics().density;

                        // The image should now be used in the cover ImageView

                        executeAsyncTask(new PrepareImageTask(WIDTH_IN_PX, HEIGHT_IN_PX, true));
                    }
                }
            }

        }

        synchronized private void deliverAutoCompletesModel()
        {
            if (allAuthors == null || allSources == null || allTags == null)
            {
                autocompletesTask = new AutocompletesFromDBTask();
                executeAsyncTask(autocompletesTask, getActivity().getApplicationContext());
            }
            else
            {
                final AddEditQuoteFragment parentFragment = (AddEditQuoteFragment) getTargetFragment();
                parentFragment.setAutocompletes(allAuthors, allSources, allTags);
            }
        }

        /**
         * Called when data is loaded from the db, now just display it in the UI
         */
        synchronized private void deliverQuoteToTheForm()
        {
            final AddEditQuoteFragment parentFragment = (AddEditQuoteFragment) getTargetFragment();
            parentFragment.fillFormWithQuoteData(quoteToEdit, tagsExistingInUI);

            if (quoteToEdit.getFrom() != null && quoteToEdit.getFrom().getImagePath() != null)
            {
                coverURI = Uri.parse(quoteToEdit.getFrom().getImagePath());

                final float WIDTH_IN_PX = ImageUtils.COVER_IMAGE_WIDTH_DP * getResources().getDisplayMetrics().density;
                final float HEIGHT_IN_PX = ImageUtils.COVER_IMAGE_HEIGHT_DP
                        * getResources().getDisplayMetrics().density;

                // The image should now be used in the cover ImageView
                executeAsyncTask(new PrepareImageTask(WIDTH_IN_PX, HEIGHT_IN_PX));
            }

        }

        /**
         * Recreating tags after configuration change
         */
        synchronized private void deliverAllTagsModel()
        {
            // recreate tags in UI
            if (tagsExistingInUI != null && !tagsExistingInUI.isEmpty())
            {
                final AddEditQuoteFragment parentFragment = (AddEditQuoteFragment) getTargetFragment();

                for (Tag t : tagsExistingInUI)
                {
                    parentFragment.addTagToUI(t);
                }
            }
        }

        synchronized private void deliverCoverImageModel()
        {
            if (coverImage != null)
            {
                final AddEditQuoteFragment parentFragment = (AddEditQuoteFragment) getTargetFragment();
                parentFragment.setCover(coverImage);
            }
        }

        /**
         * Since tag was added in the UI, the data needs to be updated
         * 
         * @param {@link com.mjaworski.myQuotes.DB.Model.Tag Tag} to be added to data deliverer
         */
        public void tagAddedToUI(Tag t)
        {
            if (tagsExistingInUI == null)
                tagsExistingInUI = new ArrayList<Tag>();

            tagsExistingInUI.add(t);
        }

        /**
         * Since tag was removed in the UI, the data needs to be updated
         * 
         * @param {@link com.mjaworski.myQuotes.DB.Model.Tag Tag} to be removed from data deliverer
         */
        public void tagRemoveFromUI(Tag t)
        {
            if (tagsExistingInUI != null)
                tagsExistingInUI.remove(t);

            tagsRemoved.add(t);
        }

        /**
         * Loads {@link com.mjaworski.myQuotes.DB.Model.Quote quote} from db (or uses retained instance) and displays it
         * in the UI
         * 
         * @param quoteID quote id
         */
        synchronized public void retrieveQuoteFromDB(int quoteID)
        {
            if (quoteToEdit == null || tagsExistingInUI == null)
            {
                if (editQuoteTask == null)
                {
                    editQuoteTask = new QuoteFromDBTask(quoteID);
                    executeAsyncTask(editQuoteTask, getActivity().getApplicationContext());
                }
            }
        }

        /**
         * Adds new quote asynchronously to the db
         * 
         * @param quoteObj quote to be added
         * @param methodCaller used to show messages in the UI
         */
        public void addNewQuote(Quote quoteObj, IPublishResults methodCaller)
        {
            executeAsyncTask(new AddQuoteTask(quoteObj, tagsExistingInUI, methodCaller), getActivity()
                    .getApplicationContext());
        }

        /**
         * Asynchronously deletes the currently loaded quote
         * 
         * @param methodCaller used to show messages in the UI
         */
        public void deleteQuote(IPublishResults methodCaller)
        {
            executeAsyncTask(new DeleteQuoteTask(methodCaller), getActivity()
                    .getApplicationContext());

        }

        /**
         * Updates the quote asynchronously in the db
         * 
         * @param quoteObj quote to be updated
         * @param methodCaller used to show messages in the UI
         */
        public void updateQuote(Quote quoteObj, IPublishResults methodCaller)
        {
            executeAsyncTask(new UpdateQuoteTask(quoteObj, tagsExistingInUI, tagsRemoved, methodCaller), getActivity()
                    .getApplicationContext());
        }

        public void captureImageFromCamera()
        {
            // create Intent to take a pic and return control to the calling app
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            createNewCoverURI(); // create a file to save the image

            // set the image file name
            intent.putExtra(MediaStore.EXTRA_OUTPUT, coverURI);
            intent.putExtra("return-data", true);

            // start the image capture Intent
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

        }

        public void captureImageFromStorage()
        {
            // create Intent to pick a picture from gallery
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
        }

        /**
         * Creates new file resource on sd and stores uri to it in coverURI field
         */
        private void createNewCoverURI()
        {
//            File temp = getCoverFile();
//            
//            Uri test1 = Uri.fromFile(temp);
//            Uri test2 = Uri.parse(temp.getPath());
//            test1.getEncodedPath();
//            coverURI = Uri.parse(getCoverFile().getPath());
            

            coverURI = Uri.fromFile(getCoverFile());
        }

        /**
         * Creates a file that will store the cover
         * 
         * @return File object where the cover will be stored
         */
        private File getCoverFile()
        {
            final String path =
                    getString(R.string.app_folder)
                            + File.separator
                            + getString(R.string.covers_folder);

            File picStorageDir = new File(Environment.getExternalStorageDirectory(), path);

            // Create the storage directory if it does not exist
            if (!picStorageDir.exists())
            {
                if (!picStorageDir.mkdirs())
                {
                    Log.e(LOG_TAG, "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;

            mediaFile = new File(picStorageDir.getPath() + File.separator +
                    "cover_" + timeStamp + ".jpg");

            return mediaFile;
        }

        @TargetApi(11)
        static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params)

        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
            else
            {
                task.execute(params);
            }
        }

        private class AutocompletesFromDBTask extends AsyncTask<Context, Void, Void>
        {
            @Override
            protected Void doInBackground(Context... ctxt)
            {
                if (allAuthors == null)
                    allAuthors = DatabaseHelper.getInstance(ctxt[0]).getAllAuthors();

                if (allSources == null)
                    allSources = DatabaseHelper.getInstance(ctxt[0]).getAllSourceTitles();

                if (allTags == null)
                    allTags = DatabaseHelper.getInstance(ctxt[0]).getAllTags();

                return (null);
            }

            @Override
            public void onPostExecute(Void arg0)
            {
                deliverAutoCompletesModel();
            }
        }

        private class QuoteFromDBTask extends AsyncTask<Context, Void, Void>
        {
            private int quoteID;

            public QuoteFromDBTask(int quoteID)
            {
                this.quoteID = quoteID;
            }

            @Override
            protected Void doInBackground(Context... ctxt)
            {
                if (quoteToEdit == null)
                    quoteToEdit = DatabaseHelper.getInstance(ctxt[0]).getQuote(quoteID);

                if (tagsExistingInUI == null)
                    tagsExistingInUI = DatabaseHelper.getInstance(ctxt[0]).getTagsAssignedToQuote(quoteToEdit);

                return (null);
            }

            @Override
            public void onPostExecute(Void arg0)
            {
                deliverQuoteToTheForm();
            }
        }

        private class DeleteQuoteTask extends AsyncTask<Context, Void, Void>
        {
            private Exception e = null;

            IPublishResults taskCreator;

            public DeleteQuoteTask(IPublishResults taskCreator)
            {
                this.taskCreator = taskCreator;
            }

            @Override
            protected Void doInBackground(Context... ctxt)
            {
                try
                {
                    final DatabaseHelper db = DatabaseHelper.getInstance(ctxt[0]);

                    TransactionManager.callInTransaction(db.getConnectionSource(),
                            new Callable<Void>()
                            {
                                public Void call() throws Exception
                                {
                                    db.getQuoteRuntimeDao().deleteById(quoteToEdit.getId());
                                    return null;
                                }
                            });
                }
                catch (SQLException e)
                {
                    this.e = e;
                }

                return null;
            }

            @Override
            public void onPostExecute(Void nothing)
            {
                if (e == null)
                {
                    ((AddEditQuoteActivity) getActivity()).quoteUpdated();
                }
                else
                {
                    taskCreator.taskFailed(getString(R.string.ct_deleting_fail));
                    Log.e(LOG_TAG, "Exception deleting a quote", e);
                }
            }
        }

        private class UpdateQuoteTask extends AsyncTask<Context, Void, Void>
        {
            private Exception e = null;

            private Quote quote;
            private List<Tag> tags;
            private List<Tag> removedTags;
            private IPublishResults taskCreator;

            public UpdateQuoteTask(Quote quote, List<Tag> tags, List<Tag> removedTags, IPublishResults taskCreator)
            {
                this.quote = quote;
                this.tags = tags;
                this.taskCreator = taskCreator;
                this.removedTags = removedTags;
            }

            @Override
            protected Void doInBackground(Context... ctxt)
            {
                try
                {
                    final DatabaseHelper db = DatabaseHelper.getInstance(ctxt[0]);

                    quote.setId(quoteToEdit.getId());

                    // check what to update

                   

                    if (allSources.contains(quote.getFrom()))
                    {
                        // means that the source is in the db however, we need to get the id to prevent creating
                        // new obj in the db

                        quote.setFrom(allSources.get(allSources.indexOf(quote.getFrom())));
                    }

                    if (allAuthors.contains(quote.getFrom().getAuthor()))
                    {
                        // means that the author is from the db however, we need the id to prevent creating new obj in
                        // the db

                        quote.getFrom().setAuthor(allAuthors.get(allAuthors.indexOf(quote.getFrom().getAuthor())));
                    }

                    // take care of storing cover
                    if (coverURI != null)
                    {
                        quote.getFrom().setImagePath(coverURI.getPath());
                    }
                    else
                    {
                        quote.getFrom().setImagePath(quoteToEdit.getFrom().getImagePath());
                    }

                    TransactionManager.callInTransaction(db.getConnectionSource(),
                            new Callable<Void>()
                            {
                                public Void call() throws Exception
                                {
                                    // update quote and associated author/source in the db
                                    db.getAuthorRuntimeDao().createOrUpdate(quote.getFrom().getAuthor());
                                    db.getSourceRuntimeDao().createOrUpdate(quote.getFrom());
                                    db.getQuoteRuntimeDao().update(quote);

                                    // now the tricky part - tags
                                    RuntimeExceptionDao<QuoteTag, Integer> quoteTagDAO = db.getQuoteTagRuntimeDao();
                                    DeleteBuilder<QuoteTag, Integer> quoteTagDelBuilder = quoteTagDAO.deleteBuilder();

                                    // if tag was removed from UI, it means it needs to be removed in the db mapping
                                    // table
                                    if (removedTags != null)
                                    {
                                        for (Tag tagToRemove : removedTags)
                                        {
                                            quoteTagDelBuilder.
                                                    where().
                                                    eq(QuoteTag.TAG_ID_FIELD_NAME, tagToRemove).
                                                    and().
                                                    eq(QuoteTag.QUOTE_ID_FIELD_NAME, quote);
                                            quoteTagDAO.delete(quoteTagDelBuilder.prepare());
                                        }
                                    }

                                    // now if there are any tags, they need to be in the db
                                    if (tags != null)
                                    {

                                        QueryBuilder<QuoteTag, Integer> quoteTagQb = db.getQuoteTagRuntimeDao()
                                                .queryBuilder();
                                        // now create tags and add mapping between tags and quote
                                        for (Tag tag : tags)
                                        {
                                            if (!allTags.contains(tag))
                                            {
                                                // new tag so definitely not in QuoteTag mapping table - add
                                                db.getTagRuntimeDao().create(tag);
                                                quoteTagDAO.create(new QuoteTag(quote, tag));
                                            }
                                            else
                                            {
                                                // tag already in the tag table, not sure if mappinng exists
                                                quoteTagQb.
                                                        selectColumns(QuoteTag.ID_FIELD_NAME).
                                                        where().
                                                        eq(QuoteTag.TAG_ID_FIELD_NAME, tag).
                                                        and().
                                                        eq(QuoteTag.QUOTE_ID_FIELD_NAME, quote);

                                                // just select the count
                                                final long noOfMatches = quoteTagQb.countOf();
                                                if (noOfMatches == 0)
                                                {
                                                    // there is no matching between this quote and tag, need to add
                                                    quoteTagDAO.create(new QuoteTag(quote, tag));
                                                }
                                            }

                                        }
                                    }
                                    return null;
                                }
                            });
                }
                catch (SQLException e)
                {
                    this.e = e;
                }

                return null;
            }

            @Override
            public void onPostExecute(Void nothing)
            {

                if (e == null)
                {
                    ((AddEditQuoteActivity) getActivity()).quoteUpdated();
                }
                else
                {
                    taskCreator.taskFailed(getString(R.string.ct_updating_fail));
                    Log.e(LOG_TAG, "Exception updating a quote", e);
                }
            }
        }

        private class AddQuoteTask extends AsyncTask<Context, Void, Void>
        {
            private Exception e = null;

            private Quote quote;
            private List<Tag> tags;
            private IPublishResults taskCreator;

            public AddQuoteTask(Quote quote, List<Tag> tags, IPublishResults taskCreator)
            {
                this.quote = quote;
                this.tags = tags;
                this.taskCreator = taskCreator;
            }

            @Override
            protected Void doInBackground(Context... ctxt)
            {
                try
                {
                    final DatabaseHelper db = DatabaseHelper.getInstance(ctxt[0]);

                    if (allSources.contains(quote.getFrom()))
                    {
                        // means that the source is from the db (as is the author) however, we need the id to prevent
                        // creating new obj in the db

                        quote.setFrom(allSources.get(allSources.indexOf(quote.getFrom())));
                    }

                    if (allAuthors.contains(quote.getFrom().getAuthor()))
                    {
                        // means that the author is from the db however, we need the id to prevent creating new obj in
                        // the db

                        quote.getFrom().setAuthor(allAuthors.get(allAuthors.indexOf(quote.getFrom().getAuthor())));
                    }

                    if (coverURI != null)
                    {
                        quote.getFrom().setImagePath(coverURI.getPath());
                    }

                    TransactionManager.callInTransaction(db.getConnectionSource(),
                            new Callable<Void>()
                            {
                                public Void call() throws Exception
                                {
                                    // create our quote and associated author/source in the db
                                    db.getAuthorRuntimeDao().createOrUpdate(quote.getFrom().getAuthor());
                                    db.getSourceRuntimeDao().createOrUpdate(quote.getFrom());
                                    db.getQuoteRuntimeDao().create(quote);

                                    // now create tags and add mapping between tags and quote
                                    if (tags != null)
                                    {
                                        for (Tag tag : tags)
                                        {
                                            // new tag, definitely not in the db
                                            if (!allTags.contains(tag))
                                            {
                                                db.getTagRuntimeDao().create(tag);
                                            }
                                            db.getQuoteTagRuntimeDao().create(new QuoteTag(quote, tag));
                                        }
                                    }
                                    return null;
                                }
                            });
                }
                catch (SQLException e)
                {
                    this.e = e;
                }

                return null;
            }

            @Override
            public void onPostExecute(Void nothing)
            {

                if (e == null)
                {
                    ((AddEditQuoteActivity) getActivity()).quoteUpdated();
                }
                else
                {
                    taskCreator.taskFailed(getString(R.string.ct_adding_fail));
                    Log.e(LOG_TAG, "Exception adding a quote", e);
                }
            }
        }

        private class PrepareImageTask extends AsyncTask<Context, Void, Void>
        {
            private float width;
            private float height;
            private boolean copyFile;

            public PrepareImageTask(float width, float height)
            {
                this(width, height, false);
            }

            public PrepareImageTask(float width, float height, boolean copyFile)
            {
                this.width = width;
                this.height = height;
                this.copyFile = copyFile;
            }

            @Override
            protected Void doInBackground(Context... ctxt)
            {
                // Image captured and saved to fileUri specified in the Intent
                coverImage = ImageUtils.decodeSampledBitmapFromUri(coverURI, width, height);
                if (coverImage != null)
                {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    coverImage.compress(Bitmap.CompressFormat.JPEG, 85, bytes);

                    // got the image from user's files, don't want to resize it, but resize COPY :)
                    if (copyFile)
                    {
                        createNewCoverURI();
                    }

                    File f = new File(coverURI.getPath());
                    // not sure if already exist, try creating the file first
                    FileOutputStream fo = null;
                    try
                    {
                        f.createNewFile();
                        // write the bytes in file
                        fo = new FileOutputStream(f);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    }
                    catch (IOException e)
                    {
                        // still, continue, the image will be saved in original
                        // size, which is still ok, if not ideal
                        Log.e(LOG_TAG, "Exception saving resized file to sd-card", e);

                        e.printStackTrace();
                    }
                    finally
                    {
                        if (fo != null)
                        {
                            try
                            {
                                fo.close();
                            }
                            catch (IOException e)
                            {
                                Log.e(LOG_TAG, "Can't even close the stream, not good... ", e);

                                e.printStackTrace();
                            }
                        }
                    }
                }
                return (null);
            }

            @Override
            public void onPostExecute(Void arg0)
            {
                deliverCoverImageModel();
            }
        }

    }
}
