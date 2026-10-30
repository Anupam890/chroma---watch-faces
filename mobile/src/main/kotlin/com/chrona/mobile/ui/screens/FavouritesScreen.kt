package com.chrona.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chrona.mobile.ui.ChronaViewModel
import com.chrona.mobile.ui.UiState
import com.chrona.mobile.ui.components.FaceCard
import com.chrona.mobile.ui.components.SectionHeader

@Composable
fun FavouritesScreen(state: UiState, vm: ChronaViewModel, onOpen: (String) -> Unit) {
    val favs = state.faces.filter { it.id in state.prefs.favouriteIds }
    LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { GridItemSpan(2) }) { SectionHeader("Favourites", if (favs.isEmpty()) "Tap the heart on any face to save it here." else "${favs.size} saved") }
        items(favs, key = { it.id }) { f ->
            FaceCard(f, state.isInstalled(f.id), state.isActive(f.id), true, state.progress[f.id], { onOpen(f.id) }, { vm.toggleFavourite(f.id) })
        }
        if (favs.isEmpty()) item(span = { GridItemSpan(2) }) { Text("Nothing here yet.", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
