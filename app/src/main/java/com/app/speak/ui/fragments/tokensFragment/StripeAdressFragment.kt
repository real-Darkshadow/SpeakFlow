package com.app.speak.ui.fragments.tokensFragment

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.app.speak.AnalyticsHelperUtil
import com.app.speak.R
import com.app.speak.databinding.BottomSheetLayoutBinding
import com.app.speak.databinding.FragmentStripeAdressBinding
import com.app.speak.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject


class StripeAdressFragment :   BottomSheetDialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentStripeAdressBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var analyticHelper: AnalyticsHelperUtil
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the bottom sheet
        _binding = FragmentStripeAdressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()

    }

    private fun setListeners() {
        binding.apply {


        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        _binding = null
    }

}