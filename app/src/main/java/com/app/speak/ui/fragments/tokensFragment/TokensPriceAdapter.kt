package com.app.speak.ui.fragments.tokensFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R
import com.app.speak.models.PlanPrices

class TokensPriceAdapter(val planPrices: List<PlanPrices>) : RecyclerView.Adapter<TokensPriceAdapter.ViewHolder>() {
    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.token_price_options, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return planPrices.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val planPrice = planPrices[holder.adapterPosition]

        holder.title.text = planPrice.planName
        holder.des.text = planPrice.planPrice

        holder.layout.setOnClickListener {
            val clickedPosition = holder.adapterPosition
            if (clickedPosition != RecyclerView.NO_POSITION && selectedItem != clickedPosition) {
                if (selectedItem != RecyclerView.NO_POSITION) {
                    planPrices[selectedItem].isSelected = false
                    notifyItemChanged(selectedItem)
                }

                planPrice.isSelected = true
                selectedItem = clickedPosition
                notifyItemChanged(clickedPosition)
            }
        }

        holder.radioButton.isChecked = planPrice.isSelected
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.add_more)
        val des: TextView = itemView.findViewById(R.id.des)
        val layout: LinearLayout = itemView.findViewById(R.id.layout)
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
    }
}
