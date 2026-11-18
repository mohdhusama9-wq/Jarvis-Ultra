package com.jarvise.ultra

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.openwakeword.OpenWakeWord
import java.util.Locale

class WakeWordService : Service() {

    companion object {
        private const val TAG = "JARVIS_DEBUG"
    }

    private var detector: OpenWakeWord? = null
    private var tts: TextToSpeech? = null
    private var speaking = false
    private var ttsReady = false

    private val channelId = "jarvise_wake_channel"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")

        createNotificationChannel()

        tts = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                ttsReady = true
                tts?.language = Locale.US
                Log.i(TAG, "TTS initialized successfully")
            } else {
                Log.e(TAG, "TTS initialization failed: $result")
            }
        }

        tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(id: String?) {
                    Log.i(TAG, "Speaking started")
                }

                override fun onDone(id: String?) {
                    speaking = false
                    Log.i(TAG, "Speaking completed")
                }

                override fun onError(id: String?) {
                    speaking = false
                    Log.e(TAG, "TTS speaking error")
                }
            }
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        Log.i(TAG, "onStartCommand called")
        startForeground(101, buildNotification())

        if (detector == null) {
            try {
                Log.i(TAG, "Creating wake word detector")

                detector = OpenWakeWord.Builder(this)
                    .setModel(OpenWakeWord.BuiltInModel.HEY_JARVIS)
                    .setThreshold(0.5f)
                    .build()

                Log.i(TAG, "Detector created. Starting listener")

                detector?.start { score ->
                    Log.i(TAG, "HEY JARVIS DETECTED! Score=$score")

                    if (!speaking && ttsReady) {
                        speaking = true
                        tts?.speak(
                            "Hello Boss. JARVIS is listening.",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "jarvis_reply"
                        )
                    } else {
                        Log.w(TAG, "TTS not ready or already speaking")
                    }
                }

                Log.i(TAG, "Detector start command sent")

            } catch (e: Exception) {
                Log.e(TAG, "WAKE WORD SERVICE FAILED", e)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "JARVISE ULTRA",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, channelId)
            .setContentTitle("JARVISE ULTRA ACTIVE")
            .setContentText("Listening for Hey JARVIS")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        Log.i(TAG, "Service destroyed")

        detector?.stop()
        detector?.release()
        detector = null

        tts?.stop()
        tts?.shutdown()
        tts = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
