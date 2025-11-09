package f.cking.software.ui.heartmonitor

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import f.cking.software.utils.graphic.GlassSystemNavbar
import f.cking.software.utils.graphic.SystemNavbarSpacer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

object HeartRateMonitorScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(address: String) {
        val viewModel: HeartRateMonitorViewModel = koinViewModel(parameters = { parametersOf(address) })

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Heart Rate Monitor") },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Device info
                    DeviceInfoCard(viewModel)

                    // Heart rate display
                    HeartRateDisplay(viewModel)

                    // Connection status
                    ConnectionStatusCard(viewModel)

                    // Additional data
                    if (viewModel.heartRateData.currentHeartRate != null) {
                        AdditionalDataCard(viewModel)
                    }

                    // Error display
                    viewModel.error?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Control buttons
                    ControlButtons(viewModel)

                    Spacer(modifier = Modifier.height(16.dp))
                }

                SystemNavbarSpacer()
            }
        }
    }

    @Composable
    private fun DeviceInfoCard(viewModel: HeartRateMonitorViewModel) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = viewModel.deviceState?.name ?: viewModel.deviceState?.address ?: "Unknown Device",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.deviceState?.address ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun HeartRateDisplay(viewModel: HeartRateMonitorViewModel) {
        val isMonitoring = viewModel.connectionStatus is HeartRateMonitorViewModel.ConnectionStatus.MONITORING
        val heartRate = viewModel.heartRateData.currentHeartRate

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (heartRate != null && isMonitoring) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing heart icon
                if (heartRate != null && isMonitoring) {
                    PulsingHeartIcon(heartRate)
                } else {
                    Text(
                        text = "♥",
                        fontSize = 64.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Heart rate value
                if (heartRate != null) {
                    Text(
                        text = "$heartRate",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMonitoring) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "BPM",
                        fontSize = 24.sp,
                        color = if (isMonitoring) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                } else {
                    Text(
                        text = "--",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "BPM",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    private fun PulsingHeartIcon(heartRate: Int) {
        // Calculate pulse duration based on heart rate (60 BPM = 1 second per beat)
        val pulseDuration = (60000 / heartRate.coerceIn(40, 200)).toInt()

        val infiniteTransition = rememberInfiniteTransition(label = "heartPulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = pulseDuration / 2),
                repeatMode = RepeatMode.Reverse
            ),
            label = "heartScale"
        )

        Text(
            text = "♥",
            fontSize = 64.sp,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.scale(scale)
        )
    }

    @Composable
    private fun ConnectionStatusCard(viewModel: HeartRateMonitorViewModel) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (viewModel.connectionStatus) {
                            is HeartRateMonitorViewModel.ConnectionStatus.DISCONNECTED -> "Disconnected"
                            is HeartRateMonitorViewModel.ConnectionStatus.CONNECTING -> "Connecting..."
                            is HeartRateMonitorViewModel.ConnectionStatus.CONNECTED -> "Connected"
                            is HeartRateMonitorViewModel.ConnectionStatus.MONITORING -> "Monitoring"
                            is HeartRateMonitorViewModel.ConnectionStatus.DISCONNECTING -> "Disconnecting..."
                            is HeartRateMonitorViewModel.ConnectionStatus.ERROR -> "Error"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                when (viewModel.connectionStatus) {
                    is HeartRateMonitorViewModel.ConnectionStatus.CONNECTING -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    is HeartRateMonitorViewModel.ConnectionStatus.MONITORING -> {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Monitoring",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    @Composable
    private fun AdditionalDataCard(viewModel: HeartRateMonitorViewModel) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Additional Data",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                viewModel.heartRateData.sensorContactDetected?.let { contact ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Sensor Contact")
                        Text(
                            text = if (contact) "Detected" else "Not Detected",
                            color = if (contact) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                viewModel.heartRateData.energyExpended?.let { energy ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Energy Expended")
                        Text(text = "$energy kJ")
                    }
                }

                viewModel.heartRateData.rrIntervals?.let { intervals ->
                    if (intervals.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "RR-Intervals")
                            Text(text = "${intervals.size} samples")
                        }
                    }
                }

                if (viewModel.heartRateData.history.isNotEmpty()) {
                    val history = viewModel.heartRateData.history
                    val avg = history.map { it.heartRate }.average().toInt()
                    val min = history.minOfOrNull { it.heartRate } ?: 0
                    val max = history.maxOfOrNull { it.heartRate } ?: 0

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Session Statistics",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Average", fontSize = 12.sp)
                        Text(text = "$avg BPM", fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Range", fontSize = 12.sp)
                        Text(text = "$min - $max BPM", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    @Composable
    private fun ControlButtons(viewModel: HeartRateMonitorViewModel) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (viewModel.connectionStatus) {
                is HeartRateMonitorViewModel.ConnectionStatus.DISCONNECTED,
                is HeartRateMonitorViewModel.ConnectionStatus.ERROR -> {
                    Button(
                        onClick = { viewModel.startMonitoring() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Monitoring")
                    }
                }

                is HeartRateMonitorViewModel.ConnectionStatus.CONNECTING,
                is HeartRateMonitorViewModel.ConnectionStatus.CONNECTED,
                is HeartRateMonitorViewModel.ConnectionStatus.MONITORING -> {
                    OutlinedButton(
                        onClick = { viewModel.stopMonitoring() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop Monitoring")
                    }
                }

                is HeartRateMonitorViewModel.ConnectionStatus.DISCONNECTING -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disconnecting...")
                    }
                }
            }
        }
    }
}
