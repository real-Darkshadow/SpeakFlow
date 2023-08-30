package com.app.speak.ui.fragments.authFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app.speak.databinding.FragmentForgotPasswordBinding
import com.app.speak.ui.ExtensionFunction.showToast
import com.app.speak.viewmodel.MainViewModel

class ForgotPassword : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPasswordBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        mainViewModel.userForgotPasswordResponse.observe(viewLifecycleOwner) {
            if (it) showToast("Mail Has Sent To Your Mail Id ")
            else showToast("Something Went Wrong")
        }
    }

    private fun setListeners() {
        binding.submitBtn.setOnClickListener {
            val email = binding.userEmail.text.toString()
            if (email.isNotEmpty()) {
                mainViewModel.forgotPassword(email)
            }
        }

    }


}