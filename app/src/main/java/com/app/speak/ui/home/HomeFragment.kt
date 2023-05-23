package com.app.speak.ui.home

import android.os.Bundle
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
    val user = Firebase.auth.currentUser
    val uid = user?.uid.toString()
    var tokens = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            generateVoice.setOnClickListener {
                val prompt = binding.promptText.text.toString()
                if (prompt.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Enter Prompt", Toast.LENGTH_LONG).show()
                } else {
                    val task = Task(
                        userId = user?.uid.toString(),
                        promptText = prompt,
                        status = "pending",
                        createdAt = FieldValue.serverTimestamp(),
                        fileUrl = "",
                        completedAt = "",
                    )
                    viewModel.addTask(task)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData(uid)
    }

    private fun setObservers() {
        viewModel.data.observe(requireActivity(), Observer { document ->
            tokens = document.getLong("tokens")?.toInt() ?: 0
            binding.tokenValue.text = tokens.toString()
        })
        viewModel.lastTaskId.observe(requireActivity(), Observer { taskId ->
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