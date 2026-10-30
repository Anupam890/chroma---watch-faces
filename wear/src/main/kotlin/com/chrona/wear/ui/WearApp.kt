package com.chrona.wear.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Watch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.chrona.wear.push.WatchFaceInstaller
import com.chrona.wear.sync.WatchSyncManager

private val WearScheme = ColorScheme(
    primary = Color(0xFF9D86FF), onPrimary = Color(0xFF14142B),
    secondary = Color(0xFFFF5E5B), tertiary = Color(0xFF5BE7C4),
    background = Color.Black, onBackground = Color(0xFFF7F3EA),
    surfaceContainer = Color(0xFF1C1C33), onSurface = Color(0xFFF7F3EA), onSurfaceVariant = Color(0xFFB8B4AE)
)

@Composable
fun WearApp(vm: WearViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.refresh() }

    MaterialTheme(colorScheme = WearScheme) {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            ScreenScaffold(scrollState = listState) { padding ->
                TransformingLazyColumn(state = listState, contentPadding = padding) {
                    item { ListHeader { Text("Chrona") } }

                    // Live transfer status
                    item {
                        when (val s = state.status) {
                            is WatchSyncManager.Status.Receiving -> StatusCard("Receiving ${s.faceId.humanise()}", s.fraction)
                            is WatchSyncManager.Status.Installing -> StatusCard("Installing ${s.faceId.humanise()}", null)
                            is WatchSyncManager.Status.Done -> Card(onClick = {}) { Text(if (s.ok) "✓ ${s.faceId.humanise()} installed" else "✕ ${s.message}") }
                            WatchSyncManager.Status.Idle -> Text(
                                when {
                                    !state.supported -> "This watch runs Wear OS 5 or older. Install Chrona faces from Google Play."
                                    !state.hasPermission -> "Allow Chrona to install watch faces."
                                    state.installed.isEmpty() -> "Pick a face in Chrona on your phone — it will appear here."
                                    else -> "${state.installed.size} installed · ${state.remainingSlots} slots free"
                                },
                                Modifier.fillMaxWidth().padding(horizontal = 8.dp), textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (state.supported && !state.hasPermission) item {
                        Button(onClick = { permissionLauncher.launch(WatchFaceInstaller.PERMISSION) }, Modifier.fillMaxWidth()) { Text("Grant permission") }
                    }

                    if (state.installed.isNotEmpty()) item { ListHeader { Text("Installed") } }
                    items(state.installed.size) { i ->
                        val f = state.installed[i]
                        val name = state.catalog.firstOrNull { it.id == f.faceId }?.name ?: f.faceId.humanise()
                        TitleCard(onClick = { if (!f.isActive) vm.setActive(f.slotId) }, title = { Text(name) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (f.isActive) { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Active") }
                                else Text("Tap to activate", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.weight(1f))
                                Button(onClick = { vm.remove(f.slotId) }, colors = ButtonDefaults.outlinedButtonColors(), modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, "Remove", Modifier.size(14.dp)) }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                            Icon(Icons.Default.Watch, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Browse faces on your phone", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, fraction: Float?) {
    Card(onClick = {}) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            if (fraction != null) CircularProgressIndicator(progress = { fraction }, Modifier.size(32.dp)) else CircularProgressIndicator(Modifier.size(32.dp))
        }
    }
}

private fun String.humanise() = split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
