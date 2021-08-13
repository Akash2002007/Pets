package com.example.pets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pets.Data.PetContract;
import com.example.pets.Data.PetDpHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;
    PetCursorAdapter mCursorAdapter;

//    private PetDpHelper mDpHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
//         To access our database, we instantiate our subclass of SQLiteOpenHelper
//         and pass the context, which is the current activity.
//        mDpHelper=new PetDpHelper(this);
//
//        find the list view which will be populated with the pet data
        ListView petListView =(ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView= findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        //Setup an adapter to create  a list item for each row of pet data in the cursor
        //There is no pet data yet(Until the loader finishes) so pass in null for the cursor
        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        //set up item click listener to show edit view
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Create a new intent to go to the editors activity
                Intent intent= new Intent(CatalogActivity.this, EditorActivity.class);

                //From the content uri that represents the specific pet that was clicked on,
                //by appending the id (passed as input to this method ) onto the
                //{@link PetEntry#CONTENT_URI}
                //For example, the URI would be "content://com.example.android.pets/pets/2"
                //if the pet with id 2 is clicked on
                Uri contentPetUri= ContentUris.withAppendedId(PetContract.PetEntry.CONTENT_URI, id);

                //set the uri on the data field of intent
                intent.setData(contentPetUri);

                //Launch the {@link EditorActivity} to display the data for current pet.
                startActivity(intent);
            }
        });

        //Kick of the loader
       getSupportLoaderManager().initLoader(PET_LOADER, null, this);

    }
//    @Override
//    protected void onStart(){
//        super.onStart();
//        displayDatabaseInfo();
//    }
    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
//    private void displayDatabaseInfo(){
//        // To access our database, we instantiate our subclass of SQLiteOpenHelper
//        // and pass the context, which is the current activity.
////        PetDpHelper mDpHelper =new PetDpHelper(this);
////
////        // Create and/or open a database to read from it
////        SQLiteDatabase db=mDpHelper.getReadableDatabase();
//
//        // Perform this raw SQL query "SELECT * FROM pets"
//        // to get a Cursor that contains all rows from the pets table.
////        Cursor cursor=db.rawQuery("SELECT * FROM "+ PetContract.PetEntry.Table_Name, null);
//
//        String[] projection={
//                PetContract.PetEntry._ID,
//                PetContract.PetEntry.COLUMN_PET_NAME,
//                PetContract.PetEntry.COLUMN_PET_BREED,
//                PetContract.PetEntry.COLUMN_PET_GENDER,
//                PetContract.PetEntry.COLUMN_PET_WEIGHT
//        };
////        Cursor cursor=db.query(
////            PetContract.PetEntry.Table_Name,
////            projection,  //This is for specific columns that we want that are specified in projection String Array above
////            null,
////            null,
////            null,
////            null,
////            null  );
//
//        Cursor cursor=getContentResolver().query(PetContract.PetEntry.CONTENT_URI,projection,null,null,null);
//
//        //find the list view which will be populated with the pet data
//        ListView petListView =(ListView) findViewById(R.id.list);
//
//        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
//        View emptyView= findViewById(R.id.empty_view);
//        petListView.setEmptyView(emptyView);
//
//        //Setup an adapter to create  a list item for each row of pet data in the cursor
//        PetCursorAdapter adapter= new PetCursorAdapter(this, cursor);
//
//        //Attach the adapter to the list View
//        petListView.setAdapter(adapter);
//
////        TextView displayView =(TextView) findViewById(R.id.text_view_pet);
//
////        try {
////
////            // Create a header in the Text View that looks like this:
////            //
////            // The pets table contains <number of rows in Cursor> pets.
////            // _id - name - breed - gender - weight
////            //
////            // In the while loop below, iterate through the rows of the cursor and display
////            // the information from each column in this order.
////            displayView.setText("The pets table contains "+ cursor.getCount() +" pets.\n\n");
////            displayView.append(PetContract.PetEntry._ID + " | " + PetContract.PetEntry.COLUMN_PET_NAME+
////                    " | "+PetContract.PetEntry.COLUMN_PET_BREED + " | " + PetContract.PetEntry.COLUMN_PET_GENDER+
////                    " | "+ PetContract.PetEntry.COLUMN_PET_WEIGHT+"\n");
////            // Figure out the index of each column
////            int idColumnIndex=cursor.getColumnIndex(PetContract.PetEntry._ID);
////            int nameColumnIndex=cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
////            int BreedColumnIndex=cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);
////            int GenderColumnIndex=cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_GENDER);
////            int weightColumnIndex=cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_WEIGHT);
////
////            // Iterate through all the returned rows in the cursor
////            while (cursor.moveToNext()){
////                // Use that index to extract the String or Int value of the word
////                // at the current row the cursor is on.
////                int CurrentId=cursor.getInt(idColumnIndex);
////                String CurrentName=cursor.getString(nameColumnIndex);
////                String CurrentBreed=cursor.getString(BreedColumnIndex);
////                int CurrentGender=cursor.getInt(GenderColumnIndex);
////                int CurrentWeight=cursor.getInt(weightColumnIndex);
////
////                // Display the values from each column of the current row in the cursor in the TextView
////                displayView.append(("\n" + CurrentId + " | " +
////                                            CurrentName + " | " +
////                                        CurrentBreed + " | " +
////                                        CurrentGender + " | " +
////                                        CurrentWeight));
////            }
////        }finally {
////            // Always close the cursor when you're done reading from it. This releases all its
////            // resources and makes it invalid.
////            cursor.close();
////        }
//    }



    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void InsertPet(){

        // Gets the database in write mode
//        SQLiteDatabase db= mDpHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues Values=new ContentValues();
        Values.put(PetContract.PetEntry.COLUMN_PET_NAME, "Toto");
        Values.put(PetContract.PetEntry.COLUMN_PET_BREED, "Terrier");
        Values.put(PetContract.PetEntry.COLUMN_PET_GENDER, PetContract.PetEntry.GENDER_MALE);
        Values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insert a new row for Toto in the database, returning the ID of that new row.
        // The first argument for db.insert() is the pets table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
//        long newRowId=db.insert(PetContract.PetEntry.Table_Name,null, Values);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri=getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, Values);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                InsertPet();
               // displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                    showDeleteConfirmation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets(){
        int rowsDeleted= getContentResolver().delete(PetContract.PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted +"roes deleted from pet Database");
        if (rowsDeleted== 0){
            Toast.makeText(this, "No pets are deleted ", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "All Pets are deleted ", Toast.LENGTH_SHORT).show();
        }
    }
private void showDeleteConfirmation(){
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(" Delete All the Pets ?");
    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            deleteAllPets();
        }
    });
    builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
if (dialog != null){
dialog.dismiss();
}
        }
    });
    AlertDialog alertDialog=builder.create();
    alertDialog.show();
}

    @NonNull

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define a projection that specifies the columns from the table we care about
        String[] projection = {
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED
        };
        //this loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,//Present activity content
                PetContract.PetEntry.CONTENT_URI,//provide content uri to query
                projection,//Columns to include in the resulting cursor
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update with new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }
}