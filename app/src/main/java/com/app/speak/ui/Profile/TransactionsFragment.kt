package com.app.speak.ui.Profile

import ExtensionFunction.gone
import ExtensionFunction.visible
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.speak.databinding.FragmentTransactionsBinding
import com.app.speak.viewmodel.MainViewModel

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
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
        binding.loading.visible()
        viewModel.getTransactions()
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
            binding.transactionsRecycler.adapter = TransactionAdapter(it)
        })
    }


}