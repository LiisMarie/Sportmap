package ee.taltech.likutt.iti0213_2019s_hw02

class TrackingLocation(var id: Int,
                       var latitude: Long,
                       var longitude: Long,
                       var recordedAt: String,
                       var sessionId: String,
                       var type: String,
                       var speed: Long) {

    override fun toString(): String {
        return "id: $id, latitude: $latitude, longitude: $longitude, recordedAt: $recordedAt, " +
                "sessionId: $sessionId, type: $type, speed: $speed"
    }
}