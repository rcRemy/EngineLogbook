/*******************************************************************************
 * Copyright (c) 2017 Remy Hasler (Hasler Electronic Engineering).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last change: 26.09.17
 ******************************************************************************/
// TODO: Change the package to match your project.
package ch.hasler.enginelogbook;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// TO USE:
// Change the package (at top) to match your project.
// Search for "TODO", and make the appropriate changes.
public class DBAdapter {

    // ///////////////////////////////////////////////////////////////////
    // Constants & Data
    // ///////////////////////////////////////////////////////////////////
    // Parts ID's (corresponds with position of <string-array name="engine_parts">)
    public static final int PLUG = 0;
    public static final int CONROD = 1;
    public static final int FRONT_BEARING = 2;
    public static final int REAR_BEARING = 3;
    public static final int PISTON = 4;
    public static final int SLEEVE = 5;
    public static final int UNDER_HEAD = 6;
    public static final int CRANKSHAFT = 7;
    public static final int PISTON_ROD = 8;
    public static final int PISTON_CLIPS = 9;
    public static final int CARBURETOR = 10;
    public static final int END_PLATE = 11;

    // Common DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;
    /*
     * CHANGE 1:
     */
    // TODO: Setup your fields here:
    // ENGINE Table - column names
    public static final String KEY_NAME = "name";
    public static final String KEY_MAKE = "make";
    public static final String KEY_FUEL = "fuel"; // integer
    public static final String KEY_DATE = "date";// integer
    public static final String KEY_STATUS = "status"; // integer
    public static final String KEY_TANKSIZE = "tanksize"; // integer
    public static final String KEY_CONSUMPTION = "consumption"; // integer
    public static final String KEY_COMPRESSION = "compression";
    public static final String KEY_PLUG = "plug";
    public static final String KEY_SPRING = "spring";
    public static final String KEY_TENSION = "tension";
    public static final String KEY_AXIALPLAY = "axialplay";
    public static final String KEY_CLEARANCE = "clearance";
    public static final String KEY_SHOE = "shoe";
    public static final String KEY_CLUTCH = "clutch";
    public static final String KEY_LIMITS = "limits"; // integer
    public static final String KEY_NOTES = "notes";
    public static final String KEY_ENG_PSC_ID = "psc_id"; // long
    public static final String KEY_ENG_SHAFT_ID = "shaft_id"; // long
    public static final String KEY_ENG_CARBURETOR_ID = "carburetor_id"; // long
    public static final String KEY_ENG_PLATE_ID = "plate_id"; // long
    public static final String KEY_ENG_HEADER_ID = "header_id"; // long

    // EVENT Table - column names
    public static final String KEY_EVENT_DATE = "date"; // long
    public static final String KEY_ENGINE_ID = "engine_id"; // long
    public static final String KEY_ACTION_ID = "action_id"; // integer
    public static final String KEY_EVENT_FUEL = "fuel"; // integer
    public static final String KEY_EVENT_PLACE = "place";
    public static final String KEY_EVENT_PART = "part";
    public static final String KEY_EVENT_NOTES = "notes";
    public static final String KEY_EVENT_PSC_ID = "psc_id"; // long
    public static final String KEY_EVENT_SHAFT_ID = "shaft_id"; // long
    public static final String KEY_EVENT_CARBURETOR_ID = "carburetor_id"; // long
    public static final String KEY_EVENT_PLATE_ID = "plate_id"; // long
    public static final String KEY_EVENT_HEADER_ID = "header_id"; // long

    // PSC SET Table - column names
    public static final String KEY_PSC_NAME = "name";
    public static final String KEY_PSC_MAKE = "make";
    public static final String KEY_PSC_FUEL = "fuel";// integer
    public static final String KEY_PSC_DATE = "date";// integer
    public static final String KEY_PSC_STATUS = "status"; // integer
    public static final String KEY_PSC_LIMITS = "limits"; // integer
    public static final String KEY_PSC_HEADER = "header"; // boolean
    public static final String KEY_PSC_NOTES = "notes";

     // COMPONENT Table - column names
    public static final String KEY_COMPONENT_NAME = "name";
    public static final String KEY_COMPONENT_MAKE = "make";
    public static final String KEY_COMPONENT_FUEL = "fuel";// integer
    public static final String KEY_COMPONENT_DATE = "date";// integer
    public static final String KEY_COMPONENT_NOTES = "notes";
    public static final String KEY_COMPONENT_CODE = "code";


    // TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
    // ENGINE Table - column numbers
    public static final int COL_NAME = 1;
    public static final int COL_MAKE = 2;
    public static final int COL_FUEL = 3;
    public static final int COL_DATE = 4;
    public static final int COL_STATUS = 5;
    public static final int COL_TANKSIZE = 6;
    public static final int COL_CONSUMPTION = 7;
    public static final int COL_COMPRESSION = 8;
    public static final int COL_PLUG = 9;
    public static final int COL_SPRING = 10;
    public static final int COL_TENSION = 11;
    public static final int COL_AXIALPLAY = 12;
    public static final int COL_CLEARANCE = 13;
    public static final int COL_SHOE = 14;
    public static final int COL_CLUTCH = 15;
    public static final int COL_LIMITS = 16;
    public static final int COL_NOTES = 17;
    public static final int COL_ENG_PSC_ID = 18;
    public static final int COL_ENG_SHAFT_ID = 19;
    public static final int COL_ENG_CARBURETOR_ID = 20;
    public static final int COL_ENG_PLATE_ID = 21;
    public static final int COL_ENG_HEADER_ID = 22;

    // EVENT Table - column numbers
    public static final int COL_EVENT_DATE = 1;
    public static final int COL_ENGINE_ID = 2;
    public static final int COL_ACTION_ID = 3;
    public static final int COL_EVENT_FUEL = 4;
    public static final int COL_EVENT_PLACE = 5;
    public static final int COL_EVENT_PART = 6;
    public static final int COL_EVENT_NOTES = 7;
    public static final int COL_EVENT_PSC_ID = 8;
    public static final int COL_EVENT_SHAFT_ID = 9;
    public static final int COL_EVENT_CARBURETOR_ID = 10;
    public static final int COL_EVENT_PLATE_ID = 11;
    public static final int COL_EVENT_HEADER_ID = 12;

    // PSC SET Table - column numbers
    public static final int COL_PSC_NAME = 1;
    public static final int COL_PSC_MAKE = 2;
    public static final int COL_PSC_FUEL = 3;
    public static final int COL_PSC_DATE = 4;
    public static final int COL_PSC_STATUS = 5;
    public static final int COL_PSC_LIMITS = 6;
    public static final int COL_PSC_HEADER = 7;
    public static final int COL_PSC_NOTES = 8;

