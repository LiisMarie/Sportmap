package ee.taltech.likutt.iti0213_2019s_hw02

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import java.util.concurrent.TimeUnit
import kotlin.math.min

class Helpers {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName

        private var mapPolylineOptions: PolylineOptions? = null

        @Synchronized
        fun getMapPolylineOptions(): PolylineOptions {
            if (mapPolylineOptions == null) {
                mapPolylineOptions = PolylineOptions()
            }
            return mapPolylineOptions!!
        }

        fun clearMapPolylineOptions(){
            mapPolylineOptions = PolylineOptions()
        }

        fun addToMapPolylineOptions(lat: Double, lon: Double, color: Int){
            Log.d("addToMapPolylineOptions", color.toString())
            getMapPolylineOptions().add(LatLng(lat, lon)).width(10f).color(color)
        }

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
            val minutes = (speed).toInt()
            val seconds = ((speed - minutes) * 60).toInt()

            return minutes.toString() + ":" + (if (seconds < 10) "0" else "") + seconds.toString()
        }

        fun getSpeed(millis: Long, distance: Float): Double {
            return millis / 60.0 / distance
        }

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

        fun validateSpeedInput(minSpeed: Double, maxSpeed: Double) : Boolean {
            if (minSpeed > 0 && maxSpeed > minSpeed) {
                return true
            }
            return false
        }

    }

}
