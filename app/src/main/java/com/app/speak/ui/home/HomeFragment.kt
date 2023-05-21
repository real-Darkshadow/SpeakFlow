package com.app.speak.ui.home

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
import com.app.speak.ui.fragments.authFragments.RegisterFragment.Companion.TAG
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
    val mAuth = Firebase.auth
    val db = Firebase.firestore
    val user = mAuth.currentUser


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            generateVoice.setOnClickListener {
                val prompt = binding.promptEditText.text
                if (prompt.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Enter Prompt", Toast.LENGTH_LONG).show()
                } else {
                    val task = Task(
                        userId = user?.uid.toString(),
                        promptText = "prompt",
                        status = "pending",
                        createdAt = FieldValue.serverTimestamp()
                    )
                    viewModel.addTask(task)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData(appPrefManager.user.uid)
    }

    private fun setObservers() {
        viewModel.data.observe(requireActivity(), Observer { document ->
            val uid = document.getString("uid")
            val tokens = document.getLong("tokens")
            Log.d("tag", tokens.toString())
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

    private fun promptToVoice(prompt: String) {
        val task = hashMapOf(
            "userId" to user?.uid.toString(),
            "promptText" to prompt,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp(),
            "fileUrl" to "",
            "completedAt" to ""
            // other fields
        )

        db.collection("Tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
}