package com.toggl.timer.startedit.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toggl.common.Constants.elapsedTimeIndicatorUpdateDelayMs
import com.toggl.common.addInterceptingOnClickListener
import com.toggl.common.performClickHapticFeedback
import com.toggl.common.setSafeText
import com.toggl.common.sheet.AlphaSlideAction
import com.toggl.common.sheet.BottomSheetCallback
import com.toggl.environment.services.time.TimeService
import com.toggl.models.domain.Workspace
import com.toggl.models.domain.WorkspaceFeature
import com.toggl.timer.R
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.di.TimerComponentProvider
import com.toggl.timer.extensions.formatForDisplaying
import com.toggl.timer.extensions.formatForDisplayingDate
import com.toggl.timer.extensions.formatForDisplayingTime
import com.toggl.timer.startedit.domain.DateTimePickMode
import com.toggl.timer.startedit.domain.StartEditAction
import com.toggl.timer.startedit.domain.StartEditState
import kotlinx.android.synthetic.main.bottom_control_panel_layout.*
import kotlinx.android.synthetic.main.fragment_dialog_start_edit.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import java.time.Duration
import java.time.OffsetDateTime
import javax.inject.Inject
import com.toggl.common.R as CommonR

class StartEditDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var timeService: TimeService
    private var timeIndicatorScheduledUpdate: Job? = null

    private val store: StartEditStoreViewModel by viewModels { viewModelFactory }

    private var editDialog: Dialog? = null
    private val dispatchingCancelListener: DialogInterface.OnCancelListener = DialogInterface.OnCancelListener {
        store.dispatch(StartEditAction.DateTimePickingCancelled)
    }

    private val bottomSheetCallback = BottomSheetCallback()

    private lateinit var bottomControlPanelAnimator: BottomControlPanelAnimator
    private lateinit var hideableStopViews: List<View>
    private lateinit var extentedTimeOptions: List<View>
    private lateinit var billableOptions: List<View>

    override fun onAttach(context: Context) {
        (requireActivity().applicationContext as TimerComponentProvider)
            .provideTimerComponent().inject(this)
        super.onAttach(context)

        val activeButtonColor = ContextCompat.getColor(context, CommonR.color.button_active)
        val inactiveButtonColor = ContextCompat.getColor(context, CommonR.color.button_inactive)
        bottomControlPanelAnimator = BottomControlPanelAnimator(activeButtonColor, inactiveButtonColor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_dialog_start_edit, container, false)

    @ExperimentalCoroutinesApi
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).also { bottomSheetDialog: BottomSheetDialog ->
            store.state
                .filter { it.editableTimeEntry != null }
                .map { BottomControlPanelParams(it.editableTimeEntry!!, it.isEditableInProWorkspace()) }
                .take(1)
                .onEach {
                    bottomSheetDialog.setOnShowListener { dialogInterface ->
                        dialogInterface.attachBottomView(bottomSheetDialog, R.layout.bottom_control_panel_layout, it)
                    }
                }
                .launchIn(lifecycleScope)
        }
    }

    @kotlinx.coroutines.FlowPreview
    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideableStopViews = listOf(stop_divider, stop_date_label)
        extentedTimeOptions = listOf(
            start_header,
            start_time_label,
            start_divider,
            start_date_label,
            stop_header,
            stop_time_label,
            stop_divider,
            stop_date_label,
            wheel_placeholder
        )
        billableOptions = listOf(billable_chip, billable_divider)

        extentedTimeOptions
            .forEach { bottomSheetCallback.addOnSlideAction(AlphaSlideAction(it, false)) }
        billableOptions
            .forEach { bottomSheetCallback.addOnSlideAction(AlphaSlideAction(it, false)) }

        store.state
            .filterNot { it.editableTimeEntry == null }
            .mapNotNull { it.editableTimeEntry?.description }
            .onEach { time_entry_description.setSafeText(it) }
            .launchIn(lifecycleScope)

        store.state
            .mapNotNull { it.editableTimeEntry }
            .distinctUntilChanged { old, new -> old.ids == new.ids && old.startTime == new.startTime }
            .onEach {
                scheduleTimeEntryIndicatorAndLabelUpdate(it)
                handleStartStopElementsState(it)
            }
            .launchIn(lifecycleScope)

        store.state
            .filter { it.editableTimeEntry != null }
            .distinctUntilChanged { old, new -> old.dateTimePickMode == new.dateTimePickMode }
            .onEach {
                startEditingTimeDate(it.dateTimePickMode, it.editableTimeEntry!!)
            }
            .launchIn(lifecycleScope)

        store.state
            .map { it.isEditableInProWorkspace() }
            .distinctUntilChanged()
            .onEach { shouldBillableOptionsShow -> billableOptions.forEach { it.isVisible = shouldBillableOptionsShow } }
            .launchIn(lifecycleScope)

        store.state
            .map { it.editableTimeEntry?.billable ?: false }
            .distinctUntilChanged()
            .onEach { billable_chip.isChecked = it }
            .launchIn(lifecycleScope)

        time_entry_description
            .onDescriptionChanged
            .distinctUntilChanged()
            .onEach { store.dispatch(it) }
            .launchIn(lifecycleScope)

        lifecycleScope.launchWhenStarted {
            store.state
                .filter { it.editableTimeEntry == null }
                .distinctUntilChanged()
                .onEach {
                    if (dialog?.isShowing == true) {
                        findNavController().popBackStack()
                    }
                }
                .collect()
        }

        billable_chip.addInterceptingOnClickListener {
            store.dispatch(StartEditAction.BillableTapped)
        }

        val bottomSheetBehavior = (dialog as BottomSheetDialog).behavior
        with(bottomSheetBehavior) {
            addBottomSheetCallback(bottomSheetCallback)
            skipCollapsed = false
            peekHeight = resources.getDimension(R.dimen.time_entry_edit_half_expanded_height).toInt()
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        with(time_entry_description) {
            requestFocus {
                activity?.getSystemService<InputMethodManager>()
                    ?.showSoftInput(time_entry_description, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        close_action.setOnClickListener {
            store.dispatch(StartEditAction.CloseButtonTapped)
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onDestroyView() {
        bottomSheetCallback.clear()
        store.dispatch(StartEditAction.DialogDismissed)
        time_entry_description.clearDescriptionChangedListeners()
        dismissEditDialog()
        super.onDestroyView()
    }

    override fun onCancel(dialog: DialogInterface) {
        (dialog as BottomSheetDialog).setOnShowListener(null)
        super.onCancel(dialog)
    }

    @ExperimentalCoroutinesApi
    private fun DialogInterface.attachBottomView(
        bottomSheetDialog: BottomSheetDialog,
        @LayoutRes layoutToAttach: Int,
        bottomControlPanelParams: BottomControlPanelParams
    ) {
        val coordinator =
            (this as BottomSheetDialog).findViewById<CoordinatorLayout>(com.google.android.material.R.id.coordinator)
        val containerLayout =
            this.findViewById<FrameLayout>(com.google.android.material.R.id.container)
        val bottomControlPanel = bottomSheetDialog.layoutInflater.inflate(layoutToAttach, null)

        val billableButton = bottomControlPanel.findViewById<ImageView>(R.id.billable_action)
        billableButton.isVisible = bottomControlPanelParams.isProWorkspace
        billableButton.setOnClickListener {
            store.dispatch(StartEditAction.BillableTapped)
        }

        store.state
            .mapNotNull { it.editableTimeEntry?.billable }
            .distinctUntilChanged()
            .onEach { setBillableButtonColor(billableButton, it) }
            .launchIn(lifecycleScope)

        bottomControlPanel.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM }
        containerLayout?.addView(bottomControlPanel)

        bottomControlPanel.post {
            (coordinator?.layoutParams as ViewGroup.MarginLayoutParams).apply {
                bottomControlPanel.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                this.bottomMargin = bottomControlPanel.measuredHeight
                containerLayout?.requestLayout()
            }

            done_action.setOnClickListener {
                context.performClickHapticFeedback()
                store.dispatch(StartEditAction.DoneButtonTapped)
            }

            project_action.setOnClickListener {
                store.dispatch(StartEditAction.ProjectButtonTapped)
            }

            tag_action.setOnClickListener {
                store.dispatch(StartEditAction.TagButtonTapped)
            }
        }
    }

    private fun startEditingTimeDate(dateTimePickMode: DateTimePickMode, editableTimeEntry: EditableTimeEntry) {
        when (dateTimePickMode) {
            DateTimePickMode.None -> dismissEditDialog()
            DateTimePickMode.StartTime -> startEditingTime(editableTimeEntry.startTimeOrNow())
            DateTimePickMode.StartDate -> startEditingDate(
                editableTimeEntry.startTimeOrNow(),
                maxTime = editableTimeEntry.endTimeOrNow()
            )
            DateTimePickMode.EndTime -> startEditingTime(editableTimeEntry.endTimeOrNow()!!)
            DateTimePickMode.EndDate -> startEditingDate(
                editableTimeEntry.endTimeOrNow()!!,
                minTime = editableTimeEntry.startTime!!
            )
        }
    }

    private fun dismissEditDialog() {
        editDialog?.dismiss()
        editDialog = null
    }

    private fun startEditingTime(initialTime: OffsetDateTime) = with(
        TimePickerDialog(requireContext(), null, initialTime.hour, initialTime.minute, true)
    ) {
        setOnCancelListener(dispatchingCancelListener)
        show()
        editDialog = this
    }

    private fun startEditingDate(
        initialTime: OffsetDateTime,
        minTime: OffsetDateTime? = null,
        maxTime: OffsetDateTime? = null
    ) = with(
        DatePickerDialog(requireContext(), null, initialTime.year, initialTime.monthValue - 1, initialTime.dayOfMonth)
    ) {
        maxTime?.let { datePicker.maxDate = maxTime.toEpochMillisecond() }
        minTime?.let { datePicker.minDate = minTime.toEpochMillisecond() }

        setOnCancelListener(dispatchingCancelListener)
        show()
        editDialog = this
    }

    private fun setBillableButtonColor(billableButton: ImageView, isBillable: Boolean) {
        bottomControlPanelAnimator.animateBackground(billableButton.background, isBillable)
        bottomControlPanelAnimator.animateColorFilter(billableButton, isBillable)
    }

    private fun scheduleTimeEntryIndicatorAndLabelUpdate(editableTimeEntry: EditableTimeEntry) {
        timeIndicatorScheduledUpdate?.cancel()
        timeIndicatorScheduledUpdate = lifecycleScope.launchWhenCreated {
            while (true) {
                time_indicator.setTextIfDifferent(editableTimeEntry.getDurationForDisplaying().formatForDisplaying())

                if (!editableTimeEntry.isRepresentingGroup() && editableTimeEntry.startTime == null) {
                    setTextOnStartTimeLabels(timeService.now())
                }

                delay(elapsedTimeIndicatorUpdateDelayMs)
            }
        }
    }

    private fun setTextOnTimeDateLabels(timeLabel: TextView, dateLabel: TextView, time: OffsetDateTime) {
        timeLabel.setTextIfDifferent(time.formatForDisplayingTime())
        dateLabel.setTextIfDifferent(time.formatForDisplayingDate())
    }

    private fun setTextOnStartTimeLabels(startTime: OffsetDateTime?) =
        setTextOnTimeDateLabels(start_time_label, start_date_label, startTime ?: timeService.now())

    private fun handleStartStopElementsState(editableTimeEntry: EditableTimeEntry) {
        with(editableTimeEntry) {

            if (isRepresentingGroup()) {
                extentedTimeOptions.forEach { it.isVisible = false }
                return
            }

            mapOf(
                start_time_label to DateTimePickMode.StartTime,
                stop_time_label to DateTimePickMode.EndTime,
                start_date_label to DateTimePickMode.StartDate,
                stop_date_label to DateTimePickMode.EndDate
            ).onEach { it.setPickerTappedActionOnLabel() }

            setTextOnStartTimeLabels(startTime)

            hideableStopViews.forEach { it.isVisible = duration != null }
            when (duration) {
                null -> {
                    stop_time_label.text =
                        if (isNotStarted()) getString(R.string.set_stop_time) else getString(R.string.stop)

                    stop_time_label.setOnClickListener {
                        // this is where 'set stop time' or 'stop' press should be handled
                    }
                }
                else -> {
                    val endTime = startTime!!.plus(duration)
                    setTextOnTimeDateLabels(stop_time_label, stop_date_label, endTime)
                }
            }
        }
    }

    private fun Workspace.isPro() = this.features.indexOf(WorkspaceFeature.Pro) != -1
    private fun StartEditState.isEditableInProWorkspace() = this.editableTimeEntry?.workspaceId?.run {
        this@isEditableInProWorkspace.workspaces[this]?.isPro()
    } ?: false

    private fun EditableTimeEntry.isRepresentingGroup() = this.ids.size > 1
    private fun EditableTimeEntry.isNotStarted() = this.ids.isEmpty()
    private fun EditableTimeEntry.isRunning() = this.ids.size == 1 && this.startTime != null && this.duration == null
    private fun EditableTimeEntry.isStopped() = this.startTime != null && this.duration != null
    private fun EditableTimeEntry.getDurationForDisplaying() = when {
        this.duration != null -> this.duration
        this.isNotStarted() && this.startTime == null -> Duration.ZERO
        this.startTime != null -> Duration.between(this.startTime, timeService.now())
        else -> throw IllegalStateException("Editable time entry must either have a duration, a start time or not be started yet (have no ids)")
    }

    private fun EditableTimeEntry.startTimeOrNow(): OffsetDateTime = this.startTime ?: timeService.now()
    private fun EditableTimeEntry.endTimeOrNow(): OffsetDateTime? = when {
        this.isStopped() -> this.startTime!!.plus(this.duration)
        this.isRunning() || this.isNotStarted() -> timeService.now()
        else -> null
    }

    fun TextView.setTextIfDifferent(newText: String) {
        if (this.text != newText) {
            this.text = newText
        }
    }

    private fun Map.Entry<TextView, DateTimePickMode>.setPickerTappedActionOnLabel() {
        val (label, action) = this
        label.setOnClickListener {
            store.dispatch(StartEditAction.PickerTapped(action))
        }
    }

    private fun OffsetDateTime.toEpochMillisecond() = this.toEpochSecond() * 1000

    private data class BottomControlPanelParams(val editableTimeEntry: EditableTimeEntry, val isProWorkspace: Boolean)
}