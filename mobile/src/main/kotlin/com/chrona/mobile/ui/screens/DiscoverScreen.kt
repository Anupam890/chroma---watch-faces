package com.chrona.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chrona.mobile.ui.ChronaViewModel
import com.chrona.mobile.ui.UiState
import com.chrona.mobile.ui.components.Dot
import com.chrona.mobile.ui.components.FaceCard
import com.chrona.mobile.ui.components.HorizontalSpace
import com.chrona.shared.FaceCategory

@Composable
fun DiscoverScreen(state: UiState, vm: ChronaViewModel, onOpen: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    LazyVerticalGrid(
        GridCells.Fixed(2), Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(Modifier.padding(4.dp, 12.dp)) {
                Text("Chrona", style = MaterialTheme.typography.displayLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(if (state.watchConnected) cs.tertiary else cs.onSurfaceVariant); HorizontalSpace(8)
                    Text(
                        when {
                            state.watch != null -> "${state.watch.deviceName} · ${state.watch.wearOsLabel}"
                            state.watchConnected -> "Watch connected"
                            else -> "No watch connected"
                        }, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    state.query, vm::search, Modifier.fillMaxWidth(), placeholder = { Text("Search faces") },
                    leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(state.category == null, { vm.selectCategory(null) }, { Text("All") }) }
                    items(FaceCategory.entries) { c -> FilterChip(state.category == c, { vm.selectCategory(c) }, { Text("${c.emoji} ${c.displayName}") }) }
                }
            }
        }
        items(state.visibleFaces, key = { it.id }) { face ->
            FaceCard(face, state.isInstalled(face.id), state.isActive(face.id), face.id in state.prefs.favouriteIds, state.progress[face.id],
                onClick = { onOpen(face.id) }, onFavourite = { vm.toggleFavourite(face.id) })
        }
    }
}
