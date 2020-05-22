package ee.taltech.likutt.iti0213_2019s_hw02.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "sportapp.db"
        const val DATABASE_VERSION = 2

        // table names
        const val SESSION_TABLE_NAME = "SESSIONS"
        const val LOCATION_TABLE_NAME = "LOCATIONS"
        const val ACCOUNT_TABLE_NAME = "ACCOUNT"
        const val SETTINGS_TABLE_NAME = "SETTINGS"

        // columns for session table
        const val SESSION_ID = "_id"
        const val SESSION_NAME = "name"
        const val SESSION_DESCRIPTION = "description"
        const val SESSION_RECORDED_AT = "recordedAt"
        const val SESSION_DURATION = "duration"
        const val SESSION_SPEED = "speed"
        const val SESSION_DISTANCE = "distance"
        const val SESSION_MIN_SPEED = "minSpeed"
        const val SESSION_MAX_SPEED = "maxSpeed"
        const val SESSION_SYNCED = "synced"
        const val SESSION_REST_ID = "restId"

        // columns for location table
        const val LOCATION_ID = "_id"
        const val LOCATION_LATITUDE = "latitude"
        const val LOCATION_LONGITUDE = "longitude"
        const val LOCATION_RECORDED_AT = "recordedAt"
        const val LOCATION_SESSION_ID = "sessionId"
        const val LOCATION_TYPE = "type"  // LOC or CP
        const val LOCATION_SPEED = "speed"  // speed from previous location to current one
        const val LOCATION_SYNCED = "synced"
        const val LOCATION_ACCURACY = "accuracy"
        const val LOCATION_ALTITUDE = "altitude"
        const val LOCATION_VERTICAL_ACCURACY = "verticalAccuracy"



        // columns for account table
        const val ACCOUNT_ID = "_id"
        const val ACCOUNT_EMAIL = "email"
        const val ACCOUNT_PASSWORD = "password"
        const val ACCOUNT_FIRST_NAME = "firstName"
        const val ACCOUNT_LAST_NAME = "lastName"

        // columns for settings table
        const val SETTINGS_ID = "_id"
        const val SETTINGS_MIN_SPEED = "minSpeed"
        const val SETTINGS_MAX_SPEED = "maxSpeed"
        const val SETTINGS_GPS_UPDATE_FREQUENCY = "gpsUpdateFrequency"
        const val SETTINGS_SYNCING_INTERVAL = "syncingInterval"

        // sql sentences for session table
        const val SQL_SESSION_CREATE_TABLE =
                "create table if not exists $SESSION_TABLE_NAME(" +
                        "$SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$SESSION_NAME TEXT NOT NULL, " +
                        "$SESSION_DESCRIPTION TEXT, " +
                        "$SESSION_RECORDED_AT TEXT NOT NULL, " +
                        "$SESSION_DURATION INTEGER NOT NULL, " +
                        "$SESSION_SPEED TEXT NOT NULL, " +
                        "$SESSION_DISTANCE REAL NOT NULL, " +
                        "$SESSION_MIN_SPEED REAL NOT NULL, " +
                        "$SESSION_MAX_SPEED REAL NOT NULL, " +
                        "$SESSION_SYNCED INTEGER NOT NULL, " +
                        "$SESSION_REST_ID STRING" +
                        ");"
        const val SQL_DELETE_TABLE_SESSION = "DROP TABLE IF EXISTS $SESSION_TABLE_NAME"

        // sql sentences for location table
        const val SQL_LOCATION_CREATE_TABLE =
                "create table if not exists $LOCATION_TABLE_NAME(" +
                        "$LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$LOCATION_LATITUDE REAL NOT NULL, " +
                        "$LOCATION_LONGITUDE REAL NOT NULL, " +
                        "$LOCATION_RECORDED_AT TEXT NOT NULL, " +
                        "$LOCATION_SESSION_ID TEXT NOT NULL, " +
                        "$LOCATION_TYPE TEXT NOT NULL, " +
                        "$LOCATION_SPEED REAL, " +
                        "$LOCATION_SYNCED INTEGER NOT NULL, " +
                        "$LOCATION_ACCURACY REAL NOT NULL, " +
                        "$LOCATION_ALTITUDE REAL NOT NULL, " +
                        "$LOCATION_VERTICAL_ACCURACY REAL" +
                        ");"
        const val SQL_DELETE_TABLE_LOCATION = "DROP TABLE IF EXISTS $LOCATION_TABLE_NAME"

        // sql sentences for account table
        const val SQL_ACCOUNT_CREATE_TABLE =
                "create table if not exists $ACCOUNT_TABLE_NAME(" +
                        "$ACCOUNT_ID INTEGER PRIMARY KEY, " +
                        "$ACCOUNT_EMAIL TEXT NOT NULL, " +
                        "$ACCOUNT_PASSWORD TEXT NOT NULL, " +
                        "$ACCOUNT_FIRST_NAME TEXT NOT NULL, " +
                        "$ACCOUNT_LAST_NAME TEXT NOT NULL" +
                        ");"
        const val SQL_DELETE_TABLE_ACCOUNT = "DROP TABLE IF EXISTS $ACCOUNT_TABLE_NAME"

        // sql sentences for settings table
        const val SQL_SETTINGS_CREATE_TABLE =
                "create table if not exists $SETTINGS_TABLE_NAME(" +
                        "$SETTINGS_ID INTEGER PRIMARY KEY, " +
                        "$SETTINGS_MIN_SPEED REAL NOT NULL, " +
                        "$SETTINGS_MAX_SPEED REAL NOT NULL, " +
                        "$SETTINGS_GPS_UPDATE_FREQUENCY INTEGER NOT NULL, " +
                        "$SETTINGS_SYNCING_INTERVAL INTEGER NOT NULL" +
                        ");"
        const val SQL_DELETE_TABLE_SETTINGS = "DROP TABLE IF EXISTS $SETTINGS_TABLE_NAME"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_SESSION_CREATE_TABLE)
        db?.execSQL(SQL_LOCATION_CREATE_TABLE)
        db?.execSQL(SQL_ACCOUNT_CREATE_TABLE)
        db?.execSQL(SQL_SETTINGS_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_TABLE_SESSION)
        db?.execSQL(SQL_DELETE_TABLE_LOCATION)
        db?.execSQL(SQL_DELETE_TABLE_ACCOUNT)
        db?.execSQL(SQL_DELETE_TABLE_SETTINGS)
        onCreate(db)
    }

}