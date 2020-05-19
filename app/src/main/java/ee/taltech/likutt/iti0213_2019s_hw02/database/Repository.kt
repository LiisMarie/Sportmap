package ee.taltech.likutt.iti0213_2019s_hw02.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.os.Build
import ee.taltech.likutt.iti0213_2019s_hw02.classes.Settings
import ee.taltech.likutt.iti0213_2019s_hw02.classes.TrackingLocation
import ee.taltech.likutt.iti0213_2019s_hw02.classes.TrackingSession
import ee.taltech.likutt.iti0213_2019s_hw02.classes.User

class Repository(val context: Context) {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase

    // opens database
    fun open(): Repository {
        dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase

        return this
    }

    // closes database
    fun close(){
        dbHelper.close()
    }


    /* CREATE */

    // for adding a session
    fun addSession(name: String, description: String?, recordedAt: String, duration: Long, speed: String, distance: Float, minSpeed: Double, maxSpeed: Double, synced: Int, restSessionId: String) : Long {
        val contentValues = ContentValues()

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
        contentValues.put(DatabaseHelper.SESSION_SYNCED, synced)
        contentValues.put(DatabaseHelper.SESSION_REST_ID, restSessionId)

        return db.insert(DatabaseHelper.SESSION_TABLE_NAME, null, contentValues)
    }

