package com.app.speak.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R

class PromptHistoryAdapter() : RecyclerView.Adapter<PromptHistoryAdapter.vh>() {
    inner class vh(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onBindViewHolder(holder: vh, position: Int) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vh {
        return vh(
            LayoutInflater.from(parent.context).inflate(R.layout.history_recycler, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 10
    }


}