    // TEST
    // COMPONENT Table - column numbers
    public static final int COL_COMPONENT_NAME = 1;
    public static final int COL_COMPONENT_MAKE = 2;
    public static final int COL_COMPONENT_FUEL = 3;
    public static final int COL_COMPONENT_DATE = 4;
    public static final int COL_COMPONENT_NOTES = 5;
    public static final int COL_COMPONENT_CODE = 6;


    public static final String[] ALL_KEYS = new String[]{KEY_ROWID, KEY_NAME,
            KEY_MAKE, KEY_FUEL, KEY_DATE, KEY_STATUS, KEY_TANKSIZE,
            KEY_CONSUMPTION, KEY_COMPRESSION, KEY_PLUG, KEY_SPRING,
            KEY_TENSION, KEY_AXIALPLAY, KEY_CLEARANCE, KEY_SHOE, KEY_CLUTCH,
            KEY_LIMITS, KEY_NOTES, KEY_ENG_PSC_ID, KEY_ENG_SHAFT_ID, KEY_ENG_CARBURETOR_ID,
            KEY_ENG_PLATE_ID, KEY_ENG_HEADER_ID};

    public static final String[] ALL_EVENT_KEYS = new String[]{KEY_ROWID,
            KEY_EVENT_DATE, KEY_ENGINE_ID, KEY_ACTION_ID, KEY_EVENT_FUEL,
            KEY_EVENT_PLACE, KEY_EVENT_PART, KEY_EVENT_NOTES, KEY_EVENT_PSC_ID,
            KEY_EVENT_SHAFT_ID, KEY_EVENT_CARBURETOR_ID, KEY_EVENT_PLATE_ID, KEY_EVENT_HEADER_ID};

    public static final String[] ALL_PSC_KEYS = new String[]{KEY_ROWID,
            KEY_PSC_NAME, KEY_PSC_MAKE, KEY_PSC_FUEL, KEY_PSC_DATE,
            KEY_PSC_STATUS, KEY_PSC_LIMITS, KEY_PSC_HEADER, KEY_PSC_NOTES};

    public static final String[] ALL_COMPONENT_KEYS = new String[]{KEY_ROWID,
            KEY_COMPONENT_NAME, KEY_COMPONENT_MAKE, KEY_COMPONENT_FUEL, KEY_COMPONENT_DATE, KEY_COMPONENT_NOTES, KEY_COMPONENT_CODE};


    // DB info: it's name, and the tables we are using.
    public static final String DATABASE_NAME = "EngineLogbook.db";
    public static final String DATABASE_ENGINE_TABLE = "engineTable";
    public static final String DATABASE_EVENT_TABLE = "eventTable";
    public static final String DATABASE_PSC_TABLE = "pscTable";
    public static final String DATABASE_COMPONENT_TABLE = "componentTable";


    // Track DB version if a new version of your application changes the format.
    public static final int DATABASE_VERSION = 27;

    private static final String DATABASE_ENGINE_CREATE_SQL = "create table "
            + DATABASE_ENGINE_TABLE
            + " ("
            + KEY_ROWID
            + " integer primary key autoincrement, "

            /*
             * CHANGE 2:
             */
            // TODO: Place your fields here!
            // + KEY_{...} + " {type} not null"
            // - Key is the column name you created above.
            // - {type} is one of: text, integer, real, blob
            // (http://www.sqlite.org/datatype3.html)
            // - "not null" means it is a required field (must be given a
            // value).
            // NOTE: All must be comma separated (end of line!) Last one must
            // have NO comma!!
            + KEY_NAME + " text not null, " + KEY_MAKE + " text not null, "
            + KEY_FUEL + " integer not null, " + KEY_DATE + " integer, "
            + KEY_STATUS + " integer not null, " + KEY_TANKSIZE
            + " integer not null, " + KEY_CONSUMPTION + " integer not null, "
            + KEY_COMPRESSION + " text not null, " + KEY_PLUG
            + " text not null, " + KEY_SPRING + " text not null, "
            + KEY_TENSION + " text not null, " + KEY_AXIALPLAY
            + " text not null, " + KEY_CLEARANCE + " text not null, "
            + KEY_SHOE + " text not null, " + KEY_CLUTCH + " text not null, "
            + KEY_LIMITS + " integer not null, " + KEY_NOTES
            + " text not null, " + KEY_ENG_PSC_ID + " integer, "
            + KEY_ENG_SHAFT_ID + " integer," + KEY_ENG_CARBURETOR_ID + " integer, "
            + KEY_ENG_PLATE_ID + " integer, " + KEY_ENG_HEADER_ID + " integer"
            // Rest of creation:
            + ");";

    private static final String DATABASE_EVENT_CREATE_SQL = "create table "
            + DATABASE_EVENT_TABLE + " (" + KEY_ROWID
            + " integer primary key autoincrement, " + KEY_EVENT_DATE
            + " integer, " + KEY_ENGINE_ID + " integer not null, "
            + KEY_ACTION_ID + " integer not null, " + KEY_EVENT_FUEL
            + " integer not null, " + KEY_EVENT_PLACE + " text not null, "
            + KEY_EVENT_PART + " text not null, " + KEY_EVENT_NOTES
            + " text not null, " + KEY_EVENT_PSC_ID + " integer, "
            + KEY_EVENT_SHAFT_ID + " integer," + KEY_EVENT_CARBURETOR_ID + " integer, "
            + KEY_EVENT_PLATE_ID + " integer, " + KEY_EVENT_HEADER_ID + " integer"
            + ");";

    private static final String DATABASE_PSC_CREATE_SQL = "create table "
            + DATABASE_PSC_TABLE + " (" + KEY_ROWID
            + " integer primary key autoincrement, " + KEY_PSC_NAME
            + " text not null, " + KEY_PSC_MAKE + " text not null, "
            + KEY_PSC_FUEL + " integer, " + KEY_PSC_DATE + " integer, "
            + KEY_STATUS + " integer not null, " + KEY_PSC_LIMITS
            + " integer not null, " + KEY_PSC_HEADER + " boolean not null, "
            + KEY_PSC_NOTES + " text not null" + ");";

