package de.bauhaupt.urlaubsschein.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import de.bauhaupt.urlaubsschein.logic.PasswordHasher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppPreferencesRepository(application)

    val onboardingAbgeschlossen: StateFlow<Boolean?> = repository.onboardingAbgeschlossen
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val passwortHash: StateFlow<String?> = repository.passwortHash
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

    suspend fun savePassword(password: String): Boolean {
        if (password.length < 6) return false
        val hash = PasswordHasher.hash(password)
        repository.setPasswortHash(hash)
        return true
    }

    fun verifyPassword(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentHash = repository.passwortHash.first()
            if (currentHash.isNullOrBlank()) {
                onResult(false)
                return@launch
            }
            val isValid = PasswordHasher.verify(password, currentHash)
            onResult(isValid)
        }
    }
}
