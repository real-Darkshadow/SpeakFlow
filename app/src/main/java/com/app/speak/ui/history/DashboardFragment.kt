package com.app.speak.ui.history

import ExtensionFunction.gone
import ExtensionFunction.visible
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.speak.databinding.FragmentDashboardBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var appPrefManager: AppPrefManager
    //get by limit and order
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
        binding.promptHistoryRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        setObservers()

    }


    private fun setObservers() {
        viewModel.prompts.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.loading.gone()
                binding.promptHistoryRecycler.adapter = PromptHistoryAdapter(it)
            } else {

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}