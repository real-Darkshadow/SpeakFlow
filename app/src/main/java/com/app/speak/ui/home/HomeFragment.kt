package com.app.speak.ui.home

import ExtensionFunction.isNotNullOrBlank
import ExtensionFunction.showToast
import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.media.*
import android.os.*
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app.speak.R
import com.app.speak.databinding.FragmentHomeBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.activity.TokensActivity
import com.app.speak.viewmodel.MainViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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


    private var interstitialAd: InterstitialAd? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uid = auth.currentUser?.uid.toString()
        MobileAds.initialize(requireContext()) {}
        viewModel.getVoices()
        loadInterstitialAd()
        setObservers()
        setListeners()
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
                    Log.d(TAG, "InterstitialAd was dismissed.")
                    // Load a new InterstitialAd
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Log.e(TAG, "InterstitialAd failed to show.")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "InterstitialAd was shown.")
                }
            }
            interstitialAd?.show(requireActivity())
        } else {
            Log.d(TAG, "The InterstitialAd wasn't ready yet.")
        }
    }

    private fun setListeners() {
        binding.apply {
            generateVoice.setOnClickListener {
                val prompt = binding.userPrompt.text.toString()
                if (prompt.isBlank()) {
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
                            showInterstitialAd()
                            dialog.dismiss()
                        }

                        dialog.show()

                    } else if (tokens < prompt.length) {
                        Toast.makeText(requireContext(), "Not enough Tokens", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val data = hashMapOf(
                            "prompt" to prompt,
                            "uid" to AppPrefManager(requireContext()).user.uid,
                            "status" to "processing"
                        )
                        viewModel.createNewProcess(data)
                    }
                }
            }
            seekbar.progress = 0

            val player = MediaPlayer()
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);


// Set the media item to be played.
            GlobalScope.launch(Dispatchers.IO){
                player.setDataSource("https://webaudioapi.com/samples/audio-tag/chrono.mp3")
                player.prepare()
            }
// Prepare the player.

            seekbar.max = player.duration
// Start the playback.
            play.setOnClickListener {
                if (!player.isPlaying) {
                    player.start()
                    binding.play.setImageResource(R.drawable.baseline_pause_24)
                } else {
                    player.pause()
                    binding.play.setImageResource(R.drawable.baseline_play_arrow_24)
                }
            }

            seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        player.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
            runnable = Runnable {
                seekbar.progress = player.currentPosition
                handler.postDelayed(runnable, 1000)
            }
            handler.postDelayed(runnable, 1000)

            player.setOnCompletionListener {
                play.setImageResource(R.drawable.baseline_play_arrow_24)
                seekbar.progress = 0
            }


        }
        binding.addMore.setOnClickListener {
            startActivity(Intent(requireContext(), TokensActivity::class.java))
        }
        binding.uploadPhoto.setOnClickListener {
            pick()
        }
        binding.extraSettings.setOnClickListener {
            val bottomSheetFragment = MyBottomSheetFragment()
            bottomSheetFragment.show(requireActivity().supportFragmentManager, "myBottomSheet")
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setObservers() {
        viewModel.taskResult.observe(viewLifecycleOwner) { data ->
            val status = data?.get("status") as? String
            if (status == "success") {
                binding.generateVoice.isClickable = true
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
            }
        }
        val user = auth.currentUser
        viewModel.userData.observe(viewLifecycleOwner) { document ->
            tokens = document?.get("tokens") as? Long ?: 0L
            val name = document?.get("name") as? String ?: user?.displayName
            binding.tokenValue.text = tokens.toString()
            binding.userName.text = "Hello\n$name."

        }
        viewModel.lastTaskId.observe(viewLifecycleOwner) { taskId ->
            binding.generateVoice.isClickable = false
            Toast.makeText(requireContext(), "Task $taskId added", Toast.LENGTH_SHORT).show()
        }
        viewModel.imageText.observe(viewLifecycleOwner) {
            val userPrompt = binding.userPrompt.text.toString()
            if (it.isNotNullOrBlank() && userPrompt.isEmpty()) {
                binding.userPrompt.setText(it)
            }
        }
        viewModel.voicesList.observe(viewLifecycleOwner) {
            val voices = it?:ArrayList()
            val voicesNames = it?.map { it.name }?: emptyList()
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
                       // Handle the case where no item is selected
                   }


               }
           }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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




}