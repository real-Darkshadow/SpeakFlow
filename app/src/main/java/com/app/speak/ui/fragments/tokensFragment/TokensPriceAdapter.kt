package com.app.speak.ui.fragments.tokensFragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R
import com.app.speak.databinding.TokenPriceOptionsBinding
import com.app.speak.models.PlanPrices
import com.app.speak.ui.ExtensionFunction.gone
import com.app.speak.ui.ExtensionFunction.visible
import kotlin.math.roundToInt

class TokensPriceAdapter(
    private val context: Context,
    val locale: String,
    val planPrices: List<PlanPrices>,
    val onclick: (String) -> Unit
) : RecyclerView.Adapter<TokensPriceAdapter.ViewHolder>() {

    private var selectedPosition = 1// Initially, no item is selected


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
            val isSelected = selectedPosition == adapterPosition
            display(isSelected)

            val priceAfterMultiplication =
                (planPrices[adapterPosition].planPrice.toInt() * 82.2).roundToInt()
            if (locale == "in") {
                binding.pricing.text = "₹${priceAfterMultiplication}"
            } else {
                binding.pricing.text = "$${planPrices[adapterPosition].planPrice.toInt()}"
            }
            binding.planName.text = planPrices[adapterPosition].planName
            binding.characterPerMont.text =
                planPrices[adapterPosition].characters + " characters per month"
            if (planPrices[adapterPosition].recommended == true) binding.recommendedChip.visible()
            else binding.recommendedChip.gone()

            binding.monthCard.setOnClickListener {
                if (selectedPosition != adapterPosition) {
                    val previousSelectedPosition = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(previousSelectedPosition)
                    notifyItemChanged(selectedPosition)
                }
                onclick(planPrices[adapterPosition].id)
            }
        }
    }

    inner class ViewHolder(val binding: TokenPriceOptionsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun display(isSelected: Boolean) {
            val backgroundResId = if (isSelected) {
                R.drawable.token_selected
            } else {
                R.drawable.input_text_background
            }
            val backgroundColor = if (isSelected) {
                ContextCompat.getColor(context, R.color.white)

            } else {
                ContextCompat.getColor(context, R.color.black)
            }
            val recommendedColor =
                ContextCompat.getColor(context, if (isSelected) R.color.black else R.color.white)
            binding.recommendedChipText.setTextColor(recommendedColor)
            binding.recommendedChipLinear.background = ColorDrawable(backgroundColor)

            binding.monthCard.apply {
                background = ContextCompat.getDrawable(context, backgroundResId)
                val textColor = ContextCompat.getColor(
                    context,
                    if (isSelected) R.color.white else R.color.black
                )
                binding.planName.setTextColor(textColor)
                binding.characterPerMont.setTextColor(textColor)
                binding.license.setTextColor(textColor)
                binding.pricing.setTextColor(textColor)
                binding.lastMonth.setTextColor(textColor)
            }

        }
    }
}
