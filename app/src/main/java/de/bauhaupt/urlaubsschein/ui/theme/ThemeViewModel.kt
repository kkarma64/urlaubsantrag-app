package de.bauhaupt.urlaubsschein.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.bauhaupt.urlaubsschein.data.repository.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ThemeRepository(application)

    val isDarkMode: StateFlow<Boolean?> = repository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setDarkMode(enabled: Boolean?) {
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }
}
