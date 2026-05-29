package com.example.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CpuWakeEvent
import com.example.domain.repository.TelemetryRepository
import com.example.domain.usecase.FreezeAppUseCase
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
            try {
                telemetryRepository.getCpuWakeEvents(0L).first().let { current ->
                    if (current.isEmpty()) {
                        val seedList = generateMockWakeLogs()
                        telemetryRepository.insertCpuWakeEvents(seedList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    private fun generateMockWakeLogs(): List<CpuWakeEvent> {
        val now = System.currentTimeMillis()
        return listOf(
            CpuWakeEvent(packageName = "com.miui.analytics", timestamp = now - 1000 * 60 * 15, wakeCount = 14, durationMs = 12000L, batteryDelta = 1.2f),
            CpuWakeEvent(packageName = "com.facebook.appmanager", timestamp = now - 1000 * 60 * 45, wakeCount = 8, durationMs = 8500L, batteryDelta = 0.8f),
            CpuWakeEvent(packageName = "com.samsung.android.bixby.agent", timestamp = now - 1000 * 60 * 80, wakeCount = 5, durationMs = 4200L, batteryDelta = 0.4f),
            CpuWakeEvent(packageName = "org.oppo.market", timestamp = now - 1000 * 60 * 120, wakeCount = 11, durationMs = 9300L, batteryDelta = 1.0f)
        )
    }

    fun setTimeframe(hours: Int) {
        _timeframeHours.value = hours
    }

    fun freezeTopOffender(packageName: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = freezeAppUseCase(packageName)
            if (result is com.example.domain.repository.CommandResult.Success) {
                onResult("Frozen top offender: $packageName")
            } else {
                onResult("Failure freezing offender.")
            }
        }
    }
}
