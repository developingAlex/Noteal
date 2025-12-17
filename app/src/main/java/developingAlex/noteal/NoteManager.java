package developingAlex.noteal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import android.content.Context;
import android.widget.Toast;

public final class NoteManager {

    private static ArrayList<String> notesArray;
    private static Context MainActivityContext;
    private static MainActivity mainActivity;
    final static int NOTE_FILE_DELIMITER = 2; //separates title from content, one per note file.
    final static int INDEX_FILE_DELIMITER = 28; // separates titles and filenames in the index file.

    public NoteManager (Context cx, MainActivity ma) throws NoteManagerException{
        MainActivityContext = cx;
        notesArray = loadNotesArray();
        mainActivity = ma;
    }


    public static Context getContext(){
        return MainActivityContext;
    }



    public static void updateTitle(int position, String newTitle) throws NoteManagerException {
        notesArray.set(position, newTitle);
        saveIndexToFile();
        mainActivity.updateList();
    }

    /**
     * find which filename/note it's talking about and then update the title.
     * remember the format of the notesArray is {title, filename, ..., title, filename} // TODO This should be a list of {title,filename} constructs to clarify.
     * so find the position of the filename, then alter the value of the position preceding that.
     * @param filename
     * @param newTitle
     * @throws NoteManagerException
     */
    public static void updateTitle(String filename, String newTitle) throws NoteManagerException {
        for(int i = 1; i < notesArray.size(); i += 2){
            if (notesArray.get(i).compareTo(filename) == 0){
                updateTitle(i - 1, newTitle);
                break;
            }
        }
    }

    public static void exportAllNotes() throws NoteManagerException {
        DateTimeFormatter stampFormat = DateTimeFormatter.ofPattern("uuuuMMddHHmmss"); // eg 20251130205923
        String timeStamp = LocalDateTime.now().format(stampFormat);
        for (int i = 0; i < notesArray.size(); i += 2 ){
            // i = note title, i+1 = note filename holding the contents..
            ArrayList<String> note = loadNote(notesArray.get(i + 1));
            String fileName = DisplayNote.cleanFileName(note.get(0));
            fileName = (timeStamp + "_" + fileName + ".txt");
            String fileContent = note.get(1);
            File file = new File(MainActivityContext.getExternalFilesDir(null), fileName);
            String result = DisplayNote.exportNoteToSDCard(fileContent, file);
            if (result == "success") {
                System.out.println("exported note to " + file.getAbsolutePath());
            } else {
                System.out.println(result);
            }
        }
    }


    public static ArrayList<String> getNotesArray(){
        return notesArray;
    }


    /**
     * Adds a new entry to the list of Notes that appears on the main activity and updates that list.
     *
    @param title The title of the Note
     @param fileName the filename of the Note (as saved in Application internal storage)
     */
    public static void addNote (String title, String fileName) throws NoteManagerException{
        //add the new note title and fileName;
        notesArray.add(title);
        notesArray.add(fileName);
        saveIndexToFile();
        mainActivity.updateList();
    }


    /**
     * Removes a note from the list of Notes that appears on teh main activity and
     * deletes the associated file stored in application internal storage
     * @param position The position of the note in the list of notes
     */
    public static void removeNote(int position) throws NoteManagerException {
        notesArray.remove(position * 2); //remove twice the same position; first time it's the note's title
        String filename = notesArray.get(position * 2);
        notesArray.remove(position * 2);// second time is the note's file
        saveIndexToFile();
        //remove the note from the filesystem too:
        File fileToDelete = mainActivity.getBaseContext().getFileStreamPath(filename);
        if (fileToDelete.exists()) {
            System.out.println("The file was found and will be deleted");
            if (fileToDelete.delete()) {
                System.out.println("The file was successfully deleted");
            } else
                throw new NoteManagerException("couldn't delete note " + position);
        }else
            throw new NoteManagerException("couldn't find note " + position + " to delete");
    }


    private static void saveIndexToFile() throws NoteManagerException {
        //Save the list out to file(using valueof(INDEX_DELIMITER) (the asci decimal code for the file
        // separator character) to separate titles and filenames and using the correct filename.
        String file = MainActivityContext.getString(R.string.notelist_file);
        String fileContent = "";
        for(int i = 0; i < notesArray.size(); i += 2){
            fileContent += notesArray.get(i) + String.valueOf((char) INDEX_FILE_DELIMITER) + notesArray.get(i + 1) + String.valueOf((char) INDEX_FILE_DELIMITER);
        }
        for(int i = 0; i < notesArray.size(); i++){
            System.out.println(notesArray.get(i));
        }
        try {
            saveTextToFile(fileContent, file);
        } catch (NoteManagerException e) {
            throw new NoteManagerException("Failed to save index to file: " + e.getMessage());
        }

    }


