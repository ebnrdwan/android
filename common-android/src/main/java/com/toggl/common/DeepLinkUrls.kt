package com.toggl.common

import android.content.Context
import android.content.res.Resources
import androidx.core.net.toUri
import androidx.fragment.app.Fragment

class DeepLinkUrls(resources: Resources) {
    val main = resources.getString(R.string.deep_link_main).toUri()
    val timeEntriesLog = resources.getString(R.string.deep_link_time_entries_log).toUri()
    val timeEntriesStartEditDialog = resources.getString(R.string.deep_link_start_edit_dialog).toUri()
    val calendar = resources.getString(R.string.deep_link_calendar).toUri()
    val reports = resources.getString(R.string.deep_link_reports).toUri()
}

val Context.deepLinks: DeepLinkUrls
    get() = DeepLinkUrls(resources)

val Fragment.deepLinks: DeepLinkUrls
    get() = DeepLinkUrls(resources)