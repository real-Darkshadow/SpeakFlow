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
import com.app.speak.databinding.FragmentLoginBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.MainActivity
import com.app.speak.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    var _binding: FragmentLoginBinding? = null
    val appPrefManager by lazy { AppPrefManager(requireActivity()) }
    private val viewModel: AuthViewModel by activityViewModels()

    val binding get() = _binding!!
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 1
    lateinit var gso: GoogleSignInOptions
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        createRequest()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            googleSignin.setOnClickListener {
                GoogleSignIn()
            }
            registerButton.setOnClickListener {
                findNavController().navigate(R.id.registerFragment)
                findNavController().clearBackStack(R.id.registerFragment)
            }
            signinCard.setOnClickListener {
                emailSignIn()
            }
        }

    }

    private fun emailSignIn() {
        val email = binding.userEmail.text.toString()
        val password = binding.userPassword.text.toString()
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Enter credentials", Toast.LENGTH_LONG).show()
        } else viewModel.emailSignIn(email, password)
    }

    private fun setObservers() {
        viewModel.emailSignInResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.isSuccess) {
                val user = mAuth.currentUser
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


    private fun createRequest() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun GoogleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Suppress
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(
                    requireContext(),
                    "Login Failed: ${e.statusCode}",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("GoogleSignIn", "Sign-in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth.currentUser
                    viewModel.storeDetailFireBase()
                    Log.d("tag", user?.uid.toString())
                    appPrefManager.setUserData(user?.uid.toString(), user?.email.toString())
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireActivity(), "Login Failed: ", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}