package com.app.speak.ui.Profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R

class profile_adapter(val profileOptionList: Map<Int, String>) : RecyclerView.Adapter<profile_adapter.vh>() {
    inner class vh(view: View):RecyclerView.ViewHolder(view){
        val text=view.findViewById<TextView>(R.id.option_name)
        val field=view.findViewById<LinearLayout>(R.id.option_id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vh {
        return  vh(LayoutInflater.from(parent.context).inflate(R.layout.profile_options,parent,false))
    }

    override fun getItemCount(): Int {
        return profileOptionList.size
    }

    override fun onBindViewHolder(holder: vh, position: Int) {
        holder.text.text=profileOptionList[position]
    }
}