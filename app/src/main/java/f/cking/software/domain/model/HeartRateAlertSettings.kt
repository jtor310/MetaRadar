package f.cking.software.domain.model

data class HeartRateAlertSettings(
    val enabled: Boolean = false,
    val highHeartRateThreshold: Int = 150,
    val lowHeartRateThreshold: Int = 50,
    val notifyOnHigh: Boolean = true,
    val notifyOnLow: Boolean = true,
    val dataRetentionDays: Int = 30, // Keep data for 30 days by default
)
