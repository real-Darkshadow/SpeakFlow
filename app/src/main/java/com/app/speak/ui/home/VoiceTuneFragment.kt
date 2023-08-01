package com.app.speak.ui.home

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import com.app.speak.R
import com.app.speak.databinding.BottomSheetLayoutBinding
import com.app.speak.databinding.FragmentHomeBinding
import com.app.speak.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoiceTuneFragment : BottomSheetDialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: BottomSheetLayoutBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the bottom sheet
        _binding = BottomSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()

    }

    private fun setListeners() {
        binding.apply {
            claritySeekbar.max = 100
            claritySeekbar.progress = viewModel.clarityPercentage
            stabilitySeekbar.max = 100
            stabilitySeekbar.progress = viewModel.stabilityPercentage
            applySettings.setOnClickListener {
                viewModel.clarityPercentage = claritySeekbar.progress
                viewModel.stabilityPercentage = stabilitySeekbar.progress
                dismiss()
            }

        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

}
