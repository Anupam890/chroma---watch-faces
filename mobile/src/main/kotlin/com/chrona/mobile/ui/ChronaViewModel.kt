package com.chrona.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrona.mobile.data.FaceRepository
import com.chrona.mobile.data.PrefsRepository
import com.chrona.mobile.sync.WatchConnection
import com.chrona.shared.FaceCategory
import com.chrona.shared.UserPrefs
import com.chrona.shared.WatchFaceItem
import com.chrona.shared.WatchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val faces: List<WatchFaceItem> = emptyList(),
    val category: FaceCategory? = null,
    val query: String = "",
    val prefs: UserPrefs = UserPrefs(),
    val watch: WatchState? = null,
    val watchConnected: Boolean = false,
    val progress: Map<String, WatchConnection.InstallProgress> = emptyMap()
) {
    val visibleFaces: List<WatchFaceItem>
        get() = faces
            .filter { category == null || it.category == category }
            .filter { query.isBlank() || it.name.contains(query, true) || it.tagline.contains(query, true) }
    fun isInstalled(id: String) = watch?.installedFaces?.any { it.faceId == id } == true
    fun slotFor(id: String) = watch?.installedFaces?.firstOrNull { it.faceId == id }?.slotId
    fun isActive(id: String) = watch?.installedFaces?.firstOrNull { it.faceId == id }?.isActive == true
}

@HiltViewModel
class ChronaViewModel @Inject constructor(
    private val faces: FaceRepository,
    private val prefsRepo: PrefsRepository,
    private val watch: WatchConnection
) : ViewModel() {

    private val category = MutableStateFlow<FaceCategory?>(null)
    private val query = MutableStateFlow("")

    val state: StateFlow<UiState> = combine(
        faces.faces, category, query, prefsRepo.prefs,
        combine(watch.watchState, watch.watchNode, watch.installProgress) { s, n, p -> Triple(s, n, p) }
    ) { list, cat, q, prefs, (ws, node, prog) ->
        UiState(list, cat, q, prefs, ws, node != null, prog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    val events = watch.events

    init {
        viewModelScope.launch { prefsRepo.prefs.collect { watch.syncPrefs(it) } }
    }

    fun selectCategory(c: FaceCategory?) { category.value = c }
    fun search(q: String) { query.value = q }
    fun toggleFavourite(id: String) = viewModelScope.launch { prefsRepo.toggleFavourite(id) }
    fun refresh() = viewModelScope.launch { watch.refreshNode() }
    fun install(face: WatchFaceItem) = viewModelScope.launch { watch.install(face, state.value.prefs.autoSetActive) }
    fun remove(id: String) = viewModelScope.launch { state.value.slotFor(id)?.let { watch.remove(it) } }
    fun setActive(id: String) = viewModelScope.launch { state.value.slotFor(id)?.let { watch.setActive(it) } }
    fun openWatchApp() = viewModelScope.launch { watch.openWatchApp() }
    fun setAutoActive(v: Boolean) = viewModelScope.launch { prefsRepo.setAutoActive(v) }
    fun setWifiOnly(v: Boolean) = viewModelScope.launch { prefsRepo.setWifiOnly(v) }
    fun setTheme(v: String) = viewModelScope.launch { prefsRepo.setTheme(v) }
}
