package com.aone.refrigeration.kisan365

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
        private lateinit var motorStatus: TextView
            private var motorOn = false
                private val channelId = "kisan365_motor_alerts"

                    override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                        createNotificationChannel()
                                                showDashboard()
                    }

                        private fun showDashboard() {
                                    val root = LinearLayout(this).apply {
                                                    orientation = LinearLayout.VERTICAL
                                                                setPadding(28, 32, 28, 24)
                                                                            gravity = Gravity.CENTER_HORIZONTAL
                                    }
                                            fun addText(value: String, size: Float = 18f) {
                                                            root.addView(TextView(this).apply {
                                                                                text = value
                                                                                                textSize = size
                                                                                                                setPadding(0, 10, 0, 10)
                                                            })
                                            }
                                                    addText("KISAN 365 POWER MONITOR", 23f)
                                                            addText("Commercial Edition • AONE REFRIGERATION", 14f)
                                                                    motorStatus = TextView(this).apply { text = "MOTOR STATUS: OFF"; textSize = 22f; setPadding(0, 12, 0, 12) }
                                                                            root.addView(motorStatus)
                                                                                    root.addView(Button(this).apply {
                                                                                                    text = "MOTOR ON / OFF (DEMO)"
                                                                                                                setOnClickListener {
                                                                                                                                    motorOn = !motorOn
                                                                                                                                                    motorStatus.text = if (motorOn) "MOTOR STATUS: ON ⚙️" else "MOTOR STATUS: OFF"
                                                                                                                                                                    sendMotorNotification(motorOn)
                                                                                                                }
                                                                                    })
                                                                                            addText("THREE PHASE MONITORING", 19f)
                                                                                                    addText("R Phase: -- V     -- A")
                                                                                                            addText("Y Phase: -- V     -- A")
                                                                                                                    addText("B Phase: -- V     -- A")
                                                                                                                            addText("Power: -- kW     Energy: -- kWh")
                                                                                                                                    addText("Power Factor: --     Temperature: -- °C")
                                                                                                                                            addText("MOTOR TIMER (5–270 MINUTES)")
                                                                                                                                                    val timerLabel = TextView(this).apply { text = "Selected time: 5 minutes"; textSize = 16f }
                                                                                                                                                            root.addView(timerLabel)
                                                                                                                                                                    root.addView(SeekBar(this).apply {
                                                                                                                                                                                    max = 265
                                                                                                                                                                                                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                                                                                                                                                                                                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                                                                                                                                                                                                                            timerLabel.text = "Selected time: ${progress + 5} minutes"
                                                                                                                                                                    }
                                                                                                                                                                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                                                                                                                                                                                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                                                                                                                                                                    })
                                                                                                                                                                    })
                                                                                                                                                                            addText("DEVICE CONNECTION", 19f)
                                                                                                                                                                                    root.addView(Button(this).apply { text = "Wi-Fi Setup (Demo)"; setOnClickListener { Toast.makeText(this@MainActivity, "Wi-Fi setup will be added next.", Toast.LENGTH_LONG).show() } })
                                                                                                                                                                                            root.addView(Button(this).apply { text = "4G SIM Setup (Demo)"; setOnClickListener { Toast.makeText(this@MainActivity, "4G modem/APN setup will be added next.", Toast.LENGTH_LONG).show() } })
                                                                                                                                                                                                    root.addView(Button(this).apply { text = "Test Notification"; setOnClickListener { sendMotorNotification(motorOn) } })
                                                                                                                                                                                                            addText("DEMO ONLY: This screen does not connect to ESP32 or control a real motor.", 13f)
                                                                                                                                                                                                                    setContentView(root)
                        }

                            private fun createNotificationChannel() {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        val channel = NotificationChannel(channelId, "KISAN 365 Motor Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                                                                            description = "Motor ON and OFF alerts"
                                                                                            enableVibration(true)
                                                                                                            vibrationPattern = longArrayOf(0, 500, 250, 500)
                                                        }
                                                                    getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
                                        }
                            }

                                private fun sendMotorNotification(isOn: Boolean) {
                                            val title = if (isOn) "Motor turned ON" else "Motor turned OFF"
                                                    val message = if (isOn) "KISAN 365 demo: motor is ON." else "KISAN 365 demo: motor is OFF."
                                                            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                            android.app.Notification.Builder(this, channelId)
                                                            } else {
                                                                            @Suppress("DEPRECATION") android.app.Notification.Builder(this)
                                                            }
                                                                    val notification = builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                                                                                .setContentTitle(title).setContentText(message).setAutoCancel(true).build()
                                                                                        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(if (isOn) 3651 else 3650, notification)
                                                                                                try {
                                                                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                                                                                                    getSystemService(VibratorManager::class.java).defaultVibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
                                                                                                                } else {
                                                                                                                                    @Suppress("DEPRECATION") val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                                                                                                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
                                                                                                                                                                    else { @Suppress("DEPRECATION") vibrator.vibrate(800) }
                                                                                                                }
                                                                                                } catch (_: Exception) { }
                                }
}

                                                            })
                                    }
                    }
}