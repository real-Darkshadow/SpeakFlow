package com.app.speak.ui.fragments.tokensFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.speak.databinding.FragmentAddTokenBinding
import com.app.speak.viewmodel.TokensViewModel


class AddTokenFragment : Fragment() {
    private var _binding:FragmentAddTokenBinding?=null
    private val binding get()=_binding!!

    private val viewmodel:TokensViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTokenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.priceOptions.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.priceOptions.isNestedScrollingEnabled = false;
        setObservers()

    }

    private fun setObservers() {
        viewmodel.planPrices.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()){

            }
            else{
                binding.priceOptions.adapter=TokensPriceAdapter(it)
            }
        })
    }

}