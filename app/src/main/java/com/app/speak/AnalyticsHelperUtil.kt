import android.os.Bundle
import com.amplitude.android.Amplitude
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class AnalyticsHelperUtil @Inject constructor(
    private val amplitude: Amplitude,
    private val firebaseAnalytics: FirebaseAnalytics,
) {

    fun logEvent(eventName: String, payload: MutableMap<String, Any>) {

        val bundle = Bundle().apply {
            for ((key, value) in payload) {
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }

        firebaseAnalytics.logEvent(
            eventName,
            bundle
        )

        amplitude.track(
            eventName,
            payload
        )

    }


}