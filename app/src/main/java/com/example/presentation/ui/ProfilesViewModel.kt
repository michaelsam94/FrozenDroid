package com.example.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.FreezeProfile
import com.example.domain.repository.ProfileRepository
import com.example.domain.usecase.SwitchProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class ProfilesUiState {
    object Loading : ProfilesUiState()
    data class Success(val profiles: List<FreezeProfile>, val activeProfile: FreezeProfile?) : ProfilesUiState()
}

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val switchProfileUseCase: SwitchProfileUseCase
) : ViewModel() {

    val uiState: StateFlow<ProfilesUiState> = combine(
        profileRepository.getProfiles(),
        profileRepository.getActiveProfile()
    ) { profiles, active ->
        ProfilesUiState.Success(profiles, active)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfilesUiState.Loading
    )

    init {
        // Build starter mock presets for clean fresh user onboarding
        viewModelScope.launch {
            profileRepository.getProfiles().first().let { current ->
                if (current.isEmpty()) {
                    val ultraBattery = FreezeProfile(
                        id = "ultra_battery",
                        name = "Ultra Battery Saver",
                        icon = "bolt",
                        frozenPackages = listOf(
                            "com.samsung.android.bixby.agent",
                            "com.miui.analytics",
                            "com.facebook.system",
                            "com.facebook.services",
                            "com.facebook.appmanager",
                            "com.oppo.market"
                        ),
                        scheduleTimes = listOf("22:00"),
                        isActive = false
                    )
                    val socialDetox = FreezeProfile(
                        id = "focus_detox",
                        name = "Focus & Detox",
                        icon = "visibility_off",
                        frozenPackages = listOf(
                            "com.facebook.system",
                            "com.facebook.services",
                            "com.netflix.mediaclient",
                            "com.google.android.youtube"
                        ),
                        scheduleTimes = emptyList(),
                        isActive = false
                    )
                    profileRepository.insertProfile(ultraBattery)
                    profileRepository.insertProfile(socialDetox)
                }
            }
        }
    }

    fun createProfile(name: String, frozenPackages: List<String>) {
        viewModelScope.launch {
            val profile = FreezeProfile(
                id = UUID.randomUUID().toString(),
                name = name,
                icon = "shield",
                frozenPackages = frozenPackages,
                scheduleTimes = emptyList(),
                isActive = false
            )
            profileRepository.insertProfile(profile)
        }
    }

    fun deleteProfile(id: String) {
        viewModelScope.launch {
            profileRepository.deleteProfile(id)
        }
    }

    fun switchProfile(profileId: String?, onCompletion: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = switchProfileUseCase(profileId)
            onCompletion(success)
        }
    }
}
