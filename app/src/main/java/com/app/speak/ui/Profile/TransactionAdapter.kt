package com.app.speak.ui.Profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.speak.R
import com.app.speak.models.TransactionHistory

class TransactionAdapter(private val transactionHistories: List<TransactionHistory>) :
    RecyclerView.Adapter<TransactionAdapter.vh>() {
    inner class vh(view: View) : RecyclerView.ViewHolder(view) {
        val date = view.findViewById<TextView>(R.id.order_date)
        val id = view.findViewById<TextView>(R.id.description)
        val status = view.findViewById<TextView>(R.id.order_status)
        val name = view.findViewById<TextView>(R.id.order_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vh {
        return vh(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.transaction_recycler, parent, false)
        )
    }

    override fun getItemCount(): Int = transactionHistories.size

    override fun onBindViewHolder(holder: vh, position: Int) {
        val data = transactionHistories[position]
        holder.date.text = data.transactionDate
        holder.name.text = data.transactionName
        holder.id.text = data.transactionId
        holder.status.text = data.transactionStatus
    }
}