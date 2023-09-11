package com.app.speak.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.speak.R
import com.app.speak.databinding.ActivityMainBinding
import com.app.speak.services.NetworkStateReceiver
import com.app.speak.ui.utils.ExtensionFunction.changeStatusBarColor
import com.app.speak.viewmodel.MainViewModel
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val stateReceiver = NetworkStateReceiver()

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
        navView.setupWithNavController(navController)
        changeStatusBarColor(color = R.color.white, 0)

    }

    override fun onResume() {
        super.onResume()
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        viewModel.getUserData(uid)

    }
}