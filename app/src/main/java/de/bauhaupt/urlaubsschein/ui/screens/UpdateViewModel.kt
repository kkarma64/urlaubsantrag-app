package de.bauhaupt.urlaubsschein.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UpdateViewModel : ViewModel() {
    private val _dialogMessage = MutableStateFlow<String?>(null)
    val dialogMessage: StateFlow<String?> = _dialogMessage.asStateFlow()

    private val _pendingApkUrl = MutableStateFlow<String?>(null)
    val pendingApkUrl: StateFlow<String?> = _pendingApkUrl.asStateFlow()

    fun showMessage(msg: String?) {
        _dialogMessage.value = msg
    }

    fun setPendingApkUrl(url: String?) {
        _pendingApkUrl.value = url
    }

    init {
        instance = this
    }

    companion object {
        var instance: UpdateViewModel? = null
    }
}
