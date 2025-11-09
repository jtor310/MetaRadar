package f.cking.software.ui.heartmonitor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import f.cking.software.domain.model.HeartRateAlertSettings
import f.cking.software.utils.graphic.GlassSystemNavbar
import f.cking.software.utils.graphic.SystemNavbarSpacer
import org.koin.androidx.compose.koinViewModel

object HeartRateSettingsScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen() {
        val viewModel: HeartRateSettingsViewModel = koinViewModel()
        val settings by viewModel.settings.collectAsState()

        var highThreshold by remember(settings) { mutableStateOf(settings.highHeartRateThreshold.toString()) }
        var lowThreshold by remember(settings) { mutableStateOf(settings.lowHeartRateThreshold.toString()) }
        var retentionDays by remember(settings) { mutableStateOf(settings.dataRetentionDays.toString()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Heart Rate Settings") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.back() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    )
                )
            },
            bottomBar = {
                GlassSystemNavbar()
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Alerts section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Heart Rate Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Enable alerts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Enable Alerts")
                                Text(
                                    text = "Notify when heart rate is too high or low",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.enabled,
                                onCheckedChange = { viewModel.updateSettings(settings.copy(enabled = it)) }
                            )
                        }

                        if (settings.enabled) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // High heart rate alert
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Notify on high heart rate")
                                Switch(
                                    checked = settings.notifyOnHigh,
                                    onCheckedChange = { viewModel.updateSettings(settings.copy(notifyOnHigh = it)) }
                                )
                            }

                            if (settings.notifyOnHigh) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = highThreshold,
                                    onValueChange = { highThreshold = it },
                                    label = { Text("High Threshold (BPM)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Low heart rate alert
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Notify on low heart rate")
                                Switch(
                                    checked = settings.notifyOnLow,
                                    onCheckedChange = { viewModel.updateSettings(settings.copy(notifyOnLow = it)) }
                                )
                            }

                            if (settings.notifyOnLow) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = lowThreshold,
                                    onValueChange = { lowThreshold = it },
                                    label = { Text("Low Threshold (BPM)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Data retention section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Data Retention",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Automatically delete readings older than:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = retentionDays,
                            onValueChange = { retentionDays = it },
                            label = { Text("Days to keep") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            supportingText = { Text("Set to 0 to keep all data forever") }
                        )
                    }
                }

                // Save button
                Button(
                    onClick = {
                        val updatedSettings = settings.copy(
                            highHeartRateThreshold = highThreshold.toIntOrNull() ?: settings.highHeartRateThreshold,
                            lowHeartRateThreshold = lowThreshold.toIntOrNull() ?: settings.lowHeartRateThreshold,
                            dataRetentionDays = retentionDays.toIntOrNull() ?: settings.dataRetentionDays
                        )
                        viewModel.updateSettings(updatedSettings)
                        viewModel.back()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Settings")
                }

                // Cleanup button
                Button(
                    onClick = { viewModel.cleanupOldData() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clean Up Old Data Now")
                }

                SystemNavbarSpacer()
            }
        }
    }
}
