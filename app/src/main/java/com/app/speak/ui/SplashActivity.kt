package com.app.speak.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.app.speak.R
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.activity.AuthActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var appPref: AppPrefManager
    private lateinit var analytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.white)
        }

        appPref = AppPrefManager(applicationContext)

        Handler(Looper.getMainLooper()).postDelayed({
            checkWhereUserOnboardedOrNot()
        }, 3500)

    }


    private fun checkWhereUserOnboardedOrNot() {
        if (Firebase.auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish()
    }


    override fun onResume() {
        super.onResume()
        analytics = Firebase.analytics
        analytics.logEvent("onboard", null)
    }

    companion object {
        private const val TAG = "SplashActivity"
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}