package f.cking.software.ui.heartmonitor

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import f.cking.software.utils.graphic.GlassSystemNavbar
import f.cking.software.utils.graphic.SystemNavbarSpacer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

object HeartRateHistoryScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(address: String) {
        val viewModel: HeartRateHistoryViewModel = koinViewModel(parameters = { parametersOf(address) })

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Heart Rate History") },
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
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (viewModel.error != null) {
                    ErrorView(viewModel.error!!, viewModel)
                } else if (viewModel.readings.isEmpty()) {
                    EmptyStateView()
                } else {
                    ContentView(viewModel)
                }

                SystemNavbarSpacer()
            }
        }
    }

    @Composable
    private fun ContentView(viewModel: HeartRateHistoryViewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device info
            DeviceInfoCard(viewModel)

            // Time range selector
            TimeRangeSelector(viewModel)

            // Statistics cards
            StatisticsCards(viewModel)

            // Chart
            ChartCard(viewModel)

            // Heart rate zones
            HeartRateZonesCard(viewModel)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    @Composable
    private fun DeviceInfoCard(viewModel: HeartRateHistoryViewModel) {
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
                    text = "${viewModel.statistics.count} readings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun TimeRangeSelector(viewModel: HeartRateHistoryViewModel) {
        Column {
            Text(
                text = "Time Range",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeartRateHistoryViewModel.TimeRange.values().forEach { range ->
                    FilterChip(
                        selected = viewModel.timeRange == range,
                        onClick = { viewModel.setTimeRange(range) },
                        label = { Text(range.displayName) }
                    )
                }
            }
        }
    }

    @Composable
    private fun StatisticsCards(viewModel: HeartRateHistoryViewModel) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Average
            StatCard(
                title = "Average",
                value = viewModel.statistics.average?.toInt()?.toString() ?: "--",
                unit = "BPM",
                modifier = Modifier.weight(1f)
            )

            // Min
            StatCard(
                title = "Min",
                value = viewModel.statistics.min?.toString() ?: "--",
                unit = "BPM",
                modifier = Modifier.weight(1f)
            )

            // Max
            StatCard(
                title = "Max",
                value = viewModel.statistics.max?.toString() ?: "--",
                unit = "BPM",
                modifier = Modifier.weight(1f)
            )
        }

        // Resting heart rate (if available)
        viewModel.statistics.restingHeartRate?.let { restingHR ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                            text = "Resting Heart Rate",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Estimated from lowest 10% of readings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = "$restingHR BPM",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    @Composable
    private fun StatCard(
        title: String,
        value: String,
        unit: String,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun ChartCard(viewModel: HeartRateHistoryViewModel) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Heart Rate Over Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (viewModel.readings.isNotEmpty()) {
                    HeartRateLineChart(
                        readings = viewModel.readings,
                        lineColor = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun HeartRateZonesCard(viewModel: HeartRateHistoryViewModel) {
        val zoneDistribution = viewModel.getZoneDistribution()
        val totalReadings = zoneDistribution.values.sum()

        if (totalReadings == 0) return

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Heart Rate Zones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HeartRateHistoryViewModel.HeartRateZone.values().forEach { zone ->
                    val count = zoneDistribution[zone] ?: 0
                    val percentage = if (totalReadings > 0) {
                        (count.toFloat() / totalReadings * 100).toInt()
                    } else 0

                    ZoneBar(
                        zone = zone,
                        percentage = percentage,
                        count = count
                    )

                    if (zone != HeartRateHistoryViewModel.HeartRateZone.values().last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun ZoneBar(
        zone: HeartRateHistoryViewModel.HeartRateZone,
        percentage: Int,
        count: Int
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(zone.colorValue))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = zone.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "$percentage% ($count)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage / 100f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(zone.colorValue))
                )
            }
        }
    }

    @Composable
    private fun EmptyStateView() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No Heart Rate Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Start monitoring to collect heart rate data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun ErrorView(error: String, viewModel: HeartRateHistoryViewModel) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "Error Loading Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
