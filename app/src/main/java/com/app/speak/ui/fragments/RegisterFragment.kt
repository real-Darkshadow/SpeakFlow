package com.app.speak.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.speak.MainActivity
import com.app.speak.R
import com.app.speak.databinding.FragmentRegisterBinding
import com.app.speak.db.AppPrefManager
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


class RegisterFragment : Fragment() {
    var _binding: FragmentRegisterBinding? = null
    val appPrefManager by lazy { AppPrefManager(requireActivity()) }
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
        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "createUserWithEmail:success")
                        val user = mAuth.currentUser
                        appPrefManager.setUserData(userName, user?.email.toString())
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            requireContext(),
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("Tag", e.toString())
        }

    }


}