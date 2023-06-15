package com.app.speak.ui.Profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.speak.R
import com.app.speak.databinding.FragmentNotificationsBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.activity.AuthActivity
import com.app.speak.ui.activity.TokensActivity
import com.app.speak.viewmodel.MainViewModel

class NotificationsFragment : Fragment() {
    private val viewModel:MainViewModel by activityViewModels()

    private var _binding: FragmentNotificationsBinding? = null
    val appPrefManager by lazy { AppPrefManager(requireActivity()) }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewModel.userData.observe(viewLifecycleOwner, Observer {
            val name=it?.get("name") as String
            val email=it?.get("email") as String
            binding.userName.text=name
            binding.userEmail.text=email
        })
    }

    private fun setListeners() {
        binding.apply {
            options.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            options.adapter = profile_adapter(viewModel.profileOptionList) { it ->
                when (it) {
                    0 -> findNavController().navigate(R.id.transactionsFragment)
                    1 -> startActivity(Intent(requireContext(), TokensActivity::class.java))
                }
            }
            options.isNestedScrollingEnabled = false;
            logoutUser.setOnClickListener {
                viewModel.uerLogout()
                requireActivity().finish()
                startActivity(Intent(requireActivity(), AuthActivity::class.java))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}