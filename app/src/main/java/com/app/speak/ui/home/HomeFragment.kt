package com.app.speak.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.app.speak.databinding.FragmentHomeBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.models.Task
import com.app.speak.viewmodel.MainViewModel
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
    var tokens = 0


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
        val uid = auth.currentUser?.uid.toString()
        viewModel.fetchData(uid)
        setObservers()
        setListeners()
    }

    private fun setListeners() {
        val uid = auth.currentUser?.uid.toString()
        binding.apply {
            generateVoice.setOnClickListener {
                val prompt = binding.promptText.text.toString()
                if (prompt.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Enter Prompt", Toast.LENGTH_LONG).show()
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

    override fun onStop() {
        super.onStop()
        viewModel.taskResult.removeObservers(viewLifecycleOwner)
        viewModel.data.removeObservers(viewLifecycleOwner)
        viewModel.lastTaskId.removeObservers(viewLifecycleOwner)

    }

    @SuppressLint("SetTextI18n")
    private fun setObservers() {
        viewModel.taskResult.observe(viewLifecycleOwner, Observer { data ->
            val status = data?.get("status") as? String
            val userId = data?.get("userId").toString()
            val prompt = data?.get("promptText").toString()
            if (status == "success") {
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
                viewModel.addPrompt(userId, prompt)
                binding.shimmerViewContainer.stopShimmer()
            }
        })
        val user = auth.currentUser
        viewModel.data.observe(viewLifecycleOwner, Observer { document ->
            tokens = document?.getLong("tokens")?.toInt() ?: 0
            val name = document.getString("name") ?: user?.displayName
            binding.tokenValue.text = tokens.toString()
            binding.userName.text = "Hello\n" + name + "."

        })
        viewModel.lastTaskId.observe(viewLifecycleOwner, Observer { taskId ->
            // Use taskId here.
            // For example, display a Toast message:
            Toast.makeText(requireContext(), "Task $taskId added", Toast.LENGTH_SHORT).show()
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}