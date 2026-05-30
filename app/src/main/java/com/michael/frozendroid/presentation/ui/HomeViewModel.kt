package com.michael.frozendroid.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.frozendroid.domain.model.AppPackage
import com.michael.frozendroid.domain.model.SafetyLevel
import com.michael.frozendroid.domain.repository.AppRepository
import com.michael.frozendroid.domain.repository.CommandResult
import com.michael.frozendroid.domain.usecase.FreezeAppUseCase
import com.michael.frozendroid.domain.usecase.GetInstalledAppsUseCase
import com.michael.frozendroid.domain.usecase.UnfreezeAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilterState(
    val showSafe: Boolean = true,
    val showCaution: Boolean = true,
    val showDangerous: Boolean = false,
    val showFrozenOnly: Boolean = false,
    val showActiveOnly: Boolean = false
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val apps: List<AppPackage>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val freezeAppUseCase: FreezeAppUseCase,
    private val unfreezeAppUseCase: UnfreezeAppUseCase,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    private val _selectedPackages = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackages = _selectedPackages.asStateFlow()

    private val _lastActionCache = MutableStateFlow<String?>(null) // Holds packageName for undo
    val lastActionCache = _lastActionCache.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        getInstalledAppsUseCase(),
        _searchQuery,
        _filterState
    ) { apps, query, filters ->
        if (apps.isEmpty()) {
            HomeUiState.Loading
        } else {
            val filtered = apps.filter { app ->
                // Query match
                val matchesQuery = app.label.contains(query, ignoreCase = true) || 
                                   app.packageName.contains(query, ignoreCase = true)

                // Safety levels filters
                val matchesSafety = when (app.safetyLevel) {
                    SafetyLevel.SAFE -> filters.showSafe
                    SafetyLevel.CAUTION -> filters.showCaution
                    SafetyLevel.DANGEROUS -> filters.showDangerous
                }

                // Dynamic Frozen state filter checking
                val matchesState = when {
                    filters.showFrozenOnly -> app.isFrozen
                    filters.showActiveOnly -> !app.isFrozen
                    else -> true
                }

                matchesQuery && matchesSafety && matchesState
            }
            HomeUiState.Success(filtered)
        }
    }.catch { e ->
        emit(HomeUiState.Error(e.localizedMessage ?: "Unknown compilation error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                appRepository.refreshAppList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilters(filters: FilterState) {
        _filterState.value = filters
    }

    fun toggleSelection(packageName: String) {
        _selectedPackages.update { current ->
            if (current.contains(packageName)) current - packageName else current + packageName
        }
    }

    fun clearSelection() {
        _selectedPackages.value = emptySet()
    }

    fun freezeApp(packageName: String, onCompletion: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = freezeAppUseCase(packageName)
            when (res) {
                is CommandResult.Success -> {
                    _lastActionCache.value = packageName
                    appRepository.refreshAppList()
                    onCompletion("Successfully frozen $packageName")
                }
                is CommandResult.Failure -> onCompletion("Error: ${res.stderr}")
                CommandResult.PermissionDenied -> onCompletion(PrivilegeSetupGuidance.message)
                CommandResult.NotAvailable -> onCompletion("Freeze unavailable on this device.")
            }
        }
    }

    fun unfreezeApp(packageName: String, onCompletion: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = unfreezeAppUseCase(packageName)
            when (res) {
                is CommandResult.Success -> {
                    appRepository.refreshAppList()
                    onCompletion("Successfully unfrozen $packageName")
                }
                is CommandResult.Failure -> onCompletion("Error: ${res.stderr}")
                CommandResult.PermissionDenied -> onCompletion(PrivilegeSetupGuidance.message)
                CommandResult.NotAvailable -> onCompletion("Unfreeze unavailable on this device.")
            }
        }
    }

    fun freezeSelected(onCompletion: (String) -> Unit) {
        viewModelScope.launch {
            val pkgs = _selectedPackages.value
            var count = 0
            var blocked = 0
            var permissionDenied = 0
            pkgs.forEach { pkg ->
                val res = freezeAppUseCase(pkg)
                when (res) {
                    is CommandResult.Success -> count++
                    CommandResult.PermissionDenied -> {
                        blocked++
                        permissionDenied++
                    }
                    else -> blocked++
                }
            }
            clearSelection()
            appRepository.refreshAppList()
            onCompletion(
                if (count == 0 && permissionDenied > 0) {
                    PrivilegeSetupGuidance.message
                } else if (blocked == 0) {
                    "Frozen $count packages successfully"
                } else {
                    "Frozen $count packages; skipped $blocked unsafe or unavailable items"
                }
            )
        }
    }

    fun unfreezeSelected(onCompletion: (String) -> Unit) {
        viewModelScope.launch {
            val pkgs = _selectedPackages.value
            var count = 0
            var failed = 0
            var permissionDenied = 0
            pkgs.forEach { pkg ->
                val res = unfreezeAppUseCase(pkg)
                when (res) {
                    is CommandResult.Success -> count++
                    CommandResult.PermissionDenied -> {
                        failed++
                        permissionDenied++
                    }
                    else -> failed++
                }
            }
            clearSelection()
            appRepository.refreshAppList()
            onCompletion(
                if (count == 0 && permissionDenied > 0) {
                    PrivilegeSetupGuidance.message
                } else if (failed == 0) {
                    "Unfrozen $count packages successfully"
                } else {
                    "Unfrozen $count packages; $failed items could not be changed"
                }
            )
        }
    }

    fun undoLastFreeze(onCompletion: (String) -> Unit) {
        val lastPkg = _lastActionCache.value ?: return
        viewModelScope.launch {
            val res = unfreezeAppUseCase(lastPkg)
            if (res is CommandResult.Success) {
                _lastActionCache.value = null
                appRepository.refreshAppList()
                onCompletion("Undo successful: $lastPkg activated")
            }
        }
    }
}