    /**
     * Saves the given content to the given filename as a textfile in the
     * application's internal storage
     *
     * @param content the bytes content of the file
     * @param fileNameArg the filename to use
     */
    public static void saveTextToFile(String content, String fileNameArg) throws NoteManagerException {
        try{
            FileOutputStream fos = MainActivityContext.openFileOutput(fileNameArg, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
        }catch(Exception e){
            throw new NoteManagerException(e.getMessage());
        }
    }


    /**
     * loadNote takes a filename argument and attempts to open and read the contents of that
     * filename in the applications internal private storage, if it can read the content it treats
     * a byte of a value of 2 as the delimiter between Title and Content. It then returns a String
     * Arraylist of 2 elements, the first being the note's title, the second being the note's
     * content, if from reading the file there is no byte delimiter of a value of 2 then it will
     * return a value of null.
     * @param filename
     * @return Arraylist of length 2 where first item is the title and second is the content, or null
     */
    public static ArrayList<String> loadNote(String filename) throws NoteManagerException {
        ArrayList<String> note = new ArrayList<String>();
        String title, content;
        title = "";
        content = "";
        try{
            FileInputStream fis = MainActivityContext.openFileInput(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, "UTF8"));
            int nextByte = bufferedReader.read();
            //read the title:
            while(nextByte != NOTE_FILE_DELIMITER){
                if(nextByte != -1) {
                    title += String.valueOf((char) nextByte);
                    nextByte = bufferedReader.read();
                }else{
                    //we've reached the end of the file and there was no byte of value 2?
                    throw new NoteManagerException("The filename provided was a file that didn't have an appropriate title/content delimiter");
                }
            }
            note.add(title);

            if(nextByte == -1){
                note.add(content);
                return note;
            }
            nextByte = bufferedReader.read();
            while(nextByte != -1 && nextByte != NOTE_FILE_DELIMITER){
                content += String.valueOf((char)nextByte);
                nextByte = bufferedReader.read();
            }
            note.add(content);

        } catch(Exception e){
            throw new NoteManagerException("There was an issue when attempting to load the note: " + e.getMessage());
        }
        return note;
    }


    private ArrayList<String> loadNotesArray() throws NoteManagerException{

        String file = MainActivityContext.getString(R.string.notelist_file);
        ArrayList<String> list = new ArrayList<String>();
        try{
            System.out.println("trying to open notelistfile: " + file);
            FileInputStream fis = MainActivityContext.openFileInput(file);
            System.out.println("about to read");
            int next = fis.read();
            System.out.println("fis.read():" + next + "=" + String.valueOf((char)next));
            System.out.println("read a byte!");
            while (next != -1){ // loop to read to the end of the file
                String noteTitle = "";
                while (next != INDEX_FILE_DELIMITER && next != -1){ // loop to read to till the next "file separator" character or eof
                    noteTitle += String.valueOf((char)next);
                    next = fis.read();
//					debug:
//					System.out.println("fis.read():"+next+"="+String.valueOf((char)next));
                }
                if (next == -1)
                    break;
                next = fis.read();
                if (next == -1)
                    break;
                String noteFile = "";
                while (next != INDEX_FILE_DELIMITER && next != -1){ // loop to read to till the next "file separator" character or eof
                    noteFile += String.valueOf((char)next);
                    next = fis.read();
                }
                // add the noteTitle and noteFile to the arraylist.
                list.add(noteTitle);
                System.out.println("adding to list:" + noteTitle);
                list.add(noteFile);
                System.out.println("adding to list:" + noteFile);
                if (next == -1)
                    break;
                next = fis.read();
            }
            fis.close();
        } catch(FileNotFoundException e) {
            // assuming no noteslist means first time use...
            Toast.makeText(MainActivityContext, "Welcome!", Toast.LENGTH_SHORT).show();
            return list;
        } catch(IOException e) {
            throw new NoteManagerException("There was an error trying to read the notelist file: " + e.getMessage());
        }
        if(!list.isEmpty() && (list.size() % 2) == 0){
            System.out.println("Noteslist file was valid, size:" + list.size());
        }else{
            if(list.isEmpty()){
                System.out.println("no Noteslist file contents");
            }else{
                System.out.println("Notelist has an uneven number of elements");
                throw new NoteManagerException("Corrupt Index File (odd number)");
            }
        }
        return list;
    }

}
