package com.chrona.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chrona.mobile.sync.WatchConnection
import com.chrona.mobile.ui.ChronaViewModel
import com.chrona.mobile.ui.UiState
import com.chrona.mobile.ui.components.FacePreview
import com.chrona.mobile.ui.components.Pill
import com.chrona.shared.CustomizationFeature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceDetailScreen(id: String, state: UiState, vm: ChronaViewModel, onBack: () -> Unit) {
    val face = state.faces.firstOrNull { it.id == id } ?: return
    val cs = MaterialTheme.colorScheme
    val accent = Color(android.graphics.Color.parseColor(face.accentColorHex))
    val progress = state.progress[id]
    val installed = state.isInstalled(id)
    val compatible = state.watch?.let { face.wffVersion.minApi <= it.apiLevel } ?: true
    val canPush = state.watch?.supportsWatchFacePush ?: true

    Scaffold(topBar = {
        TopAppBar(title = {}, navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            actions = { IconButton({ vm.toggleFavourite(id) }) { Icon(if (id in state.prefs.favouriteIds) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Favourite", tint = cs.secondary) } })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(320.dp).background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.35f), Color.Transparent))), Alignment.Center) {
                FacePreview(face, Modifier.size(240.dp))
            }
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(face.name, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                    if (face.isPremium) Pill("Pro", cs.secondary)
                }
                Text(face.tagline, style = MaterialTheme.typography.titleMedium, color = accent)
                Spacer(Modifier.height(12.dp))
                Text(face.description, style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("${face.category.emoji} ${face.category.displayName}", cs.onSurfaceVariant)
                    Pill("WFF v${face.wffVersion.version}", cs.onSurfaceVariant)
                    Pill("${face.sizeKb} KB", cs.onSurfaceVariant)
                    if (face.complicationSlots > 0) Pill("${face.complicationSlots} slots", cs.onSurfaceVariant)
                }
                Spacer(Modifier.height(24.dp))

                // Primary action
                when {
                    progress != null -> Column {
                        when (progress) {
                            is WatchConnection.InstallProgress.Transferring -> { LinearProgressIndicator(progress = { progress.fraction }, Modifier.fillMaxWidth()); Text("Sending ${(progress.fraction * 100).toInt()}%", style = MaterialTheme.typography.labelSmall) }
                            WatchConnection.InstallProgress.Preparing -> Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)); Text("Preparing…") }
                            WatchConnection.InstallProgress.InstallingOnWatch -> Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)); Text("Installing on watch…") }
                        }
                    }
                    installed -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button({ vm.setActive(id) }, Modifier.weight(1f), enabled = !state.isActive(id), shape = RoundedCornerShape(16.dp)) { Text(if (state.isActive(id)) "Active on watch" else "Set as active") }
                        OutlinedButton({ vm.remove(id) }, shape = RoundedCornerShape(16.dp)) { Text("Remove") }
                    }
                    else -> Column {
                        Button({ vm.install(face) }, Modifier.fillMaxWidth().height(52.dp), enabled = state.watchConnected && compatible && canPush, shape = RoundedCornerShape(16.dp)) {
                            Text(if (!state.watchConnected) "Connect a watch to install" else "Install on watch")
                        }
                        if (!canPush || !compatible) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (!compatible) "Requires ${face.wffVersion.minWearOs}. Your watch runs ${state.watch?.wearOsLabel}."
                                else "Direct install needs Wear OS 6. On ${state.watch?.wearOsLabel}, install this face from Google Play.",
                                style = MaterialTheme.typography.bodyMedium, color = cs.secondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
                Text("Customisation", style = MaterialTheme.typography.headlineMedium)
                Text("Edit these on the watch, or in Chrona after installing.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                face.features.forEach { f -> FeatureRow(f, accent) }

                if (face.flavors.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text("Flavors", style = MaterialTheme.typography.headlineMedium)
                    Text("One-tap presets curated by the designer.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(face.flavors) { AssistChip({}, label = { Text(it) }) } }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun FeatureRow(f: CustomizationFeature, accent: Color) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)), Alignment.Center) {
            Text(
                when (f.type) {
                    CustomizationFeature.Type.COLOR_THEME -> "🎨"; CustomizationFeature.Type.LIST -> "☰"; CustomizationFeature.Type.BOOLEAN -> "◐"
                    CustomizationFeature.Type.COMPLICATION -> "◎"; CustomizationFeature.Type.PHOTO -> "📷"; CustomizationFeature.Type.FLAVOR -> "✦"
                }
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(f.label, style = MaterialTheme.typography.titleMedium)
            if (f.options.isNotEmpty()) Text(f.options.joinToString(" · "), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        }
    }
}
