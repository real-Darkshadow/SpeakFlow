package com.app.speak.ui.Profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.speak.R
import com.app.speak.databinding.FragmentNotificationsBinding
import com.app.speak.db.AppPrefManager
import com.app.speak.ui.activity.AuthActivity
import com.app.speak.ui.activity.TokensActivity
import com.app.speak.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentNotificationsBinding? = null
    val appPrefManager by lazy { AppPrefManager(requireActivity()) }
    private val binding get() = _binding!!
    lateinit var firebaeAuth: FirebaseAuth
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
        firebaeAuth = FirebaseAuth.getInstance()
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewModel.userData.observe(viewLifecycleOwner) {
            val name = it?.get("name").toString()
            val email = it?.get("email").toString()
            binding.userName.text = name
            binding.userEmail.text = email
        }
    }

    private fun setListeners() {
        binding.apply {
            options.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            options.adapter = ProfileAdapter(viewModel.profileOptionList) { it ->
                when (it) {
                    0 -> findNavController().navigate(R.id.transactionsFragment)
                    1 -> startActivity(Intent(requireContext(), TokensActivity::class.java))
                    2 -> shareText(getString(R.string.share_text))
                    3 -> startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://sites.google.com/view/speakflow/terms-of-use")
                        )
                    )

                    4 -> startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://sites.google.com/view/speakflow/privacy-policy")
                        )
                    )

                    5 -> {}
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

    fun shareText(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)

        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}