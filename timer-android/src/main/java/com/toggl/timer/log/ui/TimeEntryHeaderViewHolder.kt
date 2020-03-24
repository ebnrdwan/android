package com.toggl.timer.log.ui

import android.view.View
import android.widget.TextView
import com.toggl.timer.R
import com.toggl.timer.extensions.formatForDisplaying
import com.toggl.timer.log.domain.DayHeaderViewModel

class TimeEntryHeaderViewHolder(itemView: View) : TimeEntryLogViewHolder(itemView) {
    private val title = itemView.findViewById<TextView>(R.id.title)
    private val duration = itemView.findViewById<TextView>(R.id.duration)

    fun bind(item: DayHeaderViewModel) {
        title.text = item.dayTitle
        duration.text = item.totalDuration.formatForDisplaying()
    }
}
