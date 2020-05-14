package ee.taltech.likutt.iti0213_2019s_hw02.classes

class TrackingSession(var id: Long,
                      var name: String,
                      var description: String,
                      var recordedAt: String,
                      var duration: Long,
                      var speed: String,
                      var distance: Float,
                      var minSpeed: Double,
                      var maxSpeed: Double,
                      var synced: Int
) {

    override fun toString(): String {
        return "id: $id, name: $name, description: $description, recordedAt: $recordedAt, " +
                "duration: $duration, speed: $speed, distance: $distance, minSpeed: $minSpeed, " +
                "maxSpeed: $maxSpeed synced: $synced"
    }
}