    private static final String DATABASE_COMPONENT_CREATE_SQL = "create table "
            + DATABASE_COMPONENT_TABLE + " (" + KEY_ROWID
            + " integer primary key autoincrement, " + KEY_COMPONENT_NAME
            + " text not null, " + KEY_COMPONENT_MAKE + " text not null, "
            + KEY_COMPONENT_FUEL + " integer, " + KEY_COMPONENT_DATE + " integer, "
            + KEY_COMPONENT_NOTES + " text not null," +  KEY_COMPONENT_CODE + " integer"
            + ");";

/*    private static final String DATABASE_SHAFT_CREATE_SQL = "create table "
            + DATABASE_SHAFT_TABLE + " (" + KEY_ROWID
            + " integer primary key autoincrement, " + KEY_SHAFT_NAME
            + " text not null, " + KEY_SHAFT_MAKE + " text not null, "
            + KEY_SHAFT_FUEL + " integer, " + KEY_SHAFT_DATE + " integer, "
            + KEY_SHAFT_NOTES + " text not null" + ");";

    private static final String DATABASE_CARBURETOR_CREATE_SQL = "create table "
            + DATABASE_CARBURETOR_TABLE + " (" + KEY_ROWID
            + " integer primary key autoincrement, " + KEY_CARBURETOR_NAME
            + " text not null, " + KEY_CARBURETOR_MAKE + " text not null, "
            + KEY_CARBURETOR_FUEL + " integer, " + KEY_CARBURETOR_DATE + " integer, "
            + KEY_CARBURETOR_NOTES + " text not null" + ");";

    private static final String DATABASE_PLATE_CREATE_SQL = "create table "
            + DATABASE_PLATE_TABLE + " (" + KEY_ROWID
            + " integer primary key autoincrement, " + KEY_PLATE_NAME
            + " text not null, " + KEY_PLATE_MAKE + " text not null, "
            + KEY_PLATE_FUEL + " integer, " + KEY_PLATE_DATE + " integer, "
            + KEY_PLATE_NOTES + " text not null" + ");";

    private static final String DATABASE_HEADER_CREATE_SQL = "create table "
            + DATABASE_HEADER_TABLE + " (" + KEY_ROWID
            + " integer primary key autoincrement, " + KEY_HEADER_NAME
            + " text not null, " + KEY_HEADER_MAKE + " text not null, "
            + KEY_HEADER_FUEL + " integer, " + KEY_HEADER_DATE + " integer, "
            + KEY_HEADER_NOTES + " text not null" + ");";*/

    // Context of application who uses us.
    private final Context context;

    private static DatabaseHelper mInstance;
    private SQLiteDatabase db;

    // ///////////////////////////////////////////////////////////////////
    // Public methods:
    // ///////////////////////////////////////////////////////////////////

    public DBAdapter(Context ctx) {
        this.context = ctx;
        // mInstance = new DatabaseHelper(context);
        mInstance = getInstance(context);
    }

