package ee.taltech.likutt.iti0213_2019s_hw02

class TrackingSession(var id: String,
                      var name: String,
                      var description: String,
                      var recordedAt: String,
                      var duration: Long,
                      var speed: String,
                      var distance: Float,
                      var minSpeed: Long,
                      var maxSpeed: Long) {

    override fun toString(): String {
        return "id: $id, name: $name, description: $description, recordedAt: $recordedAt, " +
                "duration: $duration, speed: $speed, distance: $distance, minSpeed: $minSpeed, " +
                "maxSpeed: $maxSpeed"
    }
}