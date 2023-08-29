package com.app.speak.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.speak.AnalyticsHelperUtil
import com.app.speak.AudioDownloader
import com.app.speak.R
import com.app.speak.databinding.FragmentDashboardBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.ExtensionFunction.gone
import com.app.speak.ui.ExtensionFunction.isNotNullOrBlank
import com.app.speak.ui.ExtensionFunction.showToast
import com.app.speak.ui.ExtensionFunction.visible
import com.app.speak.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var appPrefManager: AppPrefManager

    @Inject
    lateinit var analyticHelper: AnalyticsHelperUtil
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPrefManager = AppPrefManager(requireContext())
        viewModel.fetchPrompts(appPrefManager.user.uid)
        binding.loading.visible()
        binding.loadAnimation.playAnimation()
        binding.promptHistoryRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        analyticHelper.logEvent(
            "Prompt_History_Viewed", mutableMapOf(
                "email" to appPrefManager.user.email
            )
        )
        setObservers()

    }


    private fun setObservers() {
        viewModel.prompts.observe(viewLifecycleOwner, Observer { promptList ->
            if (!promptList.isNullOrEmpty()) {
                binding.loading.gone()
                binding.loadAnimation.cancelAnimation()
                binding.errorImage.gone()
                binding.promptHistoryRecycler.adapter =
                    PromptHistoryAdapter(promptList, requireContext()) { string, bool ->
                        when (bool) {
                            false -> {
                                viewModel.regeneratePrompt.value = string
                                findNavController().navigate(R.id.navigation_home)
                            }

                            true -> {
                                if (string.isNotNullOrBlank()) AudioDownloader(requireContext()).downloadFile(
                                    string
                                )
                                else showToast("Some Error Occurred")
                            }
                        }

                    }
            } else {
                binding.loading.gone()
                binding.errorImage.visible()
                binding.loadAnimation.cancelAnimation()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}