package com.app.speak.ui.home

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.media.*
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.app.speak.AnalyticsHelperUtil
import com.app.speak.AudioDownloader
import com.app.speak.BuildConfig
import com.app.speak.R
import com.app.speak.databinding.FragmentHomeBinding
import com.app.speak.databinding.TokensWarningBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.utils.ExtensionFunction.gone
import com.app.speak.ui.utils.ExtensionFunction.isNotNullOrBlank
import com.app.speak.ui.utils.ExtensionFunction.showToast
import com.app.speak.ui.utils.ExtensionFunction.visible
import com.app.speak.viewmodel.MainViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    var tokens = 0L
    lateinit var runnable: Runnable
    private var TAG = "MainFrag"
    private var handler = Handler()
    private lateinit var uid: String
    private lateinit var audioDownloader: AudioDownloader

    private var interstitialAd: InterstitialAd? = null

    @Inject
    lateinit var analyticHelper: AnalyticsHelperUtil
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = Firebase.auth
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        loadInterstitialAd()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uid = auth.currentUser?.uid.toString()
        MobileAds.initialize(requireContext()) {}
        audioDownloader = AudioDownloader(requireContext())
        binding.background.gone()
        binding.loading.visible()
        binding.loadAnimation.playAnimation()
        viewModel.getVoices()

        analyticHelper.logEvent(
            "Home_Viewed", mutableMapOf(
                "uid" to uid
            )
        )
        loadInterstitialAd()
        setObservers()
        setListeners()
    }



    private fun setListeners() {
        binding.apply {
            generateVoice.setOnClickListener {
                val prompt = binding.userPrompt.text.toString()
                if (prompt.isBlank()) {
                    Toast.makeText(requireContext(), "Enter Prompt", Toast.LENGTH_LONG).show()
                } else {
                    if (tokens <= 0) {
                        showCustomDialog(requireContext())
                    } else if (tokens < prompt.length) {
                        Toast.makeText(requireContext(), "Not enough Tokens", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        analyticHelper.logEvent(
                            "Voice_Generate_button", mutableMapOf(
                                "uid" to uid,
                                "voiceId" to viewModel.selectedVoiceId
                            )
                        )
                        val data = hashMapOf(
                            "prompt" to prompt,
                            "uid" to AppPrefManager(requireContext()).user.uid,
                            "status" to "processing",
                            "voiceId" to viewModel.selectedVoiceId
                        )
                        if (tokens < 60) {
                            showInterstitialAd()
                        }
                        binding.loadAnimation.playAnimation()
                        binding.loading.visible()
                        generateVoice.isClickable = false
                        viewModel.createNewProcess(data)

                    }
                }
            }
            userPrompt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    binding.characters.text = p0?.length.toString()
                }

                override fun afterTextChanged(p0: Editable?) {
                    viewModel.imageText.value = ""
                }
            })
            downloadButton.setOnClickListener {
                analyticHelper.logEvent(
                    "Voice_Download_button", mutableMapOf(
                        "uid" to uid,
                    )
                )
                if (tokens <= 60) {
                    showInterstitialAd()
                }

                audioDownloader.downloadFile(viewModel.audioLink)
            }

        }

        binding.uploadPhoto.setOnClickListener {
            pick()
        }
        binding.extraSettings.setOnClickListener {
            val bottomSheetFragment = VoiceTuneFragment()
            bottomSheetFragment.show(requireActivity().supportFragmentManager, "myBottomSheet")
        }
        // Set click listener for the play button
        binding.playButton.setOnClickListener {
            viewModel.togglePlayback()
        }


        // Set SeekBar listener
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }


    @SuppressLint("SetTextI18n")
    private fun setObservers() {
        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if (!isPlaying) {
                binding.loading.gone()
                binding.loadAnimation.cancelAnimation()
            }
            binding.playButton.setImageResource(
                if (isPlaying) R.drawable.baseline_pause_24
                else R.drawable.baseline_play_arrow_24
            )
        }
        lifecycleScope.launch {
            while (true) {
                viewModel.getCurrentPosition().let { position ->
                    viewModel.getDuration().let { duration ->
                        binding.seekbar.max = duration
                        binding.seekbar.progress = position
                    }
                }
                delay(100)
            }
        }
        viewModel.currentProgress.observe(viewLifecycleOwner) { progress ->
            binding.seekbar.progress = progress
        }

        viewModel.regeneratePrompt.observe(viewLifecycleOwner) {
            if (it.isNotNullOrBlank()) {
                binding.userPrompt.setText(it)
                viewModel.regeneratePrompt.value = ""
            }
        }
        viewModel.taskResult.observe(viewLifecycleOwner) { data ->
            when (data?.get("status").toString()) {
                "success" -> {
                    viewModel.taskListenerDocRef.remove()
                    analyticHelper.logEvent(
                        "Voice_Generate_Success", mutableMapOf(
                            "uid" to uid,
                        )
                    )
                    viewModel.getUserData(uid)
                    viewModel.audioName = data?.get("prompt").toString()
                    viewModel.audioLink = data?.get("signedUrl").toString()
                    if (viewModel.audioLink.isNotNullOrBlank()) {
                        val layoutParams =
                            binding.uploadPhoto.layoutParams as CoordinatorLayout.LayoutParams
                        layoutParams.anchorId = binding.playerLayout.id
                        binding.uploadPhoto.layoutParams = layoutParams
                        viewModel.initializeMediaPlayer(viewModel.audioLink)
                        binding.audioName.text = viewModel.audioName
                        binding.playerLayout.visible()
                    } else showToast("An Error Occurred\n Please Contact Support")
                    viewModel.taskResult.value = null
                    binding.generateVoice.isClickable = true
                }

                "error" -> {
                    viewModel.taskListenerDocRef.remove()
                    analyticHelper.logEvent(
                        "Voice_Generate_Error", mutableMapOf(
                            "uid" to uid,
                        )
                    )
                    viewModel.getUserData(uid)
                    binding.loading.gone()
                    binding.loadAnimation.cancelAnimation()
                    viewModel.taskResult.value = null
                    binding.generateVoice.isClickable = true
                    showToast("An Error Occurred\n Please Try Again")
                }
            }
        }

        val user = auth.currentUser
        viewModel.userData.observe(viewLifecycleOwner) { document ->
            binding.loading.gone()
            binding.loadAnimation.cancelAnimation()
            binding.background.visible()
            tokens = document?.get("tokens") as? Long ?: 0L
            val name = document?.get("name") as? String ?: user?.displayName
            binding.tokenValue.text = tokens.toString()
            binding.userName.text = "Hello\n$name."
        }
        viewModel.lastTaskId.observe(viewLifecycleOwner) { taskId ->
            viewModel.taskListener()
        }
        viewModel.imageText.observe(viewLifecycleOwner) {
            if (it.isNotNullOrBlank()) {
                binding.userPrompt.setText(it)
            }
        }
        viewModel.voicesList.observe(viewLifecycleOwner) {
            val voices = it ?: ArrayList()
            val voicesNames = it?.map { it.name } ?: emptyList()
            val voiceAdapter = ArrayAdapter(
                requireContext(), android.R.layout
                    .simple_spinner_item, voicesNames
            )
            voiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.apply {
                voiceSpinner.adapter = voiceAdapter
                voiceSpinner.visibility = View.VISIBLE
                voiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        viewModel.selectedVoiceId = voices[position].id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        viewModel.selectedVoiceId = voices[0].id
                    }


                }
            }
        }
    }


    private fun pick() {
        ImagePicker.with(this).galleryOnly()
            .compress(1024)         //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            )  //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    val image = InputImage.fromFilePath(requireContext(), fileUri)
                    viewModel.codeFromUri(image)
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }



    private fun showCustomDialog(context: Context) {
        // Inflate the dialog layout using ViewBinding
        val dialogBinding = TokensWarningBinding.inflate(LayoutInflater.from(context))

//        // Customize the dialog title and message (optional)
//        dialogBinding.dialogTitle.text = "Custom Dialog Title"
//        dialogBinding.dialogMessage.text = "This is a custom dialog example."

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogBinding.root)
            .setPositiveButton("OK") { dialog, _ ->
                // Do something when the "OK" button is clicked
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Do something when the "Cancel" button is clicked
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            BuildConfig.mob_id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "InterstitialAd was loaded.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.message.let { Log.d(TAG, it) }
                    interstitialAd = null
                }
            }
        )
    }

    private fun showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    loadInterstitialAd()
                }

                override fun onAdShowedFullScreenContent() {
                    loadInterstitialAd()
                }
            }
            interstitialAd?.show(requireActivity())
        } else {
            Log.d(TAG, "The InterstitialAd wasn't ready yet.")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}