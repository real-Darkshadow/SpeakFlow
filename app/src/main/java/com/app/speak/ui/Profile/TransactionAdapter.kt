package com.app.speak.ui.Profile

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.databinding.TransactionRecyclerBinding
import com.app.speak.models.TransactionHistory
import com.app.speak.ui.ExtensionFunction.timestampToDate

class TransactionAdapter(private val transactionHistories: List<TransactionHistory>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: TransactionRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            TransactionRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = transactionHistories.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = transactionHistories[position]
        with(holder) {
            binding.orderDate.text = data.transactionDate.timestampToDate()
            binding.orderName.text = data.transactionName
            binding.description.text = data.transactionId
            binding.status.text = data.transactionStatus
            binding.amount.text = "${if (data.currency == "inr") "₹ " else "$ "}${data.amount}"
        }
    }
}