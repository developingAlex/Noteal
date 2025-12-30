package developingAlex.noteal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

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
                MainActivity.EXTRA_IS_NEW_NOTE,
                true);
        if (!creatingNewNote) {
            noteTitle = getIntent().getStringExtra(
                    MainActivity.EXTRA_NOTETITLE);
            noteFile = getIntent().getStringExtra(
                    MainActivity.EXTRA_NOTEFILE);

            System.out.println("Loading Note...");
            System.out.println("noteTitle = " + noteTitle);
            System.out.println("noteFile = " + noteFile);

            try {
                ArrayList<String> note = NoteManager.loadNote(noteFile);
                ((EditText)findViewById(R.id.title_text)).setText(note.get(0));
                ((EditText)findViewById(R.id.body_text)).setText(note.get(1));
            } catch (NoteManagerException e) {
                myToast(e.getMessage(), Toast.LENGTH_LONG);
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
        // Only save if a title and contents are present, otherwise disregard.
        if (!(content.isEmpty() || title.isEmpty())) {
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
    public static String cleanFileName(String filenameToClean) {
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
                                Toast.LENGTH_LONG);
                        return true;
                    }
                    // TODO: prompt user to overwrite any existing file with same
                    //  filename
                    String filename = "noteal-" + proposedExportedFilename + ".txt";
                    File file = new File(
                            this.getBaseContext().getExternalFilesDir(null),
                            filename
                    );
                    String result = exportNoteToSDCard(content, file);
                    if (result == "success") {
                        myToast("Note saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG);
                    } else {
                        myToast(result, Toast.LENGTH_LONG);
                    }
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
                    Toast.LENGTH_LONG);
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
     * @param content contents of the file
     * @param file the file to use
     * @return String message indicating success or failure
     */
    public static String exportNoteToSDCard(String content, File file) {

//        File file = new File(
//                getApplicationContext().getExternalFilesDir(null),
//                filename);
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println(content);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            // remnant logic from when permissions mattered,
            // may not be required any more?
            return "error: FileNotFoundException: " + e.getMessage();
        } catch (IOException e) {
            return "error: IOException: " + e.getMessage();
        }
        return "success";
    }


    /**
     * Save the Note being displayed in the current activity to application-
     * specific internal storage. (this is some storage only accessible to
     * the app, I don't think you can see it even when plugging the phone
     * into a computer with a usb cable and filesharing mode enabled, a
     * little confusing because we have the function exportNoteToSDCard which
     * states also that it exports to app-specific storage which means inside
     * the phones /android/data/ folder )
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
            myToast("You need a title to save!", Toast.LENGTH_SHORT);
            return false;
        }

        System.out.println("Title: " + title);
        String content = ((EditText)findViewById(R.id.body_text))
                .getText()
                .toString();
        if (content.compareTo("") == 0) {
            System.out.println("You need content to save!");
            myToast("You need content to save!", Toast.LENGTH_SHORT);
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


            /* probably overkill, but if the random UUID fileName is already taken in the list of
             notes, generate another one */
            File filePath = this.getBaseContext()
                    .getFileStreamPath(fileName);
            System.out.println(
                    "testing the existence of the file: " + fileName);
            int i = 1;
            while (filePath.exists()) {
                System.out.println("File ALREADY EXISTS!!!!!!");
                fileName = UUID.randomUUID().toString(); /* that filename is
                                                          * taken so generate
                                                          * another */
                i++;
                filePath = this.getBaseContext()
                        .getFileStreamPath(fileName);
            }
        }

        /* construct the entire string to write to the filesystem file:
         * content: note_title + note_content; */
        // Todo: why am I writing the title into the file as well? its' already tracked by the
        //  note array.
        String entireContent = NoteManager.deDuplicateTitle(title, fileName) +
                String.valueOf((char)NoteManager.NOTE_FILE_DELIMITER) +
                content;

        // save the entire string to file named fileName
        try {
            NoteManager.saveTextToFile(entireContent, fileName);
        } catch (NoteManagerException e) {
            myToast("Failed to save note to file: " + e.getMessage(), Toast.LENGTH_LONG);
        }

        noteFile = fileName;

        noteTitle = title;

        // update the List file with the new addition;
        try {
            if (creatingNewNote) {
                NoteManager.addNote(title, fileName);

                creatingNewNote = false;
            } else {
                    NoteManager.updateTitle(fileName, title);
            }
        } catch (NoteManagerException e) {
            myToast(e.getMessage(), Toast.LENGTH_LONG);
        }
        return true;
    }

    /**
     * Custom method to display a Toast message
     *
     * @param message message to display
     * @param duration Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     */
    private void myToast(String message, int duration){
        Toast.makeText(this.getBaseContext(), message, duration).show();
    }
}
