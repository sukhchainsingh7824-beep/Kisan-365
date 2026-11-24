package com.aone.kisan365

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class DashboardActivity : Activity() {
    private val green = Color.rgb(18, 112, 62)
    private val ink = Color.rgb(35, 55, 43)
    private val pale = Color.rgb(243, 248, 244)
    private val motorNames = mutableListOf("MAIN PUMP")
    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(pale) }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 18, 16, 16) }
        val scroll = ScrollView(this).apply { addView(content) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setBackgroundColor(Color.WHITE); setPadding(4, 6, 4, 6); elevation = 8f }
        root.addView(nav, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
        showPage("Home")
    }

    private fun showPage(page: String) {
        content.removeAllViews()
        nav.removeAllViews()
        when (page) {
            "Home" -> buildHome()
            "Timer" -> buildTimer()
            "Alerts" -> buildAlerts()
            else -> buildSettings()
        }
        listOf("Home", "Timer", "Alerts", "Settings").forEach { label ->
            val b = Button(this).apply {
                text = when (label) { "Home" -> "⌂\nHome"; "Timer" -> "◷\nTimer"; "Alerts" -> "◉\nAlerts"; else -> "⚙\nSettings" }
                textSize = 11f; setTextColor(if (page == label) green else ink); setBackgroundColor(Color.TRANSPARENT); isAllCaps = false
                setOnClickListener { showPage(label) }
            }
            nav.addView(b, LinearLayout.LayoutParams(0, -2, 1f))
        }
    }

    private fun buildHome() {
        title("🌾 KISAN 365")
        text("SMART MOTOR & POWER MONITOR", 13f, ink)
        card {
            text("DEVICE STATUS", 11f)
            text("NOT LINKED", 19f, Color.rgb(190, 75, 55), true)
            text("POWER SUPPLY: WAITING FOR DEVICE", 13f)
            text("Connect and activate a compatible controller to view live readings.", 11f)
        }
        heading("POWER DASHBOARD")
        metricCard("R PHASE VOLTAGE", "-- V", "R PHASE CURRENT", "-- A")
        metricCard("Y PHASE VOLTAGE", "-- V", "Y PHASE CURRENT", "-- A")
        metricCard("B PHASE VOLTAGE", "-- V", "B PHASE CURRENT", "-- A")
        metricCard("TOTAL CURRENT", "-- A", "POWER", "-- kW")
        metricCard("FREQUENCY", "-- Hz", "POWER FACTOR", "--")
        metricCard("ENERGY", "-- kWh", "TEMPERATURE", "-- °C")
        heading("MOTOR CONTROL")
        motorNames.forEachIndexed { index, name -> motorCard(index + 1, name) }
        text("Live values and motor commands appear only after a verified device connection. No sample readings are shown.", 11f, Color.DKGRAY)
    }

    private fun motorCard(number: Int, name: String) {
        card {
            text("MOTOR %02d · %s".format(number, name), 17f, green, true)
            text("STATUS: NOT CONNECTED", 13f, Color.GRAY, true)
            metricCard("CURRENT", "-- A", "MOTOR TEMPERATURE", "-- °C")
            val buttons = LinearLayout(this@DashboardActivity).apply { orientation = LinearLayout.HORIZONTAL }
            buttons.addView(Button(this@DashboardActivity).apply { text = "MOTOR ON"; isEnabled = false }, LinearLayout.LayoutParams(0, -2, 1f))
            buttons.addView(Button(this@DashboardActivity).apply { text = "MOTOR OFF"; isEnabled = false }, LinearLayout.LayoutParams(0, -2, 1f))
            addView(buttons)
            text("Controls locked until the device is linked.", 10f, Color.GRAY)
        }
    }

    private fun buildTimer() {
        title("MOTOR TIMER")
        card {
            text("Automatic motor OFF timer", 17f, green, true)
            text("Choose a duration from 5 to 720 minutes.", 12f)
            addView(TextView(this@DashboardActivity).apply { text = "60 minutes"; textSize = 22f; setTextColor(green) })
            addView(SeekBar(this@DashboardActivity).apply { max = 715; progress = 55; isEnabled = false })
            text("Timer cannot start until a controller is connected. A real automatic OFF requires controller acknowledgement.", 11f, Color.DKGRAY)
            addView(Button(this@DashboardActivity).apply { text = "START TIMER"; isEnabled = false })
            addView(Button(this@DashboardActivity).apply { text = "CANCEL TIMER"; isEnabled = false })
        }
    }

    private fun buildAlerts() {
        title("ALERTS")
        card {
            text("No alerts received yet.", 16f, ink, true)
            text("Power supply ON/OFF, motor trips, overload and device-offline alerts will appear here after Firebase and controller integration.", 12f)
        }
    }

    private fun buildSettings() {
        title("SETTINGS")
        card {
            heading("MANAGE MOTORS")
            text("Add or remove up to 7 motors here. The Home dashboard remains read-only for motor setup.", 11f)
            motorNames.toList().forEachIndexed { index, name ->
                val row = LinearLayout(this@DashboardActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                row.addView(TextView(this@DashboardActivity).apply { text = "MOTOR %02d · %s".format(index + 1, name); setTextColor(ink); textSize = 13f }, LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(Button(this@DashboardActivity).apply {
                    text = "DELETE"; isAllCaps = false; isEnabled = motorNames.size > 1
                    setOnClickListener { motorNames.remove(name); showPage("Settings") }
                })
                addView(row)
            }
            addView(Button(this@DashboardActivity).apply {
                text = "＋ ADD NEW MOTOR"; isAllCaps = false; isEnabled = motorNames.size < 7
                setOnClickListener { motorNames.add("MOTOR " + (motorNames.size + 1)); showPage("Settings") }
            })
        }
        card {
            heading("MOTOR AMPERE PROTECTION")
            text("Set limits only after checking the motor nameplate and measuring normal running current. Hardware overload and emergency protection must remain independent.", 11f)
            addView(Spinner(this@DashboardActivity).apply {
                adapter = ArrayAdapter(this@DashboardActivity, android.R.layout.simple_spinner_dropdown_item, motorNames)
            })
            addView(EditText(this@DashboardActivity).apply { hint = "Minimum current (A)"; inputType = 8194 })
            addView(EditText(this@DashboardActivity).apply { hint = "Maximum current (A)"; inputType = 8194 })
            addView(Button(this@DashboardActivity).apply { text = "SAVE PROTECTION"; isAllCaps = false; isEnabled = false })
            text("Protection settings are not sent to a controller yet.", 10f, Color.GRAY)
        }
        card {
            heading("DEVICE LINKING")
            text("QR activation, customer login and serial-number verification require Firebase Authentication and a secure activation backend.", 11f)
            addView(Button(this@DashboardActivity).apply { text = "DEVICE SETUP NOT READY"; isEnabled = false })
        }
        card {
            heading("ADMIN / FIREBASE")
            text("Firebase project is included in the Android build. Authentication, database rules, FCM token registration and admin roles still need integration.", 11f)
        }
    }

    private fun title(value: String) {
        content.addView(TextView(this).apply { text = value; textSize = 24f; typeface = Typeface.DEFAULT_BOLD; setTextColor(green); setPadding(0, 4, 0, 5) })
    }
    private fun heading(value: String) {
        content.addView(TextView(this).apply { text = value; textSize = 18f; typeface = Typeface.DEFAULT_BOLD; setTextColor(green); setPadding(0, 14, 0, 8) })
    }
    private fun text(value: String, size: Float = 14f, color: Int = ink, bold: Boolean = false) {
        content.addView(TextView(this).apply { text = value; textSize = size; setTextColor(color); if (bold) typeface = Typeface.DEFAULT_BOLD; setPadding(2, 5, 2, 5) })
    }
    private fun card(block: LinearLayout.() -> Unit) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(13, 12, 13, 12); setBackgroundColor(Color.WHITE); elevation = 2f }
        val params = LinearLayout.LayoutParams(-1, -2); params.setMargins(0, 7, 0, 7)
        content.addView(box, params); box.block()
    }
    private fun metricCard(label1: String, value1: String, label2: String, value2: String) {
        card {
            val row = LinearLayout(this@DashboardActivity).apply { orientation = LinearLayout.HORIZONTAL }
            fun cell(label: String, value: String): LinearLayout = LinearLayout(this@DashboardActivity).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(this@DashboardActivity).apply { text = label; textSize = 10f; setTextColor(Color.GRAY) })
                addView(TextView(this@DashboardActivity).apply { text = value; textSize = 19f; typeface = Typeface.DEFAULT_BOLD; setTextColor(ink) })
            }
            row.addView(cell(label1, value1), LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(cell(label2, value2), LinearLayout.LayoutParams(0, -2, 1f))
            addView(row)
        }
    }
}
