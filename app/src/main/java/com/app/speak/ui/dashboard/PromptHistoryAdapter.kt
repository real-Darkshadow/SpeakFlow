package com.app.speak.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R

class PromptHistoryAdapter(val prompts: ArrayList<String>) :
    RecyclerView.Adapter<PromptHistoryAdapter.vh>() {
    inner class vh(view: View) : RecyclerView.ViewHolder(view) {
        val prompt = view.findViewById<TextView>(R.id.prompt)
    }

    override fun onBindViewHolder(holder: vh, position: Int) {
        holder.prompt.text = prompts[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vh {
        return vh(
            LayoutInflater.from(parent.context).inflate(R.layout.history_recycler, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return prompts.size
    }


}