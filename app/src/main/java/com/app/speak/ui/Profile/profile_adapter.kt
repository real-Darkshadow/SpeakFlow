package com.app.speak.ui.Profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R

class profile_adapter(val profileOptionList: Map<Int, String>,val onclick: (Int) -> Unit,) : RecyclerView.Adapter<profile_adapter.vh>() {
    inner class vh(view: View):RecyclerView.ViewHolder(view) {
        val text = view.findViewById<TextView>(R.id.option_name)
        val field = view.findViewById<ConstraintLayout>(R.id.option_id)
        val option_image = view.findViewById<ImageView>(R.id.option_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vh {
        return  vh(LayoutInflater.from(parent.context).inflate(R.layout.profile_options,parent,false))
    }

    override fun getItemCount(): Int {
        return profileOptionList.size
    }

    override fun onBindViewHolder(holder: vh, position: Int) {
        val profileOption = profileOptionList[position]
        holder.text.text = profileOption
        holder.field.setOnClickListener { onclick(position) }
        when (position) {
            0 -> holder.option_image.setBackgroundResource(R.drawable.transaction)
            1 -> holder.option_image.setBackgroundResource(R.drawable.coin)
            2 -> holder.option_image.setBackgroundResource(R.drawable.chat)
            3 -> holder.option_image.setBackgroundResource(R.drawable.terms_and_conditions)
            4 -> holder.option_image.setBackgroundResource(R.drawable.privacy)
            5 -> holder.option_image.setBackgroundResource(R.drawable.app)
            6 -> holder.option_image.setBackgroundResource(R.drawable.delete)
        }
    }
}