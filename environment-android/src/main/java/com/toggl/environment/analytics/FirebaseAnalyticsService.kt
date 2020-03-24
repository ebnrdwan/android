package com.toggl.environment.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.toggl.environment.services.analytics.AnalyticsService
import com.toggl.environment.services.analytics.Event
import javax.inject.Inject

class FirebaseAnalyticsService @Inject constructor(context: Context) :
    AnalyticsService {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun track(event: Event) =
        firebaseAnalytics.logEvent(event.name, event.parameters.toBundle())

    private fun Map<String, String>.toBundle(): Bundle = Bundle().let { bundle ->
        this.keys.forEach {
            bundle.putString(it, this[it])
        }
        bundle
    }
}
