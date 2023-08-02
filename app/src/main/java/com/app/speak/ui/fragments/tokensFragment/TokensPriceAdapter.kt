package com.app.speak.ui.fragments.tokensFragment

import ExtensionFunction.gone
import ExtensionFunction.visible
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R
import com.app.speak.databinding.TokenPriceOptionsBinding
import com.app.speak.models.PlanPrices

class TokensPriceAdapter(
    private val context: Context,
    val planPrices: List<PlanPrices>, val onclick: (String) -> Unit
) :
    RecyclerView.Adapter<TokensPriceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            TokenPriceOptionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return planPrices.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            binding.pricing.text = "$" + planPrices[position].planPrice
            binding.planName.text = planPrices[position].planName
            binding.characterPerMont.text = planPrices[position].characters + "characters per month"
            if (planPrices[position].recommended == true) binding.recommendedChip.visible()
            else binding.recommendedChip.gone()
            binding.monthCard.setOnClickListener {
                onclick(planPrices[position].id)
                val isSelected = true // Set isSelected based on your business logic
                display(isSelected)
            }
        }

    }

    inner class ViewHolder(val binding: TokenPriceOptionsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun display(isSelected: Boolean) {
            binding.monthCard.background =
                if (isSelected)
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.tokens_card_gradient
                    )
                else
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.tokens_card_gradient
                    )
        }
    }
}
