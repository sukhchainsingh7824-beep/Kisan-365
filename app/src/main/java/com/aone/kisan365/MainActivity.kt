package com.aone.kisan365

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*

class DashboardActivity : Activity() {

    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 20, 18, 20)
            setBackgroundColor(Color.rgb(244, 247, 242))
        }

        scroll.addView(layout)
        setContentView(scroll)

        addTitle("🌾 KISAN 365")
        addText("SMART MOTOR & POWER MONITOR")
        addText("DEVICE STATUS: NOT CONNECTED")
        addText("POWER SUPPLY: WAITING FOR DEVICE")

        addTitle("POWER DASHBOARD")

        addInfo("Voltage: -- V")
        addInfo("Current: -- A")
        addInfo("Frequency: -- Hz")

        addTitle("MOTOR CONTROL")

        for (i in 1..7) {
            addMotor(i)
        }

        addTitle("SYSTEM STATUS")
        addText("Firebase: Pending")
        addText("FCM: Pending")
        addText("Hardware: Not Connected")
    }

    private fun addTitle(value: String) {
        val text = TextView(this).apply {
            this.text = value
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(20, 100, 50))
            setPadding(0, 20, 0, 12)
        }
        layout.addView(text)
    }

    private fun addText(value: String) {
        val text = TextView(this).apply {
            this.text = value
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(4, 8, 4, 8)
        }
        layout.addView(text)
    }

    private fun addInfo(value: String) {
        val card = createCard()
        val text = TextView(this).apply {
            this.text = value
            textSize = 18f
            setTextColor(Color.DKGRAY)
            setPadding(8, 10, 8, 10)
        }
        card.addView(text)
        layout.addView(card)
    }

    private fun addMotor(number: Int) {
        val card = createCard()

        val title = TextView(this).apply {
            text = "MOTOR %02d".format(number)
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(20, 100, 50))
        }

        val status = TextView(this).apply {
            text = "STATUS: OFF"
            textSize = 16f
            setTextColor(Color.RED)
            setPadding(0, 10, 0, 10)
        }

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val on = Button(this).apply {
            text = "MOTOR ON"
            setOnClickListener {
                status.text = "STATUS: ON (DEMO)"
                status.setTextColor(Color.rgb(0, 140, 40))
            }
        }

        val off = Button(this).apply {
            text = "MOTOR OFF"
            setOnClickListener {
                status.text = "STATUS: OFF"
                status.setTextColor(Color.RED)
            }
        }

        buttons.addView(on, LinearLayout.LayoutParams(0, -2, 1f))
        buttons.addView(off, LinearLayout.LayoutParams(0, -2, 1f))

        card.addView(title)
        card.addView(status)
        card.addView(buttons)
        layout.addView(card)
    }

    private fun createCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 14, 14, 14)
            setBackgroundColor(Color.WHITE)

            val params = LinearLayout.LayoutParams(
                -1, -2
            )
            params.setMargins(0, 8, 0, 8)
            layoutParams = params
        }
    }
}
