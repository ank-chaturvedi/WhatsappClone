package com.basics.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.basics.whatsappclone.auth.LoginActivity


class SplashActivity : AppCompatActivity() {


    private val SPLASH_SCREEN_TIME_OUT=2000L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        Handler().postDelayed({
            val i = Intent(
                this,
                LoginActivity::class.java
            )
            startActivity(i)
            finish()

        }, SPLASH_SCREEN_TIME_OUT)


    }
}
