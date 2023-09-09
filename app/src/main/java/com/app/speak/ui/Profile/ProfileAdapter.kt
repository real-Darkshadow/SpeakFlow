package com.app.speak.ui.Profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R

class ProfileAdapter(
    private val profileOptionList: Map<Int, Pair<String, String>>,
    val onclick: (Int) -> Unit,
) : RecyclerView.Adapter<ProfileAdapter.vh>() {
    inner class vh(view: View) : RecyclerView.ViewHolder(view) {
        val text = view.findViewById<TextView>(R.id.order_name)
        val field = view.findViewById<ConstraintLayout>(R.id.option_id)
        val option_image = view.findViewById<ImageView>(R.id.option_image)
        val des = view.findViewById<TextView>(R.id.description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vh {
        return vh(
            LayoutInflater.from(parent.context).inflate(R.layout.profile_options, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return profileOptionList.size
    }

    override fun onBindViewHolder(holder: vh, position: Int) {
        val profileOption = profileOptionList[position]
        holder.text.text = profileOption?.first
        holder.field.setOnClickListener { onclick(position) }
        holder.des.text = profileOption?.second
        when (position) {
            0 -> holder.option_image.setBackgroundResource(R.drawable.transaction)
            1 -> holder.option_image.setBackgroundResource(R.drawable.token_black)
            2 -> holder.option_image.setBackgroundResource(R.drawable.share_bl)
            3 -> holder.option_image.setBackgroundResource(R.drawable.terms_condition)
            4 -> holder.option_image.setBackgroundResource(R.drawable.privacy_policy)
            5 -> holder.option_image.setBackgroundResource(R.drawable.delete)

        }
    }
}