    private static DatabaseHelper getInstance(Context ctx) {
        /**
         * use the application context as suggested by CommonsWare. this will
         * ensure that you don't accidentally leak an Activities context (see
         * this article for more information:
         * http://android-developers.blogspot.
         * nl/2009/01/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    // Open the database connection.
    public DBAdapter open() {
        db = mInstance.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        mInstance.close();
    }

    // ///////////////////////////////////////////////////////////////////
    // ENGINE Table methods:
    // ///////////////////////////////////////////////////////////////////
    // Adds a new set of engine values to the database.
    public long insertEngine(String name, String make, int fuel, long date,
                             int status, int tanksize, int consumption, String compression,
                             String plug, String spring, String tension, String play,
                             String clearance, String shoe, String clutch, int limits,
                             String notes, long pscId, long shaftId, long carbId, long plateId, long headerId) {
        /*
         * CHANGE 3:
         */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_MAKE, make);
        initialValues.put(KEY_FUEL, fuel);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_STATUS, status);
        initialValues.put(KEY_TANKSIZE, tanksize);
        initialValues.put(KEY_CONSUMPTION, consumption);
        initialValues.put(KEY_COMPRESSION, compression);
        initialValues.put(KEY_PLUG, plug);
        initialValues.put(KEY_SPRING, spring);
        initialValues.put(KEY_TENSION, tension);
        initialValues.put(KEY_AXIALPLAY, play);
        initialValues.put(KEY_CLEARANCE, clearance);
        initialValues.put(KEY_SHOE, shoe);
        initialValues.put(KEY_CLUTCH, clutch);
        initialValues.put(KEY_LIMITS, limits);
        initialValues.put(KEY_NOTES, notes);
        initialValues.put(KEY_ENG_PSC_ID, pscId);
        initialValues.put(KEY_ENG_SHAFT_ID, shaftId);
        initialValues.put(KEY_ENG_CARBURETOR_ID, carbId);
        initialValues.put(KEY_ENG_PLATE_ID, plateId);
        initialValues.put(KEY_ENG_HEADER_ID, headerId);

        // Insert it into the database.
        return db.insert(DATABASE_ENGINE_TABLE, null, initialValues);
    }

    // Deletes a engine from the database and all corresponding events, by
    // engineId (primary key)
    public boolean deleteEngine(long engId) {
        setEventEngineIdToDeleted(engId); // delete also the according events
        String where = KEY_ROWID + "=" + engId;
        return db.delete(DATABASE_ENGINE_TABLE, where, null) != 0;
    }

    // Deletes all database data (engines and events)
    public void deleteAll() {
        Cursor c = getAllEngines(KEY_MAKE);
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteEngine(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Returns all data in the database.
    public Cursor getAllEngines(String orderKey) {
        String where = null;
        String order = orderKey + " ASC";
        Cursor c = db.query(true, DATABASE_ENGINE_TABLE, ALL_KEYS, where, null,
                null, null, order, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Gets a specific engine (by engineId)
    public Cursor getEngine(long engineId) {
        String where = KEY_ROWID + "=" + engineId;
        Cursor c = db.query(true, DATABASE_ENGINE_TABLE, ALL_KEYS, where, null,
                null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Changes an existing engine to new data.
    public boolean updateEngine(long engineId, String name, String make,
                                int fuel, long date, int status, int tanksize, int consumption,
                                String compression, String plug, String spring, String tension,
                                String play, String clearance, String shoe, String clutch,
                                int limits, String notes, long pscId) {
        String where = KEY_ROWID + "=" + engineId;

        /*
         * CHANGE 4:
         */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_NAME, name);
        newValues.put(KEY_MAKE, make);
        newValues.put(KEY_FUEL, fuel);
        newValues.put(KEY_DATE, date);
        newValues.put(KEY_STATUS, status);
        newValues.put(KEY_TANKSIZE, tanksize);
        newValues.put(KEY_CONSUMPTION, consumption);
        newValues.put(KEY_COMPRESSION, compression);
        newValues.put(KEY_PLUG, plug);
        newValues.put(KEY_SPRING, spring);
        newValues.put(KEY_TENSION, tension);
        newValues.put(KEY_AXIALPLAY, play);
        newValues.put(KEY_CLEARANCE, clearance);
        newValues.put(KEY_SHOE, shoe);
        newValues.put(KEY_CLUTCH, clutch);
        newValues.put(KEY_LIMITS, limits);
        newValues.put(KEY_NOTES, notes);
        newValues.put(KEY_ENG_PSC_ID, pscId);

        // Insert it into the database.
        return db.update(DATABASE_ENGINE_TABLE, newValues, where, null) != 0;
    }

    // Updates fuel data for a specific engine
    public boolean updateFuel(long engineId, int fuel) {
        String where = KEY_ROWID + "=" + engineId;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_FUEL, fuel);

        // Insert it into the database.
        return db.update(DATABASE_ENGINE_TABLE, newValues, where, null) != 0;
    }

    // Gets all makes which were entered
    public ArrayList<String> getAllMakes() {
        ArrayList<String> makes = new ArrayList<String>();

        Cursor c = db.rawQuery("SELECT DISTINCT(" + KEY_MAKE + ") FROM "
                + DATABASE_ENGINE_TABLE + " UNION SELECT DISTINCT("
                + KEY_PSC_MAKE + ") FROM " + DATABASE_PSC_TABLE, null);

        if (c != null && c.moveToFirst()) {
            do {
                makes.add(c.getString(0));
            } while (c.moveToNext());
        }

        return makes;
    }

    // Gets all different strings which were entered by KEY (for autofill)
    public ArrayList<String> getEngineArrayNameByKey(String key) {
        ArrayList<String> strings = new ArrayList<String>();

        Cursor c = db.rawQuery("SELECT DISTINCT(" + key + ") FROM "
                + DATABASE_ENGINE_TABLE, null);

        if (c != null && c.moveToFirst()) {
            do {
                strings.add(c.getString(0));
            } while (c.moveToNext());
        }
        return strings;
    }

    // Updates the psc set id from specific engine
    public boolean updateEnginePscId(long engineId, long pscId) {
        String where = KEY_ROWID + "=" + engineId;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_ENG_PSC_ID, pscId);

        // Insert it into the database.
        return db.update(DATABASE_ENGINE_TABLE, newValues, where, null) != 0;
    }

    // ///////////////////////////////////////////////////////////////////
    // EVENT Table methods:
    // ///////////////////////////////////////////////////////////////////
    // Adds a new set of values to the database.
    public long insertEvent(long date, long engineId, long actionId, int fuel,
                            String place, int part, String notes, long pscId) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_EVENT_DATE, date);
        initialValues.put(KEY_ENGINE_ID, engineId);
        initialValues.put(KEY_ACTION_ID, actionId);
        initialValues.put(KEY_EVENT_FUEL, fuel);
        initialValues.put(KEY_EVENT_PLACE, place);
        initialValues.put(KEY_EVENT_PART, part);
        initialValues.put(KEY_EVENT_NOTES, notes);
        initialValues.put(KEY_EVENT_PSC_ID, pscId);

        // Insert it into the database.
        return db.insert(DATABASE_EVENT_TABLE, null, initialValues);
    }

    // Deletes a row from the database, by eventId (primary key)
    public boolean deleteEvent(long eventId) {
        String where = KEY_ROWID + "=" + eventId;
        return db.delete(DATABASE_EVENT_TABLE, where, null) != 0;
    }

    // Deletes all event rows where no engine and psc exists
    public boolean cleanupEvents() {
        String where = KEY_ENGINE_ID + "=" + Integer.MAX_VALUE + " AND "
                + KEY_EVENT_PSC_ID + "=" + Integer.MAX_VALUE;
        return db.delete(DATABASE_EVENT_TABLE, where, null) != 0;
    }

    // Deletes all event rows from a specific engine (when engine is deleted)
    public boolean deleteEventAllFromEngine(long engineId) {
        String where = KEY_ENGINE_ID + "=" + engineId;
        return db.delete(DATABASE_EVENT_TABLE, where, null) != 0;
    }

    // Returns all event data in the database.
    public Cursor getEventAll() {
        String where = null;
        Cursor c = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Gets a specific event (by eventId)
    public Cursor getEvent(long eventId) {
        String where = KEY_ROWID + "=" + eventId;
        Cursor c = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Gets all events from engine (by engineId)
    public Cursor getEventEngine(long engineId) {
        String where = KEY_ENGINE_ID + "=" + engineId;
        String order = KEY_EVENT_DATE + " DESC, " + KEY_ROWID + " DESC";
        Cursor c = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Gets all events from engine and built in psc (by engineId)
    public Cursor getEventEnginePsc(long engineId) {
        Cursor cEng = getEngine(engineId);
        long pscId = cEng.getLong(COL_ENG_PSC_ID);
        String where = KEY_ENGINE_ID + "=" + engineId + " OR "
                + KEY_EVENT_PSC_ID + "=" + pscId;
        String order = KEY_EVENT_DATE + " DESC, " + KEY_ROWID + " DESC";
        Cursor c = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Changes an existing event to new data.
    public boolean updateEvent(long eventId, long date, long engineId,
                               long actionId, int fuel, String place, int part, String notes,
                               long pscId) {
        String where = KEY_ROWID + "=" + eventId;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_EVENT_DATE, date);
        newValues.put(KEY_ENGINE_ID, engineId);
        newValues.put(KEY_ACTION_ID, actionId);
        newValues.put(KEY_EVENT_FUEL, fuel);
        newValues.put(KEY_EVENT_PLACE, place);
        newValues.put(KEY_EVENT_PART, part);
        newValues.put(KEY_EVENT_NOTES, notes);
        newValues.put(KEY_EVENT_PSC_ID, pscId);

        // Insert it into the database.
        return db.update(DATABASE_EVENT_TABLE, newValues, where, null) != 0;
    }

    // Gets a specific fuel summation (by engineId)
    public int getEventSumFuel(long engineId) {
        int fueltotal = 0;
        Cursor c = db.rawQuery("SELECT SUM(" + KEY_EVENT_FUEL + ") FROM "
                + DATABASE_EVENT_TABLE + " where " + KEY_ENGINE_ID + "="
                + engineId, null);
        if (c.moveToFirst()) {
            fueltotal = c.getInt(0);
        }
        c.close();

        return fueltotal;
    }

    // Gets all places which were entered
    public ArrayList<String> getEventPlaces() {
        ArrayList<String> places = new ArrayList<String>();

        Cursor c = db.rawQuery("SELECT DISTINCT(" + KEY_EVENT_PLACE + ") FROM "
                + DATABASE_EVENT_TABLE, null);

        if (c != null && c.moveToFirst()) {
            do {
                places.add(c.getString(0));
            } while (c.moveToNext());
        }
        return places;
    }

    // Gets a time of last engine part change (by engineId and partId)
    public long getEventLastPartchange(long engineId, int partId) {
        // get date of last engine part change
        long unixTime = 0;
        String where = KEY_ENGINE_ID + "=" + engineId + " AND "
                + KEY_EVENT_PART + "=" + partId + " AND (" + KEY_ACTION_ID
                + "=1 OR " + KEY_ACTION_ID + "=2)";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        if (cd != null && cd.moveToFirst()) {
            unixTime = cd.getLong(COL_EVENT_DATE);
        }
        cd.close();
        return unixTime;
    }

    // Gets a time of last engine psc exchange (by engineId)
    public long getEventLastPscExchange(long engineId) {
        // get date of last engine part change
        long unixTime = 0;
        String where = KEY_ENGINE_ID + "=" + engineId + " AND ("
                + KEY_ACTION_ID + "=4)";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        if (cd != null && cd.moveToFirst()) {
            unixTime = cd.getLong(COL_EVENT_DATE);
        }
        cd.close();
        return unixTime;
    }

    // Gets psc exchange date before given time
    public long getEventExchangeBefore(long engineId, long eventTime) {
        long time = 0;
        String where = KEY_ENGINE_ID + "=" + engineId + " AND (" + KEY_ACTION_ID + "=4 AND " + KEY_EVENT_DATE + "<" + eventTime + ")";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where, null, null, null, order, null);

        if (cd != null && cd.moveToFirst()) {
            time = cd.getLong(COL_EVENT_DATE);
        }
        cd.close();

        return time;
    }

    // Gets psc exchange date before given time
    public long getEventExchangeAfter(long engineId, long eventTime) {
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();

        String where = KEY_ENGINE_ID + "=" + engineId + " AND (" + KEY_ACTION_ID + "=4 AND " + KEY_EVENT_DATE + ">" + eventTime + ")";
        String order = KEY_EVENT_DATE + " ASC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where, null, null, null, order, null);

        if (cd != null && cd.moveToFirst()) {
            time = cd.getLong(COL_EVENT_DATE);
        }
        cd.close();

        return time;
    }

    // Gets a time list of engine part changes (by engineId and partId)
    public ArrayList<Long> getEventPartchanges(long engineId, int partId) {
        // get date of last engine part change
        ArrayList<Long> changeTimes = new ArrayList<Long>();
        String where = KEY_ENGINE_ID + "=" + engineId + " AND "
                + KEY_EVENT_PART + "=" + partId + " AND (" + KEY_ACTION_ID
                + "=1 OR " + KEY_ACTION_ID + "=2)";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        if (cd != null && cd.moveToFirst()) {
            do {
                changeTimes.add(cd.getLong(COL_EVENT_DATE));
            } while (cd.moveToNext());
        }
        cd.close();
        return changeTimes;
    }

    // Gets a count of engine part change (by engineId and partId)
    public int getEventCountPartchange(long engineId, int partId) {
        // get rows of engine part changes
        int changes = 0;
        String where = KEY_ENGINE_ID + "=" + engineId + " AND "
                + KEY_EVENT_PART + "=" + partId + " AND (" + KEY_ACTION_ID
                + "=1 OR " + KEY_ACTION_ID + "=2)";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        changes = cd.getCount() + 1;
        cd.close();
        return changes;
    }

    // Gets engine part fuel amount (by engineId and partId)
    public int getEventPartFuel(long engineId, int partId) {
        // get date of last engine part change
        long unixTime = 0;
        String where = KEY_ENGINE_ID + "=" + engineId + " AND "
                + KEY_EVENT_PART + "=" + partId + " AND (" + KEY_ACTION_ID
                + "=1 OR " + KEY_ACTION_ID + "=2)";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        if (cd != null && cd.moveToFirst()) {
            unixTime = cd.getLong(COL_EVENT_DATE);
        }
        cd.close();

        // get total engine part fuel since last change
        return getEngineFuelSince(engineId, unixTime);
    }

    // Gets an array list with all part data (by engineId and partId)
    public ArrayList<PartsData> getPartsDataList(Context context,
                                                 long engineId, int partId) {
        ArrayList<PartsData> Data = new ArrayList<PartsData>();
        int deltaFuel = 0;

        // add the changed parts to the list
        String where = KEY_ENGINE_ID + "=" + engineId + " AND "
                + KEY_EVENT_PART + "=" + partId + " AND (" + KEY_ACTION_ID
                + "=1 OR " + KEY_ACTION_ID + "=2)";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        int partnbr = cd.getCount() + 1;
        if (cd != null && cd.moveToFirst()) {
            do {
                PartsData d = new PartsData();
                d.set_partid(partId);
                d.set_partnbr(partnbr);
                d.set_date(cd.getLong(COL_EVENT_DATE));
                d.set_fuel(getEngineFuelSince(engineId,
                        cd.getLong(COL_EVENT_DATE))
                        - deltaFuel);
                d.set_notes(cd.getString(COL_EVENT_NOTES));
                Data.add(d);
                deltaFuel = getEngineFuelSince(engineId,
                        cd.getLong(COL_EVENT_DATE));
                partnbr--;
            } while (cd.moveToNext());
        }
        cd.close();
        // add the first part to the list (original part since purchase)
        Cursor cursor = getEngine(engineId);
        long saleDate = 0;
        int initialFuel = 0;
        if (cursor.moveToFirst()) {
            saleDate = cursor.getLong(DBAdapter.COL_DATE);
            initialFuel = cursor.getInt(DBAdapter.COL_FUEL);
        }
        PartsData d = new PartsData();
        d.set_partid(partId);
        d.set_partnbr(partnbr);
        d.set_date(saleDate);
        d.set_fuel(getEngineFuelSince(engineId, saleDate) - deltaFuel
                + initialFuel);
        d.set_notes(context.getString(R.string.xls_original_part));
        Data.add(d);

        return Data;
    }

    // Gets fuel amount of engine since date (time)
    private int getEngineFuelSince(long engineId, long time) {
        int fueltotal = 0;
        // get total engine part fuel since last change
        Cursor c = db.rawQuery("SELECT SUM(" + KEY_EVENT_FUEL + ") FROM "
                + DATABASE_EVENT_TABLE + " where " + KEY_ENGINE_ID + "="
                + engineId + " AND " + KEY_EVENT_DATE + ">" + time, null);
        if (c != null && c.moveToFirst()) {
            fueltotal = c.getInt(0);
        }
        c.close();

        return fueltotal;
    }

    // Gets psc part fuel amount (by pscId and partId)
    public int getEventPscPartFuel(long pscId, int partId) {
        // get date of last engine part change
        long unixTime = 0;
        String where = KEY_EVENT_PSC_ID + "=" + pscId + " AND "
                + KEY_EVENT_PART + "=" + partId + " AND (" + KEY_ACTION_ID
                + "=1 OR " + KEY_ACTION_ID + "=2)";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        if (cd != null && cd.moveToFirst()) {
            unixTime = cd.getLong(COL_EVENT_DATE);
        }
        cd.close();

        // get total engine part fuel since last change
        return getPscFuelSince(pscId, unixTime);
    }

    // Gets fuel amount of psc since date (time)
    private int getPscFuelSince(long pscId, long time) {
        int fueltotal = 0;
        // get total engine part fuel since last change
        Cursor c = db.rawQuery("SELECT SUM(" + KEY_EVENT_FUEL + ") FROM "
                + DATABASE_EVENT_TABLE + " where " + KEY_EVENT_PSC_ID + "="
                + pscId + " AND " + KEY_EVENT_DATE + ">" + time, null);
        if (c != null && c.moveToFirst()) {
            fueltotal = c.getInt(0);
        }
        c.close();

        return fueltotal;
    }

    // Gets a time of last psc part change (by pscId and partId)
    public long getEventPscLastPartchange(long pscId, int partId) {
        // get date of last engine part change
        long unixTime = 0;
        String where = KEY_EVENT_PSC_ID + "=" + pscId + " AND "
                + KEY_EVENT_PART + "=" + partId + " AND (" + KEY_ACTION_ID
                + "=1 OR " + KEY_ACTION_ID + "=2)";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        if (cd != null && cd.moveToFirst()) {
            unixTime = cd.getLong(COL_EVENT_DATE);
        }
        cd.close();
        return unixTime;
    }

    // Gets all events from psc (by pscId)
    public Cursor getEventPsc(long pscId) {
        String where = KEY_EVENT_PSC_ID + "=" + pscId + " AND " + KEY_ACTION_ID
                + "!=4";
        String order = KEY_EVENT_DATE + " DESC, " + KEY_ROWID + " DESC";
        Cursor c = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Gets an array list with all part data (by pscId and partId)
    public ArrayList<PartsData> getPscPartsDataList(Context context,
                                                    long pscId, int partId) {
        ArrayList<PartsData> Data = new ArrayList<PartsData>();
        int deltaFuel = 0;

        // add the changed parts to the list
        String where = KEY_EVENT_PSC_ID + "=" + pscId + " AND "
                + KEY_EVENT_PART + "=" + partId + " AND (" + KEY_ACTION_ID
                + "=1 OR " + KEY_ACTION_ID + "=2)";
        String order = KEY_EVENT_DATE + " DESC";
        Cursor cd = db.query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS, where,
                null, null, null, order, null);
        int partnbr = cd.getCount() + 1;
        if (cd != null && cd.moveToFirst()) {
            do {
                PartsData d = new PartsData();
                d.set_partid(partId);
                d.set_partnbr(partnbr);
                d.set_date(cd.getLong(COL_EVENT_DATE));
                d.set_fuel(getPscFuelSince(pscId, cd.getLong(COL_EVENT_DATE))
                        - deltaFuel);
                d.set_notes(cd.getString(COL_EVENT_NOTES));
                Data.add(d);
                deltaFuel = getPscFuelSince(pscId, cd.getLong(COL_EVENT_DATE));
                partnbr--;
            } while (cd.moveToNext());
        }
        cd.close();
        // add the first part to the list (original part since purchase)
        Cursor cursor = getPsc(pscId);
        long saleDate = 0;
        int initialFuel = 0;
        if (cursor.moveToFirst()) {
            saleDate = cursor.getLong(DBAdapter.COL_PSC_DATE);
            initialFuel = cursor.getInt(DBAdapter.COL_PSC_FUEL);
        }
        PartsData d = new PartsData();
        d.set_partid(partId);
        d.set_partnbr(partnbr);
        d.set_date(saleDate);
        d.set_fuel(getPscFuelSince(pscId, saleDate) - deltaFuel + initialFuel);
        d.set_notes(context.getString(R.string.xls_original_part));
        Data.add(d);

        return Data;
    }

    // Updates the psc set id to deleted
    public boolean setEventPscIdToDeleted(long pscId) {
        String where = KEY_EVENT_PSC_ID + "=" + pscId;
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_EVENT_PSC_ID, Integer.MAX_VALUE);

        return db.update(DATABASE_EVENT_TABLE, newValues, where, null) != 0;
    }

    // Updates the engine id to deleted
    public boolean setEventEngineIdToDeleted(long engId) {
        String where = KEY_ENGINE_ID + "=" + engId;
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_ENGINE_ID, Integer.MAX_VALUE);

        return db.update(DATABASE_EVENT_TABLE, newValues, where, null) != 0;
    }

    // ///////////////////////////////////////////////////////////////////
    // PSC Table methods:
    // ///////////////////////////////////////////////////////////////////
    // Adds a new set of values to the database.
    public long insertPsc(String name, String make, int fuel, long date,
                          int status, Boolean header, int limits, String notes) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PSC_NAME, name);
        initialValues.put(KEY_PSC_MAKE, make);
        initialValues.put(KEY_PSC_FUEL, fuel);
        initialValues.put(KEY_PSC_DATE, date);
        initialValues.put(KEY_PSC_STATUS, status);
        initialValues.put(KEY_PSC_HEADER, header);
        initialValues.put(KEY_PSC_LIMITS, limits);
        initialValues.put(KEY_PSC_NOTES, notes);

        // Insert it into the database.
        return db.insert(DATABASE_PSC_TABLE, null, initialValues);
    }

    // Changes an existing psc to new data.
    public boolean updatePsc(long pccId, String name, String make, int fuel,
                             long date, int status, Boolean header, int limits, String notes) {
        String where = KEY_ROWID + "=" + pccId;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_PSC_NAME, name);
        newValues.put(KEY_PSC_MAKE, make);
        newValues.put(KEY_PSC_FUEL, fuel);
        newValues.put(KEY_PSC_DATE, date);
        newValues.put(KEY_PSC_STATUS, status);
        newValues.put(KEY_PSC_HEADER, header);
        newValues.put(KEY_PSC_LIMITS, limits);
        newValues.put(KEY_PSC_NOTES, notes);

        // Insert it into the database.
        return db.update(DATABASE_PSC_TABLE, newValues, where, null) != 0;
    }

    // Gets a specific psc (by pscId)
    public Cursor getPsc(long pscId) {
        String where = KEY_ROWID + "=" + pscId;
        Cursor c = db.query(true, DATABASE_PSC_TABLE, ALL_PSC_KEYS, where,
                null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Deletes a psc from the database by pscId (primary key)
    public boolean deletePsc(long pscId) {
        setEventPscIdToDeleted(pscId);
        String where = KEY_ROWID + "=" + pscId;
        return db.delete(DATABASE_PSC_TABLE, where, null) != 0;
    }

    // Returns all psc in the database.
    public Cursor getAllPsc(String orderKey) {
        String where = null;
        String order = orderKey + " ASC";
        Cursor c = db.query(true, DATABASE_PSC_TABLE, ALL_PSC_KEYS, where,
                null, null, null, order, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Updates limit data for a specific psc
    public boolean updatePscLimit(long pscId, int limit) {
        String where = KEY_ROWID + "=" + pscId;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_PSC_LIMITS, limit);

        // Insert it into the database.
        return db.update(DATABASE_PSC_TABLE, newValues, where, null) != 0;
    }

    // Updates status data for a specific psc
    public boolean updatePscStatus(long pscId, int status) {
        String where = KEY_ROWID + "=" + pscId;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_PSC_STATUS, status);

        // Insert it into the database.
        return db.update(DATABASE_PSC_TABLE, newValues, where, null) != 0;
    }

    // Returns engine id where psc is built in.
    public long getWherePscBuiltin(long pscId) {
        long engId = Integer.MAX_VALUE;
        String where = KEY_ENG_PSC_ID + "=" + pscId;
        Cursor c = db.query(true, DATABASE_ENGINE_TABLE, ALL_KEYS, where, null,
                null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                engId = c.getLong(DBAdapter.COL_ROWID);
            } while (c.moveToNext());
        }
        return engId;
    }

    // Updates the psc set id from specific event
    public boolean updateEventPscId(long eventId, long pscId) {
        String where = KEY_ROWID + "=" + eventId;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_EVENT_PSC_ID, pscId);

        // Insert it into the database.
        return db.update(DATABASE_EVENT_TABLE, newValues, where, null) != 0;
    }


    // ///////////////////////////////////////////////////////////////////
    // SHAFT Table methods:
    // ///////////////////////////////////////////////////////////////////
    // Adds a new set of values to the database.
