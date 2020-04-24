package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class Repository(val context: Context) {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase

    fun open(): Repository{
        dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase

        return this;
    }

    fun close(){
        dbHelper.close()
    }


    // create

    fun addSession(name: String, description: String?, recordedAt: String, duration: Long, speed: String, distance: Float, minSpeed: Int, maxSpeed: Int) : Long {
        var contentValues = ContentValues()

        contentValues.put(DatabaseHelper.SESSION_NAME, name)
        if (description != null) {
            contentValues.put(DatabaseHelper.SESSION_DESCRIPTION, description)
        }
        contentValues.put(DatabaseHelper.SESSION_RECORDED_AT, recordedAt)
        contentValues.put(DatabaseHelper.SESSION_DURATION, duration)
        contentValues.put(DatabaseHelper.SESSION_SPEED, speed)
        contentValues.put(DatabaseHelper.SESSION_DISTANCE, distance)
        contentValues.put(DatabaseHelper.SESSION_MIN_SPEED, minSpeed)
        contentValues.put(DatabaseHelper.SESSION_MAX_SPEED, maxSpeed)

        val id = db.insert(DatabaseHelper.SESSION_TABLE_NAME, null, contentValues)

        return id
    }

    fun addLocation(latitude: Double, longitude: Double, sessionId: Long, type: String, speed: Double?, recordedAt: String) {
        var contentValues = ContentValues()
        contentValues.put(DatabaseHelper.LOCATION_LATITUDE, latitude)
        contentValues.put(DatabaseHelper.LOCATION_LONGITUDE, longitude)
        contentValues.put(DatabaseHelper.LOCATION_SESSION_ID, sessionId)
        contentValues.put(DatabaseHelper.LOCATION_TYPE, type)
        if (speed != null) {
            contentValues.put(DatabaseHelper.LOCATION_SPEED, speed)
        }
        contentValues.put(DatabaseHelper.LOCATION_RECORDED_AT, recordedAt)

        db.insert(DatabaseHelper.LOCATION_TABLE_NAME, null, contentValues)
    }


    // read

    fun fetchSessions() : Cursor {
        val columns = arrayOf(
                DatabaseHelper.SESSION_ID,
                DatabaseHelper.SESSION_NAME,
                DatabaseHelper.SESSION_DESCRIPTION,
                DatabaseHelper.SESSION_RECORDED_AT,
                DatabaseHelper.SESSION_DURATION,
                DatabaseHelper.SESSION_SPEED,
                DatabaseHelper.SESSION_DISTANCE,
                DatabaseHelper.SESSION_MIN_SPEED,
                DatabaseHelper.SESSION_MAX_SPEED
        )
        val orderBy = "${DatabaseHelper.SESSION_RECORDED_AT} DESC"

        val cursor = db.query(
                DatabaseHelper.SESSION_TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                orderBy
        )

        return cursor
    }

    fun fetchLocationsForGivenSession(sessionId: Long) : Cursor {
        val columns = arrayOf(
                DatabaseHelper.LOCATION_ID,
                DatabaseHelper.LOCATION_LATITUDE,
                DatabaseHelper.LOCATION_LONGITUDE,
                DatabaseHelper.LOCATION_RECORDED_AT,
                DatabaseHelper.LOCATION_SESSION_ID,
                DatabaseHelper.LOCATION_TYPE,
                DatabaseHelper.LOCATION_SPEED
        )
        val orderBy = "${DatabaseHelper.LOCATION_RECORDED_AT} DESC"
        val where = "${DatabaseHelper.LOCATION_SESSION_ID} = $sessionId"

        val cursor = db.query(
                DatabaseHelper.LOCATION_TABLE_NAME,
                columns,
                where,
                null,
                null,
                null,
                orderBy
        )

        return cursor
    }

    fun getAllLocations(): List<TrackingLocation> {
        val trackingLocations = ArrayList<TrackingLocation>()
        val columns = arrayOf(
                DatabaseHelper.LOCATION_ID,
                DatabaseHelper.LOCATION_LATITUDE,
                DatabaseHelper.LOCATION_LONGITUDE,
                DatabaseHelper.LOCATION_RECORDED_AT,
                DatabaseHelper.LOCATION_SESSION_ID,
                DatabaseHelper.LOCATION_TYPE,
                DatabaseHelper.LOCATION_SPEED
        )
        val orderBy = "${DatabaseHelper.LOCATION_RECORDED_AT} DESC"
        val cursor = db.query(
                DatabaseHelper.LOCATION_TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                orderBy
        )
        while (cursor.moveToNext()){
            trackingLocations.add(
                    TrackingLocation(
                            cursor.getInt(cursor.getColumnIndex(DatabaseHelper.LOCATION_ID)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_LATITUDE)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_LONGITUDE)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOCATION_RECORDED_AT)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_SESSION_ID)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOCATION_TYPE)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_SPEED))
                    )
            )
        }
        return trackingLocations;
    }

    fun getAllSessions(): List<TrackingSession>{
        val trackingSessions = ArrayList<TrackingSession>()
        val cursor = fetchSessions()
        while (cursor.moveToNext()){
            trackingSessions.add(
                    TrackingSession(
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SESSION_ID)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_NAME)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_RECORDED_AT)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SESSION_DURATION)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_SPEED)),
                            cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.SESSION_DISTANCE)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SESSION_MIN_SPEED)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SESSION_MAX_SPEED))
                    )
            )
        }
        return trackingSessions;
    }

    fun getSessionById(id: Long) : TrackingSession? {
        val cursor = fetchSessions()
        while (cursor.moveToNext()){
            if (cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SESSION_ID)) == id) {
                return TrackingSession(
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SESSION_ID)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_NAME)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_RECORDED_AT)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SESSION_DURATION)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_SPEED)),
                        cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.SESSION_DISTANCE)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SESSION_MIN_SPEED)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SESSION_MAX_SPEED))
                )
            }

        }
        return null;
    }

    fun getLocationsForGivenSession(sessionId: Long) : List<TrackingLocation>{
        val trackingLocations = ArrayList<TrackingLocation>()
        val cursor = fetchLocationsForGivenSession(sessionId)
        while (cursor.moveToNext()){
            trackingLocations.add(
                    TrackingLocation(
                            cursor.getInt(cursor.getColumnIndex(DatabaseHelper.LOCATION_ID)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_LATITUDE)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_LONGITUDE)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOCATION_RECORDED_AT)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_SESSION_ID)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOCATION_TYPE)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_SPEED))
                    )
            )
        }
        return trackingLocations;
    }


    // update

    fun updateSessionDurationSpeedDistance(id: Long, duration: Long, speed: String, distance: Float) {
        var contentValues = ContentValues()
        contentValues.put(DatabaseHelper.SESSION_DURATION, duration)
        contentValues.put(DatabaseHelper.SESSION_SPEED, speed)
        contentValues.put(DatabaseHelper.SESSION_DISTANCE, distance)
        val where = "${DatabaseHelper.SESSION_ID}='$id'"
        db.update(DatabaseHelper.SESSION_TABLE_NAME, contentValues, where, null)
    }

    fun updateSessionNameDescription(id: Long, name: String, description: String?) {
        var contentValues = ContentValues()
        contentValues.put(DatabaseHelper.SESSION_NAME, name)
        contentValues.put(DatabaseHelper.SESSION_DESCRIPTION, description)
        val where = "${DatabaseHelper.SESSION_ID}='$id'"
        db.update(DatabaseHelper.SESSION_TABLE_NAME, contentValues, where, null)
    }


    // delete

    fun deleteSessionWithItsLocations(id: Long) {
        val where = "${DatabaseHelper.SESSION_ID}='$id'"
        db.delete(DatabaseHelper.SESSION_TABLE_NAME, where, null)
        val where2 = "${DatabaseHelper.LOCATION_SESSION_ID}='$id'"
        db.delete(DatabaseHelper.LOCATION_TABLE_NAME, where2, null)

    }
}