package f.cking.software.ui.heartmonitor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import f.cking.software.data.repo.DevicesRepository
import f.cking.software.domain.interactor.GetHeartRateHistoryInteractor
import f.cking.software.domain.model.DeviceData
import f.cking.software.domain.model.HeartRateReading
import f.cking.software.utils.navigation.BackCommand
import f.cking.software.utils.navigation.Router
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class HeartRateHistoryViewModel(
    private val address: String,
    private val router: Router,
    private val devicesRepository: DevicesRepository,
    private val getHeartRateHistoryInteractor: GetHeartRateHistoryInteractor,
) : ViewModel() {

    var deviceState: DeviceData? by mutableStateOf(null)
    var readings: List<HeartRateReading> by mutableStateOf(emptyList())
    var statistics: Statistics by mutableStateOf(Statistics())
    var timeRange: TimeRange by mutableStateOf(TimeRange.LAST_24_HOURS)
    var isLoading: Boolean by mutableStateOf(false)
    var error: String? by mutableStateOf(null)

    data class Statistics(
        val average: Double? = null,
        val min: Int? = null,
        val max: Int? = null,
        val count: Int = 0,
        val restingHeartRate: Int? = null
    )

    enum class TimeRange(val displayName: String, val milliseconds: Long) {
        LAST_HOUR("Last Hour", TimeUnit.HOURS.toMillis(1)),
        LAST_6_HOURS("Last 6 Hours", TimeUnit.HOURS.toMillis(6)),
        LAST_24_HOURS("Last 24 Hours", TimeUnit.DAYS.toMillis(1)),
        LAST_7_DAYS("Last 7 Days", TimeUnit.DAYS.toMillis(7)),
        LAST_30_DAYS("Last 30 Days", TimeUnit.DAYS.toMillis(30)),
        ALL_TIME("All Time", Long.MAX_VALUE);
    }

    init {
        viewModelScope.launch {
            devicesRepository.observeDevice(address).collect { device ->
                deviceState = device
            }
        }
        loadData()
    }

    fun back() {
        router.navigate(BackCommand)
    }

    fun setTimeRange(range: TimeRange) {
        if (timeRange != range) {
            timeRange = range
            loadData()
        }
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading = true
            error = null

            try {
                val fromTime = if (timeRange == TimeRange.ALL_TIME) {
                    0L
                } else {
                    System.currentTimeMillis() - timeRange.milliseconds
                }

                // Load readings
                readings = getHeartRateHistoryInteractor.execute(
                    deviceAddress = address,
                    fromTime = fromTime
                )

                // Load statistics
                val stats = getHeartRateHistoryInteractor.getStatistics(
                    deviceAddress = address,
                    fromTime = fromTime
                )

                // Calculate resting heart rate (lowest 10% average)
                val restingHR = calculateRestingHeartRate(readings)

                statistics = Statistics(
                    average = stats.average,
                    min = stats.min,
                    max = stats.max,
                    count = stats.count,
                    restingHeartRate = restingHR
                )

                Timber.tag(TAG).d("Loaded ${readings.size} readings for time range: ${timeRange.displayName}")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to load heart rate history")
                error = "Failed to load data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun calculateRestingHeartRate(readings: List<HeartRateReading>): Int? {
        if (readings.isEmpty()) return null

        // Take the lowest 10% of readings as resting heart rate
        val sortedHeartRates = readings.map { it.heartRate }.sorted()
        val lowest10Percent = sortedHeartRates.take((sortedHeartRates.size * 0.1).toInt().coerceAtLeast(1))

        return lowest10Percent.average().toInt()
    }

    fun getHeartRateZone(heartRate: Int): HeartRateZone {
        // Using standard heart rate zones based on max heart rate
        // Simplified formula: Max HR = 220 - age (we'll use 180 as average max)
        val maxHR = 180

        return when {
            heartRate < maxHR * 0.5 -> HeartRateZone.RESTING
            heartRate < maxHR * 0.6 -> HeartRateZone.WARM_UP
            heartRate < maxHR * 0.7 -> HeartRateZone.FAT_BURN
            heartRate < maxHR * 0.8 -> HeartRateZone.CARDIO
            heartRate < maxHR * 0.9 -> HeartRateZone.PEAK
            else -> HeartRateZone.MAXIMUM
        }
    }

    fun getZoneDistribution(): Map<HeartRateZone, Int> {
        if (readings.isEmpty()) return emptyMap()

        return readings
            .groupBy { getHeartRateZone(it.heartRate) }
            .mapValues { it.value.size }
    }

    enum class HeartRateZone(val displayName: String, val colorValue: Long) {
        RESTING("Resting", 0xFF9E9E9E),
        WARM_UP("Warm Up", 0xFF64B5F6),
        FAT_BURN("Fat Burn", 0xFF81C784),
        CARDIO("Cardio", 0xFFFFB74D),
        PEAK("Peak", 0xFFFF8A65),
        MAXIMUM("Maximum", 0xFFEF5350);
    }

    companion object {
        private const val TAG = "HeartRateHistoryVM"
    }
}
