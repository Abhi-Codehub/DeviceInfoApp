package com.example.deviceinfoapp

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.round
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var sensorManager: SensorManager
    private lateinit var list: List<Sensor>

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.displayInfo)
        textView.text = getSystemDetails()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        textView = findViewById(R.id.displayInfo)

        list = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)
        if (list.isNotEmpty()) {
            sensorManager.registerListener(sel, list[0], SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Toast.makeText(baseContext, "Error: No Accelerometer.", Toast.LENGTH_LONG).show()
        }

    }

    var sel: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        @SuppressLint("SetTextI18n")
        override fun onSensorChanged(event: SensorEvent) {
            val values = event.values
            val sensor = """
            x: ${values[0]}
            y: ${values[1]}
            z: ${values[2]}
            """.trimIndent()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    textView.text = getSystemDetails() + sensor
                }
            } catch (e: CameraAccessException) {
                Toast.makeText(
                    this@MainActivity,
                    "Camera Access Exception Occurred",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("HardwareIds")
    private fun getSystemDetails() : String {
        return "Manufacture: ${Build.MANUFACTURER} \n" +
                "Model: ${Build.MODEL} \n" +
                "Brand: ${Build.BRAND} \n" +
                getRAM() +
                getBatteryInfo() +
                "Version Code: ${Build.VERSION.RELEASE}\n" +
                "Incremental: ${Build.VERSION.INCREMENTAL} \n" +
                getCamerasMegaPixel() +
                getScreenResolution(this) +
                "SDK: ${Build.VERSION.SDK_INT} \n" +
//                getIMEI() +
                "\nDeviceID: ${Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)} \n" +
                "ID: ${Build.ID} \n" +
                "User: ${Build.USER} \n" +
                "Type: ${Build.TYPE} \n" +
                "Base: ${Build.VERSION_CODES.BASE} \n" +
                "Board: ${Build.BOARD} \n" +
                "Host: ${Build.HOST} \n" +
                "FingerPrint: ${Build.FINGERPRINT} \n" +
                "Display: ${Build.DISPLAY} \n" +
                "CPU ABI: ${ Build.CPU_ABI } \n" +
                "Radio Version: ${Build.getRadioVersion() } \n" +
                "BootLoader: ${ Build.BOOTLOADER } \n" +
                "Hardware: ${ Build.HARDWARE } \n" +
                "Product: ${Build.PRODUCT} \n"
    }

    private fun getScreenResolution(context: Context): String {
        val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        return "Screen Resolution: $width x $height pixels \n"
    }

    private fun getIMEI() : String {
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            "IMEI number : ${telephonyManager.imei} \n"
        } else {
            return ""
        }
    }

    @Throws(CameraAccessException::class)
    private fun getCamerasMegaPixel(): String {
        var output: String
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = manager.cameraIdList
        var characteristics = manager.getCameraCharacteristics(cameraIds[0])
        output = "Back camera MP: " + calculateMegaPixel(
            characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!!
                .width.toFloat(),
            characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!!.height.toFloat()
        ) + "\n"
        characteristics = manager.getCameraCharacteristics(cameraIds[1])
        output += "Front camera MP: " + calculateMegaPixel(
            characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!!
                .width.toFloat(),
            characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!!
                .height.toFloat()
        ) + "\n"
        return output
    }

    private fun calculateMegaPixel(width: Float, height: Float): Int {
        return (width * height / 1024000).roundToInt()
    }

    private fun getRAM() : String{
        val actManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val availMemory = round( memInfo.availMem.toDouble()/(1024*1024*1024))
        val totalMemory= round( memInfo.totalMem.toDouble()/(1024*1024*1024))

        return "Available RAM: $availMemory\nTotal RAM: $totalMemory \nIs device low on RAM: ${actManager.isLowRamDevice}"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getBatteryInfo() : String {
        val batLevel = this.getSystemService(BATTERY_SERVICE) as BatteryManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            "Battery: ${batLevel.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)} \nIs device in charging: ${batLevel.isCharging} \nTime remaining for charging: ${batLevel.computeChargeTimeRemaining() / 1000 / 60} minutes\n"
        } else {
            "Battery: ${batLevel.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)} \nIs device in charging: ${batLevel.isCharging} \n"
        }
    }

}