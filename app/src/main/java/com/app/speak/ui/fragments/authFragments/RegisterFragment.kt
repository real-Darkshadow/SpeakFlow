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
import com.app.speak.ui.MainActivity
import com.app.speak.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val viewModel: AuthViewModel by activityViewModels()
    private val binding get() = _binding!!
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN: Int = 1
    lateinit var gso: GoogleSignInOptions
    lateinit var mAuth: FirebaseAuth
    private val firebaseAnalytics = Firebase.analytics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        createRequest()
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
            googleSignin.setOnClickListener {
                googleSignIn()
            }
        }

    }

    private fun signIn() {
        val userName = binding.userName.text.toString()
        val email = binding.userEmail.text.toString()
        val password = binding.userPassword.text.toString()
        if (email.isBlank() || password.isBlank() || userName.isBlank()) {
            Toast.makeText(requireContext(), "Enter Full Details", Toast.LENGTH_LONG).show()
        } else viewModel.emailSignUp(email, password)
    }

    private fun setObserver() {
        viewModel.emailSignUpResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.isSuccess) {
                Log.d("TAG", "createUserWithEmail: success")
                val user = mAuth.currentUser
                val name = binding.userName.toString()
                val email = user?.email.toString()
                val uid = user?.uid.toString()
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {}
                viewModel.storeDetailFireBase(name, uid, email)
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
        mGoogleSignInClient =
            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Suppress
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task =
                com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(
                    data
                )
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
                    val name = user?.displayName.toString()
                    val email = user?.email.toString()
                    val uid = user?.uid.toString()
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {}
                    viewModel.storeDetailFireBase(name, uid, email)
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireActivity(), "Login Failed: ", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        val TAG = "tag"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}