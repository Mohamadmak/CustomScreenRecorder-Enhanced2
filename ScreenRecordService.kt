package com.customrecorder.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ScreenRecordService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjectionManager: MediaProjectionManager? = null

    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0
    private var recordingWidth = 0
    private var recordingHeight = 0

    private var sideBarEnabled = false
    private var sideBarColor = 0
    private var resolution = 0

    companion object {
        private const val TAG = "ScreenRecordService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "screen_record_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        val displayMetrics = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        screenDensity = displayMetrics.densityDpi
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED)
            val data = intent.getParcelableExtra<Intent>("data")
            resolution = intent.getIntExtra("resolution", 0)
            sideBarEnabled = intent.getBooleanExtra("sideBarEnabled", false)
            sideBarColor = intent.getIntExtra("sideBarColor", 0)

            if (resultCode == Activity.RESULT_OK && data != null) {
                startRecording(resultCode, data)
            }
        }
        return START_NOT_STICKY
    }

    private fun startRecording(resultCode: Int, data: Intent) {
        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
        
        calculateRecordingDimensions()
        setupMediaRecorder()
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        createVirtualDisplay()
        mediaRecorder?.start()
        
        Log.d(TAG, "Recording started with dimensions: ${recordingWidth}x${recordingHeight}")
    }

    private fun calculateRecordingDimensions() {
        when (resolution) {
            0 -> { // 9:16 (TikTok)
                if (sideBarEnabled) {
                    recordingHeight = (screenWidth * 16) / 9
                    val sideBarWidth = (recordingHeight - screenHeight) / 2
                    recordingWidth = screenWidth + (sideBarWidth * 2)
                } else {
                    recordingWidth = screenWidth
                    recordingHeight = screenHeight
                }
            }
            1 -> { // 16:9 (YouTube)
                recordingWidth = screenWidth
                recordingHeight = (screenWidth * 9) / 16
            }
            2 -> { // 1:1 (Square)
                val size = minOf(screenWidth, screenHeight)
                recordingWidth = size
                recordingHeight = size
            }
        }
    }

    private fun setupMediaRecorder() {
        val outputFile = getOutputFile()
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        
        mediaRecorder?.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile.absolutePath)
            setVideoSize(recordingWidth, recordingHeight)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(8000000)
            setVideoFrameRate(30)
            
            try {
                prepare()
            } catch (e: Exception) {
                Log.e(TAG, "MediaRecorder prepare failed", e)
            }
        }
    }

    private fun createVirtualDisplay() {
        val surface = mediaRecorder?.surface
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenRecord",
            recordingWidth,
            recordingHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            null
        )
        
        // إذا كانت الأشرطة الجانبية مفعلة، ارسم الأشرطة
        if (sideBarEnabled && resolution == 0) {
            drawSideBars(surface)
        }
    }

    private fun drawSideBars(surface: Surface?) {
        if (surface == null) return
        
        try {
            val canvas = surface.lockCanvas(null)
            val paint = Paint().apply {
                color = getSideBarColor()
                style = Paint.Style.FILL
            }
            
            val sideBarWidth = (recordingWidth - screenWidth) / 2
            
            // رسم الشريط الأيسر
            canvas.drawRect(0f, 0f, sideBarWidth.toFloat(), recordingHeight.toFloat(), paint)
            
            // رسم الشريط الأيمن
            canvas.drawRect(
                (recordingWidth - sideBarWidth).toFloat(),
                0f,
                recordingWidth.toFloat(),
                recordingHeight.toFloat(),
                paint
            )
            
            surface.unlockCanvasAndPost(canvas)
        } catch (e: Exception) {
            Log.e(TAG, "Error drawing side bars", e)
        }
    }

    private fun getSideBarColor(): Int {
        return when (sideBarColor) {
            0 -> Color.BLACK
            1 -> Color.WHITE
            2 -> Color.GRAY
            3 -> Color.BLUE
            else -> Color.BLACK
        }
    }

    private fun getOutputFile(): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(downloadsDir, "ScreenRecord_$timestamp.mp4")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تسجيل الشاشة",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعارات تسجيل الشاشة"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, ScreenRecordService::class.java).apply {
            action = "STOP_RECORDING"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("جاري تسجيل الشاشة")
            .setContentText("اضغط لإيقاف التسجيل")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "إيقاف", stopPendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            
            virtualDisplay?.release()
            virtualDisplay = null
            
            mediaProjection?.stop()
            mediaProjection = null
            
            Log.d(TAG, "Recording stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

