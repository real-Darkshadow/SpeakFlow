package com.app.speak.models

data class CrashlyticsCustomLog(
    var exception: Exception? = null,
    var message: String? = null,
    var function: String? = null

)
