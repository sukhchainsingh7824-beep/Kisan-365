package com.aone.kisan365

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

class SplashActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private val openDashboard = Runnable {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashImage = ImageView(this).apply {
            setImageResource(R.drawable.kisan_splash)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "KISAN 365 Power Monitor"
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        val frame = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.WHITE)
            addView(
                splashImage,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            )
        }
        setContentView(frame)
        handler.postDelayed(openDashboard, 2200L)
    }

    override fun onDestroy() {
        handler.removeCallbacks(openDashboard)
        super.onDestroy()
    }
}
