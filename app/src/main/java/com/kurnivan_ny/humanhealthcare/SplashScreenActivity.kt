package com.kurnivan_ny.humanhealthcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.kurnivan_ny.humanhealthcare.R.layout.activity_splash_screen
import com.kurnivan_ny.humanhealthcare.viewPager.OnBoardingActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_splash_screen)

        var handler = Handler()
        handler.postDelayed({
            val intent = Intent(this@SplashScreenActivity,
                OnBoardingActivity::class.java)
            startActivity(intent)
            finish()
        }, 5000)

    }
}