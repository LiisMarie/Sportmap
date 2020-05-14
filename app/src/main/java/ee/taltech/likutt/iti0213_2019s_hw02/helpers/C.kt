package ee.taltech.likutt.iti0213_2019s_hw02.helpers

class C {
    companion object {
        val NOTIFICATION_CHANNEL = "default_channel"
        val NOTIFICATION_ACTION_WP = "ee.taltech.likutt.wp"
        val NOTIFICATION_ACTION_CP = "ee.taltech.likutt.cp"

        val UPDATE_SETTINGS = "ee.taltech.likutt.update_settings"
        val GPS_UPDATE_FRQUENCY = "ee.taltech.likutt.gps_update_frequency"
        val SYNCING_INTERVAL = "ee.taltech.likutt.syncing_interval"

        val LOCATION_UPDATE_ACTION = "ee.taltech.likutt.location_update"

        val LOCATION_UPDATE_ACTION_LATITUDE = "ee.taltech.likutt.location_update.latitude"
        val LOCATION_UPDATE_ACTION_LONGITUDE = "ee.taltech.likutt.location_update.longitude"
        val LOCATION_UPDATE_ACTION_SPEED = "ee.taltech.likutt.location_update.speed"

        val NOTIFICATION_ID = 4321
        val REQUEST_PERMISSIONS_REQUEST_CODE = 34;

        val STATISTICS_UPDATE_ACTION = "ee.taltech.likutt.statistics_update"

        val STATISTICS_UPDATE_OVERALL_TOTAL = "ee.taltech.likutt.statistics_update.overall_total"
        val STATISTICS_UPDATE_OVERALL_DURATION = "ee.taltech.likutt.statistics_update.overall_duration"
        val STATISTICS_UPDATE_OVERALL_TEMPO = "ee.taltech.likutt.statistics_update.overall_tempo"
        val STATISTICS_UPDATE_WP_TOTAL = "ee.taltech.likutt.statistics_update.wp_total"
        val STATISTICS_UPDATE_WP_DIRECT = "ee.taltech.likutt.statistics_update.wp_direct"
        val STATISTICS_UPDATE_WP_TEMPO = "ee.taltech.likutt..statistics.wp_tempo"
        val STATISTICS_UPDATE_CP_TOTAL = "ee.taltech.likutt.statistics_update.cp_total"
        val STATISTICS_UPDATE_CP_DIRECT = "ee.taltech.likutt.statistics_update.cp_direct"
        val STATISTICS_UPDATE_CP_TEMPO = "ee.taltech.likutt.statistics_update.cp_tempo"

        val CURRENT_SESSION_ID = "ee.taltech.likutt.current_session.id"

        val CURRENT_WP_LATITUDE = "ee.taltech.likutt.current_wp.latitude"
        val CURRENT_WP_LONGITUDE = "ee.taltech.likutt.current_wp.longitude"

        val NEW_CP_LATITUDE = "ee.taltech.likutt.new_cp.latitude"
        val NEW_CP_LONGITUDE = "ee.taltech.likutt.new_cp.longitude"

        val RESTORE_COMPASS_SET = "ee.taltech.likutt.restore.compass_set"
        val RESTORE_MAP_CENTERED_SET = "ee.taltech.likutt.restore.map_centered_set"
        val RESTORE_TRACKING_SET = "ee.taltech.likutt.restore.tracking_set"
        val RESTORE_MAP_DIRECTION = "ee.taltech.likutt.restore.map_direction"
        val RESTORE_LOCATION_SERVICE_ACTIVE = "ee.taltech.likutt.restore.location_service_active"

        val OLD_SESSION_ID = "ee.taltech.likutt.old_session_id"

        val FROM_WHERE_TO_SETTINGS = "ee.taltech.likutt.from_where_to_settings"

        const val LOCAL_LOCATION_TYPE_START = "start"
        const val LOCAL_LOCATION_TYPE_LOC = "00000000-0000-0000-0000-000000000001"
        const val LOCAL_LOCATION_TYPE_CP = "00000000-0000-0000-0000-000000000002"
        const val LOCAL_LOCATION_TYPE_WP = "00000000-0000-0000-0000-000000000003"

        const val REST_BASE_URL = "https://sportmap.akaver.com/api/v1/"

        const val REST_LOCATIONID_LOC = "00000000-0000-0000-0000-000000000001"
        const val REST_LOCATIONID_WP = "00000000-0000-0000-0000-000000000002"
        const val REST_LOCATIONID_CP = "00000000-0000-0000-0000-000000000003"

        const val SYNCING_SYNCED = 1
        const val SYNCING_NOT_SYNCED = 0
        const val SYNCING_NO_NEED_TO_SYNC = 2
    }
}