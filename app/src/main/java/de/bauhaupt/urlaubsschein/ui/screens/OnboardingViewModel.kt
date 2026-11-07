package de.bauhaupt.urlaubsschein.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppPreferencesRepository(application)

    val onboardingAbgeschlossen: StateFlow<Boolean?> = repository.onboardingAbgeschlossen
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingAbgeschlossen(true)
        }
    }
}
