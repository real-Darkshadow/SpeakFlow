package com.app.speak.ui.activity

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.app.speak.R
import com.app.speak.databinding.ActivityMainBinding
import com.app.speak.databinding.ActivityTokensBinding
import com.app.speak.viewmodel.MainViewModel
import com.app.speak.viewmodel.TokensViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TokensActivity : AppCompatActivity() {
    lateinit var binding:ActivityTokensBinding
    private val viewModel: TokensViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTokensBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.getPrices()
        setListeners()
        setObservers()
    }

    private fun setObservers() {
//
    }

    private fun setListeners() {
//
    }


}