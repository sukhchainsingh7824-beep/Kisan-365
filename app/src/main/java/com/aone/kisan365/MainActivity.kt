package com.aone.kisan365

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.firebase.database.*

class DashboardActivity : Activity() {
    private val green = Color.rgb(22, 131, 74)
    private val darkGreen = Color.rgb(12, 96, 52)
    private val ink = Color.rgb(24, 54, 41)
    private val muted = Color.rgb(116, 132, 122)
    private val prefs by lazy { getSharedPreferences("kisan365", MODE_PRIVATE) }
    private var deviceId = ""
    private var deviceRef: DatabaseReference? = null
    private var telemetryListener: ValueEventListener? = null
    private lateinit var root: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var supplyText: TextView
    private lateinit var rVoltage: TextView
    private lateinit var yVoltage: TextView
    private lateinit var bVoltage: TextView
    private lateinit var rCurrent: TextView
    private lateinit var yCurrent: TextView
    private lateinit var bCurrent: TextView
    private lateinit var motorText: TextView
    private lateinit var messageText: TextView
    private lateinit var deviceButton: Button
    private var online = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = darkGreen
        window.navigationBarColor = green
        deviceId = prefs.getString("device_id", "") ?: ""
        buildUi()
        if (deviceId.isNotBlank()) connectDevice() else showDisconnected()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun text(value: String, size: Float, color: Int = ink, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(13), dp(14), dp(13))
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.WHITE); cornerRadius = dp(15).toFloat()
            setStroke(dp(1), Color.rgb(226, 235, 229))
        }
    }

    private fun addGap(parent: LinearLayout, h: Int = 10) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(h)))
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(242, 247, 243))
        }
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(18))
        }
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(14))
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(darkGreen, green)
            ).apply { cornerRadius = dp(22).toFloat() }
        }
        hero.addView(text("🌾 KISAN 365", 24f, Color.WHITE, true))
        hero.addView(text("SMART MOTOR & POWER MONITOR", 12f, Color.WHITE))
        addGap(hero, 10)
        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        statusText = text("● DEVICE OFFLINE", 13f, Color.WHITE, true)
        supplyText = text("POWER UNKNOWN", 13f, Color.WHITE, true)
        topRow.addView(statusText, LinearLayout.LayoutParams(0, -2, 1f))
        topRow.addView(supplyText)
        hero.addView(topRow)
        body.addView(hero)
        addGap(body, 14)

        body.addView(text("Power Dashboard", 20f, ink, true))
        addGap(body, 8)
        val deviceCard = card()
        deviceCard.addView(text("Connected Device", 15f, ink, true))
        addGap(deviceCard, 5)
        val idLine = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        messageText = text(if (deviceId.isBlank()) "No device linked" else deviceId, 13f, muted)
        idLine.addView(messageText, LinearLayout.LayoutParams(0, -2, 1f))
        deviceButton = Button(this).apply {
            text = if (deviceId.isBlank()) "LINK DEVICE" else "CHANGE"
            setOnClickListener { showDeviceDialog() }
        }
        idLine.addView(deviceButton)
        deviceCard.addView(idLine)
        body.addView(deviceCard)
        addGap(body)

        val phaseCard = card()
        phaseCard.addView(text("Three Phase Readings", 16f, ink, true))
        addGap(phaseCard, 10)
        val phaseRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun phase(label: String): Pair<LinearLayout, TextView> {
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                setPadding(dp(5), dp(12), dp(5), dp(12))
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.rgb(247, 249, 248)); cornerRadius = dp(10).toFloat()
                }
            }
            col.addView(text(label, 12f, green, true))
            val value = text("-- V", 20f, ink, true)
            col.addView(value)
            return col to value
        }
        val (rCol, rv) = phase("R PHASE"); rVoltage = rv
        val (yCol, yv) = phase("Y PHASE"); yVoltage = yv
        val (bCol, bv) = phase("B PHASE"); bVoltage = bv
        phaseRow.addView(rCol, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(5) })
        phaseRow.addView(yCol, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(5) })
        phaseRow.addView(bCol, LinearLayout.LayoutParams(0, -2, 1f))
        phaseCard.addView(phaseRow)
        addGap(phaseCard, 8)
        val amps = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        rCurrent = text("R: -- A", 12f, muted); yCurrent = text("Y: -- A", 12f, muted); bCurrent = text("B: -- A", 12f, muted)
        amps.addView(rCurrent, LinearLayout.LayoutParams(0, -2, 1f))
        amps.addView(yCurrent, LinearLayout.LayoutParams(0, -2, 1f))
        amps.addView(bCurrent, LinearLayout.LayoutParams(0, -2, 1f))
        phaseCard.addView(amps)
        body.addView(phaseCard)
        addGap(body)
        val motorCard = card()
        val motorTitle = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        motorTitle.addView(text("⚙", 21f, green, true))
        motorTitle.addView(text("  Motor 01 · Main Pump", 16f, ink, true))
        motorCard.addView(motorTitle)
        addGap(motorCard, 5)
        motorText = text("MOTOR UNKNOWN", 14f, muted, true)
        motorCard.addView(motorText)
        addGap(motorCard, 8)
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        controls.addView(Button(this).apply {
            text = "MOTOR ON"
            setOnClickListener { sendMotorCommand(true) }
        }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(6) })
        controls.addView(Button(this).apply {
            text = "MOTOR OFF"
            setOnClickListener { sendMotorCommand(false) }
        }, LinearLayout.LayoutParams(0, -2, 1f))
        motorCard.addView(controls)
        body.addView(motorCard)
        addGap(body)
        val note = card()
        note.addView(text("Live data", 15f, ink, true))
        note.addView(text("No sample readings are shown. Voltage, current and supply state appear only after the linked ESP8266 sends real telemetry.", 12f, muted))
        body.addView(note)

        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
            setPadding(0, dp(7), 0, dp(28)); minimumHeight = dp(66); setBackgroundColor(Color.WHITE)
        }
        listOf("⌂\nHome", "◷\nTimer", "◉\nAlerts", "⚙\nSettings").forEach { label ->
            nav.addView(TextView(this).apply {
                text = label; gravity = Gravity.CENTER; textSize = 13f; setTextColor(green); minHeight = dp(48); isClickable = true
                setOnClickListener { if (label.contains("Settings")) showSettingsInfo() else if (label.contains("Timer")) showTimerInfo() else if (label.contains("Alerts")) showAlertsInfo() }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(nav)
        setContentView(root)
    }

    private fun showDisconnected() {
        online = false
        statusText.text = "● DEVICE OFFLINE"
        supplyText.text = "POWER UNKNOWN"
        rVoltage.text = "-- V"; yVoltage.text = "-- V"; bVoltage.text = "-- V"
        rCurrent.text = "R: -- A"; yCurrent.text = "Y: -- A"; bCurrent.text = "B: -- A"
        motorText.text = "MOTOR UNKNOWN"
        messageText.text = "No device linked"
    }

    private fun showDeviceDialog() {
        val input = EditText(this).apply {
            hint = "Enter Device ID (e.g. K365-0001)"
            setText(deviceId)
        }
        AlertDialog.Builder(this).setTitle("Link KISAN 365 Device")
            .setMessage("Enter the Device ID printed in its QR code.")
            .setView(input)
            .setPositiveButton("LINK") { _, _ ->
                val id = input.text.toString().trim()
                if (id.isBlank() || !id.matches(Regex("[A-Za-z0-9_-]{3,64}"))) {
                    Toast.makeText(this, "Enter a valid Device ID", Toast.LENGTH_LONG).show()
                } else {
                    deviceId = id
                    prefs.edit().putString("device_id", id).apply()
                    deviceButton.text = "CHANGE"
                    connectDevice()
                }
            }.setNegativeButton("CANCEL", null).show()
    }

    private fun connectDevice() {
        detachListener()
        messageText.text = deviceId
        showDisconnected()
        // Shared telemetry contract: devices/{deviceId}/telemetry
        val base = FirebaseDatabase.getInstance("https://kisan365-commercial-default-rtdb.firebaseio.com")
        deviceRef = base.getReference("devices").child(deviceId)
        val telemetry = deviceRef!!.child("telemetry")
        telemetryListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                online = lastSeen > 0 && System.currentTimeMillis() - lastSeen < 90_000L
                statusText.text = if (online) "● DEVICE ONLINE" else "● DEVICE OFFLINE"
                val supply = snapshot.child("supplyOn").getValue(Boolean::class.java)
                supplyText.text = when { !online -> "POWER UNKNOWN"; supply == true -> "POWER SUPPLY ON"; supply == false -> "POWER SUPPLY OFF"; else -> "POWER UNKNOWN" }
                fun number(path: String): String = snapshot.child(path).getValue(Number::class.java)?.toDouble()?.let { String.format("%.1f", it) } ?: "--"
                rVoltage.text = number("voltage/r").let { "$it V" }
                yVoltage.text = number("voltage/y").let { "$it V" }
                bVoltage.text = number("voltage/b").let { "$it V" }
                rCurrent.text = "R: " + number("current/r") + " A"
                yCurrent.text = "Y: " + number("current/y") + " A"
                bCurrent.text = "B: " + number("current/b") + " A"
                motorText.text = if (!online) "MOTOR UNKNOWN" else if (snapshot.child("motorOn").getValue(Boolean::class.java) == true) "MOTOR RUNNING" else "MOTOR OFF"
                if (!online) {
                    rVoltage.text = "-- V"; yVoltage.text = "-- V"; bVoltage.text = "-- V"
                    rCurrent.text = "R: -- A"; yCurrent.text = "Y: -- A"; bCurrent.text = "B: -- A"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                showDisconnected()
                messageText.text = "Firebase: " + error.message
            }
        }
        telemetry.addValueEventListener(telemetryListener!!)
    }

    private fun sendMotorCommand(on: Boolean) {
        if (!online || deviceRef == null) {
            Toast.makeText(this, "Device offline. Motor command not sent.", Toast.LENGTH_LONG).show()
            return
        }
        deviceRef!!.child("commands").child("motor01").setValue(on)
            .addOnSuccessListener { Toast.makeText(this, if (on) "Motor ON command sent" else "Motor OFF command sent", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(this, "Command failed: ${it.message}", Toast.LENGTH_LONG).show() }
    }

    private fun showSettingsInfo() {
        AlertDialog.Builder(this).setTitle("Settings")
            .setMessage("Linked Device: ${if (deviceId.isBlank()) "None" else deviceId}\nDefault Motor Start Delay: 5 seconds\nChange device from the Link Device button.")
            .setPositiveButton("OK", null).show()
    }
    private fun showTimerInfo() {
        AlertDialog.Builder(this).setTitle("Motor Timer")
            .setMessage("Timer controls will be enabled after the ESP8266 controller reports and acknowledges commands.")
            .setPositiveButton("OK", null).show()
    }
    private fun showAlertsInfo() {
        AlertDialog.Builder(this).setTitle("Alerts")
            .setMessage("No device alerts yet. Alerts will appear after the linked ESP8266 sends events.")
            .setPositiveButton("OK", null).show()
    }
    private fun detachListener() {
        if (deviceRef != null && telemetryListener != null) deviceRef!!.child("telemetry").removeEventListener(telemetryListener!!)
        telemetryListener = null
    }
    override fun onDestroy() {
        detachListener()
        super.onDestroy()
    }
}
