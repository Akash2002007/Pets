package com.example.pets.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.IllegalFormatException;
import java.util.IllformedLocaleException;

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG=PetProvider.class.getSimpleName();
    private PetDpHelper mDpHelper;

    private static final int PETS=100;
    private static final int PETS_ID =101;
    private static final UriMatcher sUriMatcher =new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS+"/#",PETS_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        mDpHelper= new PetDpHelper(getContext());

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database=mDpHelper.getReadableDatabase();
        Cursor cursor =null;
        int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:

            cursor=database.query(PetContract.PetEntry.Table_Name, projection, selection, selectionArgs,null, null,sortOrder);
             break;

            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs=new String[]{
                        String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetContract.PetEntry.Table_Name, projection , selection, selectionArgs, null ,null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
                }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

                return cursor;
        }





    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown Uri "+ uri+ " with match " + match);
        }

    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for "+ uri);
        }

         }
    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values){

        // Check that the name is not null
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if(name==null){
            throw new IllegalArgumentException("Pet requires a name");
        }

     Integer gender= values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender==null || !PetContract.PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight=values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight <0){
                throw new IllegalArgumentException("Pet requires valid weight");
            }


        // Get writeable database
        SQLiteDatabase database=mDpHelper.getWritableDatabase();
        // Insert the new pet with the given values
        long id =database.insert(PetContract.PetEntry.Table_Name, null, values);

        if (id==-1){
            Log.e(LOG_TAG, "Failed to insert row for "+ uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Track the number of rows that were deleted
        int rowsDeleted;

        SQLiteDatabase database=mDpHelper.getWritableDatabase();
        final int match= sUriMatcher.match(uri);
        switch (match){
            case PETS:
//                // Delete all rows that match the selection and selection args
//                return database.delete(PetContract.PetEntry.Table_Name, selection, selectionArgs);

                rowsDeleted=database.delete(PetContract.PetEntry.Table_Name, selection, selectionArgs);
                break;
            case PETS_ID:
                // Delete a single row given by the ID in the URI
                selection= PetContract.PetEntry._ID+ "=?";
                selectionArgs = new  String[]{
                        String.valueOf(ContentUris.parseId(uri))};
//                return database.delete(PetContract.PetEntry.Table_Name, selection, selectionArgs);
                rowsDeleted=database.delete(PetContract.PetEntry.Table_Name, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for" + uri);
                }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match= sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatedPet(uri, contentValues, selection, selectionArgs);
            case PETS_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs= new String[]{
                        String.valueOf(ContentUris.parseId(uri))};
                return updatedPet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for "+ uri);
                }
        }
    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatedPet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs){
        if (contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)){
            String name= contentValues.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name==null){
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if(contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)){
            Integer gender=contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender==null || !PetContract.PetEntry.isValidGender(gender)){
                throw new IllegalArgumentException("Pet requires valid Gender");
            }
        }
// If there are no values to update, then don't try to update the database
    if (contentValues.size()==0){
        return 0;
    }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database=mDpHelper.getWritableDatabase();
//        // Returns the number of database rows affected by the update statement
//        return database.update(PetContract.PetEntry.Table_Name, contentValues, selection, selectionArgs);

        // Perform the update on the database and get the number of rows affected
        int rowUpdate =database.update(PetContract.PetEntry.Table_Name, contentValues, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowUpdate != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowUpdate;
    }

    }


