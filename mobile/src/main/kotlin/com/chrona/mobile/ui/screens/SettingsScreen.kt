package com.chrona.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chrona.mobile.ui.ChronaViewModel
import com.chrona.mobile.ui.UiState
import com.chrona.mobile.ui.components.SectionHeader

@Composable
fun SettingsScreen(state: UiState, vm: ChronaViewModel) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionHeader("Settings")
        SettingSwitch("Set as active after install", "Switch the watch to the new face automatically.", state.prefs.autoSetActive, vm::setAutoActive)
        SettingSwitch("Large faces on Wi-Fi only", "Skip Bluetooth transfer for faces over 500 KB.", state.prefs.wifiOnlyLargeFaces, vm::setWifiOnly)
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Column(Modifier.padding(20.dp, 8.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row { listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (k, l) -> FilterChip(state.prefs.themeMode == k, { vm.setTheme(k) }, { Text(l) }, Modifier.padding(end = 8.dp)) } }
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Column(Modifier.padding(20.dp, 8.dp)) {
            Text("About", style = MaterialTheme.typography.titleMedium)
            Text("Chrona 1.0.0 · Watch Face Format + Watch Face Push", color = cs.onSurfaceVariant)
            Text("Open source under MIT. github.com/<your-handle>/chrona", color = cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(20.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked, onChange)
    }
}
