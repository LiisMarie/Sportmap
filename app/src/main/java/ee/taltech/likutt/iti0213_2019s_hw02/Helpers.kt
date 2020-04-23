package ee.taltech.likutt.iti0213_2019s_hw02

import java.util.concurrent.TimeUnit

class Helpers {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName

        fun getTimeString(millis: Long): String {
            return String.format(
                    "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(millis)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(millis)
                    )
            )
        }

        fun getPaceAsString(millis: Long, distance: Float): String {
            //Log.d(TAG, millis.toString() + '-' + distance.toString())
            val speed = millis / 60.0 / distance
            if (speed > 99) return "--:--"
            val minutes = (speed).toInt();
            val seconds = ((speed - minutes) * 60).toInt()

            return minutes.toString() + ":" + (if (seconds < 10) "0" else "") + seconds.toString();
        }

        fun getSpeed(millis: Long, distance: Float): Double {
            return millis / 60.0 / distance
        }

    }

}
