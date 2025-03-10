package com.app.speak.ui.activity

import com.app.speak.ui.utils.ExtensionFunction.changeStatusBarColor
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.speak.R
import com.app.speak.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        changeStatusBarColor(color = R.color.white,0)
    }

    override fun onStart() {
        super.onStart()
    }
}