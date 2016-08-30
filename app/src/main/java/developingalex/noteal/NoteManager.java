package developingalex.noteal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public final class NoteManager {
    private static ArrayList<String> notesArray;
    private static Context MainActivityContext;
    private static MainActivity MA;

    public NoteManager (Context cx, MainActivity ma){
        MainActivityContext = cx;
        notesArray = loadNotesArray();
        MA = ma;
    }
    public static Context getContext(){
        return MainActivityContext;
    }
    public static void updateTitle(String filename, String newTitle){
        //find which filename/note it's talking about and then update the title.
        //remember the format of the notesArray is {title, filename, ..., title, filename}
//		so find the position of the filename, then alter the value of the postion preceding that.
        int i;
        for(i=1; i<notesArray.size(); i+=2){
            if (notesArray.get(i).compareTo(filename)==0){
                break;
            }
        }
        updateTitle(i-1, newTitle);

    }

    public static void updateTitle(int position, String newTitle){
        notesArray.set(position, newTitle);
        saveIndexToFile();
        MA.updateList();
    }
    public static ArrayList<String> getNotesArray(){
        return notesArray;
    }
    public static void addNote (String title, String fileName){
        //add the new note title and fileName;
        notesArray.add(title);
        notesArray.add(fileName);
        saveIndexToFile();
        MA.updateList();
    }
    public static void removeNote(int position){
        notesArray.remove(position*2); //remove twice the same position; first time it's the note's title
        String filename = notesArray.get(position*2);
        notesArray.remove(position*2);// second time is the note's file
        saveIndexToFile();
        //remove the note from the filesystem too:
        File fileToDelete = MA.getBaseContext().getFileStreamPath(filename);
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
        //Save the list out to file(using valueof(28) to separate titles and filenames and using the correct filename.
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
            System.out.println("Failed to save index to file...");
            if (e.getMessage()!=null){
                myToast(e.getMessage(), 2);
            }
            System.exit(2);
        }

        return 0;
    }
    public static ArrayList<String> loadNote(String filename){
        ArrayList<String> note = new ArrayList<String>();
        String title, content;
        title = content = "";
        try{
            FileInputStream fis = MainActivityContext.openFileInput(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, "UTF8"));
            int next = bufferedReader.read();
            while(next != -1 && next != 2){
                title += String.valueOf((char)next);
                next = bufferedReader.read();
            }
            note.add(title);
//			debug:
//			System.out.println("finished loading note title");
            if(next == -1){
                note.add(content);
                return note;
            }
            next = bufferedReader.read();
            while(next != -1 && next != 2){
                content += String.valueOf((char)next);
                next = bufferedReader.read();
            }
            note.add(content);
//			debug:
//			System.out.println("finished loading note");
        }catch(Exception e){
            myToast(e.getMessage(),1);
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
            System.out.println("about to read");
            int next = fis.read();
            System.out.println("fis.read():"+next+"="+String.valueOf((char)next));
            System.out.println("read a byte!");
            while (next != -1){//loop to read to the end of the file
                String noteTitle = "";
                while (next != 28 && next != -1){//loop to read to till the next "file separator" character or eof
                    noteTitle += String.valueOf((char)next);
                    next = fis.read();
//					debug:
//					System.out.println("fis.read():"+next+"="+String.valueOf((char)next));
                }
                if (next == -1)
                    break;
                next = fis.read();
//				debug:
//				System.out.println("fis.read():"+next+"="+String.valueOf((char)next));
                if (next == -1)
                    break;
                String noteFile = "";
                while (next != 28 && next != -1){ //loop to read to till the next "file separator" character or eof
                    noteFile += String.valueOf((char)next);
                    next = fis.read();
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
                next = fis.read();
            }
            fis.close();
        }
        catch(Exception e){
            myToast("Welcome!",1);//assuming no noteslist means first time use...
            System.out.println(e.getMessage());
            return list;
        }
        if(!list.isEmpty() && list.size()%2==0){
//			myToast("Noteslist file was valid, size:"+list.size(),1);
            System.out.println("Noteslist file was valid, size:"+list.size());
        }else{
            if(list.isEmpty()){
                System.out.println("no Noteslist file contents");
            }else{
                System.out.println("Notelist has an uneven number of elements");
                myToast("Corrupt Index File[odd number], tell Alex he done goofed",2);
            }
        }
//		debug:
//		int i;
//		for(i=0; i<list.size();i++){
//			System.out.println("list.get(i):"+list.get(i));
//		}
        return list;
    }
    public static void myToast(String msg, int length_of_time){
        if (length_of_time==2){
            Toast.makeText(MainActivityContext, msg, Toast.LENGTH_LONG).show();
        }
        if(length_of_time==1){
            Toast.makeText(MainActivityContext, msg, Toast.LENGTH_SHORT).show();
        }
    }
}