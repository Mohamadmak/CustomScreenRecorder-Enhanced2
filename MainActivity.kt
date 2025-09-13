package com.customrecorder.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var resolutionSpinner: Spinner
    private lateinit var sideBarSwitch: Switch
    private lateinit var colorSpinner: Spinner
    private lateinit var recordButton: Button
    private lateinit var statusText: TextView
    private lateinit var infoText: TextView

    private val REQUEST_CODE_PERMISSIONS = 1000
    private val REQUEST_CODE_SCREEN_CAPTURE = 1001

    private var isRecording = false
    private var mediaProjectionManager: MediaProjectionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupSpinners()
        setupClickListeners()
        updateInfo()
        
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private fun initViews() {
        resolutionSpinner = findViewById(R.id.resolutionSpinner)
        sideBarSwitch = findViewById(R.id.sideBarSwitch)
        colorSpinner = findViewById(R.id.colorSpinner)
        recordButton = findViewById(R.id.recordButton)
        statusText = findViewById(R.id.statusText)
        infoText = findViewById(R.id.infoText)
    }

    private fun setupSpinners() {
        // Resolution options
        val resolutions = arrayOf("9:16 (تيك توك)", "16:9 (يوتيوب)", "1:1 (مربع)")
        val resolutionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, resolutions)
        resolutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        resolutionSpinner.adapter = resolutionAdapter

        // Color options
        val colors = arrayOf("أسود", "أبيض", "رمادي", "أزرق")
        val colorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colors)
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = colorAdapter

        resolutionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateInfo()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        sideBarSwitch.setOnCheckedChangeListener { _, _ -> updateInfo() }
    }

    private fun setupClickListeners() {
        recordButton.setOnClickListener {
            if (!isRecording) {
                if (checkPermissions()) {
                    startScreenCapture()
                } else {
                    requestPermissions()
                }
            } else {
                stopRecording()
            }
        }
    }

    private fun updateInfo() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val screenRatio = String.format("%.1f:9", (screenHeight.toFloat() / screenWidth.toFloat()) * 9)

        val selectedResolution = resolutionSpinner.selectedItemPosition
        val sideBarEnabled = sideBarSwitch.isChecked

        val info = StringBuilder()
        info.append("معلومات الشاشة:\n")
        info.append("الأبعاد: ${screenWidth}x${screenHeight}\n")
        info.append("النسبة: $screenRatio\n\n")

        when (selectedResolution) {
            0 -> { // 9:16
                val targetHeight = (screenWidth * 16) / 9
                if (sideBarEnabled) {
                    val sideBarWidth = (targetHeight - screenHeight) / 2
                    val totalWidth = screenWidth + (sideBarWidth * 2)
                    info.append("دقة التسجيل: ${totalWidth}x${targetHeight}\n")
                    info.append("عرض الأشرطة الجانبية: ${sideBarWidth}px لكل جانب\n")
                    info.append("النتيجة بعد القص: ${screenWidth}x${screenHeight} (مثالي لتيك توك)")
                } else {
                    info.append("دقة التسجيل: ${screenWidth}x${screenHeight}\n")
                    info.append("تحذير: قد تظهر أشرطة سوداء في كين ماستر")
                }
            }
            1 -> { // 16:9
                val targetHeight = (screenWidth * 9) / 16
                info.append("دقة التسجيل: ${screenWidth}x${targetHeight}\n")
                info.append("مثالي لليوتيوب والمنصات الأفقية")
            }
            2 -> { // 1:1
                val size = minOf(screenWidth, screenHeight)
                info.append("دقة التسجيل: ${size}x${size}\n")
                info.append("مثالي للمنشورات المربعة")
            }
        }

        infoText.text = info.toString()
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        return permissions.all { 
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED 
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS)
    }

    private fun startScreenCapture() {
        val captureIntent = mediaProjectionManager?.createScreenCaptureIntent()
        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                startRecordingService(resultCode, data)
            } else {
                Toast.makeText(this, "تم إلغاء إذن تسجيل الشاشة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecordingService(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(this, ScreenRecordService::class.java).apply {
            putExtra("resultCode", resultCode)
            putExtra("data", data)
            putExtra("resolution", resolutionSpinner.selectedItemPosition)
            putExtra("sideBarEnabled", sideBarSwitch.isChecked)
            putExtra("sideBarColor", colorSpinner.selectedItemPosition)
        }
        
        startForegroundService(serviceIntent)
        
        isRecording = true
        recordButton.text = "إيقاف التسجيل"
        recordButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        statusText.text = "جاري التسجيل..."
        statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
    }

    private fun stopRecording() {
        val serviceIntent = Intent(this, ScreenRecordService::class.java)
        stopService(serviceIntent)
        
        isRecording = false
        recordButton.text = "بدء التسجيل"
        recordButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        statusText.text = "جاهز للتسجيل"
        statusText.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        
        Toast.makeText(this, "تم حفظ الفيديو في مجلد التحميلات", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startScreenCapture()
            } else {
                Toast.makeText(this, "الصلاحيات مطلوبة لتسجيل الشاشة", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

