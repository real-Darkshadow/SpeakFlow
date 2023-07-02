package com.app.speak.ui.fragments.tokensFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.speak.databinding.FragmentAddTokenBinding
import com.app.speak.viewmodel.TokensViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase


class AddTokenFragment : Fragment() {
    private var _binding: FragmentAddTokenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TokensViewModel by activityViewModels()
    private val TAG = "AddToken"
    val db = Firebase.firestore
    val functions = Firebase.functions
    private var interstitialAd: InterstitialAd? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTokenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.priceOptions.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.priceOptions.isNestedScrollingEnabled = false;
        viewModel.userDataListener(FirebaseAuth.getInstance().uid.toString())
        loadInterstitialAd()
        setObservers()
        setListeners()

    }

    override fun onResume() {
        super.onResume()
    }

    private fun setListeners() {
        binding.apply {
            watchAd.setOnClickListener {
                showInterstitialAd()
            }
        }
    }

    private fun setObservers() {

        viewModel.userData.observe(viewLifecycleOwner) { document ->
            viewModel.tokens = document?.get("tokens") as? Long ?: 0L
            binding.availableToken.text = viewModel.tokens.toString()
        }

        viewModel.planPrices.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {

            } else {
                binding.priceOptions.adapter = TokensPriceAdapter(it)
            }
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            "ca-app-pub-2526931847493583/6336229825",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "InterstitialAd was loaded.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError?.message?.let { Log.d(TAG, it) }
                    interstitialAd = null
                }
            }
        )
    }

    private fun showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "InterstitialAd was dismissed.")
                    // Load a new InterstitialAd
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Log.e(TAG, "InterstitialAd failed to show.")
                    loadInterstitialAd()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "InterstitialAd was shown.")
                }
            }
            interstitialAd?.show(requireActivity())
        } else {
            Log.d(TAG, "The InterstitialAd wasn't ready yet.")
            loadInterstitialAd()
        }
    }


}