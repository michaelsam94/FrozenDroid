package com.michael.frozendroid.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.frozendroid.domain.model.CpuWakeEvent
import com.michael.frozendroid.domain.repository.TelemetryRepository
import com.michael.frozendroid.domain.usecase.FreezeAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CpuMonitorUiState {
    object Loading : CpuMonitorUiState()
    data class Success(val events: List<CpuWakeEvent>, val topOffenderPackage: String?) : CpuMonitorUiState()
}

@HiltViewModel
class CpuMonitorViewModel @Inject constructor(
    private val telemetryRepository: TelemetryRepository,
    private val freezeAppUseCase: FreezeAppUseCase
) : ViewModel() {

    private val _timeframeHours = MutableStateFlow(8) // 8h, 24h, 168h (7d)
    val timeframeHours = _timeframeHours.asStateFlow()

    init {
        viewModelScope.launch {
            telemetryRepository.deleteFabricatedWakeEvents()
        }
    }

    val uiState: StateFlow<CpuMonitorUiState> = _timeframeHours.flatMapLatest { hours ->
        val cutoff = System.currentTimeMillis() - (hours * 60 * 60 * 1000L)
        telemetryRepository.getCpuWakeEvents(cutoff).map { list ->
            // Aggregate in-memory to discover top offender package
            val topPackage = list.groupBy { it.packageName }
                .maxByOrNull { (_, events) -> events.sumOf { it.wakeCount } }?.key
            
            CpuMonitorUiState.Success(list, topPackage)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CpuMonitorUiState.Loading
    )

    fun setTimeframe(hours: Int) {
        _timeframeHours.value = hours
    }

    fun freezeTopOffender(packageName: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = freezeAppUseCase(packageName)
            if (result is com.michael.frozendroid.domain.repository.CommandResult.Success) {
                onResult("Frozen top offender: $packageName")
            } else {
                onResult("Failure freezing offender.")
            }
        }
    }
}
