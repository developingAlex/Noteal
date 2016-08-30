package developingalex.noteal;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements DialogInterface.OnClickListener {
    public static final String EXTRA_NEWNOTE = "com.example.Noteal1.NEWNOTE";
    public static final String EXTRA_NOTETITLE = "com.example.Noteal1.NOTETITLE";
    public static final String EXTRA_NOTEFILE = "com.example.Noteal1.NOTEFILE";
    public static Color colorOfBackground;
    public static Color colorOfText;
    private static int noteToDelete = -1;
    //private static ArrayList<String> notesArray;
    public NoteManager nm;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println(getString(R.string.welcome_message));
        nm = new NoteManager(this.getBaseContext(), this);

        //pull in the list of note files and titles to display in the list of notes.
//		Call to read from the set file and return an array.



        lv = (ListView) this.findViewById(R.id.list);
        lv.setBackgroundColor(getResources().getColor(R.color.Black));
        lv.setCacheColorHint(getResources().getColor(R.color.Green));
        lv.setDrawSelectorOnTop(true);
        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new
                                              AdapterView.OnItemLongClickListener() {
                                                  @Override
                                                  public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                                                      return onLongListItemClick(v, pos, id);
                                                  }
                                              });
        lv.setOnItemClickListener(new
                                          AdapterView.OnItemClickListener() {
                                              @Override
                                              public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                                                  onListItemClick(lv, v, pos, id);
                                              }
                                          });
        updateList();
    }

    public void updateList() {
        lv.setAdapter(new ArrayAdapter<String>(this,
                R.layout.my_list_layout, getNotesTitles(NoteManager.getNotesArray())));
    }


    private ArrayList<String> getNotesTitles(ArrayList<String> x){
        if(x==null)
            return null;
        if(x.isEmpty())
            return x;
        ArrayList<String> y = new ArrayList<String>();
        int i;
        for (i=0; i<x.size(); i+=2){
            y.add(x.get(i));
            System.out.println("Pulling titles from notesarray, got:"+x.get(i));
        }
        return y;
    }
    public void onBackPressed(){
        moveTaskToBack(true);

    }
    //when the user taps a note in the list of notes
    protected void onListItemClick(ListView l, View v, int position, long id){
        //super.onListItemClick(l, v, position, id);
        Object o = lv.getAdapter().getItem(position);
        String noteTitle = o.toString();
        String noteFile = NoteManager.getNotesArray().get((position*2)+1);
        System.out.println("You selected: "+ noteTitle);
        System.out.println("which uses the file: "+ noteFile);
        Intent intent = new Intent(this, DisplayNote.class);
        intent.putExtra(EXTRA_NEWNOTE, false);//false: load it from it's file.
        intent.putExtra(EXTRA_NOTETITLE, noteTitle);
        intent.putExtra(EXTRA_NOTEFILE, noteFile);
        startActivity(intent);
    }

    //when the user holds down on a note in the list of notes:
    protected boolean onLongListItemClick(View v, int pos, long id){
        //bring up the dialog box to confirm deletion.
        AlertDialog ad = new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_delete_note)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(R.string.dialog_delete_title)
                .setPositiveButton("Yes", this)
                .setNegativeButton("No", this)
                .setCancelable(false)
                .create();
        ad.show();
        noteToDelete=pos;
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
        switch(which){
            case DialogInterface.BUTTON_POSITIVE: //yes
                if(noteToDelete != -1){
                    myToast("Deleted..",1);
                    deleteNote(noteToDelete);
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE: //no
                break;
            case DialogInterface.BUTTON_NEUTRAL: //nuetered //unnecessary
                myToast("Please select yes or no",1);
                break;
            default:
                // nothing
                break;
        }
    }



    //method to call to delete a note at a certain location in the list, list starts at position:0
    private void deleteNote(int listPosition){
        //remove from the note list and update the view, remove from the index file and delete off file system.
        NoteManager.removeNote(listPosition);
        updateList();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==R.id.action_add){
//			Show the next activity with blank inputs

            Intent intent = new Intent(this, DisplayNote.class);
            intent.putExtra(EXTRA_NEWNOTE, true); //true: it's a new note
            startActivity(intent);
            return true;
        }
        return true;
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


    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
*/