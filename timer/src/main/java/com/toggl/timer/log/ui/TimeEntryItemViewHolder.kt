package com.toggl.timer.log.ui

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.toggl.timer.R
import com.toggl.timer.extensions.formatForDisplaying
import com.toggl.timer.log.domain.FlatTimeEntryViewModel
import com.toggl.timer.log.domain.formatForDisplay

class TimeEntryItemViewHolder(itemView: View, private val onContinueTappedListener: (Long) -> Unit)
    : TimeEntryLogViewHolder(itemView) {
    private val addDescriptionLabel = itemView.findViewById<View>(R.id.add_description_label)
    private val project = itemView.findViewById<TextView>(R.id.project_label)
    private val description = itemView.findViewById<TextView>(R.id.description)
    private val duration = itemView.findViewById<TextView>(R.id.duration)
    private val continueButton = itemView.findViewById<View>(R.id.continue_button)
    private val billableIcon = itemView.findViewById<View>(R.id.billable_icon)

    fun bind(item: FlatTimeEntryViewModel) {

        val hasDescription = item.description.isNotBlank()
        addDescriptionLabel.isVisible = !hasDescription
        description.isVisible = hasDescription
        description.text = item.description

        project.text = item.project?.formatForDisplay()
        project.isVisible = item.project != null

        billableIcon.isVisible = item.billable

        duration.text = item.duration.formatForDisplaying()

        continueButton.setOnClickListener {
            onContinueTappedListener(item.id)
        }
    }
}
