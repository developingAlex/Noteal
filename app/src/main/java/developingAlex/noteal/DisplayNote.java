package developingAlex.noteal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

        creatingNewNote = getIntent().getBooleanExtra(MainActivity.EXTRA_NEWNOTE, true); //TODO: I
        // believe this line is what's causing the app to crash when it's been inactive for a long
        // duration but subsequently switched back to and then crashes. The whole process was
        // previously killed by the Android system, but then the system brings it back to this class
        // upon resuming and it then thinks it was just created from the user selecting it in the
        // list but it's not so this line fails.
        if (!creatingNewNote){
            noteTitle = getIntent().getStringExtra(MainActivity.EXTRA_NOTETITLE);
            noteFile =  getIntent().getStringExtra(MainActivity.EXTRA_NOTEFILE);

//			myToast("Loading Note...",1);
//			myToast("noteTitle="+noteTitle,1);
//			myToast("noteFile="+noteFile,1);
            System.out.println("Loading Note...");
            System.out.println("noteTitle="+noteTitle);
            System.out.println("noteFile="+ noteFile);

            ArrayList<String> note = NoteManager.loadNote(noteFile);
            if (note.size()!=2){
                myToast("Bad error: note was meant to be composed of title and contents, (2 things) ...didn't find 2 things..", 1);
            }else{
                ((EditText)findViewById(R.id.title_text)).setText(note.get(0));
                ((EditText)findViewById(R.id.body_text)).setText(note.get(1));
            }
        }else{
//			myToast("new Note...",1);
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


    public void onBackPressed(){
        String title = ((EditText)findViewById(R.id.title_text)).getText().toString();
        String content = ((EditText)findViewById(R.id.body_text)).getText().toString();
        if (!(content.length() == 0 || title.length() == 0)){//using length because to use isEmpty() requires api lvl 9 or higher...
            saveNoteToAppStorage();
        } //only save when back is pressed if a title and contents are present. otherwise disregard
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    private String cleanFileName(String filenameToClean){
        return filenameToClean.replaceAll("[^a-zA-Z0-9]","");
    }


    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==R.id.action_save){
            if(saveNoteToAppStorage()==false){
                return true;
            }
        }
        if (item.getItemId()==R.id.action_export){
            if(externalMediaWriteable() && noteFile.compareTo("")!= 0){
                String content = ((EditText)findViewById(R.id.body_text)).getText().toString();
                String title = ((EditText)findViewById(R.id.title_text)).getText().toString();
                String proposedExportedFilename = cleanFileName(title);
                if(proposedExportedFilename.compareTo("") == 0){
                    myToast("Cannot export note with that title, please change title to " +
                            "consist of simple english characters and numbers", 2);
                    return true;
                }
                //TODO: prompt user to overwrite any existing file with same filename
                String filename = "noteal-"+ proposedExportedFilename + ".txt";
                exportNoteToSDCard(content, filename);
            }
        }
        return true; //return false to allow normal menu processing to proceed, true to consume it here.
    }


    //return true if able to write to external media, false if not.
    private boolean externalMediaWriteable(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            return true; //can read and write
        } else {
            myToast("Unable to write to SD card because SD media is not mounted.\nExternal storage state:"+ state,2);

            return false;
        }
    }

    //writes the string con out to the file specified by path on the sdcard
    //return true on success,
    //print problem and return false on failure.
    private boolean exportNoteToSDCard(String con, String filename){
        File file = new File(this.getBaseContext().getExternalFilesDir(null), filename);
        try{
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println(con);
            pw.flush();
            pw.close();
            f.close();
            myToast("Note saved to: " + file.getAbsolutePath(),2);
        }catch(FileNotFoundException e){
            // remnant logic from when permissions mattered, may not be required any more?
            myToast("Error writing to file...",1);
            myToast(e.getMessage(),2);
            return false;
        }catch(IOException e){
            myToast("There was an IO error",1);
            return false;
        }

        return true;
    }


    /**
     * Save the Note being displayed in the current activity to app storage.
     * @return True if successful, False otherwise.
     */
    private boolean saveNoteToAppStorage(){

//		Take the contents and title from the GUI and save it into a new file. Also update the List file to reflect the fact we now have a new file.
        String title = ((EditText)findViewById(R.id.title_text)).getText().toString();
        if(title.compareTo("") == 0){
            System.out.println("You need a title to save!");
            myToast("You need a title to save!",1);
            return false;
        }

        System.out.println("Title: " + title);
        String content = ((EditText)findViewById(R.id.body_text)).getText().toString();
        if(content.compareTo("") == 0){
            System.out.println("You need content to save!");
            myToast("You need content to save!",1);
            return false;
        }
        if(content.length()<10){
//			myToast("content: "+content, 1);
            System.out.println("content: "+content);
        }else{
//			myToast("content: "+content.substring(0,8),1);
            System.out.println("content: "+content.substring(0,8));
        }
        String fileName = noteFile;
        if(creatingNewNote) {//if we have to make a new note file, then try using the title as the filename
            fileName = UUID.randomUUID().toString();


//			if fileName is already taken in the list of notes, append 1 or something...
//			File testExistence = new File(fileName);//this line doesn't seem to work
            File testExistence = this.getBaseContext().getFileStreamPath(fileName);
            int i = 1;
            System.out.println("testing the existence of the file: "+fileName);
            while(testExistence.exists()){
                System.out.println("File ALREADY EXISTS!!!!!!");
                fileName = UUID.randomUUID().toString(); // that filename is taken so generate another
                i++;
                testExistence = this.getBaseContext().getFileStreamPath(fileName);
            }
        }
//		construct the entire string to write to the filesystem file: content: note_title + note_content;
        int fileDelimiter = this.getApplicationContext().getResources().getInteger(R.integer.BYTE_VALUE_DELIMITER_FOR_SPLITTING_NOTE_FILES_TITLE_AND_CONTENT);
        String entireContent = title + String.valueOf((char)fileDelimiter) + content;
//		save the entire string to file with file name:fileName
        myTextToFile(entireContent,fileName);
        noteFile = fileName;
        noteTitle = title;
        //update the List file with the new addition;
        if(creatingNewNote){
            NoteManager.addNote(title, fileName);
            creatingNewNote=false; //otherwise when creating a new note, multiple saves will result in multiple files.
        }else{
            NoteManager.updateTitle(fileName, title);
        }
        return true;
    }


    private int myTextToFile(String content, String fileNameArg){
        try{
            FileOutputStream fos = this.getBaseContext().openFileOutput(fileNameArg, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
//			myToast("Successfully wrote note file: "+fileNameArg, 1);
            System.out.println("Successfully wrote note file: "+fileNameArg);
        }catch(Exception e){
            myToast("Failed to save note to file...",1);
            if (e.getMessage()!=null){
                myToast(e.getMessage(), 2);
            }
            System.exit(2);
        }

        return 0;
    }


    private void myToast(String msg, int length_of_time){
        if (length_of_time==2){
            Toast.makeText(this.getBaseContext(), msg, Toast.LENGTH_LONG).show();
        }
        if(length_of_time==1){
            Toast.makeText(this.getBaseContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
