package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "app.db"
        const val DATABASE_VERSION = 1

        const val SESSION_TABLE_NAME = "SESSIONS"
        const val LOCATION_TABLE_NAME = "LOCATIONS"

        const val SESSION_ID = "_id"
        const val SESSION_NAME = "name"
        const val SESSION_DESCRIPTION = "description"
        const val SESSION_RECORDED_AT = "recordedAt"
        const val SESSION_DURATION = "duration"
        const val SESSION_SPEED = "speed"
        const val SESSION_DISTANCE = "distance"
        const val SESSION_MIN_SPEED = "minSpeed"
        const val SESSION_MAX_SPEED = "maxSpeed"

        const val LOCATION_ID = "_id"
        const val LOCATION_LATITUDE = "latitude"
        const val LOCATION_LONGITUDE = "longitude"
        const val LOCATION_RECORDED_AT = "recordedAt"
        const val LOCATION_SESSION_ID = "sessionId"
        const val LOCATION_TYPE = "type"  // LOC or CP
        const val LOCATION_SPEED = "speed"  // speed from previous location to current one

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
                        "$SESSION_MAX_SPEED REAL NOT NULL" +
                        ");"
        const val SQL_DELETE_TABLE_SESSION = "DROP TABLE IF EXISTS $SESSION_TABLE_NAME"

        const val SQL_LOCATION_CREATE_TABLE =
                "create table if not exists $LOCATION_TABLE_NAME(" +
                        "$LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$LOCATION_LATITUDE INTEGER NOT NULL, " +
                        "$LOCATION_LONGITUDE INTEGER NOT NULL, " +
                        "$LOCATION_RECORDED_AT TEXT NOT NULL, " +
                        "$LOCATION_SESSION_ID TEXT NOT NULL, " +
                        "$LOCATION_TYPE TEXT NOT NULL, " +
                        "$LOCATION_SPEED REAL" +
                        ");"
        const val SQL_DELETE_TABLE_LOCATION = "DROP TABLE IF EXISTS $LOCATION_TABLE_NAME"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_SESSION_CREATE_TABLE)
        db?.execSQL(SQL_LOCATION_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_TABLE_SESSION)
        db?.execSQL(SQL_DELETE_TABLE_LOCATION)
        onCreate(db)
    }

}