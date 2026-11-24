
package com.aone.kisan365

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Typeface

class MainActivity : Activity() {

    private val motorCount = 7
    private val motorStates = BooleanArray(7)
    private val motorTimers = arrayOfNulls<CountDownTimer>(7)
    private val remainingTimes = LongArray(7)

    private lateinit var mainLayout: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var timerTexts: Array<TextView?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timerTexts = arrayOfNulls(motorCount)

        val scroll = ScrollView(this)
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 22, 18, 22)
            setBackgroundColor(Color.rgb(244, 247, 242))
        }

        scroll.addView(mainLayout)
        setContentView(scroll)

        buildDashboard()
    }

    private fun buildDashboard() {

        val heading = TextView(this).apply {
            text = "🌾 KISAN 365"
            textSize = 29f
            setTextColor(Color.rgb(25, 100, 45))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(8, 15, 8, 20)
        }
        mainLayout.addView(heading)

        val subtitle = TextView(this).apply {
            text = "SMART FARM MOTOR MONITORING"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
        }
        mainLayout.addView(subtitle)

        statusText = TextView(this).apply {
            text = "System Status: DEMO MODE"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(190, 110, 0))
            setPadding(8, 20, 8, 20)
            typeface = Typeface.DEFAULT_BOLD
        }
        mainLayout.addView(statusText)

        addSectionTitle("POWER MONITOR")

        val powerCard = createCard()
        powerCard.addView(infoText("Voltage: Demo data"))
        powerCard.addView(infoText("Current: Demo data"))
        powerCard.addView(infoText("Frequency: Demo data"))
        powerCard.addView(infoText("Power Supply: Not Connected"))
        mainLayout.addView(powerCard)

        addSectionTitle("MOTOR CONTROL - 7 MOTORS")

        for (i in 0 until motorCount) {
            addMotorCard(i)
        }

        addSectionTitle("SYSTEM INFORMATION")

        val info = createCard()
        info.addView(infoText("App Version: 1.0"))
        info.addView(infoText("Device: KISAN 365"))
        info.addView(infoText("Connection: Not Connected"))
        info.addView(infoText("Firebase: Pending Setup"))
        mainLayout.addView(info)

        val footer = TextView(this).apply {
            text = "KISAN 365 | Smart Agriculture"
            gravity = Gravity.CENTER
            textSize = 13f
            setPadding(8, 25, 8, 20)
            setTextColor(Color.DKGRAY)
        }
        mainLayout.addView(footer)
    }

    private fun addMotorCard(index: Int) {

        val card = createCard()

        val title = TextView(this).apply {
            text = "MOTOR ${index + 1}"
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(30, 90, 45))
            setPadding(0, 5, 0, 12)
        }
        card.addView(title)

        val state = TextView(this).apply {
            text = "Status: OFF"
            textSize = 17f
            setTextColor(Color.RED)
        }
        card.addView(state)

        val timerText = TextView(this).apply {
            text = "Timer: Not Set"
            textSize = 15f
            setPadding(0, 8, 0, 8)
        }
        timerTexts[index] = timerText
        card.addView(timerText)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val onButton = Button(this).apply {
            text = "MOTOR ON"
            setOnClickListener {
                motorStates[index] = true
                state.text = "Status: ON (DEMO)"
                state.setTextColor(Color.rgb(0, 140, 40))
                statusText.text = "Motor ${index + 1} ON - DEMO MODE"
            }
        }

        val offButton = Button(this).apply {
            text = "MOTOR OFF"
            setOnClickListener {
                motorStates[index] = false
                stopTimer(index)
                state.text = "Status: OFF"
                state.setTextColor(Color.RED)
                timerText.text = "Timer: Not Set"
                statusText.text = "Motor ${index + 1} OFF - DEMO MODE"
            }
        }

        controls.addView(onButton,
            LinearLayout.LayoutParams(0, -2, 1f))
        controls.addView(offButton,
            LinearLayout.LayoutParams(0, -2, 1f))

        card.addView(controls)

        val timerButton = Button(this).apply {
            text = "SET MOTOR TIMER"
            setOnClickListener {
                showTimerDialog(index)
            }
        }
        card.addView(timerButton)

        mainLayout.addView(card)
    }

    private fun showTimerDialog(index: Int) {

        val input = EditText(this).apply {
            hint = "Timer minutes (e.g. 30)"
            inputType = 2
        }

        AlertDialog.Builder(this)
            .setTitle("Motor ${index + 1} Timer")
            .setMessage("Enter running time in minutes")
            .setView(input)
            .setPositiveButton("START TIMER") { _, _ ->

                val minutes = input.text.toString().toLongOrNull()

                if (minutes == null || minutes <= 0) {
                    Toast.makeText(
                        this,
                        "Enter valid minutes",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    motorStates[index] = true
                    startTimer(index, minutes * 60 * 1000)
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun startTimer(index: Int, duration: Long) {

        stopTimer(index)
        remainingTimes[index] = duration

        motorTimers[index] = object : CountDownTimer(duration, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                remainingTimes[index] = millisUntilFinished

                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished / 1000) % 60

                timerTexts[index]?.text =
                    "Time Remaining: %02d:%02d".format(minutes, seconds)
            }

            override fun onFinish() {
                motorStates[index] = false
                timerTexts[index]?.text = "Timer Finished"
                statusText.text =
                    "Motor ${index + 1} Timer Finished - DEMO"
                Toast.makeText(
                    this@MainActivity,
                    "Motor ${index + 1} timer finished",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.start()
    }

    private fun stopTimer(index: Int) {
        motorTimers[index]?.cancel()
        motorTimers[index] = null
        remainingTimes[index] = 0
    }

    private fun createCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            setBackgroundColor(Color.WHITE)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 10, 0, 10)
            layoutParams = params
        }
    }

    private fun infoText(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(4, 8, 4, 8)
        }
    }

    private fun addSectionTitle(value: String) {
        val title = TextView(this).apply {
            text = value
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(25, 100, 45))
            setPadding(4, 20, 4, 5)
        }
        mainLayout.addView(title)
    }
}
