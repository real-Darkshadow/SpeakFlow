package com.app.speak.ui.Profile

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
import com.app.speak.databinding.FragmentTransactionsBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.utils.ExtensionFunction.gone
import com.app.speak.ui.utils.ExtensionFunction.visible
import com.app.speak.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var appPrefManager: AppPrefManager

    @Inject
    lateinit var analyticHelper: AnalyticsHelperUtil
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransactionsBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPrefManager = AppPrefManager(requireContext())
        binding.loading.visible()
        binding.loadAnimation.playAnimation()
        viewModel.getTransactions()
        analyticHelper.logEvent(
            "Transaction_History_Viewed", mutableMapOf(
                "email" to appPrefManager.user.email
            )
        )
        setObservers()
        setListeners()
    }

    private fun setListeners() {
        binding.transactionsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.toolbar.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setObservers() {
        viewModel.transactionHistory.observe(viewLifecycleOwner, Observer {
            binding.loading.gone()
            binding.loadAnimation.cancelAnimation()
            if (!it.isEmpty()) {
                binding.errorImage.gone()
                binding.transactionsRecycler.adapter = TransactionAdapter(it)
            } else binding.errorImage.visible()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}