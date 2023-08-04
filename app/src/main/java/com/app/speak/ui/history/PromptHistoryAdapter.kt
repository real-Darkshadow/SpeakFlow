package com.app.speak.ui.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.databinding.HistoryRecyclerBinding
import com.app.speak.models.PromptModel

class PromptHistoryAdapter(
    private val prompts: List<PromptModel>,
    val context: Context,
    val onClick: (String, Boolean) -> Unit
) :
    RecyclerView.Adapter<PromptHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: HistoryRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            HistoryRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prompt = prompts[position].prompt
        val audioUrl = prompts[position].audioUrl
        holder.binding.prompt.text = prompt
        holder.binding.generateVoice.setOnClickListener {
            onClick(prompt, false)
        }
        holder.binding.downloadVoice.setOnClickListener {
            onClick(audioUrl, true)
        }

    }

    override fun getItemCount(): Int {
        return prompts.size
    }
}