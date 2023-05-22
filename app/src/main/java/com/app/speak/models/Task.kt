package com.app.speak.models

data class Task(
    val userId: String,
    val promptText: String,
    val status: String,
    val createdAt: Any?,  // Any to support FieldValue.serverTimestamp() and Timestamp
    var fileUrl: String? = "",
    var completedAt: String? = ""
)