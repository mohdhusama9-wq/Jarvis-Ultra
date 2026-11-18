package com.jarvise.ultra

import android.app.Activity
import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
            setBackgroundColor(Color.rgb(0, 8, 0))
        }

        val title = TextView(this).apply {
            text = "JARVISE ULTRA"
            textSize = 30f
            setTextColor(Color.GREEN)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "\n[ SYSTEM INITIALIZED ]\n"
            textSize = 16f
            setTextColor(Color.GREEN)
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "WAKE WORD: OFFLINE\n\nSay: HEY JARVIS"
            textSize = 17f
            setTextColor(Color.GREEN)
            gravity = Gravity.CENTER
        }

        val startButton = Button(this).apply {
            text = "ACTIVATE JARVIS"
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.GREEN)
            setOnClickListener {
                activateJarvis()
            }
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(status)

        layout.addView(startButton, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 65
        ).apply {
            topMargin = 40
        })

        setContentView(layout)
    }

    private fun activateJarvis() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO), 100
            )
            return
        }

        val intent = Intent(this, WakeWordService::class.java)
        startForegroundService(intent)
        status.text = "JARVIS SERVICE STARTING...\n\nWAKE WORD: HEY JARVIS"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            activateJarvis()
        }
    }
}
