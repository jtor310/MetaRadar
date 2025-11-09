package f.cking.software.data.repo

import android.content.SharedPreferences
import f.cking.software.domain.model.HeartRateAlertSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HeartRateSettingsRepository(
    private val sharedPreferences: SharedPreferences
) {

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<HeartRateAlertSettings> = _settings

    fun getSettings(): HeartRateAlertSettings {
        return _settings.value
    }

    fun updateSettings(settings: HeartRateAlertSettings) {
        saveSettings(settings)
        _settings.value = settings
    }

    private fun loadSettings(): HeartRateAlertSettings {
        return HeartRateAlertSettings(
            enabled = sharedPreferences.getBoolean(KEY_ALERTS_ENABLED, false),
            highHeartRateThreshold = sharedPreferences.getInt(KEY_HIGH_THRESHOLD, 150),
            lowHeartRateThreshold = sharedPreferences.getInt(KEY_LOW_THRESHOLD, 50),
            notifyOnHigh = sharedPreferences.getBoolean(KEY_NOTIFY_HIGH, true),
            notifyOnLow = sharedPreferences.getBoolean(KEY_NOTIFY_LOW, true),
            dataRetentionDays = sharedPreferences.getInt(KEY_RETENTION_DAYS, 30)
        )
    }

    private fun saveSettings(settings: HeartRateAlertSettings) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_ALERTS_ENABLED, settings.enabled)
            putInt(KEY_HIGH_THRESHOLD, settings.highHeartRateThreshold)
            putInt(KEY_LOW_THRESHOLD, settings.lowHeartRateThreshold)
            putBoolean(KEY_NOTIFY_HIGH, settings.notifyOnHigh)
            putBoolean(KEY_NOTIFY_LOW, settings.notifyOnLow)
            putInt(KEY_RETENTION_DAYS, settings.dataRetentionDays)
            apply()
        }
    }

    companion object {
        private const val KEY_ALERTS_ENABLED = "heart_rate_alerts_enabled"
        private const val KEY_HIGH_THRESHOLD = "heart_rate_high_threshold"
        private const val KEY_LOW_THRESHOLD = "heart_rate_low_threshold"
        private const val KEY_NOTIFY_HIGH = "heart_rate_notify_high"
        private const val KEY_NOTIFY_LOW = "heart_rate_notify_low"
        private const val KEY_RETENTION_DAYS = "heart_rate_retention_days"
    }
}
