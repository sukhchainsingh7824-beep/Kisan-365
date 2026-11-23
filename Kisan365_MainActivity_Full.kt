package com.aone.refrigeration.kisan365

import android.Manifest
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    data class Motor(
        val id: Int,
        var name: String,
        var serial: String,
        var isOn: Boolean = false
    )

    private val motors = mutableListOf<Motor>()
    private lateinit var listLayout: LinearLayout
    private lateinit var countText: TextView
    private val prefs by lazy { getSharedPreferences("kisan365_motors", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMotors()
        createNotificationChannels()
        showScreen()
        askNotificationPermission()
    }

    private fun showScreen() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 24)
            setBackgroundColor(0xFFF4F8F3.toInt())
        }

        val title = TextView(this).apply {
            text = "KISAN 365 POWER MONITOR"
            textSize = 23f
            setTextColor(0xFF075B28.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 8)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "MY MOTORS  |  AONE REFRIGERATION"
            textSize = 15f
            setTextColor(0xFF333333.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 18)
        }
        root.addView(subtitle)

        countText = TextView(this).apply {
            textSize = 17f
            setTextColor(0xFF075B28.toInt())
            setPadding(4, 10, 4, 12)
        }
        root.addView(countText)

        listLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(listLayout)

        val addButton = Button(this).apply {
            text = if (motors.size >= 5) "MAXIMUM 5 MOTORS ADDED" else "+ ADD MOTOR"
            isEnabled = motors.size < 5
            setOnClickListener { showAddMotorDialog() }
        }
        root.addView(addButton)

        val note = TextView(this).apply {
            text = "Note: Motor ON/OFF buttons are demo controls. Live readings and real motor control need ESP32/backend connection."
            textSize = 12f
            setTextColor(0xFF666666.toInt())
            setPadding(4, 18, 4, 8)
        }
        root.addView(note)

        scroll.addView(root)
        setContentView(scroll)
        refreshMotorCards()
    }

    private fun refreshMotorCards() {
        listLayout.removeAllViews()
        countText.text = "Connected motors: ${motors.size}/5"

        if (motors.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No motors added yet. Tap ADD MOTOR to add your first motor."
                textSize = 16f
                setTextColor(0xFF555555.toInt())
                setPadding(8, 24, 8, 24)
            }
            listLayout.addView(empty)
        }

        motors.forEach { motor ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 14, 16, 14)
                setBackgroundColor(0xFFFFFFFF.toInt())
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 7, 0, 12)
                layoutParams = params
            }

            val name = TextView(this).apply {
                text = motor.name
                textSize = 20f
                setTextColor(0xFF075B28.toInt())
            }
            card.addView(name)

            val serial = TextView(this).apply {
                text = "Serial: ${motor.serial}"
                textSize = 13f
                setTextColor(0xFF555555.toInt())
            }
            card.addView(serial)

            val status = TextView(this).apply {
                text = if (motor.isOn) "● MOTOR ON" else "● MOTOR OFF"
                textSize = 17f
                setTextColor(if (motor.isOn) 0xFF138A36.toInt() else 0xFFC62828.toInt())
                setPadding(0, 12, 0, 8)
            }
            card.addView(status)

            val toggle = Button(this).apply {
                text = if (motor.isOn) "TURN MOTOR OFF" else "TURN MOTOR ON"
                setOnClickListener {
                    motor.isOn = !motor.isOn
                    saveMotors()
                    sendMotorNotification(motor)
                    refreshMotorCards()
                }
            }
            card.addView(toggle)
            listLayout.addView(card)
        }

        // Update the Add Motor button after every refresh.
        val root = listLayout.parent as? LinearLayout ?: return
        val add = root.getChildAt(root.childCount - 2) as? Button
        add?.isEnabled = motors.size < 5
        add?.text = if (motors.size >= 5) "MAXIMUM 5 MOTORS ADDED" else "+ ADD MOTOR"
    }

    private fun showAddMotorDialog() {
        if (motors.size >= 5) {
            Toast.makeText(this, "One farmer can add maximum 5 motors.", Toast.LENGTH_LONG).show()
            return
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 0)
        }
        val nameInput = EditText(this).apply {
            hint = "Motor name (e.g. Tubewell 1)"
            singleLine = true
        }
        val serialInput = EditText(this).apply {
            hint = "Device Serial Number"
            singleLine = true
        }
        form.addView(nameInput)
        form.addView(serialInput)

        AlertDialog.Builder(this)
            .setTitle("Add New Motor")
            .setView(form)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("ADD") { _, _ ->
                val name = nameInput.text.toString().trim()
                val serial = serialInput.text.toString().trim()
                if (name.isBlank() || serial.isBlank()) {
                    Toast.makeText(this, "Enter motor name and serial number.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                if (motors.any { it.serial.equals(serial, ignoreCase = true) }) {
                    Toast.makeText(this, "This serial number is already added.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                val nextId = (motors.maxOfOrNull { it.id } ?: 0) + 1
                motors.add(Motor(nextId, name, serial))
                saveMotors()
                showScreen()
                Toast.makeText(this, "Motor added successfully.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun saveMotors() {
        val array = JSONArray()
        motors.forEach { motor ->
            array.put(JSONObject().apply {
                put("id", motor.id)
                put("name", motor.name)
                put("serial", motor.serial)
                put("isOn", motor.isOn)
            })
        }
        prefs.edit().putString("motors_json", array.toString()).apply()
    }

    private fun loadMotors() {
        motors.clear()
        val raw = prefs.getString("motors_json", "[]") ?: "[]"
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                motors.add(
                    Motor(
                        item.optInt("id", i + 1),
                        item.optString("name", "Motor ${i + 1}"),
                        item.optString("serial", "K365-${i + 1}"),
                        item.optBoolean("isOn", false)
                    )
                )
            }
        } catch (_: Exception) {
            motors.clear()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            for (id in 1..5) {
                val onChannel = NotificationChannel(
                    "k365_motor_${id}_on",
                    "Motor $id ON alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alert when Motor $id turns ON"
                    enableVibration(true)
                }
                val offChannel = NotificationChannel(
                    "k365_motor_${id}_off",
                    "Motor $id OFF alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alert when Motor $id turns OFF"
                    enableVibration(true)
                }
                manager.createNotificationChannel(onChannel)
                manager.createNotificationChannel(offChannel)
            }
        }
    }

    private fun sendMotorNotification(motor: Motor) {
        val isOn = motor.isOn
        val channelId = "k365_motor_${motor.id}_${if (isOn) "on" else "off"}"
        val title = "${motor.name}: ${if (isOn) "POWER ON" else "POWER OFF"}"
        val message = "Device ${motor.serial} ${if (isOn) "turned ON" else "turned OFF"}"

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Allow notifications in app settings to receive alerts.", Toast.LENGTH_LONG).show()
            return
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .build()
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(motor.id * 10 + if (isOn) 1 else 2, notification)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 365)
        }
    }
}
