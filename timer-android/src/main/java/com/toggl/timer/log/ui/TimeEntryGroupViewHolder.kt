package com.toggl.timer.log.ui

import android.view.View
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.toggl.timer.R
import com.toggl.timer.extensions.formatForDisplaying
import com.toggl.timer.log.domain.TimeEntryGroupViewModel

class TimeEntryGroupViewHolder(itemView: View, private val onContinueTappedListener: (Long) -> Unit, private val onExpandTappedListener: (List<Long>) -> Unit)
    : TimeEntryLogViewHolder(itemView) {
    private val addDescriptionLabel = itemView.findViewById<View>(R.id.add_description_label)
    private val project = itemView.findViewById<TextView>(R.id.project_label)
    private val description = itemView.findViewById<TextView>(R.id.description)
    private val duration = itemView.findViewById<TextView>(R.id.duration)
    private val groupCount = itemView.findViewById<TextView>(R.id.group_count)
    private val continueButton = itemView.findViewById<View>(R.id.continue_button)

    fun bind(item: TimeEntryGroupViewModel) {

        val hasDescription = item.description.isNotBlank()
        addDescriptionLabel.isVisible = !hasDescription
        description.isVisible = hasDescription
        description.text = item.description
        groupCount.text = "${item.timeEntryIds.size}"

        val itemProject = item.project
        if (itemProject == null) {
            project.isVisible = false
        } else {
            project.isVisible = true
            project.text = itemProject.name
            project.setTextColor(itemProject.color.toColorInt())
        }

        duration.text = item.duration.formatForDisplaying()
        continueButton.setOnClickListener {
            onContinueTappedListener(item.timeEntryIds.first())
        }
        groupCount.setOnClickListener {
            onExpandTappedListener(item.timeEntryIds)
        }
    }
}
