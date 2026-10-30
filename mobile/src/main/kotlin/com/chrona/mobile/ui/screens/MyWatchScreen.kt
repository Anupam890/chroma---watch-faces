package com.chrona.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chrona.mobile.ui.ChronaViewModel
import com.chrona.mobile.ui.UiState
import com.chrona.mobile.ui.components.FacePreview
import com.chrona.mobile.ui.components.Pill
import com.chrona.mobile.ui.components.SectionHeader

@Composable
fun MyWatchScreen(state: UiState, vm: ChronaViewModel, onOpen: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val w = state.watch
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { SectionHeader("My Watch", w?.let { "${it.deviceName} · ${it.wearOsLabel}" } ?: "Nothing paired yet") }
        item {
            Column(Modifier.padding(16.dp).clip(RoundedCornerShape(24.dp)).background(cs.surfaceContainer).padding(20.dp)) {
                if (w == null) {
                    Text("No watch connected", style = MaterialTheme.typography.titleLarge)
                    Text("Open Chrona on your Wear OS watch, or pair a watch in the Wear OS app. Then pull to refresh.", color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Row { Button(vm::refresh, shape = RoundedCornerShape(14.dp)) { Text("Retry") }; Spacer(Modifier.width(8.dp)); OutlinedButton(vm::openWatchApp, shape = RoundedCornerShape(14.dp)) { Text("Open on watch") } }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill(if (w.supportsWatchFacePush) "Direct install ✓" else "Play Store only", if (w.supportsWatchFacePush) cs.tertiary else cs.secondary)
                        Pill("API ${w.apiLevel}", cs.onSurfaceVariant)
                        if (w.batteryPercent >= 0) Pill("🔋 ${w.batteryPercent}%", cs.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("${w.installedFaces.size} Chrona faces installed · ${w.remainingSlots} slots free", color = cs.onSurfaceVariant)
                    if (!w.hasPushPermission && w.supportsWatchFacePush) {
                        Spacer(Modifier.height(8.dp))
                        Text("Chrona needs permission on the watch to install faces.", color = cs.secondary)
                        TextButton(vm::openWatchApp) { Text("Grant on watch") }
                    }
                }
            }
        }
        if (w != null && w.installedFaces.isNotEmpty()) {
            item { SectionHeader("Installed") }
            items(w.installedFaces, key = { it.slotId }) { inst ->
                val face = state.faces.firstOrNull { it.id == inst.faceId } ?: return@items
                Row(Modifier.fillMaxWidth().clickable { onOpen(face.id) }.padding(20.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    FacePreview(face, Modifier.size(64.dp), animate = false)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(face.name, style = MaterialTheme.typography.titleMedium)
                        Text(if (inst.isActive) "Active now" else "Tap to manage", color = if (inst.isActive) cs.tertiary else cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (!inst.isActive) TextButton({ vm.setActive(face.id) }) { Text("Activate") }
                }
            }
        }
    }
}
