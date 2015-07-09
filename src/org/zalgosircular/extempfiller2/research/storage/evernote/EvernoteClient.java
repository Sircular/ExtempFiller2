package org.zalgosircular.extempfiller2.research.storage.evernote;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Bridges the gap between the Evernote API provided at
 * https://github.com/evernote/evernote-sdk-java and the main program.
 *
 * @author Logan Lembke
 */
public class EvernoteClient {

    private static final int MAX_RETRIES = 15;

    // Used for Authentication
    private UserStoreClient userStore;

    // Used for interacting with user data
    private NoteStoreClient noteStore;

    // Used for timing api requests
    private long rateTimer = 0;

    // How long to wait between each api request in milliseconds
    private final int TIMER = 1000;

    /**
     * Creates a new instance of an Evernote client.
     *
     * @throws Exception All exceptions are thrown to the calling program
     */
    public EvernoteClient(EvernoteService type, String token) throws Exception {
        final EvernoteAuth evernoteAuth = new EvernoteAuth(type, token);
        final ClientFactory factory = new ClientFactory(evernoteAuth);

        userStore = factory.createUserStoreClient();

        // Check that API is current with standards
        final boolean versionOk = userStore.checkVersion("CHS Extemp Filler (Java)",
                com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
        if (!versionOk)
            throw new RuntimeException("Incompatible Evernote client protocol version");

        // Set up the NoteStore client
        noteStore = factory.createNoteStoreClient();

        // Start api request timing
        checkRateTimer();
        validateTagsNotebook();
    }

    public Notebook initHTMLNotebook() throws Exception {
        try {
            Notebook HTMLNotebook = getNotebook("Web Notes");
            if (HTMLNotebook == null) {
                //logger.info("Creating Web Notes Notebook");
                return createNotebook("Web Notes");
            } else {
                return HTMLNotebook;
            }
        } catch (final Exception e) {
            //logger.severe("Error while creating the Web Notes Notebook: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a list of notes from the Evernote service matching a filter
     *
     * @param filter Limits the selection of notes. NoteFilters can filter by tags, notebooks, date, etc.
     * @param amount The amount of notes to return
     * @return A list of notes held on the server matching the filter
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized List<Note> getNotes(final NoteFilter filter, final int amount) throws Exception {
        try {
            // it looks like the default gets us what we need (GUID)
            NotesMetadataResultSpec resultSpec = new NotesMetadataResultSpec();
            // Always check the rate timer to make sure we do not overburden the server
            checkRateTimer();
            final List<NoteMetadata> noteList = noteStore.findNotesMetadata(filter, 0, amount, resultSpec).getNotes();
            final LinkedList<Note> downloadedNotes = new LinkedList<Note>();
            for (final NoteMetadata note : noteList)
                downloadedNotes.add(noteStore.getNote(note.getGuid(), true, true, true, true));
            return downloadedNotes;
        } catch (final EDAMSystemException edam) {
            // We are being throttled by Evernote
            if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
                //logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
                Thread.sleep(edam.getRateLimitDuration() * 1000);
                return getNotes(filter, amount);
            } else
                throw edam;
        }
    }

    /**
     * Retrieves a list of all the notes held by the Evernote service
     *
     * @param amount The amount of notes to return. The list is sorted by creation date.
     *               Eg. 10 returns the first 10 notes created.
     * @return A list of notes held on the server
     * @throws Exception All exceptions are thrown to the calling program
     */
    public List<Note> getNotes(final int amount) throws Exception {
        final NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.CREATED.getValue());
        filter.setAscending(true);
        return getNotes(filter, amount);
    }

    /**
     * Retrieves a list of notes from the Evernote service contained within a given notebook
     *
     * @param notebook The containing notebook
     * @param amount   The amount of notes to return
     * @return A list of notes held on the server within the notebook
     * @throws Exception All exceptions are thrown to the calling program
     */
    public List<Note> getNotesInNotebook(final Notebook notebook, final int amount) throws Exception {
        final NoteFilter filter = new NoteFilter();
        filter.setNotebookGuid(notebook.getGuid());
        filter.setOrder(NoteSortOrder.CREATED.getValue());
        filter.setAscending(true);
        return getNotes(filter, amount);
    }

    /**
     * Retrieves a list of notes from the Evernote service marked with a given tag
     *
     * @param tag    The marked tag
     * @param amount The amount of notes to return
     * @return A list of notes held on the server marked with given tag
     * @throws Exception All exceptions are thrown to the calling program
     */
    public List<Note> getNotesByTag(final Tag tag, final int amount) throws Exception {
        final NoteFilter filter = new NoteFilter();
        filter.setTagGuids(Arrays.asList(tag.getGuid()));
        filter.setOrder(NoteSortOrder.CREATED.getValue());
        filter.setAscending(true);
        return getNotes(filter, amount);
    }

    /**
     * Retrieves a list of the notebooks from the Evernote service
     *
     * @return A list of the notebooks held on the server
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized List<Notebook> getNotebooks() throws Exception {
        try {
            // Always check the rate timer to make sure we do not overburden the server
            checkRateTimer();
            return noteStore.listNotebooks();
        } catch (final EDAMSystemException edam) {
            // We are being throttled
            if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
                //logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
                Thread.sleep(edam.getRateLimitDuration() * 1000);
                return getNotebooks();
            } else
                throw edam;
        }
    }

    /**
     * Retrieves a notebook from the Evernote service with a given name
     *
     * @param name The name of the desired notebook
     * @return The notebook
     * @throws Exception All exceptions are thrown to the calling program
     */
    public Notebook getNotebook(String name) throws Exception {
        //Notebooks are limited to titles of 100 characters or less
        if (name.length() > 100)
            name = name.substring(0, 99).trim();
        for (final Notebook notebook : getNotebooks())
            if (notebook.getName().equalsIgnoreCase(name))
                return notebook;
        return null;
    }

    /**
     * Retrieves a list of the notebooks held by the Evernote service within a given stack
     *
     * @param stack The containing stack
     * @return A list of notebooks held on the server within the stack
     * @throws Exception All exceptions are thrown to the calling program
     */
    public List<Notebook> getNotebooksInStack(final String stack) throws Exception {
        final List<Notebook> notebooks = getNotebooks();
        final List<Notebook> toReturn = new LinkedList<Notebook>();
        for (final Notebook notebook : notebooks)
            if (notebook.getStack().equalsIgnoreCase(stack))
                toReturn.add(notebook);
        return toReturn;
    }

    /**
     * Retrieves a list of tags from the Evernote service
     *
     * @return A list of the tags held on the server
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized List<Tag> getTags() throws Exception {
        try {
            checkRateTimer();
            List<Tag> tags = noteStore.listTags();
            return tags;
        } catch (final EDAMSystemException edam) {
            if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
                //logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
                Thread.sleep(edam.getRateLimitDuration() * 1000);
                return getTags();
            } else
                throw edam;
        }
    }

    /**
     * Consults the Tag Names notebook for tag names
     *
     * @return A List of the desired names of created tags based on the tags notebook
     * @throws Exception
     */
    public List<String> getFullyNamedTags() throws Exception {
        final LinkedList<String> tags = new LinkedList<String>();
        final Notebook tagNotebook = getNotebook("Tag Names");
        final List<Note> notedTags = getNotesInNotebook(tagNotebook, 10000);
        String content;
        for (final Note note : notedTags) {
            content = note.getContent();
            content = content.substring(
                    content.indexOf("<p>") + 3,
                    content.indexOf("</p>")
            );
            tags.add(content);
        }
        return tags;
    }

    /**
     * Retrieves a tag from the Evernote service with a given name
     *
     * @param name The name of the desired tag
     * @return The tag
     * @throws Exception All exceptions are thrown to the calling program
     */
    public Tag getTag(String name) throws Exception {
        //tag names are limited to 100 characters... no commas
        if (name.length() > 100)
            name = name.substring(0, 99).trim();
        if (name.contains(","))
            name = name.replace(",", "");
        for (final Tag tag : getTags())
            if (tag.getName().equalsIgnoreCase(name))
                return tag;
        return null;
    }

    /**
     * Creates a note on the Evernote servers with a given title and given content within a given notebook and with given tags
     *
     * @param title    The title to name the note
     * @param content  The content to be held within the note
     * @param notebook The notebook to contain the note
     * @param tags     The tags to apply to the note
     * @return The note created on the server
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized Note createTextNote(final String title, final String content, final Notebook notebook, final List<Tag> tags) throws Exception {
        // Create a local note
        final Note note = new Note();

        // Set the title, notebook, and tags
        note.setTitle(title);
        note.setNotebookGuid(notebook.getGuid());
        final List<String> tagGuids = new LinkedList<String>();
        for (final Tag tag : tags)
            tagGuids.add(tag.getGuid());
        note.setTagGuids(tagGuids);

        // Construct the skeleton enml
        final String code = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                + "<en-note>"
                + "<p>" + content
                + "</p>"
                + "</en-note>";
        note.setContent(code);

        // Create an uninitialized note to hold the server note
        Note newNote;
        try {
            // Always check the rate timer to make sure we do not overburden the server
            checkRateTimer();
            newNote = noteStore.createNote(note);
        } catch (final EDAMSystemException edam) {
            // We are being throttled by Evernote
            if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
                //logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
                Thread.sleep(edam.getRateLimitDuration() * 1000);
                // Always check the rate timer to make sure we do not overburden the server
                checkRateTimer();
                newNote = noteStore.createNote(note);
            } else
                throw edam;
        }
        //logger.info("Successfully created a new note with GUID: "
        //        + newNote.getGuid() + " and name: " + newNote.getTitle());
        return newNote;
    }

    /**
     * Deletes a note on the Evernote servers
     *
     * @param note Note to delete
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized void deleteNote(final Note note) throws Exception {
        noteStore.expungeNote(note.getGuid());
    }

    /**
     * Creates a notebook on the Evernote servers with a given title
     *
     * @param desiredTitle The title to name the notebook
     * @return The notebook created on the server
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized Notebook createNotebook(final String desiredTitle) throws Exception {
        // Create a local notebook
        final Notebook notebook = new Notebook();
        // Set the title; the title can be 100 characters max
        String realTitle;
        if (desiredTitle.length() > 100)
            realTitle = desiredTitle.substring(0, 99).trim();
        else
            realTitle = desiredTitle;
        notebook.setName(realTitle);

        // Create an uninitialized notebook to hold the server notebook
        Notebook newNotebook;
        try {
            // Always check the rate timer to make sure we do not overburden the server
            checkRateTimer();
            newNotebook = noteStore.createNotebook(notebook);
        } catch (final EDAMSystemException edam) {
            // We are being throttled by Evernote
            if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
                //logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
                Thread.sleep(edam.getRateLimitDuration() * 1000);
                // Always check the rate timer to make sure we do not overburden the server
                checkRateTimer();
                newNotebook = noteStore.createNotebook(notebook);
            } else
                throw edam;
        }
        //logger.info("Successfully created a new notebook with GUID: "
        //        + newNotebook.getGuid() + " and name: " + newNotebook.getName());

        // If we had to shorten the title, create a note in the folder with the desired folder
        if (!realTitle.equals(desiredTitle))
            createTextNote("Desired Title", desiredTitle, newNotebook, new LinkedList<Tag>());
        return newNotebook;
    }

    /**
     * Deletes a notebook on the Evernote servers
     *
     * @param notebook Notebook to delete
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized void deleteNotebook(final Notebook notebook) throws Exception {
        noteStore.expungeNotebook(notebook.getGuid());
    }

    /**
     * Creates a tag on the Evernote servers with a given title
     *
     * @param desiredName The name to title the tag
     * @return The tag created on the server
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized Tag createTag(final String desiredName) throws Exception {
        // Create a local tag
        final Tag tag = new Tag();

        // Set the name; the name can be 100 characters max; the name cannot contain commas
        String realName;
        if (desiredName.length() > 100)
            realName = desiredName.substring(0, 99).trim();
        else
            realName = desiredName.trim();
        realName = realName.replace(",", "");
        tag.setName(realName);

        // Create an uninitialized notebook to hold the server notebook
        Tag newTag;
        try {
            // Always check the rate timer to make sure we do not overburden the server
            checkRateTimer();
            newTag = noteStore.createTag(tag);
        } catch (final EDAMSystemException edam) {
            // We are being throttled by Evernote
            if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
                //logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
                Thread.sleep(edam.getRateLimitDuration() * 1000);
                // Always check the rate timer to make sure we do not overburden the server
                checkRateTimer();
                newTag = noteStore.createTag(tag);
            } else
                throw edam;
        }

        // logger.info("Successfully created a new tag with GUID: "
        //         + newTag.getGuid() + " and name: " + newTag.getName());

        // Create a notebook for holding notes with desired tag names
        Notebook tagNotebook = getNotebook("Tag Names");
        if (tagNotebook == null) {
            //    logger.info("Creating Tag Names Notebook");
            tagNotebook = createNotebook("Tag Names");
        }

        // If we had to shorten the name, create a note with the desired name
        createTextNote("Desired Tag Name", desiredName, tagNotebook, Arrays.asList(newTag));
        return newTag;
    }

    /**
     * Deletes a tag on the Evernote servers. Deletes any notes marked with given tag
     *
     * @param tag Tag to delete
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized void deleteTag(final Tag tag) throws Exception {
        List<Note> notes;
        while ((notes = getNotesByTag(tag, 100)).size() != 0)
            for (final Note note : notes)
                deleteNote(note);
        noteStore.expungeTag(tag.getGuid());
    }

    /**
     * Returns the current users' username
     *
     * @return String: Username
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized String getUsername() throws Exception {
        checkRateTimer();
        return userStore.getUser().getUsername();
    }

    /**
     * Returns the current users' email
     *
     * @return String: Email
     * @throws Exception All exceptions are thrown to the calling program
     */
    public synchronized String getEmail() throws Exception {
        checkRateTimer();
        return userStore.getUser().getEmail();
    }

    /**
     * Creates the tag notebook, and checks the notebook against tags on the server
     *
     * @throws Exception All exceptions are thrown to the calling program
     */
    private void validateTagsNotebook() throws Exception {
        //logger.info("Validating Tags Notebook");
        if (getNotebook("Tag Names") == null) {
            //    logger.info("Creating Tag Names Notebook");
            createNotebook("Tag Names");
        }
    }

    /**
     * Regulates how often the api is called
     *
     * @throws InterruptedException Interrupted whilst sleeping
     */
    private void checkRateTimer() throws InterruptedException {
        if (System.currentTimeMillis() < rateTimer + TIMER)
            Thread.sleep(rateTimer + TIMER - System.currentTimeMillis());
        rateTimer = Calendar.getInstance().getTimeInMillis();
    }
}

