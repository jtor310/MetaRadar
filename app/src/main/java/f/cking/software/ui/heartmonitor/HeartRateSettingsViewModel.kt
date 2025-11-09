package f.cking.software.ui.heartmonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import f.cking.software.data.repo.HeartRateSettingsRepository
import f.cking.software.domain.interactor.CleanupOldHeartRateDataInteractor
import f.cking.software.domain.model.HeartRateAlertSettings
import f.cking.software.utils.navigation.BackCommand
import f.cking.software.utils.navigation.Router
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class HeartRateSettingsViewModel(
    private val router: Router,
    private val heartRateSettingsRepository: HeartRateSettingsRepository,
    private val cleanupOldHeartRateDataInteractor: CleanupOldHeartRateDataInteractor,
) : ViewModel() {

    val settings: StateFlow<HeartRateAlertSettings> = heartRateSettingsRepository.settings

    fun back() {
        router.navigate(BackCommand)
    }

    fun updateSettings(newSettings: HeartRateAlertSettings) {
        viewModelScope.launch {
            try {
                heartRateSettingsRepository.updateSettings(newSettings)
                Timber.tag(TAG).d("Settings updated: $newSettings")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to update settings")
            }
        }
    }

    fun cleanupOldData() {
        viewModelScope.launch {
            try {
                val deletedCount = cleanupOldHeartRateDataInteractor.execute()
                Timber.tag(TAG).d("Cleaned up $deletedCount old heart rate readings")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to cleanup old data")
            }
        }
    }

    companion object {
        private const val TAG = "HeartRateSettingsVM"
    }
}
