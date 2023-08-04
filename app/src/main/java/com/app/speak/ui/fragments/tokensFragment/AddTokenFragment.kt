package com.app.speak.ui.fragments.tokensFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.app.speak.AnalyticsHelperUtil
import com.app.speak.R
import com.app.speak.databinding.FragmentAddTokenBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.ExtensionFunction.getLocale
import com.app.speak.ui.ExtensionFunction.gone
import com.app.speak.ui.ExtensionFunction.showToast
import com.app.speak.ui.ExtensionFunction.visible
import com.app.speak.ui.utils.GridItemDecoration
import com.app.speak.viewmodel.TokensViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddTokenFragment : Fragment() {
    private var _binding: FragmentAddTokenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TokensViewModel by activityViewModels()
    private val TAG = "AddToken"
    val db = Firebase.firestore
    val functions = Firebase.functions
    private var interstitialAd: InterstitialAd? = null
    lateinit var paymentSheet: PaymentSheet
    lateinit var paymentIntentClientSecret: String
    lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    lateinit var appPrefManager: AppPrefManager
    private lateinit var customerId: String
    private lateinit var ephemeralKeySecret: String

    @Inject
    lateinit var analyticHelper: AnalyticsHelperUtil
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
        binding.priceOptions.addItemDecoration(
            GridItemDecoration(
                resources.getDimensionPixelSize(
                    R.dimen.plan_spacing
                ),
                true
            )
        )
        binding.priceOptions.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.priceOptions.isNestedScrollingEnabled = false;
        appPrefManager = AppPrefManager(requireContext())
        viewModel.getUserData(appPrefManager.user.uid)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        analyticHelper.logEvent(
            "Add_Token_Viewed", mutableMapOf(
                "email" to appPrefManager.user.email
            )
        )
        viewModel.getPrices()
        binding.background.gone()
        binding.loading.visible()
        setObservers()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        loadInterstitialAd()
    }

    private fun setListeners() {
        binding.apply {
            watchAd.setOnClickListener {
                analyticHelper.logEvent(
                    "Ad_button_clicked", mutableMapOf(
                        "email" to appPrefManager.user.email,
                        "uid" to appPrefManager.user.uid,
                    )
                )
                showInterstitialAd()
            }
            checkoutBtn.setOnClickListener {
                binding.loading.visible()
                binding.background.isClickable = false
                val data = hashMapOf(
                    "uid" to appPrefManager.user.uid,
                    "currency" to if (getLocale() == "in") "inr" else "usd",
                    "planId" to viewModel.selectedPlan,
                    "name" to appPrefManager.user.name,
                    "email" to appPrefManager.user.email,
                )
                stripePayment(data)
            }
            backButton.setOnClickListener {
                requireActivity().finish()
            }
        }
    }

    private fun setObservers() {

        viewModel.userData.observe(viewLifecycleOwner) { document ->
            viewModel.tokens = document?.get("tokens") as? Long ?: 0L
            binding.availableToken.text = viewModel.tokens.toString()
        }

        viewModel.planPrices.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.loading.gone()
                binding.background.visible()
                val locale = getLocale()
                binding.priceOptions.adapter =
                    TokensPriceAdapter(requireContext(), locale, it) { plan ->
                        viewModel.selectedPlan = plan
                    }
            }
        }
        viewModel.stripeCheckoutResult.observe(viewLifecycleOwner) {
            binding.loading.gone()
            binding.background.isClickable = true
            it?.let { stripe ->
                customerId = stripe.customer
                paymentIntentClientSecret = stripe.paymentIntent
                ephemeralKeySecret = stripe.ephemeralKey
//                viewModel.transactionId = stripe.orderId
                customerConfig = PaymentSheet.CustomerConfiguration(customerId, ephemeralKeySecret)
                PaymentConfiguration.init(
                    requireContext(),
                    stripe.publishableKey
                )
                presentPaymentSheet()
            }
        }
    }


    private fun stripePayment(data: HashMap<String, String>) {
        try {
            viewModel.stripeCheckout(data)
        } catch (e: Exception) {
            Log.e(TAG, "stripePayment: ${e.message}")
        }
    }

    private fun presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "SpeakFlow",
                customer = customerConfig,
                allowsDelayedPaymentMethods = true
            )
        )
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                analyticHelper.logEvent(
                    "Payment_Sheet_Cancelled", mutableMapOf(
                        "email" to appPrefManager.user.email,
                        "plan" to viewModel.selectedPlan
                    )
                )
                showToast("Canceled")
            }

            is PaymentSheetResult.Failed -> {
                analyticHelper.logEvent(
                    "Token_Purchase_Failed", mutableMapOf(
                        "email" to appPrefManager.user.email,
                        "plan" to viewModel.selectedPlan,
                        "uid" to appPrefManager.user.uid,
                        "currency" to getLocale()
                    )
                )
                showToast("Error: ${paymentSheetResult.error}")
            }

            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                analyticHelper.logEvent(
                    "Token_purchased", mutableMapOf(
                        "email" to appPrefManager.user.email,
                        "plan" to viewModel.selectedPlan,
                        "uid" to appPrefManager.user.uid,
                        "currency" to getLocale()
                    )
                )
                viewModel.getUserData(appPrefManager.user.uid)
                showToast("Completed")
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
                    analyticHelper.logEvent(
                        "Ad_Shown", mutableMapOf(
                            "email" to appPrefManager.user.email,
                            "uid" to appPrefManager.user.uid,
                        )
                    )
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