package com.chrona.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chrona.mobile.sync.WatchConnection
import com.chrona.shared.WatchFaceItem

@Composable
fun Pill(text: String, color: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color,
        modifier = modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun FaceCard(
    face: WatchFaceItem,
    installed: Boolean,
    active: Boolean,
    favourite: Boolean,
    progress: WatchConnection.InstallProgress?,
    onClick: () -> Unit,
    onFavourite: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.clip(RoundedCornerShape(24.dp)).background(cs.surfaceContainer).clickable(onClick = onClick).padding(14.dp)
    ) {
        Box {
            FacePreview(face, Modifier.fillMaxWidth())
            Icon(
                if (favourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null,
                tint = if (favourite) cs.secondary else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(28.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)).clickable(onClick = onFavourite).padding(5.dp)
            )
            if (active) Pill("Active", cs.tertiary, Modifier.align(Alignment.BottomStart).padding(6.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50)))
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(face.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (face.isPremium) Pill("Pro", cs.secondary)
        }
        Text(face.tagline, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        when (progress) {
            is WatchConnection.InstallProgress.Transferring -> LinearProgressIndicator(progress = { progress.fraction }, Modifier.fillMaxWidth())
            null -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(face.wffVersion.minWearOs, cs.onSurfaceVariant)
                if (installed) Pill("Installed", cs.tertiary)
            }
            else -> LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun Dot(color: Color) = Box(Modifier.size(8.dp).clip(CircleShape).background(color))

@Composable
fun HorizontalSpace(w: Int) = Spacer(Modifier.width(w.dp))
