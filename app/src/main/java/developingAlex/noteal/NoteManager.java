package developingAlex.noteal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.widget.Toast;

public final class NoteManager {

    private static ArrayList<String> notesArray;
    private static Context MainActivityContext;
    private static MainActivity mainActivity;
    final static int DELIMITER_BYTE_VALUE_FOR_NOTE_FILES = 2; //separates title from content, one per note file.


    public NoteManager (Context cx, MainActivity ma){
        MainActivityContext = cx;
        notesArray = loadNotesArray();
        mainActivity = ma;
    }


    public static Context getContext(){
        return MainActivityContext;
    }


    public static void updateTitle(String filename, String newTitle){
        //find which filename/note it's talking about and then update the title.
        //remember the format of the notesArray is {title, filename, ..., title, filename} //TODO This should be a list of {title,filename} constructs to clarify.
//		so find the position of the filename, then alter the value of the position preceding that.
        int i;
        for(i=1; i<notesArray.size(); i+=2){
            if (notesArray.get(i).compareTo(filename)==0){
                updateTitle(i-1, newTitle);
                break;
            }
        }
    }


    public static void updateTitle(int position, String newTitle){
        notesArray.set(position, newTitle);
        saveIndexToFile();
        mainActivity.updateList();
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
    public static void addNote (String title, String fileName){
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
    public static void removeNote(int position){
        notesArray.remove(position*2); //remove twice the same position; first time it's the note's title
        String filename = notesArray.get(position*2);
        notesArray.remove(position*2);// second time is the note's file
        saveIndexToFile();
        //remove the note from the filesystem too:
        File fileToDelete = mainActivity.getBaseContext().getFileStreamPath(filename);
        if(fileToDelete.exists()){
            System.out.println("The file was found and will be deleted");
            if(fileToDelete.delete()){
                System.out.println("The file was successfully deleted");
            }else
                myToast("couldn't delete the file!",1);
        }else
            myToast("couldn't find the file to delete!",1);
    }


    private static void saveIndexToFile(){
        //Save the list out to file(using valueof(28) (the asci decimal code for the file
        // separator character) to separate titles and filenames and using the correct filename.
        String file = MainActivityContext.getString(R.string.notelist_file);
        String fileContent="";
        for(int i = 0; i<notesArray.size();i+=2){
//					fileContent += notesArray.get(i) + String.valueOf((char)28) + notesArray.get(i++) + String.valueOf((char)28);// this line incorrectly increments i
            fileContent += notesArray.get(i) + String.valueOf((char)28) + notesArray.get(i+1) + String.valueOf((char)28);
        }
        for(int i = 0; i<notesArray.size();i++){
//					fileContent += notesArray.get(i) + String.valueOf((char)28) + notesArray.get(i++) + String.valueOf((char)28);// this line incorrectly increments i
            System.out.println(notesArray.get(i));
        }

        myTextToFile(fileContent, file);
//				debug:
//				System.out.println("this is what is being written to the noteslist.txt file:");
//				System.out.println(fileContent);
    }


    private static int myTextToFile(String content, String fileNameArg){
        try{
            FileOutputStream fos = MainActivityContext.openFileOutput(fileNameArg, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
//			myToast("Successfully wrote index file: "+fileNameArg, 1);
//			debug:
//			System.out.println("Successfully wrote index file: "+fileNameArg);
        }catch(Exception e){
//			myToast("Failed to save index to file...",1);
            System.out.println("Noteal: Failed to save index to file...");
            if (e.getMessage()!=null){
                myToast(e.getMessage(), 2);
            }
            System.exit(2);
        }

        return 0;
    }


    /**
     * loadNote takes a filename argument and attempts to open and read the contents of that
     * filename in the applications internal private storage, if it can read the content it treats
     * a byte of a value of 2 as the delimiter between Title and Content. It then returns a String
     * Arraylist of 2 elements one, the first being the note's title, the second being the note's
     * content, if from reading the file there is no byte delimiter of a value of 2 then it will
     * return a value of null.
     * @param filename
     * @return
     */
    public static ArrayList<String> loadNote(String filename){
        ArrayList<String> note = new ArrayList<String>();
        String title, content;
        title = content = "";
        int titleDelimiter = MainActivityContext.getResources().getInteger(R.integer.BYTE_VALUE_DELIMITER_FOR_SPLITTING_NOTE_FILES_TITLE_AND_CONTENT);
        try{
            FileInputStream fis = MainActivityContext.openFileInput(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, "UTF8"));
            int nextByte = bufferedReader.read();
            //read the title:
            while(nextByte != titleDelimiter){
                if(nextByte != -1) {
                    title += String.valueOf((char) nextByte);
                    nextByte = bufferedReader.read();
                }else{
                    //we've reached the end of the file and there was no byte of value 2?
                    myToast("The filename provided was a file that didn't have an appropriate title/content delimiter",2);
                    return null;
                }
            }
            note.add(title);

            if(nextByte == -1){
                note.add(content);
                return note;
            }
            nextByte = bufferedReader.read();
            while(nextByte != -1 && nextByte != 2){
                content += String.valueOf((char)nextByte);
                nextByte = bufferedReader.read();
            }
            note.add(content);

        }catch(Exception e){
            myToast("There was an issue when attempting to load the note: "+e.getMessage(),1);
        }
        return note;
    }


    private ArrayList<String> loadNotesArray(){
//		does the file exist?
        String file = MainActivityContext.getString(R.string.notelist_file);
        ArrayList<String> list = new ArrayList<String>();
        try{
            System.out.println("trying to open notelistfile: "+file);
            FileInputStream fis = MainActivityContext.openFileInput(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, "UTF8"));
            System.out.println("about to read");
            int next = bufferedReader.read();
            System.out.println("fis.read():"+next+"="+String.valueOf((char)next));
            System.out.println("read a byte!");
            while (next != -1){//loop to read to the end of the file
                String noteTitle = "";
                while (next != 28 && next != -1){//loop to read to till the next "file separator" character or eof
                    noteTitle += String.valueOf((char)next);
                    next = bufferedReader.read();
//					debug:
//					System.out.println("fis.read():"+next+"="+String.valueOf((char)next));
                }
                if (next == -1)
                    break;
                next = bufferedReader.read();
//				debug:
//				System.out.println("fis.read():"+next+"="+String.valueOf((char)next));
                if (next == -1)
                    break;
                String noteFile = "";
                while (next != 28 && next != -1){ //loop to read to till the next "file separator" character or eof
                    noteFile += String.valueOf((char)next);
                    next = bufferedReader.read();
//					debug:
//					System.out.println("fis.read():"+next+"="+String.valueOf((char)next));
                }
//				add the noteTitle and noteFile to the arraylist.
                list.add(noteTitle);
                System.out.println("adding to list:"+noteTitle);
                list.add(noteFile);
                System.out.println("adding to list:"+noteFile);
                if (next == -1)
                    break;
                next = bufferedReader.read();
            }
            fis.close();
        }
        catch(Exception e){
            myToast("Welcome!",1);//assuming no noteslist means first time use...
            System.out.println(e.getMessage());
            return list;
        }
        if(!list.isEmpty() && list.size()%2==0){
            System.out.println("Noteslist file was valid, size:"+list.size());
        }else{
            if(list.isEmpty()){
                System.out.println("no Noteslist file contents");
            }else{
                System.out.println("Notelist has an uneven number of elements");
                myToast("Corrupt Index File[odd number]",2);
            }
        }
        return list;
    }


    private static void myToast(String msg, int length_of_time){
        if (length_of_time==2){
            Toast.makeText(MainActivityContext, msg, Toast.LENGTH_LONG).show();
        }
        if(length_of_time==1){
            Toast.makeText(MainActivityContext, msg, Toast.LENGTH_SHORT).show();
        }
    }

}
