package com.app.speak.models

data class TransactionHistory(
    var transactionName: String,
    var transactionDate: String,
    var transactionStatus: String,
    var transactionId: String,
    var amount: Long
)