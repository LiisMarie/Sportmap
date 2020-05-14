package ee.taltech.likutt.iti0213_2019s_hw02.classes

class Settings(var minSpeed: Double,  // in km per secs
           var maxSpeed: Double,  // in km per secs
           var gpsUpdateFrequency: Long,  // in milliseconds
           var syncingInterval: Long) {  // in milliseconds  MAX = 60s

    override fun toString(): String {
        return "minSpeed: $minSpeed, maxSpeed: $maxSpeed, gpsUpdateFrequency: $gpsUpdateFrequency, " +
                "syncingInterval: $syncingInterval"
    }
}