/*    public long insertShaft(String name, String make, int fuel, long date, String notes) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SHAFT_NAME, name);
        initialValues.put(KEY_SHAFT_MAKE, make);
        initialValues.put(KEY_SHAFT_FUEL, fuel);
        initialValues.put(KEY_SHAFT_DATE, date);
        initialValues.put(KEY_SHAFT_NOTES, notes);

        // Insert it into the database.
        return db.insert(DATABASE_SHAFT_TABLE, null, initialValues);
    }*/

    // Gets a specific shaft (by pscId)
    public Cursor getComponent(long componentId) {
        String where = KEY_ROWID + "=" + componentId;
        Cursor c = db.query(true, DATABASE_COMPONENT_TABLE, ALL_COMPONENT_KEYS, where,
                null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // ///////////////////////////////////////////////////////////////////
    // CARBURETOR Table methods:
    // ///////////////////////////////////////////////////////////////////
    // Adds a new set of values to the database.
    public long insertComponent(String name, String make, int fuel, long date, String notes, int code) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_COMPONENT_NAME, name);
        initialValues.put(KEY_COMPONENT_MAKE, make);
        initialValues.put(KEY_COMPONENT_FUEL, fuel);
        initialValues.put(KEY_COMPONENT_DATE, date);
        initialValues.put(KEY_COMPONENT_NOTES, notes);
        initialValues.put(KEY_COMPONENT_CODE, code);

        // Insert it into the database.
        return db.insert(DATABASE_COMPONENT_TABLE, null, initialValues);
    }

