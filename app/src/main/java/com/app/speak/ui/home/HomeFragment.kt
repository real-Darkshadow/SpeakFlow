package com.app.speak.ui.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.app.speak.R
import com.app.speak.databinding.FragmentHomeBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.models.Task
import com.app.speak.viewmodel.MainViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    val appPrefManager by lazy { AppPrefManager(requireActivity()) }
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    var tokens = 0L


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = Firebase.auth
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("tag", "notSignin")
        }
    }

    private var rewardedAd: RewardedAd? = null
    private final var TAG = "MainActivity"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = auth.currentUser?.uid.toString()
        viewModel.fetchData(uid)
        initAd()
        setObservers()
        setListeners()
    }

    fun initAd() {
        var adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            requireContext(),
            "ca-app-pub-2526931847493583/6336229825",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError?.toString()?.let { Log.d(TAG, it) }
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedAd = ad
                }
            })
    }

    fun showAd() {

        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
        rewardedAd?.let { ad ->
            ad.show(requireActivity(), OnUserEarnedRewardListener { rewardItem ->
                // Handle the reward.
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                val db = db.collection("Users").document(auth.uid.toString())
                db.update("tokens", rewardAmount)
                Log.d(TAG, "User earned the reward.")
            })
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
    }

    private fun setListeners() {
        val uid = auth.currentUser?.uid.toString()
        binding.apply {
            generateVoice.setOnClickListener {
                initAd()
                val prompt = binding.promptText.text.toString()
                if (prompt.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Enter Prompt", Toast.LENGTH_LONG).show()
                } else {
                    if (tokens <= 0) {
                        val dialog = Dialog(requireContext())
                        dialog.setContentView(R.layout.tokens_warning)

                        val dialogView = dialog.findViewById<View>(R.id.layoutDialogContainer)
                        val layoutParams = dialogView.layoutParams
                        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                        dialogView.layoutParams = layoutParams

                        val button = dialog.findViewById<Button>(R.id.watchad)
                        button.setOnClickListener {
                            showAd()
                            dialog.dismiss()
                        }

                        dialog.show()

                    } else {
                        val task = Task(
                            userId = uid,
                            promptText = prompt,
                            status = "pending",
                            createdAt = FieldValue.serverTimestamp(),
                            fileUrl = "",
                            completedAt = "",
                        )
                        viewModel.addTask(task)
                        shimmerViewContainer.startShimmer()
                    }
                }
            }
        }
    }



    @SuppressLint("SetTextI18n")
    private fun setObservers() {
        viewModel.taskResult.observe(viewLifecycleOwner, Observer { data ->
            val status = data?.get("status") as? String
            val userId = data?.get("userId").toString()
            val prompt = data?.get("promptText").toString()
            if (status == "success") {
                binding.generateVoice.isClickable = true
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
                viewModel.addPrompt(userId, prompt)
                binding.shimmerViewContainer.stopShimmer()
            }
        })
        val user = auth.currentUser
        viewModel.data.observe(viewLifecycleOwner, Observer { document ->
            tokens = document?.get("tokens") as? Long ?: 0L
            val name = document?.get("name") as? String ?: user?.displayName
            binding.tokenValue.text = tokens.toString()
            binding.userName.text = "Hello\n" + name + "."

        })
        viewModel.lastTaskId.observe(viewLifecycleOwner, Observer { taskId ->
            binding.generateVoice.isClickable = false
            Toast.makeText(requireContext(), "Task $taskId added", Toast.LENGTH_SHORT).show()
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}