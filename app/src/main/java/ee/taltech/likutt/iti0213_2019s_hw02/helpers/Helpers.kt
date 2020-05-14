package ee.taltech.likutt.iti0213_2019s_hw02.helpers

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.PolylineOptions
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Helpers {

    companion object {

        private var mapPolylineOptions: PolylineOptions? = null

        fun clearMapPolylineOptions(){
            mapPolylineOptions = PolylineOptions()
        }

        // generates colors map according to provided min and max speed
        fun generateColorsForSpeeds(minSpeed: Double, maxSpeed: Double): Map<List<Double>, Int> {
            val speedColorMap = mutableMapOf<List<Double>, Int>()
            val speedDifference : Double = (maxSpeed - minSpeed)
            val diff : Double = speedDifference / 255.toDouble()
            var speedToChange : Double = minSpeed
            var i = 0

            while (speedToChange < maxSpeed) {
                val speedList = mutableListOf<Double>()
                speedList.add(speedToChange)

                var green : Int
                var red : Int
                if (speedToChange <= (minSpeed + speedDifference / 2)) {
                    red = ((speedToChange * 2) * 255.0).toInt()
                    green = 255
                } else {
                    red = 255
                    green = (255.0 + 255.0 - ((speedToChange  * 2)* 255)).toInt()
                }

                speedToChange += diff
                speedList.add(speedToChange)

                speedColorMap[speedList] = Color.rgb(red, green, 0)

                i += 1
            }

            return speedColorMap
        }

        // gets color for given speed
        fun getColorForSpeed(colorMap: Map<List<Double>, Int>, speed: Double, minSpeed: Double, maxSpeed: Double) : Int{
            if (speed < minSpeed) {
                return Color.rgb(0, 255, 0)
            } else if (speed > maxSpeed) {
                return Color.rgb(255, 0, 0)
            }
            for ((k, v) in colorMap) {
                if (k[0] <= speed && k[1] > speed) {
                    return v
                }
            }
            return Color.rgb(255, 0, 0)
        }

        // returns time string from input time as millis
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

        // returns pace as a string
        fun getPaceAsString(millis: Long, distance: Float): String {
            val speed = millis / 60.0 / distance
            if (speed > 99) return "--:--"
            val minutes = (speed).toInt()
            val seconds = ((speed - minutes) * 60).toInt()

            return minutes.toString() + ":" + (if (seconds < 10) "0" else "") + seconds.toString()
        }

        // returns speed from time and distance
        fun getSpeed(millis: Long, distance: Float): Double {
            return millis / 60.0 / distance
        }

        // returns marker icon from drawable (used for drawing checkpoints and waypoints)
        fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
            val canvas = Canvas()
            val bitmap: Bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
            )
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        // for validating user input speed
        fun validateSpeedInput(minSpeed: Double, maxSpeed: Double) : Boolean {
            if (minSpeed > 0 && maxSpeed > minSpeed) {
                return true
            }
            return false
        }

    }

}
