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
import com.app.speak.AnalyticsHelperUtil
import com.app.speak.BuildConfig
import com.app.speak.R
import com.app.speak.databinding.FragmentLoginBinding
import com.app.speak.ui.utils.ExtensionFunction.gone
import com.app.speak.ui.utils.ExtensionFunction.hideKeyboard
import com.app.speak.ui.utils.ExtensionFunction.isValidEmail
import com.app.speak.ui.utils.ExtensionFunction.logError
import com.app.speak.ui.utils.ExtensionFunction.visible
import com.app.speak.ui.activity.MainActivity
import com.app.speak.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {
    var _binding: FragmentLoginBinding? = null
    private val viewModel: AuthViewModel by activityViewModels()
    val firebaseAnalytics = Firebase.analytics
    val binding get() = _binding!!
    val RC_SIGN_IN: Int = 1
    lateinit var gso: GoogleSignInOptions
    lateinit var mAuth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient

    @Inject
    lateinit var analyticHelper: AnalyticsHelperUtil
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
            forgotPassword.setOnClickListener {
                findNavController().navigate(R.id.forgotPassword)
            }
        }

    }

    private fun emailSignIn() {
        val email = binding.userEmail.text.toString()
        val password = binding.userPassword.text.toString()
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(requireContext(), "Enter credentials", Toast.LENGTH_LONG).show()
        } else if (!email.isValidEmail()) {
            Toast.makeText(requireContext(), "Enter Valid Email", Toast.LENGTH_LONG).show()
        } else {
            binding.loading.visible()
            viewModel.emailSignIn(email, password)
            hideKeyboard()
        }
    }

    private fun setObservers() {
        viewModel.emailSignInResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.isSuccess) {
                analyticHelper.logEvent(
                    "Email_Login", mutableMapOf(
                        "email" to binding.userEmail.text.toString(),
                    )
                )
                binding.loading.gone()
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            } else {
                analyticHelper.logEvent(
                    "Email_Login_Error", mutableMapOf(
                        "email" to binding.userEmail.text.toString(),
                    )
                )
                binding.loading.gone()
                val exception = result.exceptionOrNull()
                val errorMessage = exception?.message ?: "Unknown error occurred"
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun createRequest() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.web_client)
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
                binding.loading.visible()
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
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth.currentUser
                        val name = user?.displayName.toString()
                        val email = user?.email.toString()
                        val uid = user?.uid.toString()
                        val isNew=task.getResult().additionalUserInfo!!.isNewUser
                        if (isNew) viewModel.storeDetailFireBase(name, uid, email)
                        analyticHelper.logEvent(
                            "Google_Login", mutableMapOf(
                                "email" to email,
                                "uid" to uid,
                                "name" to name
                            )
                        )
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {}
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        binding.loading.gone()
                        analyticHelper.logEvent("Google_Login_Error", mutableMapOf())
                        // If sign in fails, display a message to the user.
                        Toast.makeText(requireActivity(), "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            logError { }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}