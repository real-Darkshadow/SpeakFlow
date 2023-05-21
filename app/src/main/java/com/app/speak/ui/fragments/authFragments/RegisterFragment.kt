package com.app.speak.ui.fragments.authFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.app.speak.R
import com.app.speak.databinding.FragmentRegisterBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.MainActivity
import com.app.speak.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


class RegisterFragment : Fragment() {
    var _binding: FragmentRegisterBinding? = null
    val appPrefManager by lazy { AppPrefManager(requireActivity()) }
    private val viewModel: AuthViewModel by activityViewModels()
    val binding get() = _binding!!
    lateinit var gso: GoogleSignInOptions
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setObserver()
    }

    private fun setListeners() {
        binding.apply {
            signinCard.setOnClickListener {
                signIn()
            }
            loginButton.setOnClickListener {
                findNavController().popBackStack()
                findNavController().clearBackStack(R.id.loginFragment)
            }
        }

    }

    private fun signIn() {
        val userName = binding.userName.text.toString()
        val email = binding.userEmail.text.toString()
        val password = binding.userPassword.text.toString()
        if (email.isNullOrBlank() || password.isNullOrBlank() || userName.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Enter Full Details", Toast.LENGTH_LONG).show()
        } else viewModel.emailSignUp(email, password)
    }

    private fun setObserver() {
        viewModel.emailSignUpResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.isSuccess) {
                Log.d("TAG", "createUserWithEmail: success")
                val user = mAuth.currentUser
                viewModel.storeDetailFireBase()
                appPrefManager.setUserData(user?.uid.toString(), user?.email.toString())
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            } else {
                val exception = result.exceptionOrNull()
                val errorMessage = exception?.message ?: "Unknown error occurred"
                Toast.makeText(
                    requireContext(),
                    errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    companion object {
        val TAG = "tag"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}