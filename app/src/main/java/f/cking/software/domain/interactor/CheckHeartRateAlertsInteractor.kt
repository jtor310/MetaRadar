package f.cking.software.domain.interactor

import f.cking.software.domain.model.HeartRateAlertSettings
import f.cking.software.domain.model.HeartRateReading

class CheckHeartRateAlertsInteractor {

    fun execute(reading: HeartRateReading, settings: HeartRateAlertSettings): AlertResult? {
        if (!settings.enabled) {
            return null
        }

        return when {
            settings.notifyOnHigh && reading.heartRate >= settings.highHeartRateThreshold -> {
                AlertResult.HighHeartRate(reading.heartRate, settings.highHeartRateThreshold)
            }
            settings.notifyOnLow && reading.heartRate <= settings.lowHeartRateThreshold -> {
                AlertResult.LowHeartRate(reading.heartRate, settings.lowHeartRateThreshold)
            }
            else -> null
        }
    }

    sealed class AlertResult {
        abstract val currentHeartRate: Int
        abstract val threshold: Int

        data class HighHeartRate(
            override val currentHeartRate: Int,
            override val threshold: Int
        ) : AlertResult() {
            val title = "High Heart Rate Alert"
            val message = "Your heart rate ($currentHeartRate BPM) is above the threshold ($threshold BPM)"
        }

        data class LowHeartRate(
            override val currentHeartRate: Int,
            override val threshold: Int
        ) : AlertResult() {
            val title = "Low Heart Rate Alert"
            val message = "Your heart rate ($currentHeartRate BPM) is below the threshold ($threshold BPM)"
        }
    }
}