/*    // ///////////////////////////////////////////////////////////////////
    // CARBURETOR Table methods:
    // ///////////////////////////////////////////////////////////////////
    // Adds a new set of values to the database.
    public long insertCarburetor(String name, String make, int fuel, long date, String notes) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CARBURETOR_NAME, name);
        initialValues.put(KEY_CARBURETOR_MAKE, make);
        initialValues.put(KEY_CARBURETOR_FUEL, fuel);
        initialValues.put(KEY_CARBURETOR_DATE, date);
        initialValues.put(KEY_CARBURETOR_NOTES, notes);

        // Insert it into the database.
        return db.insert(DATABASE_CARBURETOR_TABLE, null, initialValues);
    }

    // ///////////////////////////////////////////////////////////////////
    // PLATE Table methods:
    // ///////////////////////////////////////////////////////////////////
    // Adds a new set of values to the database.
    public long insertPlate(String name, String make, int fuel, long date, String notes) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PLATE_NAME, name);
        initialValues.put(KEY_PLATE_MAKE, make);
        initialValues.put(KEY_PLATE_FUEL, fuel);
        initialValues.put(KEY_PLATE_DATE, date);
        initialValues.put(KEY_PLATE_NOTES, notes);

        // Insert it into the database.
        return db.insert(DATABASE_PLATE_TABLE, null, initialValues);
    }

    // ///////////////////////////////////////////////////////////////////
    // HEADER Table methods:
    // ///////////////////////////////////////////////////////////////////
    // Adds a new set of values to the database.
    public long insertHeader(String name, String make, int fuel, long date, String notes) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_HEADER_NAME, name);
        initialValues.put(KEY_HEADER_MAKE, make);
        initialValues.put(KEY_HEADER_FUEL, fuel);
        initialValues.put(KEY_HEADER_DATE, date);
        initialValues.put(KEY_HEADER_NOTES, notes);

        // Insert it into the database.
        return db.insert(DATABASE_HEADER_TABLE, null, initialValues);
    }*/


    // Updates the aux set id from specific engine
    public boolean updateEngineAuxId(long engineId, long shaftId, long carbId, long plateId, long headerId) {
        String where = KEY_ROWID + "=" + engineId;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_ENG_SHAFT_ID, shaftId);
        newValues.put(KEY_ENG_CARBURETOR_ID, carbId);
        newValues.put(KEY_ENG_PLATE_ID, plateId);
        newValues.put(KEY_ENG_HEADER_ID, headerId);

        // Insert it into the database.
        return db.update(DATABASE_ENGINE_TABLE, newValues, where, null) != 0;
    }

    // Updates the aux set id from specific event
    public boolean updateEventAuxId(long eventId, long shaftId, long carbId, long plateId, long headerId) {
        String where = KEY_ROWID + "=" + eventId;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_EVENT_SHAFT_ID, shaftId);
        newValues.put(KEY_EVENT_CARBURETOR_ID, carbId);
        newValues.put(KEY_EVENT_PLATE_ID, plateId);
        newValues.put(KEY_EVENT_HEADER_ID, headerId);

        // Insert it into the database.
        return db.update(DATABASE_EVENT_TABLE, newValues, where, null) != 0;
    }


    // ///////////////////////////////////////////////////////////////////
    // Private Helper Classes:
    // ///////////////////////////////////////////////////////////////////

    /**
     * Private class which handles database creation and upgrading. Used to
     * handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_ENGINE_CREATE_SQL);
            _db.execSQL(DATABASE_EVENT_CREATE_SQL);
            _db.execSQL(DATABASE_PSC_CREATE_SQL);
            _db.execSQL(DATABASE_COMPONENT_CREATE_SQL);
/*            _db.execSQL(DATABASE_SHAFT_CREATE_SQL);
            _db.execSQL(DATABASE_CARBURETOR_CREATE_SQL);
            _db.execSQL(DATABASE_PLATE_CREATE_SQL);
            _db.execSQL(DATABASE_HEADER_CREATE_SQL);*/
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {

            if (oldVersion == 26) {
                _db.execSQL(DATABASE_COMPONENT_CREATE_SQL);
/*                _db.execSQL(DATABASE_CARBURETOR_CREATE_SQL);
                _db.execSQL(DATABASE_PLATE_CREATE_SQL);
                _db.execSQL(DATABASE_HEADER_CREATE_SQL);*/

                _db.execSQL("ALTER TABLE " + DATABASE_ENGINE_TABLE
                        + " ADD COLUMN " + KEY_ENG_SHAFT_ID + " integer");
                _db.execSQL("ALTER TABLE " + DATABASE_ENGINE_TABLE
                        + " ADD COLUMN " + KEY_ENG_CARBURETOR_ID + " integer");
                _db.execSQL("ALTER TABLE " + DATABASE_ENGINE_TABLE
                        + " ADD COLUMN " + KEY_ENG_PLATE_ID + " integer");
                _db.execSQL("ALTER TABLE " + DATABASE_ENGINE_TABLE
                        + " ADD COLUMN " + KEY_ENG_HEADER_ID + " integer");

                _db.execSQL("ALTER TABLE " + DATABASE_EVENT_TABLE
                        + " ADD COLUMN " + KEY_EVENT_SHAFT_ID + " integer");
                _db.execSQL("ALTER TABLE " + DATABASE_EVENT_TABLE
                        + " ADD COLUMN " + KEY_EVENT_CARBURETOR_ID + " integer");
                _db.execSQL("ALTER TABLE " + DATABASE_EVENT_TABLE
                        + " ADD COLUMN " + KEY_EVENT_PLATE_ID + " integer");
                _db.execSQL("ALTER TABLE " + DATABASE_EVENT_TABLE
                        + " ADD COLUMN " + KEY_EVENT_HEADER_ID + " integer");
            }
        }
    }

    public void updateTo26() {

        Cursor cPcc = db.query(true, DATABASE_PSC_TABLE, ALL_PSC_KEYS, null,
                null, null, null, null, null);
        if (cPcc.getCount() == 0) {

            Cursor cEng = db.query(true, DATABASE_ENGINE_TABLE, ALL_KEYS, null,
                    null, null, null, null, null);
            if (cEng != null && cEng.moveToFirst()) {
                do {
                    String name = cEng.getString(COL_NAME) + " #"
                            + String.valueOf(cEng.getLong(COL_ROWID));
                    String make = cEng.getString(COL_MAKE);
                    int fuel = cEng.getInt(COL_FUEL);
                    long date = cEng.getLong(COL_DATE);
                    int status = cEng.getInt(COL_STATUS);
                    boolean header = false;
                    int limits = cEng.getInt(COL_LIMITS);
                    String notes = "";

                    long pscId = insertPsc(name, make, fuel, date, status,
                            header, limits, notes);
                    updateEnginePscId(cEng.getLong(COL_ROWID), pscId);

                    String where = KEY_ENGINE_ID + "="
                            + cEng.getLong(COL_ROWID);
                    Cursor cEvent = db
                            .query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS,
                                    where, null, null, null, null, null);
                    if (cEvent != null && cEvent.moveToFirst()) {
                        do {
                            updateEventPscId(cEvent.getLong(COL_ROWID), pscId);
                        } while (cEvent.moveToNext());
                    }
                    cEvent.close();
                } while (cEng.moveToNext());
            }
            cEng.close();
        }
    }

    public void updateTo27() {

        Cursor cShaft = db.query(true, DATABASE_COMPONENT_TABLE, ALL_COMPONENT_KEYS, null,
                null, null, null, null, null);
        if (cShaft.getCount() == 0) {

            Cursor cEng = db.query(true, DATABASE_ENGINE_TABLE, ALL_KEYS, null,
                    null, null, null, null, null);
            if (cEng != null && cEng.moveToFirst()) {
                do {
                    String name = cEng.getString(COL_NAME) + " OEM ";
                    String make = cEng.getString(COL_MAKE);
                    int fuel = cEng.getInt(COL_FUEL);
                    long date = cEng.getLong(COL_DATE);
                    String notes = "";

                    long shaftId = insertComponent(name + "Shaft", make, fuel, date, notes, CRANKSHAFT);
                    long carburetorId = insertComponent(name + "Carburetor", make, fuel, date, notes, CARBURETOR);
                    long plateId = insertComponent(name + "Plate", make, fuel, date, notes, END_PLATE);
                    long headerId = insertComponent(name + "Header", make, fuel, date, notes, UNDER_HEAD);
                    updateEngineAuxId(cEng.getLong(COL_ROWID), shaftId, carburetorId, plateId, headerId);

                    String where = KEY_ENGINE_ID + "="
                            + cEng.getLong(COL_ROWID);
                    Cursor cEvent = db
                            .query(true, DATABASE_EVENT_TABLE, ALL_EVENT_KEYS,
                                    where, null, null, null, null, null);
                    if (cEvent != null && cEvent.moveToFirst()) {
                        do {
                            updateEventAuxId(cEvent.getLong(COL_ROWID), shaftId, carburetorId, plateId, headerId);
                        } while (cEvent.moveToNext());
                    }
                    cEvent.close();
                } while (cEng.moveToNext());
            }
            cEng.close();
        }
    }
}
