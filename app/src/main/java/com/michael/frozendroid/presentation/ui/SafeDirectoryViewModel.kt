package com.michael.frozendroid.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.frozendroid.domain.model.AppPackage
import com.michael.frozendroid.domain.model.SafetyLevel
import com.michael.frozendroid.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SafeDirectoryUiState {
    object Loading : SafeDirectoryUiState()
    data class Success(val apps: List<AppPackage>, val overrides: Map<String, SafetyLevel>) : SafeDirectoryUiState()
}

@HiltViewModel
class SafeDirectoryViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _carrierFilter = MutableStateFlow("Universal")
    val carrierFilter = _carrierFilter.asStateFlow()

    val uiState: StateFlow<SafeDirectoryUiState> = combine(
        appRepository.getApps(),
        appRepository.getSafetyOverrides(),
        _carrierFilter
    ) { apps, overrides, carrier ->
        val filtered = if (carrier == "Universal") {
            apps
        } else {
            apps.filter { it.carrier.equals(carrier, ignoreCase = true) }
        }
        SafeDirectoryUiState.Success(filtered, overrides)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SafeDirectoryUiState.Loading
    )

    fun setCarrierFilter(carrier: String) {
        _carrierFilter.value = carrier
    }

    fun saveOverride(packageName: String, safetyLevel: SafetyLevel?) {
        viewModelScope.launch {
            appRepository.saveSafetyOverride(packageName, safetyLevel)
            appRepository.refreshAppList()
        }
    }
}
