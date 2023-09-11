package com.app.speak.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.speak.R
import com.app.speak.databinding.ActivityTokensBinding
import com.app.speak.services.NetworkStateReceiver
import com.app.speak.ui.utils.ExtensionFunction.changeStatusBarColor
import com.app.speak.viewmodel.TokensViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TokensActivity : AppCompatActivity() {
    private val stateReceiver = NetworkStateReceiver()
    lateinit var binding: ActivityTokensBinding
    private val viewModel: TokensViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTokensBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor(color = R.color.white,0)
    }



}