    // for adding a location
    fun addLocation(location: Location, sessionId: Long, type: String, speed: Double?, recordedAt: String, synced: Int) : Long {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.LOCATION_LATITUDE, location.latitude)
        contentValues.put(DatabaseHelper.LOCATION_LONGITUDE, location.longitude)
        contentValues.put(DatabaseHelper.LOCATION_SESSION_ID, sessionId)
        contentValues.put(DatabaseHelper.LOCATION_TYPE, type)
        if (speed != null) {
            contentValues.put(DatabaseHelper.LOCATION_SPEED, speed)
        }
        contentValues.put(DatabaseHelper.LOCATION_RECORDED_AT, recordedAt)
        contentValues.put(DatabaseHelper.LOCATION_SYNCED, synced)
        contentValues.put(DatabaseHelper.LOCATION_ACCURACY, location.accuracy)
        contentValues.put(DatabaseHelper.LOCATION_ALTITUDE, location.altitude)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            contentValues.put(DatabaseHelper.LOCATION_VERTICAL_ACCURACY, location.verticalAccuracyMeters)
        }

        return db.insert(DatabaseHelper.LOCATION_TABLE_NAME, null, contentValues)
    }

    // for adding an user
    fun addUser(email: String, password: String, firstName: String, lastName: String) {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.ACCOUNT_ID, 0)
        contentValues.put(DatabaseHelper.ACCOUNT_EMAIL, email)
        contentValues.put(DatabaseHelper.ACCOUNT_PASSWORD, password)
        contentValues.put(DatabaseHelper.ACCOUNT_FIRST_NAME, firstName)
        contentValues.put(DatabaseHelper.ACCOUNT_LAST_NAME, lastName)
        db.insert(DatabaseHelper.ACCOUNT_TABLE_NAME, null, contentValues)
    }

    // for adding settings
    fun addSettings(minSpeed: Double, maxSpeed: Double, gpsUpdateFrequency: Long, syncingInterval: Long) {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.SETTINGS_ID, 0)
        contentValues.put(DatabaseHelper.SETTINGS_MIN_SPEED, minSpeed)
        contentValues.put(DatabaseHelper.SETTINGS_MAX_SPEED, maxSpeed)
        contentValues.put(DatabaseHelper.SETTINGS_GPS_UPDATE_FREQUENCY, gpsUpdateFrequency)
        contentValues.put(DatabaseHelper.SETTINGS_SYNCING_INTERVAL, syncingInterval)
        db.insert(DatabaseHelper.SETTINGS_TABLE_NAME, null, contentValues)
    }


    /* READ */

    // fetches all sessions
    private fun fetchSessions() : Cursor {
        val columns = arrayOf(
                DatabaseHelper.SESSION_ID,
                DatabaseHelper.SESSION_NAME,
                DatabaseHelper.SESSION_DESCRIPTION,
                DatabaseHelper.SESSION_RECORDED_AT,
                DatabaseHelper.SESSION_DURATION,
                DatabaseHelper.SESSION_SPEED,
                DatabaseHelper.SESSION_DISTANCE,
                DatabaseHelper.SESSION_MIN_SPEED,
                DatabaseHelper.SESSION_MAX_SPEED,
                DatabaseHelper.SESSION_SYNCED,
                DatabaseHelper.SESSION_REST_ID
        )
        val orderBy = "${DatabaseHelper.SESSION_RECORDED_AT} DESC"

        return db.query(
                DatabaseHelper.SESSION_TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                orderBy
        )
    }

    // fetches locations that are recorded during given session
    private fun fetchLocationsForGivenSession(sessionId: Long) : Cursor {
        val columns = arrayOf(
                DatabaseHelper.LOCATION_ID,
                DatabaseHelper.LOCATION_LATITUDE,
                DatabaseHelper.LOCATION_LONGITUDE,
                DatabaseHelper.LOCATION_RECORDED_AT,
                DatabaseHelper.LOCATION_SESSION_ID,
                DatabaseHelper.LOCATION_TYPE,
                DatabaseHelper.LOCATION_SPEED,
                DatabaseHelper.LOCATION_SYNCED,
                DatabaseHelper.LOCATION_ACCURACY,
                DatabaseHelper.LOCATION_ALTITUDE,
                DatabaseHelper.LOCATION_VERTICAL_ACCURACY
        )
        val orderBy = "${DatabaseHelper.LOCATION_ID} ASC"
        val where = "${DatabaseHelper.LOCATION_SESSION_ID} = $sessionId"

        return db.query(
                DatabaseHelper.LOCATION_TABLE_NAME,
                columns,
                where,
                null,
                null,
                null,
                orderBy
        )
    }

    // for getting all recorded sessions
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
                            cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SESSION_MIN_SPEED)),
                            cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SESSION_MAX_SPEED)),
                            cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SESSION_SYNCED)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_REST_ID))
                    )
            )
        }
        return trackingSessions
    }

    // for getting session by id
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
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SESSION_MIN_SPEED)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SESSION_MAX_SPEED)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SESSION_SYNCED)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.SESSION_REST_ID))
                )
            }

        }
        return null
    }

    // for getting locations for given session
    fun getLocationsForGivenSession(sessionId: Long) : List<TrackingLocation>{
        val trackingLocations = ArrayList<TrackingLocation>()
        val cursor = fetchLocationsForGivenSession(sessionId)
        while (cursor.moveToNext()){
            trackingLocations.add(
                    TrackingLocation(
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_ID)),
                            cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LOCATION_LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LOCATION_LONGITUDE)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOCATION_RECORDED_AT)),
                            cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOCATION_SESSION_ID)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOCATION_TYPE)),
                            cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LOCATION_SPEED)),
                            cursor.getInt(cursor.getColumnIndex(DatabaseHelper.LOCATION_SYNCED)),
                            cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.LOCATION_ACCURACY)),
                            cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LOCATION_ALTITUDE)),
                            cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.LOCATION_VERTICAL_ACCURACY))
                            )
            )
        }
        return trackingLocations
    }

    // fetching user
    private fun fetchUser() : Cursor {
        val columns = arrayOf(
                DatabaseHelper.ACCOUNT_ID,
                DatabaseHelper.ACCOUNT_EMAIL,
                DatabaseHelper.ACCOUNT_PASSWORD,
                DatabaseHelper.ACCOUNT_FIRST_NAME,
                DatabaseHelper.ACCOUNT_LAST_NAME
        )

        return db.query(
                DatabaseHelper.ACCOUNT_TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        )
    }

    // for getting user
    fun getUser() : User? {
        val cursor = fetchUser()
        while (cursor.moveToNext()){
            return User(
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_PASSWORD)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_FIRST_NAME)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_LAST_NAME))
            )
        }
        return null
    }

    // fetching settings
    private fun fetchSettings() : Cursor {
        val columns = arrayOf(
                DatabaseHelper.SETTINGS_ID,
                DatabaseHelper.SETTINGS_MIN_SPEED,
                DatabaseHelper.SETTINGS_MAX_SPEED,
                DatabaseHelper.SETTINGS_GPS_UPDATE_FREQUENCY,
                DatabaseHelper.SETTINGS_SYNCING_INTERVAL
        )

        return db.query(
                DatabaseHelper.SETTINGS_TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        )
    }

    // for getting settings
    fun getSettings() : Settings? {
        val cursor = fetchSettings()
        while (cursor.moveToNext()){
            return Settings(
                    cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SETTINGS_MIN_SPEED)),
                    cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SETTINGS_MAX_SPEED)),
                    cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SETTINGS_GPS_UPDATE_FREQUENCY)),
                    cursor.getLong(cursor.getColumnIndex(DatabaseHelper.SETTINGS_SYNCING_INTERVAL))
            )
        }
        return null
    }


    /* UPDATE */

    // updates sessions duration, speed and distance
    fun updateSessionDurationSpeedDistance(id: Long, duration: Long, speed: String, distance: Float) {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.SESSION_DURATION, duration)
        contentValues.put(DatabaseHelper.SESSION_SPEED, speed)
        contentValues.put(DatabaseHelper.SESSION_DISTANCE, distance)
        val where = "${DatabaseHelper.SESSION_ID}='$id'"
        db.update(DatabaseHelper.SESSION_TABLE_NAME, contentValues, where, null)
    }

    // updates sessions name and description
    fun updateSessionNameDescription(id: Long, name: String, description: String?) {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.SESSION_NAME, name)
        contentValues.put(DatabaseHelper.SESSION_DESCRIPTION, description)
        val where = "${DatabaseHelper.SESSION_ID}='$id'"
        db.update(DatabaseHelper.SESSION_TABLE_NAME, contentValues, where, null)
    }

    // updates sessions min and max speed
    fun updateSessionMinMaxSpeed(id: Long, minSpeed: Double, maxSpeed: Double) {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.SESSION_MIN_SPEED, minSpeed)
        contentValues.put(DatabaseHelper.SESSION_MAX_SPEED, maxSpeed)
        val where = "${DatabaseHelper.SESSION_ID}='$id'"
        db.update(DatabaseHelper.SESSION_TABLE_NAME, contentValues, where, null)
    }

    // updates session rest session id property
    fun updateSessionRestId(id: Long, restSessionId: String) {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.SESSION_REST_ID, restSessionId)
        val where = "${DatabaseHelper.SESSION_ID}='$id'"
        db.update(DatabaseHelper.SESSION_TABLE_NAME, contentValues, where, null)
    }

    // updates session synced property
    fun updateSessionSynced(id: Long, synced: Int) {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.SESSION_SYNCED, synced)
        val where = "${DatabaseHelper.SESSION_ID}='$id'"
        db.update(DatabaseHelper.SESSION_TABLE_NAME, contentValues, where, null)
    }

    // updates locations synced property
    fun updateLocationsSynced(id: Long, synced: Int) {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.LOCATION_SYNCED, synced)
        val where = "${DatabaseHelper.LOCATION_ID}='$id'"
        db.update(DatabaseHelper.LOCATION_TABLE_NAME, contentValues, where, null)
    }


    /* DELETE */

    // deletes session with given id and its locations
    fun deleteSessionWithItsLocations(id: Long) {
        val where = "${DatabaseHelper.SESSION_ID}='$id'"
        db.delete(DatabaseHelper.SESSION_TABLE_NAME, where, null)
        val where2 = "${DatabaseHelper.LOCATION_SESSION_ID}='$id'"
        db.delete(DatabaseHelper.LOCATION_TABLE_NAME, where2, null)

    }

    // deletes logged in user
    fun deleteUser() {
        // LOGGED IN USER ID IS ALWAYS 0
        val where = "${DatabaseHelper.ACCOUNT_ID}='0'"
        db.delete(DatabaseHelper.ACCOUNT_TABLE_NAME, where, null)
    }

    // deletes settings
    fun deleteSettings() {
        // ACTIVE SESSION SETTINGS ID IS ALWAYS 0
        val where = "${DatabaseHelper.SETTINGS_ID}='0'"
        db.delete(DatabaseHelper.SETTINGS_TABLE_NAME, where, null)
    }
}