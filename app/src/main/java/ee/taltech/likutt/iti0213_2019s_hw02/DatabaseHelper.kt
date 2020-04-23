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
                        "$SESSION_ID TEXT PRIMARY KEY, " +
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

    fun addSession(name: String, description: String?, recordedAt: String, duration: Long, speed: String, distance: Float, minSpeed: Int, maxSpeed: Int) : String {
        val db = writableDatabase
        var contentValues = ContentValues()

        val randomInteger = (1..3000).shuffled().first()
        val id = "$randomInteger---$recordedAt"
        contentValues.put(SESSION_ID, id)

        contentValues.put(SESSION_NAME, name)
        if (description != null) {
            contentValues.put(SESSION_DESCRIPTION, description)
        }
        contentValues.put(SESSION_RECORDED_AT, recordedAt)
        contentValues.put(SESSION_DURATION, duration)
        contentValues.put(SESSION_SPEED, speed)
        contentValues.put(SESSION_DISTANCE, distance)
        contentValues.put(SESSION_MIN_SPEED, minSpeed)
        contentValues.put(SESSION_MAX_SPEED, maxSpeed)

        db.insert(SESSION_TABLE_NAME, null, contentValues)
        db.close()

        return id
    }

    fun addLocation(latitude: Double, longitude: Double, sessionId: String, type: String, speed: Double?, recordedAt: String) {
        val db = writableDatabase
        var contentValues = ContentValues()
        contentValues.put(LOCATION_LATITUDE, latitude)
        contentValues.put(LOCATION_LONGITUDE, longitude)
        contentValues.put(LOCATION_SESSION_ID, sessionId)
        contentValues.put(LOCATION_TYPE, type)
        if (speed != null) {
            contentValues.put(LOCATION_SPEED, speed)
        }
        contentValues.put(LOCATION_RECORDED_AT, recordedAt)

        db.insert(LOCATION_TABLE_NAME, null, contentValues)
        db.close()
    }

    fun fetchSessions() : Cursor {
        val db = writableDatabase

        val columns = arrayOf(
                SESSION_ID,
                SESSION_NAME,
                SESSION_DESCRIPTION,
                SESSION_RECORDED_AT,
                SESSION_DURATION,
                SESSION_SPEED,
                SESSION_DISTANCE,
                SESSION_MIN_SPEED,
                SESSION_MAX_SPEED
        )
        val orderBy = "$SESSION_RECORDED_AT DESC"

        val cursor = db.query(
                SESSION_TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                orderBy
        )

        return cursor
    }

    fun fetchLocationsForGivenSession(sessionId: String) : Cursor {
        val db = writableDatabase

        val columns = arrayOf(
                LOCATION_ID,
                LOCATION_LATITUDE,
                LOCATION_LONGITUDE,
                LOCATION_RECORDED_AT,
                LOCATION_SESSION_ID,
                LOCATION_TYPE,
                LOCATION_SPEED
        )
        val orderBy = "$LOCATION_RECORDED_AT DESC"
        val where = "$LOCATION_SESSION_ID = $sessionId"

        val cursor = db.query(
                SESSION_TABLE_NAME,
                columns,
                where,
                null,
                null,
                null,
                orderBy
        )

        return cursor
    }

    fun getAllSessions(): List<TrackingSession>{
        val trackingSessions = ArrayList<TrackingSession>()
        val cursor = fetchSessions()
        while (cursor.moveToNext()){
            trackingSessions.add(
                    TrackingSession(
                            cursor.getString(cursor.getColumnIndex(SESSION_ID)),
                            cursor.getString(cursor.getColumnIndex(SESSION_NAME)),
                            cursor.getString(cursor.getColumnIndex(SESSION_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndex(SESSION_RECORDED_AT)),
                            cursor.getLong(cursor.getColumnIndex(SESSION_DURATION)),
                            cursor.getString(cursor.getColumnIndex(SESSION_SPEED)),
                            cursor.getFloat(cursor.getColumnIndex(SESSION_DISTANCE)),
                            cursor.getLong(cursor.getColumnIndex(SESSION_MIN_SPEED)),
                            cursor.getLong(cursor.getColumnIndex(SESSION_MAX_SPEED))
                            )
            )
        }
        cursor.close()
        return trackingSessions;
    }

    fun getLocationsForGivenSession(sessionId: String) : List<TrackingLocation>{
        val trackingLocations = ArrayList<TrackingLocation>()
        val cursor = fetchLocationsForGivenSession(sessionId)
        while (cursor.moveToNext()){
            trackingLocations.add(
                    TrackingLocation(
                            cursor.getInt(cursor.getColumnIndex(LOCATION_ID)),
                            cursor.getLong(cursor.getColumnIndex(LOCATION_LATITUDE)),
                            cursor.getLong(cursor.getColumnIndex(LOCATION_LONGITUDE)),
                            cursor.getString(cursor.getColumnIndex(LOCATION_RECORDED_AT)),
                            cursor.getString(cursor.getColumnIndex(LOCATION_SESSION_ID)),
                            cursor.getString(cursor.getColumnIndex(LOCATION_TYPE)),
                            cursor.getLong(cursor.getColumnIndex(LOCATION_SPEED))
                    )
            )
        }
        cursor.close()
        return trackingLocations;
    }

    fun updateSessionDurationSpeedDistance(id: String, duration: Long, speed: String, distance: Float) {
        val db = writableDatabase
        var contentValues = ContentValues()
        contentValues.put(SESSION_DURATION, duration)
        contentValues.put(SESSION_SPEED, speed)
        contentValues.put(SESSION_DISTANCE, distance)
        val where = "$SESSION_ID='$id'"
        db.update(SESSION_TABLE_NAME, contentValues, where, null)
        db.close()
    }

}