package com.app.speak.ui.fragments.tokensFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R
import com.app.speak.models.planPrices

class TokensPriceAdapter(val planPrices: List<planPrices>) : RecyclerView.Adapter<TokensPriceAdapter.vh>() {
    inner class vh(view:View):RecyclerView.ViewHolder(view){
        val title=view.findViewById<TextView>(R.id.title)
        val des=view.findViewById<TextView>(R.id.des)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vh {
        return vh(LayoutInflater.from(parent.context).inflate(R.layout.token_price_options,parent,false))
    }

    override fun getItemCount(): Int {
        return planPrices.size
    }

    override fun onBindViewHolder(holder: vh, position: Int) {
        val planPrices=planPrices[position]
        holder.title.text=planPrices.planName
        holder.des.text=planPrices.price

    }
}