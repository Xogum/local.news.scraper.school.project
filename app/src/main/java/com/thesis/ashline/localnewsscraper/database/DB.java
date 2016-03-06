package com.thesis.ashline.localnewsscraper.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.thesis.ashline.localnewsscraper.R;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


public class DB extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.thesis.ashline.localnewsscraper/databases/";
    public static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "geodata.db";
    private static String TABLE_CITY = "uk_towns_cities_postcodes";

    private final Context context;
    private SQLiteDatabase db;


    // constructor
    public DB(Context context) {

        super( context , DB_NAME , null , DATABASE_VERSION);
        this.context = context;

    }


    // Creates a empty database on the system and rewrites it with your own database.
    public void create() throws IOException{

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.db = this.getReadableDatabase();

//            try {

//                copyDataBase();
//                importCSVData();
            readRawTextAndInsert();
//            } catch (IOException e) {

//                throw new Error("Error importing csv");

//            }
        }

    }

    // Check if the database exist to avoid re-copy the data
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{


            String path = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            // database don't exist yet.
            e.printStackTrace();

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    // copy your assets db to the new system DB
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    private void importCSVData() {
        String [] dbCols= {"_id", "city", "county", "country"};
        InputStream inStream;
        try {

            inStream = context.getResources().openRawResource(R.raw.uk_towns_cities_postcodes);

            int size = inStream.available();

            byte[] buffer = new byte[size];

            inStream.read(buffer);


        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));
        String line = "";
        db.beginTransaction();
        try {
            line = buffer.readLine();
            while ((line = buffer.readLine()) != null) {
                String[] colums = line.split(",");
                if (colums.length != 4) {
                    Log.d("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }
                ContentValues cv = new ContentValues(5);
                cv.put(dbCols[0], colums[0].trim());
                cv.put(dbCols[1], colums[1].trim());
                cv.put(dbCols[2], colums[2].trim());
                cv.put(dbCols[3], colums[3].trim());
                cv.put(dbCols[4], colums[4].trim());
                db.insert(TABLE_CITY, null, cv);


            }
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // This method read and writes csv data in one step.
    private boolean readRawTextAndInsert() {
        String [] dbCols= {"_id", "city", "county", "country"};
        InputStream inputStream = context.getResources().openRawResource(R.raw.uk_towns_cities_postcodes);
        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader bufferedreader = new BufferedReader(inputreader);
        String line;
//        StringTokenizer st = null;
//        StringBuilder stringBuilder = new StringBuilder();

        db.beginTransaction();

        try {

            line = bufferedreader.readLine();
            while ((line = bufferedreader.readLine()) != null) {
                String[] colums = line.split(",");
                if (colums.length != 4) {
                    Log.d("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }
                ContentValues cv = new ContentValues(4);
                cv.put(dbCols[0], colums[0].trim());
                cv.put(dbCols[1], colums[1].trim());
                cv.put(dbCols[2], colums[2].trim());
                cv.put(dbCols[3], colums[3].trim());
                db.insert(TABLE_CITY, null, cv);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        return true;
    }

    //Open the database
    public boolean open() {

        try {
            String myPath = DB_PATH + DB_NAME;
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            return true;

        } catch(SQLException sqle) {
            db = null;
            return false;
        }

    }

    @Override
    public synchronized void close() {

        if(db != null)
            db.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CITY + " (_id INTEGER PRIMARY KEY, city TEXT, county TEXT, country TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    //
    // PUBLIC METHODS TO ACCESS DB CONTENT
    //


    // Get cities
    public List<City> getCities() {

        List<City> cities = null;

        try {
            String myPath = DB_PATH + DB_NAME;

            String query  = "SELECT * FROM " + TABLE_CITY;
            SQLiteDatabase db = SQLiteDatabase.openDatabase( myPath , null, SQLiteDatabase.OPEN_READONLY);
            Cursor cursor = db.rawQuery(query, null);

            // go over each row, build elements and add it to list
            cities = new LinkedList<City>();

            if (cursor.moveToFirst()) {
                do {

                    City city  = new City();
                    city.id      = Integer.parseInt(cursor.getString(0));
                    city.name    = cursor.getString(1);
                    city.county    = cursor.getString(2);
                    city.country    = cursor.getString(3);

                    cities.add(city);

                } while (cursor.moveToNext());
            }
        } catch(Exception e) {
            // sql error
        }

        return cities;
    }

    public Cursor getCityCursor(String args) {
        String sqlQuery = "";
        Cursor result = null;

        sqlQuery  = " SELECT _id" + ", name ";
        sqlQuery += " FROM " + TABLE_CITY;
        sqlQuery += " WHERE name LIKE '%" + args + "%' ";
        sqlQuery += " ORDER BY name";

        if (db == null)
        {
            open();
        }

        if (db!=null)
        {
            result = db.rawQuery(sqlQuery, null);
        }
        return result;
    }
}