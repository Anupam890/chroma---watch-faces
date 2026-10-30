package com.chrona.wear.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrona.shared.Catalog
import com.chrona.shared.InstalledFace
import com.chrona.shared.WatchFaceItem
import com.chrona.wear.push.WatchFaceInstaller
import com.chrona.wear.sync.WatchSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WearUiState(
    val supported: Boolean = false,
    val hasPermission: Boolean = false,
    val installed: List<InstalledFace> = emptyList(),
    val remainingSlots: Int = 0,
    val catalog: List<WatchFaceItem> = Catalog.faces,
    val status: WatchSyncManager.Status = WatchSyncManager.Status.Idle
)

@HiltViewModel
class WearViewModel @Inject constructor(
    private val installer: WatchFaceInstaller,
    private val sync: WatchSyncManager
) : ViewModel() {
    private val _state = MutableStateFlow(WearUiState(installer.isSupported, installer.hasPermission))
    val state: StateFlow<WearUiState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch { sync.status.collect { s -> _state.value = _state.value.copy(status = s); if (s is WatchSyncManager.Status.Done) refresh() } }
    }

    fun refresh() = viewModelScope.launch {
        val (list, remaining) = installer.installed()
        _state.value = _state.value.copy(hasPermission = installer.hasPermission, installed = list, remainingSlots = remaining)
        sync.publishState()
    }
    fun setActive(slotId: String) = viewModelScope.launch { sync.setActive(slotId); refresh() }
    fun remove(slotId: String) = viewModelScope.launch { sync.remove(slotId); refresh() }
}
