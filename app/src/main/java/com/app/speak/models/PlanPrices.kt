package com.app.speak.models

data class PlanPrices(
    val planName: String,
    val planPrice: String,
    val id: String,
    val characters: String,
    val recommended: Any? = false
)
