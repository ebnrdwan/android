package com.toggl.timer.log.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.toggl.timer.R
import com.toggl.timer.log.domain.DayHeaderViewModel
import com.toggl.timer.log.domain.FlatTimeEntryViewModel
import com.toggl.timer.log.domain.TimeEntryGroupViewModel
import com.toggl.timer.log.domain.TimeEntryViewModel
import java.lang.IllegalStateException

class TimeEntriesLogAdapter(
    private val onContinueTappedListener: (Long) -> Unit = {},
    private val onExpandTappedListener: (List<Long>) -> Unit = {}
) : ListAdapter<TimeEntryViewModel, TimeEntryLogViewHolder>(diffCallback) {

    private val dayHeaderViewType = 0
    private val flatTimeEntryViewType = 1
    private val timeEntryGroupViewType = 2

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is DayHeaderViewModel -> dayHeaderViewType
            is FlatTimeEntryViewModel -> flatTimeEntryViewType
            is TimeEntryGroupViewModel -> timeEntryGroupViewType
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeEntryLogViewHolder {
        val layoutId = when (viewType) {
            dayHeaderViewType -> R.layout.time_entries_log_header
            flatTimeEntryViewType -> R.layout.time_entries_log_item
            timeEntryGroupViewType -> R.layout.time_entries_group_item
            else -> throw IllegalStateException()
        }

        return LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
            .let { createViewHolder(viewType, it) }
    }

    override fun onBindViewHolder(holder: TimeEntryLogViewHolder, position: Int) {
        when (holder) {
            is TimeEntryItemViewHolder -> holder.bind(getItem(position) as FlatTimeEntryViewModel)
            is TimeEntryHeaderViewHolder -> holder.bind(getItem(position) as DayHeaderViewModel)
            is TimeEntryGroupViewHolder -> holder.bind(getItem(position) as TimeEntryGroupViewModel)
        }
    }

    private fun createViewHolder(viewType: Int, itemView: View): TimeEntryLogViewHolder =
        when (viewType) {
            dayHeaderViewType -> TimeEntryHeaderViewHolder(itemView)
            flatTimeEntryViewType -> TimeEntryItemViewHolder(itemView, onContinueTappedListener)
            timeEntryGroupViewType -> TimeEntryGroupViewHolder(itemView, onContinueTappedListener, onExpandTappedListener)
            else -> throw IllegalStateException()
        }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<TimeEntryViewModel>() {
            override fun areItemsTheSame(
                oldItem: TimeEntryViewModel,
                newItem: TimeEntryViewModel
            ): Boolean =
                when (oldItem) {
                    is FlatTimeEntryViewModel ->
                        newItem is FlatTimeEntryViewModel && oldItem.id == newItem.id
                    is DayHeaderViewModel ->
                        newItem is DayHeaderViewModel && oldItem.dayTitle == newItem.dayTitle
                    is TimeEntryGroupViewModel ->
                        newItem is TimeEntryGroupViewModel && oldItem.timeEntryIds == newItem.timeEntryIds
                }

            override fun areContentsTheSame(
                oldItem: TimeEntryViewModel,
                newItem: TimeEntryViewModel
            ): Boolean =
                when (oldItem) {
                    is FlatTimeEntryViewModel ->
                        newItem is FlatTimeEntryViewModel && oldItem == newItem
                    is DayHeaderViewModel ->
                        newItem is DayHeaderViewModel && oldItem == newItem
                    is TimeEntryGroupViewModel ->
                        newItem is TimeEntryGroupViewModel && oldItem == newItem
                }
        }
    }
}
