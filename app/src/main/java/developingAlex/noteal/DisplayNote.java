package developingAlex.noteal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class DisplayNote extends AppCompatActivity {

    private boolean creatingNewNote;
    private String noteTitle;
    private String noteFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_note);

        creatingNewNote = getIntent().getBooleanExtra(
                MainActivity.EXTRA_NEWNOTE,
                true);
        if (!creatingNewNote) {
            noteTitle = getIntent().getStringExtra(
                    MainActivity.EXTRA_NOTETITLE);
            noteFile = getIntent().getStringExtra(
                    MainActivity.EXTRA_NOTEFILE);

            System.out.println("Loading Note...");
            System.out.println("noteTitle=" + noteTitle);
            System.out.println("noteFile=" + noteFile);

            ArrayList<String> note = NoteManager.loadNote(noteFile);
            if (note.size() != 2) {
                myToast("Bad error: note was meant to be composed of title " +
                        "and contents, (2 things) ...didn't find 2 things..",
                        1);
            } else {
                ((EditText)findViewById(R.id.title_text)).setText(note.get(0));
                ((EditText)findViewById(R.id.body_text)).setText(note.get(1));
            }
        } else {
            System.out.println("new Note...");
            noteTitle = "";
            noteFile = "";
            ((EditText)findViewById(R.id.title_text)).requestFocus();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_note, menu);
        return true;
    }


    /**
     *  Logic to run when the back button is pressed - autosave changes.
     */
    public void onBackPressed() {
        String title = ((EditText)findViewById(R.id.title_text))
                .getText()
                .toString();
        String content = ((EditText)findViewById(R.id.body_text))
                .getText()
                .toString();
        if (!(content.length() == 0 || title.length() == 0)) {
            /**
             * only save when back is pressed if a title and contents are
             * present, otherwise disregard.
             * (using length because to use isEmpty() requires api lvl 9 or
             * higher)
             */
            saveNoteToAppStorage();
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    /**
     * Get a filename from a title.
     *
     * If a title contains spaces or punctuation characters they are stripped
     * out.
     *
     * @param filenameToClean
     * @return a 'cleaned' version of the filename
     */
    private String cleanFileName(String filenameToClean) {
        return filenameToClean.replaceAll("[^a-zA-Z0-9]", "");
    }


    /**
     * Handle menu selections
     *
     * @param item the menu item that was tapped by the user
     * @return true to close the menu, false to keep it open
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_save:
                saveNoteToAppStorage();
                return true;
            case R.id.action_export:
                if (externalMediaWritable() && noteFile.compareTo("") != 0) {
                    String content = ((EditText) findViewById(R.id.body_text))
                            .getText()
                            .toString();
                    String title = ((EditText) findViewById(R.id.title_text))
                            .getText()
                            .toString();
                    String proposedExportedFilename = cleanFileName(title);
                    if (proposedExportedFilename.compareTo("") == 0) {
                        myToast("Cannot export note with that title, please " +
                                        "change title to consist of simple english " +
                                        "characters and numbers",
                                2);
                        return true;
                    }
                    // TODO: prompt user to overwrite any existing file with same
                    //  filename
                    String filename = "noteal-" + proposedExportedFilename + ".txt";
                    exportNoteToSDCard(content, filename);
                }
                return true;
        }
        return true;
    }


    /**
     * Check if external media is writeable
     *
     * @return true if external media is available for reading and writing
     */
    private boolean externalMediaWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true; //can read and write
        } else {
            myToast("Unable to write to SD card because SD media is not " +
                    "mounted.\nExternal storage state:" + state,
                    2);
            return false;
        }
    }


    /**
     * Saves the note to external app-specific storage
     *
     * Typically /Android/data/<developer>.<application>/files
     *
     * This allows it to be more easily accessible to the user.
     *
     * @param con contents of the file
     * @param filename filename to use for the new file
     * @return true on success, Toast the problem and return false on failure
     */
    private boolean exportNoteToSDCard(String con, String filename) {

        File file = new File(
                this.getBaseContext().getExternalFilesDir(null),
                filename);
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println(con);
            pw.flush();
            pw.close();
            f.close();
            myToast("Note saved to: " + file.getAbsolutePath(), 2);
        } catch (FileNotFoundException e) {
            // remnant logic from when permissions mattered,
            // may not be required any more?
            myToast("Error writing to file...", 1);
            myToast(e.getMessage(), 2);
            return false;
        } catch (IOException e) {
            myToast("There was an IO error", 1);
            return false;
        }
        return true;
    }


    /**
     * Save the Note being displayed in the current activity to application-
     * specific internal storage.
     *
     * Take the contents and title from the GUI and save it into a new file.
     * Also update the List file to reflect the fact we now have a new file.
     *
     * @return True if successful, False otherwise.
     */
    private boolean saveNoteToAppStorage() {

        String title = ((EditText)findViewById(R.id.title_text))
                .getText()
                .toString();
        if (title.compareTo("") == 0) {
            System.out.println("You need a title to save!");
            myToast("You need a title to save!", 1);
            return false;
        }

        System.out.println("Title: " + title);
        String content = ((EditText)findViewById(R.id.body_text))
                .getText()
                .toString();
        if (content.compareTo("") == 0) {
            System.out.println("You need content to save!");
            myToast("You need content to save!", 1);
            return false;
        }

        if (content.length() < 10) {
            System.out.println("content: " + content);
        } else {
            System.out.println("content: " + content.substring(0, 8));
        }

        String fileName = noteFile;
        if (creatingNewNote) {
            /* if we have to make a new note file, then try using the title as
             * the filename */
            fileName = UUID.randomUUID().toString();


            /* if fileName is already taken in the list of notes, append 1 or
             * something... */
            File testExistence = this.getBaseContext()
                    .getFileStreamPath(fileName);
            System.out.println(
                    "testing the existence of the file: " + fileName);
            int i = 1;
            while (testExistence.exists()) {
                System.out.println("File ALREADY EXISTS!!!!!!");
                fileName = UUID.randomUUID().toString(); /* that filename is
                                                          * taken so generate
                                                          * another */
                i++;
                testExistence = this.getBaseContext()
                        .getFileStreamPath(fileName);
            }
        }

        /* construct the entire string to write to the filesystem file:
         * content: note_title + note_content; */
        int fileDelimiter = this.getApplicationContext()
                .getResources()
                .getInteger(
                        R.integer.BYTE_VALUE_DELIMITER_FOR_SPLITTING_NOTE_FILES_TITLE_AND_CONTENT);
        String entireContent = title +
                String.valueOf((char)fileDelimiter) +
                content;

        // save the entire string to file with file name:fileName
        myTextToFile(entireContent, fileName);
        noteFile = fileName;
        noteTitle = title;

        // update the List file with the new addition;
        if (creatingNewNote) {
            NoteManager.addNote(title, fileName);
            creatingNewNote = false;
            /* otherwise when creating a new note, multiple saves will result
            * in multiple files. */
        } else {
            NoteManager.updateTitle(fileName, title);
        }

        return true;
    }

    /**
     * Saves the given content to the given filename as a textfile in the
     * application's internal storage
     *
     * @param content the bytes content of the file
     * @param fileNameArg the filename to use
     */
    private void myTextToFile(String content, String fileNameArg) {
        try {
            FileOutputStream fos = this.getBaseContext()
                    .openFileOutput(fileNameArg, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
            System.out.println("Successfully wrote note file: " + fileNameArg);
        } catch (Exception e) {
            myToast("Failed to save note to file...", 1);
            if (e.getMessage()!=null) {
                myToast(e.getMessage(), 2);
            }
            System.exit(2);
        }
    }

    /**
     * Custom method to display a Toast message
     *
     * @param msg message to display
     * @param length_of_time integer either 1 for short time or 2 for longer
     */
    private void myToast(String msg, int length_of_time){
        if (length_of_time == 2) {
            Toast.makeText(
                    this.getBaseContext(),
                    msg,
                    Toast.LENGTH_LONG)
                            .show();
        }

        if (length_of_time == 1) {
            Toast.makeText(
                    this.getBaseContext(),
                    msg,
                    Toast.LENGTH_SHORT)
                            .show();
        }
    }
}
