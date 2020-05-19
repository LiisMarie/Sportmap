package ee.taltech.likutt.iti0213_2019s_hw02.classes

class TrackingLocation(var id: Long,
                       var latitude: Double,
                       var longitude: Double,
                       var recordedAt: String,
                       var sessionId: Long,
                       var type: String,
                       var speed: Double?,
                       var synced: Int,
                       var accuracy: Float,
                       var altitude: Double,
                       var verticalAccuracy: Float?
) {

    override fun toString(): String {
        return "id: $id, latitude: $latitude, longitude: $longitude, recordedAt: $recordedAt, " +
                "sessionId: $sessionId, type: $type, speed: $speed, synced: $synced"
    }
}