package com.toggl.environment.analytics

import com.microsoft.appcenter.analytics.Analytics
import com.toggl.environment.services.analytics.AnalyticsService
import com.toggl.environment.services.analytics.Event

class AppCenterAnalyticsService :
    AnalyticsService {
    override fun track(event: Event) =
        Analytics.trackEvent(event.name, event.parameters)
}
