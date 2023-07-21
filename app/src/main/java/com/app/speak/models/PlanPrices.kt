package com.app.speak.models

data class PlanPrices(
    val planName:String,
    val planPrice:String,
    val id:String,
    var isSelected: Boolean = false

)
