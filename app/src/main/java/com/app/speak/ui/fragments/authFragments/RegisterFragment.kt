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
import com.app.speak.databinding.FragmentRegisterBinding
import com.app.speak.ui.utils.ExtensionFunction.gone
import com.app.speak.ui.utils.ExtensionFunction.hideKeyboard
import com.app.speak.ui.utils.ExtensionFunction.isValidEmail
import com.app.speak.ui.utils.ExtensionFunction.logError
import com.app.speak.ui.utils.ExtensionFunction.visible
import com.app.speak.ui.activity.MainActivity
import com.app.speak.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val viewModel: AuthViewModel by activityViewModels()
    private val binding get() = _binding!!
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN: Int = 1
    lateinit var gso: GoogleSignInOptions
    lateinit var mAuth: FirebaseAuth

    @Inject
    lateinit var analyticHelper: AnalyticsHelperUtil
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
        } else if (!email.isValidEmail()) {
            Toast.makeText(requireContext(), "Enter Valid Email", Toast.LENGTH_LONG).show()
        } else {
            binding.loading.visible()
            viewModel.emailSignUp(email, password)
            hideKeyboard()
        }
    }

    private fun setObserver() {
        viewModel.emailSignUpResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.isSuccess) {
                Log.d("TAG", "createUserWithEmail: success")
                val user = mAuth.currentUser
                val name = binding.userName.text.toString()
                val email = user?.email.toString()
                val uid = user?.uid.toString()
                viewModel.storeDetailFireBase(name, uid, email)
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
                analyticHelper.logEvent(
                    "Email_Registration", mutableMapOf(
                        "email" to email,
                        "uid" to uid,
                        "name" to name
                    )
                )
            } else {
                binding.loading.gone()
                val exception = result.exceptionOrNull()
                val errorMessage = exception?.message ?: "Unknown error occurred"
                analyticHelper.logEvent(
                    "Email_Registration_error", mutableMapOf(
                        "email" to binding.userEmail.text.toString(),
                    )
                )
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
            .requestIdToken(BuildConfig.web_client)
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
                binding.loading.visible()
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                binding.loading.gone()
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
                        val isNew=task.result.additionalUserInfo!!.isNewUser
                        if (isNew) viewModel.storeDetailFireBase(name, uid, email)
                        analyticHelper.logEvent(
                            "Google_Registration", mutableMapOf(
                                "email" to email,
                                "uid" to uid,
                                "name" to name
                            )
                        )
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        analyticHelper.logEvent("Google_Registration_error", mutableMapOf())
                        binding.loading.gone()
                        // If sign in fails, display a message to the user.
                        Toast.makeText(requireActivity(), "Login Failed: ", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        } catch (e: Exception) {
            logError {}
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