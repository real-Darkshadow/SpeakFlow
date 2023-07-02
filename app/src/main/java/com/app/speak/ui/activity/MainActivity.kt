package com.app.speak.ui.activity

import ExtensionFunction.changeStatusBarColor
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.speak.R
import com.app.speak.databinding.ActivityMainBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.viewmodel.MainViewModel
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        MobileAds.initialize(this) {}
        val uid = FirebaseAuth.getInstance().uid.toString()
        navView.setupWithNavController(navController)
        viewModel.getUserData(uid)
        changeStatusBarColor(color = R.color.white, 0)

    }
}